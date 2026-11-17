package io.github.root_LH.specnote.member.dto;

import io.github.root_LH.specnote.member.domain.Member;
import io.github.root_LH.specnote.member.domain.Role;

import java.time.LocalDateTime;

/**
 * 엔티티를 그대로 반환하지 않고 필요한 값만 담아 응답 (비밀번호 노출 방지)
 */
public record MemberResponse(
        Long memberId,
        String email,
        String nickname,
        Role role,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getCreatedAt());
    }
}
