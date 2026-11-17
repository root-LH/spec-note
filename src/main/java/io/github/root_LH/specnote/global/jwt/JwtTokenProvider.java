package io.github.root_LH.specnote.global.jwt;

import io.github.root_LH.specnote.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성·검증 담당
 * - subject: 회원 ID
 * - role: 권한 (USER / ADMIN)
 * - type: ACCESS / REFRESH (리프레시 토큰으로 API를 호출하는 것을 막기 위함)
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String createAccessToken(Long memberId, Role role) {
        return createToken(memberId, role, TokenType.ACCESS, properties.accessTokenValiditySeconds());
    }

    public String createRefreshToken(Long memberId, Role role) {
        return createToken(memberId, role, TokenType.REFRESH, properties.refreshTokenValiditySeconds());
    }

    public long getAccessTokenValiditySeconds() {
        return properties.accessTokenValiditySeconds();
    }

    private String createToken(Long memberId, Role role, TokenType type, long validitySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * 서명과 만료를 검증하고 Claims를 반환.
     * 실패하면 ExpiredJwtException, JwtException 등을 던지므로 호출하는 쪽에서 처리.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return TokenType.ACCESS.name().equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TokenType.REFRESH.name().equals(claims.get(CLAIM_TYPE, String.class));
    }

    /**
     * 토큰 정보로 인증 객체를 만듦.
     * principal에 회원 ID(Long)를 넣어서 컨트롤러에서 @AuthenticationPrincipal Long memberId 로 꺼내 씀.
     */
    public Authentication getAuthentication(Claims claims) {
        Long memberId = Long.valueOf(claims.getSubject());
        String role = claims.get(CLAIM_ROLE, String.class);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(memberId, null, authorities);
    }
}
