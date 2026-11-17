package io.github.root_LH.specnote.auth.service;

import io.github.root_LH.specnote.auth.dto.LoginRequest;
import io.github.root_LH.specnote.auth.dto.RefreshRequest;
import io.github.root_LH.specnote.auth.dto.SignupRequest;
import io.github.root_LH.specnote.auth.dto.TokenResponse;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.global.jwt.JwtTokenProvider;
import io.github.root_LH.specnote.member.domain.Member;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.createUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname());
        return memberRepository.save(member).getId();
    }

    public TokenResponse login(LoginRequest request) {
        // 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 응답
        // → 어떤 이메일이 가입돼 있는지 외부에서 알아낼 수 없게 함
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return issueTokens(member);
    }

    public TokenResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parse(request.refreshToken());
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        return issueTokens(member);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());
        return TokenResponse.bearer(accessToken, jwtTokenProvider.getAccessTokenValiditySeconds(), refreshToken);
    }
}
