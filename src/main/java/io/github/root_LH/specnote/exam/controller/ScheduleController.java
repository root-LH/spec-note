package io.github.root_LH.specnote.exam.controller;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.dto.ScheduleResponse;
import io.github.root_LH.specnote.exam.service.ExamQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Schedule", description = "시험 일정 캘린더 (비회원 가능)")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ExamQueryService examQueryService;

    @Operation(summary = "기간별 일정",
            description = "접수 시작·마감·시험일·발표일 중 하나라도 기간에 걸친 일정. 최대 93일, 날짜 형식 2026-12-01")
    @GetMapping
    public List<ScheduleResponse> getSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) ExamCategory category) {
        return examQueryService.getSchedulesInPeriod(from, to, category);
    }
}
