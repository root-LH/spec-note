package io.github.root_LH.specnote.member.service;

import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.member.dto.MemberResponse;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse getMe(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
