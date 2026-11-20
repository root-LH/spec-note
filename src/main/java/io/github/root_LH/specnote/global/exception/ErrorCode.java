package io.github.root_LH.specnote.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "지원하지 않는 HTTP 메서드입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "C004", "조회 기간이 올바르지 않습니다. (시작일 ≤ 종료일, 최대 93일)"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    // 인증·인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다."),

    // 회원
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M001", "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "회원을 찾을 수 없습니다."),

    // 시험·일정
    EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "시험을 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "시험 일정을 찾을 수 없습니다."),
    INVALID_SCHEDULE_DATES(HttpStatus.BAD_REQUEST, "E003", "일정 순서가 올바르지 않습니다. (접수 시작 ≤ 접수 마감 ≤ 시험일 ≤ 발표일)"),
    DUPLICATE_EXAM_CODE(HttpStatus.CONFLICT, "E004", "이미 등록된 시험 코드입니다."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "E005", "같은 회차·단계의 일정이 이미 있습니다."),

    // 내 스펙
    SPEC_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "스펙을 찾을 수 없습니다."),
    SPEC_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "P002", "시험(examId) 또는 직접 입력한 이름(customName) 중 하나는 필요합니다."),
    INVALID_SPEC_DATES(HttpStatus.BAD_REQUEST, "P003", "만료일은 취득일 이후여야 합니다."),

    // 외부 API
    EXTERNAL_API_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "X001", "외부 API 인증키가 설정되지 않았습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "X002", "외부 API 호출에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
