package io.github.root_LH.specnote.global.exception;

import lombok.Getter;

/**
 * 서비스 로직에서 의도적으로 던지는 예외. GlobalExceptionHandler가 ErrorCode에 맞는 응답으로 바꿈.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
