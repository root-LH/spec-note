package io.github.root_LH.specnote.exam.controller;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.dto.ExamDetailResponse;
import io.github.root_LH.specnote.exam.dto.ExamSummaryResponse;
import io.github.root_LH.specnote.exam.service.ExamQueryService;
import io.github.root_LH.specnote.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exam", description = "시험 조회 (비회원 가능)")
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamQueryService examQueryService;

    @Operation(summary = "시험 목록·검색", description = "category, keyword는 선택. page는 0부터 시작")
    @GetMapping
    public PageResponse<ExamSummaryResponse> searchExams(
            @RequestParam(required = false) ExamCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return examQueryService.searchExams(category, keyword, page, size);
    }

    @Operation(summary = "시험 상세", description = "아직 끝나지 않은 회차 일정 포함")
    @GetMapping("/{examId}")
    public ExamDetailResponse getExam(@PathVariable Long examId) {
        return examQueryService.getExam(examId);
    }
}
