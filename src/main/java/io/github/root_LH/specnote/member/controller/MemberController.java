package io.github.root_LH.specnote.member.controller;

import io.github.root_LH.specnote.member.dto.MemberResponse;
import io.github.root_LH.specnote.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Me", description = "로그인한 회원 본인")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "Access Token 필요")
    @GetMapping
    public MemberResponse getMe(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return memberService.getMe(memberId);
    }
}
