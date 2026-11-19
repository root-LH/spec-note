package io.github.root_LH.specnote.spec.controller;

import io.github.root_LH.specnote.spec.dto.SpecRequest;
import io.github.root_LH.specnote.spec.dto.SpecResponse;
import io.github.root_LH.specnote.spec.service.MemberSpecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "My Spec", description = "내 자격증·어학 점수 관리")
@RestController
@RequestMapping("/api/me/specs")
@RequiredArgsConstructor
public class MemberSpecController {

    private final MemberSpecService specService;

    @Operation(summary = "내 스펙 목록")
    @GetMapping
    public List<SpecResponse> getMySpecs(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return specService.getMySpecs(memberId);
    }

    @Operation(summary = "스펙 등록", description = "expireDate를 비우면 시험 유효기간으로 자동 계산")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecResponse create(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                               @Valid @RequestBody SpecRequest request) {
        return specService.create(memberId, request);
    }

    @Operation(summary = "스펙 수정")
    @PutMapping("/{specId}")
    public SpecResponse update(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                               @PathVariable Long specId,
                               @Valid @RequestBody SpecRequest request) {
        return specService.update(memberId, specId, request);
    }

    @Operation(summary = "스펙 삭제")
    @DeleteMapping("/{specId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                       @PathVariable Long specId) {
        specService.delete(memberId, specId);
    }
}
