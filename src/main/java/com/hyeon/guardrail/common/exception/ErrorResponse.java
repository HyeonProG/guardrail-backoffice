package com.hyeon.guardrail.common.exception;

/** 공통 에러 응답 구조 */
public record ErrorResponse(boolean success, String code, String message) {

  public static ErrorResponse from(ErrorCode errorCode) {
    return new ErrorResponse(false, errorCode.name(), errorCode.getMessage());
  }

  public static ErrorResponse from(ErrorCode errorCode, String message) {
    return new ErrorResponse(false, errorCode.name(), message);
  }
}
