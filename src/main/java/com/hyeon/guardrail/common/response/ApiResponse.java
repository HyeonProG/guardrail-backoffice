package com.hyeon.guardrail.common.response;

/** 공통 성공 응답 구조 */
public record ApiResponse<T>(boolean success, T data, String message) {

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null);
  }

  public static <T> ApiResponse<T> ok(T data, String message) {
    return new ApiResponse<>(true, data, message);
  }

  public static ApiResponse<Void> ok() {
    return new ApiResponse<>(true, null, null);
  }

  public static ApiResponse<Void> ok(String message) {
    return new ApiResponse<>(true, null, message);
  }
}
