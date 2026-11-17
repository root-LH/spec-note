package io.github.root_LH.specnote.global.jwt;

import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.global.exception.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증이 필요한 API를 인증 없이 호출했을 때 401 JSON 응답.
 * 필터가 남긴 원인(만료/위조)이 있으면 그 코드로 응답.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = request.getAttribute(JwtAuthenticationFilter.EXCEPTION_ATTRIBUTE) instanceof ErrorCode code
                ? code
                : ErrorCode.UNAUTHORIZED;
        ErrorResponseWriter.write(response, errorCode);
    }
}
