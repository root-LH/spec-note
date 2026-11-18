package io.github.root_LH.specnote.exam.controller;

import io.github.root_LH.specnote.exam.dto.ExamCreateRequest;
import io.github.root_LH.specnote.exam.dto.ExamUpdateRequest;
import io.github.root_LH.specnote.exam.dto.ScheduleCreateRequest;
import io.github.root_LH.specnote.exam.service.ExamAdminService;
import io.github.root_LH.specnote.global.common.IdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/admin/** 는 SecurityConfig에서 ADMIN 권한만 허용
 */
@Tag(name = "Admin - Exam", description = "관리자 전용 시험·일정 관리")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ExamAdminController {

    private final ExamAdminService examAdminService;

    @Operation(summary = "시험 등록")
    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createExam(@Valid @RequestBody ExamCreateRequest request) {
        return new IdResponse(examAdminService.createExam(request));
    }

    @Operation(summary = "시험 수정")
    @PutMapping("/exams/{examId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateExam(@PathVariable Long examId, @Valid @RequestBody ExamUpdateRequest request) {
        examAdminService.updateExam(examId, request);
    }

    @Operation(summary = "회차 일정 등록")
    @PostMapping("/exams/{examId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse addSchedule(@PathVariable Long examId, @Valid @RequestBody ScheduleCreateRequest request) {
        return new IdResponse(examAdminService.addSchedule(examId, request));
    }

    @Operation(summary = "회차 일정 삭제")
    @DeleteMapping("/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(@PathVariable Long scheduleId) {
        examAdminService.deleteSchedule(scheduleId);
    }
}
