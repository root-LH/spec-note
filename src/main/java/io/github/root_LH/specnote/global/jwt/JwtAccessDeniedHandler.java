package io.github.root_LH.specnote.global.jwt;

import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.global.exception.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인은 했지만 권한이 부족할 때 (예: 일반 회원이 /api/admin 호출) 403 JSON 응답
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponseWriter.write(response, ErrorCode.ACCESS_DENIED);
    }
}
