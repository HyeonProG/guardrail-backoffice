package com.hyeon.guardrail.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Controller 공통 성공 응답 상태 정의 */
@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {
  SUCCESS(HttpStatus.OK, true, 200, "요청이 정상 처리되었습니다."),
  CREATED(HttpStatus.CREATED, true, 201, "요청이 정상 생성되었습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, false, 400, "잘못된 요청입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, false, 401, "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, false, 403, "접근 권한이 없습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, false, 405, "허용되지 않은 요청 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버 내부 오류가 발생했습니다.");

  private final HttpStatusCode httpStatusCode;
  private final boolean isSuccess;
  private final int code;
  private final String message;
}
