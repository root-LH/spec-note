package io.github.root_LH.specnote.global.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 jwt.* 값을 담는 설정 클래스
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}
