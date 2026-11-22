package io.github.root_LH.specnote.exam.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.dto.ExamCreateRequest;
import io.github.root_LH.specnote.exam.dto.ExamUpdateRequest;
import io.github.root_LH.specnote.exam.dto.ScheduleCreateRequest;
import io.github.root_LH.specnote.exam.dto.ScheduleResponse;
import io.github.root_LH.specnote.exam.dto.ScheduleUpdateRequest;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.exam.repository.ExamScheduleRepository;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 관리자용 시험·일정 관리 (공공데이터 API에 없는 어학·민간 시험 등)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExamAdminService {

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final Clock clock;

    public Long createExam(ExamCreateRequest request) {
        String examCode = StringUtils.hasText(request.examCode()) ? request.examCode().trim() : null;
        if (examCode != null && examRepository.existsByExamCode(examCode)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EXAM_CODE);
        }

        Exam exam = Exam.createManual(
                examCode,
                request.name(),
                request.category(),
                request.organizer(),
                request.validityMonths(),
                request.officialUrl());
        return examRepository.save(exam).getId();
    }

    public void updateExam(Long examId, ExamUpdateRequest request) {
        Exam exam = findExam(examId);
        // 변경 감지(dirty checking): 트랜잭션이 끝날 때 바뀐 값만 UPDATE 됨 → save() 호출 불필요
        exam.update(
                request.name(),
                request.category(),
                request.organizer(),
                request.validityMonths(),
                request.officialUrl());
    }

    /** 관리 화면용: 지난 회차까지 전부 (최신순) */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long examId) {
        findExam(examId);
        LocalDate today = LocalDate.now(clock);
        return scheduleRepository.findAllWithExamByExamId(examId).stream()
                .map(schedule -> ScheduleResponse.of(schedule, today))
                .toList();
    }

    public Long addSchedule(Long examId, ScheduleCreateRequest request) {
        Exam exam = findExam(examId);
        if (scheduleRepository.existsByExamIdAndRoundNameAndStage(examId, request.roundName(), request.stage())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SCHEDULE);
        }

        ExamSchedule schedule = ExamSchedule.create(
                exam,
                request.roundName(),
                request.stage(),
                request.regStart(),
                request.regEnd(),
                request.examDate(),
                request.resultDate());
        return scheduleRepository.save(schedule).getId();
    }

    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleUpdateRequest request) {
        ExamSchedule schedule = scheduleRepository.findWithExamById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        // 날짜 순서 검증은 엔티티가 담당
        schedule.changeDates(request.regStart(), request.regEnd(), request.examDate(), request.resultDate());
        return ScheduleResponse.of(schedule, LocalDate.now(clock));
    }

    public void deleteSchedule(Long scheduleId) {
        ExamSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        scheduleRepository.delete(schedule);
    }

    private Exam findExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    }
}
