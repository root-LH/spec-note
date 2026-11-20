package io.github.root_LH.specnote.exam.sync;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.external.QnetClient;
import io.github.root_LH.specnote.exam.external.QnetScheduleItem;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.global.config.PublicDataProperties;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 공공데이터 API → 우리 DB 동기화
 *
 * 1단계. 종목 등록: Q-Net 종목 목록에서 지정 계열(기본: 기사)의 새 종목을 exam에 추가
 * 2단계. 일정 동기화: exam_code가 있는 국가자격의 올해 일정(10월부터는 내년 일정도) 반영
 *
 * 외부 API 호출은 트랜잭션 밖에서 하고, DB 반영만 짧은 트랜잭션으로 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSyncService {

    /** 이 달부터 내년 일정도 조회 (보통 연말에 다음 해 일정이 공고됨) → 그 전에는 호출 수 절약 */
    private static final int NEXT_YEAR_FROM_MONTH = 10;

    private final ExamRepository examRepository;
    private final QnetClient qnetClient;
    private final ExamCatalogImporter catalogImporter;
    private final ExamScheduleSyncWriter syncWriter;
    private final PublicDataProperties properties;
    private final Clock clock;

    public boolean isAvailable() {
        return qnetClient.isConfigured();
    }

    public ExamSyncResult syncAll() {
        if (!qnetClient.isConfigured()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_NOT_CONFIGURED);
        }
        SyncCounter counter = new SyncCounter();

        importCatalog(counter);
        syncSchedules(counter);

        ExamSyncResult result = counter.toResult();
        log.info("[시험 동기화] 완료 {}", result);
        return result;
    }

    private void importCatalog(SyncCounter counter) {
        try {
            counter.examsAdded = catalogImporter.importNew(
                    qnetClient.fetchExamCatalog(), properties.qnet().seriesNames());
        } catch (BusinessException e) {
            // 종목 목록이 실패해도 이미 등록된 시험의 일정 동기화는 계속 진행
            log.warn("[종목 등록] 실패 → 기존 시험만 일정 동기화 reason={}", e.getMessage());
            counter.catalogFailed = true;
        }
    }

    private void syncSchedules(SyncCounter counter) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        boolean includeNextYear = today.getMonthValue() >= NEXT_YEAR_FROM_MONTH;

        List<Exam> targets = examRepository.findAllByExamCodeIsNotNull().stream()
                .filter(exam -> exam.getCategory() == ExamCategory.NATIONAL)
                .toList();

        for (Exam exam : targets) {
            counter.examCount++;
            try {
                // 올해 일정은 반드시 성공해야 함 (실패하면 이 시험은 실패로 처리)
                List<QnetScheduleItem> items =
                        new ArrayList<>(qnetClient.fetchSchedules(today.getYear(), exam.getExamCode()));
                if (includeNextYear) {
                    items.addAll(fetchOptional(today.getYear() + 1, exam));
                }
                syncWriter.upsert(exam.getId(), QnetScheduleItem.mergeSameRound(items), now, counter);
            } catch (BusinessException e) {
                // 한 시험이 실패해도 나머지 시험은 계속 동기화
                log.warn("[시험 동기화] 실패 exam={} code={} reason={}", exam.getName(), exam.getExamCode(), e.getMessage());
                counter.failedExams++;
            }
        }
    }

    /** 내년 일정은 아직 공고 전일 수 있으므로 실패해도 올해 결과는 반영 */
    private List<QnetScheduleItem> fetchOptional(int year, Exam exam) {
        try {
            return qnetClient.fetchSchedules(year, exam.getExamCode());
        } catch (BusinessException e) {
            log.info("[시험 동기화] {}년 일정 없음 또는 조회 실패 → 건너뜀 exam={}", year, exam.getName());
            return List.of();
        }
    }
}
