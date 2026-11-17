package io.github.root_LH.specnote.global.exception;

import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * 모든 에러 응답의 공통 형식
 * {
 *   "code": "C001",
 *   "message": "입력값이 올바르지 않습니다.",
 *   "errors": [ { "field": "email", "reason": "올바른 형식의 이메일 주소여야 합니다" } ]
 * }
 */
public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorDetail> errors
) {

    public record FieldErrorDetail(String field, String reason) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        List<FieldErrorDetail> details = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), details);
    }
}
