package io.github.root_LH.specnote.exam.service;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.dto.ExamDetailResponse;
import io.github.root_LH.specnote.exam.dto.ExamSummaryResponse;
import io.github.root_LH.specnote.exam.dto.ScheduleResponse;
import io.github.root_LH.specnote.exam.repository.ExamRepository;
import io.github.root_LH.specnote.exam.repository.ExamScheduleRepository;
import io.github.root_LH.specnote.global.common.PageResponse;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 시험·일정 조회 (비회원도 사용)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamQueryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long MAX_PERIOD_DAYS = 93;   // 캘린더 3개월 + 여유

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final Clock clock;

    public PageResponse<ExamSummaryResponse> searchExams(ExamCategory category, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),   // 한 번에 너무 많이 요청하지 못하게 제한
                Sort.by("name"));
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        return PageResponse.from(
                examRepository.search(category, normalizedKeyword, pageRequest)
                        .map(ExamSummaryResponse::from));
    }

    public ExamDetailResponse getExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        LocalDate today = LocalDate.now(clock);
        List<ScheduleResponse> upcoming = scheduleRepository.findUpcomingByExamId(examId, today).stream()
                .map(schedule -> ScheduleResponse.of(schedule, today))
                .toList();

        return ExamDetailResponse.of(exam, upcoming);
    }

    public List<ScheduleResponse> getSchedulesInPeriod(LocalDate from, LocalDate to, ExamCategory category) {
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > MAX_PERIOD_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        LocalDate today = LocalDate.now(clock);
        return scheduleRepository.findInPeriod(from, to, category).stream()
                .map(schedule -> ScheduleResponse.of(schedule, today))
                .toList();
    }
}
