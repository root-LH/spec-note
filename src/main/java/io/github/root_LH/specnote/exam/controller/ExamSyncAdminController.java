package io.github.root_LH.specnote.exam.controller;

import io.github.root_LH.specnote.exam.sync.ExamSyncResult;
import io.github.root_LH.specnote.exam.sync.ExamSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Exam", description = "관리자 전용 시험·일정 관리")
@RestController
@RequestMapping("/api/admin/exams/sync")
@RequiredArgsConstructor
public class ExamSyncAdminController {

    private final ExamSyncService examSyncService;

    @Operation(summary = "공공데이터 시험 일정 동기화",
            description = "Q-Net 종목코드가 있는 국가자격의 올해·내년 일정을 가져옴. 여러 번 실행해도 중복 생성되지 않음")
    @PostMapping
    public ExamSyncResult sync() {
        return examSyncService.syncAll();
    }
}
