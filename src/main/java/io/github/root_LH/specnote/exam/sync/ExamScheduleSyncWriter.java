package io.github.root_LH.specnote.exam.sync;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.external.QnetScheduleItem;
import io.github.root_LH.specnote.exam.external.QnetScheduleItem.StageDates;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.exam.repository.ExamScheduleRepository;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * API에서 받은 일정을 DB에 반영 (upsert).
 * (시험, 회차명, 단계)가 같은 일정이 있으면 날짜만 갱신하고, 없으면 새로 만듦.
 *
 * 시험 1개 단위로 트랜잭션을 나눔 → 한 시험이 실패해도 다른 시험의 동기화 결과는 유지됨.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamScheduleSyncWriter {

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;

    @Transactional
    public void upsert(Long examId, List<QnetScheduleItem> items, LocalDateTime syncedAt, SyncCounter counter) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        for (QnetScheduleItem item : items) {
            for (StageDates dates : item.stages()) {
                try {
                    upsertStage(exam, item.roundName(), dates, syncedAt, counter);
                } catch (BusinessException e) {
                    // 원본 데이터의 날짜 순서가 이상한 경우 → 그 일정만 건너뛰고 계속 진행
                    log.warn("[시험 동기화] 건너뜀 exam={} round={} stage={} reason={}",
                            exam.getName(), item.roundName(), dates.stage(), e.getMessage());
                    counter.invalid++;
                }
            }
        }
    }

    private void upsertStage(Exam exam, String roundName, StageDates dates,
                             LocalDateTime syncedAt, SyncCounter counter) {
        Optional<ExamSchedule> existing =
                scheduleRepository.findByExamIdAndRoundNameAndStage(exam.getId(), roundName, dates.stage());

        if (existing.isPresent()) {
            ExamSchedule schedule = existing.get();
            if (schedule.hasSameDates(dates.regStart(), dates.regEnd(), dates.examDate(), dates.resultDate())) {
                counter.unchanged++;
            } else {
                // 변경 감지로 UPDATE (save 호출 불필요)
                schedule.changeDates(dates.regStart(), dates.regEnd(), dates.examDate(), dates.resultDate());
                counter.updated++;
            }
            schedule.markSynced(syncedAt);
            return;
        }

        ExamSchedule schedule = ExamSchedule.create(exam, roundName, dates.stage(),
                dates.regStart(), dates.regEnd(), dates.examDate(), dates.resultDate());
        schedule.markSynced(syncedAt);
        scheduleRepository.save(schedule);
        counter.created++;
    }
}
