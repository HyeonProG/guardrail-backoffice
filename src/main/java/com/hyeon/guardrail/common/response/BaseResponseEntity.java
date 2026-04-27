package com.hyeon.guardrail.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Controller 공통 성공 응답 래퍼 */
@Getter
@Schema(description = "공통 성공 응답 구조")
public class BaseResponseEntity<T> {

  @Schema(hidden = true)
  private final HttpStatusCode httpStatusCode;

  @Schema(description = "요청 성공 여부", example = "true")
  private final boolean isSuccess;

  @Schema(description = "응답 메시지", example = "요청이 정상 처리되었습니다.")
  private final String message;

  @Schema(description = "응답 코드", example = "200")
  private final int code;

  @Schema(description = "응답 데이터")
  private final T result;

  /** 데이터 포함 기본 성공 응답 생성 */
  public BaseResponseEntity(T result) {
    this(BaseResponseStatus.SUCCESS, result);
  }

  /** 데이터 없는 기본 성공 응답 생성 */
  public BaseResponseEntity() {
    this(BaseResponseStatus.SUCCESS);
  }

  /** 상태 기반 데이터 없는 성공 응답 생성 */
  public BaseResponseEntity(BaseResponseStatus status) {
    this(status, (T) null);
  }

  /** 상태 기반 데이터 포함 성공 응답 생성 */
  public BaseResponseEntity(BaseResponseStatus status, T result) {
    this(
        status.getHttpStatusCode(),
        status.isSuccess(),
        status.getMessage(),
        status.getCode(),
        result);
  }

  /** 상태 기반 메시지 재정의 성공 응답 생성 */
  public BaseResponseEntity(BaseResponseStatus status, String message) {
    this(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), null);
  }

  /** 상태 기반 데이터와 메시지 재정의 성공 응답 생성 */
  public BaseResponseEntity(BaseResponseStatus status, T result, String message) {
    this(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), result);
  }

  /** 200 OK 데이터 응답 생성 */
  public static <T> BaseResponseEntity<T> success(T result) {
    return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS, result);
  }

  /** 200 OK 메시지 포함 데이터 응답 생성 */
  public static <T> BaseResponseEntity<T> success(T result, String message) {
    return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS, result, message);
  }

  /** 200 OK 데이터 없는 응답 생성 */
  public static BaseResponseEntity<Void> success() {
    return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
  }

  /** 200 OK 메시지 포함 데이터 없는 응답 생성 */
  public static BaseResponseEntity<Void> success(String message) {
    return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS, message);
  }

  /** 201 Created 데이터 응답 생성 */
  public static <T> BaseResponseEntity<T> created(T result, String message) {
    return new BaseResponseEntity<>(BaseResponseStatus.CREATED, result, message);
  }

  /** 사용자 정의 상태 코드 기반 성공 응답 생성 */
  public static <T> BaseResponseEntity<T> of(
      HttpStatus httpStatus, boolean isSuccess, int code, String message, T result) {
    return new BaseResponseEntity<>(httpStatus, isSuccess, message, code, result);
  }

  private BaseResponseEntity(
      HttpStatusCode httpStatusCode, boolean isSuccess, String message, int code, T result) {
    this.httpStatusCode = httpStatusCode;
    this.isSuccess = isSuccess;
    this.message = message;
    this.code = code;
    this.result = result;
  }
}
