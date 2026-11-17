package io.github.root_LH.specnote.global.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security 필터 단계(컨트롤러 도달 전)에서 에러를 JSON으로 직접 쓰기 위한 도구.
 * 필터에서 난 예외는 @RestControllerAdvice가 잡지 못하기 때문에 필요함.
 */
public final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"%s\",\"message\":\"%s\",\"errors\":[]}"
                .formatted(errorCode.getCode(), errorCode.getMessage()));
    }
}
