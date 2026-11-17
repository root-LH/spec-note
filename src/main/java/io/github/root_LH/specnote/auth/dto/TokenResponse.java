package io.github.root_LH.specnote.auth.dto;

public record TokenResponse(
        String grantType,
        String accessToken,
        long accessTokenExpiresIn,   // 초 단위
        String refreshToken
) {

    public static TokenResponse bearer(String accessToken, long expiresIn, String refreshToken) {
        return new TokenResponse("Bearer", accessToken, expiresIn, refreshToken);
    }
}
