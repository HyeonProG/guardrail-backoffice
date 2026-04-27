package com.hyeon.guardrail.common.exception;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 공통 예외 응답 처리기 */
@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

  /** 상태 기반 비즈니스 예외 응답 처리 */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleBaseException(BaseException exception) {
    BaseResponseEntity<Void> response =
        new BaseResponseEntity<>(exception.getStatus(), exception.getMessage());
    log.error("BaseException -> {}({})", exception.getStatus(), exception.getMessage(), exception);
    return ResponseEntity.status(exception.getStatus().getHttpStatusCode()).body(response);
  }

  /** 인증 실패 예외 응답 처리 */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleBadCredentialsException(
      BadCredentialsException exception) {
    BaseResponseEntity<Void> response = new BaseResponseEntity<>(BaseResponseStatus.UNAUTHORIZED);
    log.error("BadCredentialsException", exception);
    return ResponseEntity.status(BaseResponseStatus.UNAUTHORIZED.getHttpStatusCode())
        .body(response);
  }

  /** RequestBody 검증 실패 응답 처리 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    FieldError fieldError = exception.getBindingResult().getFieldError();
    String message =
        fieldError != null
            ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
            : BaseResponseStatus.INVALID_REQUEST.getMessage();

    log.warn("Validation failed: {}", message);
    return ResponseEntity.status(BaseResponseStatus.INVALID_REQUEST.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.INVALID_REQUEST, message));
  }

  /** 파라미터 검증 실패 응답 처리 */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleConstraintViolationException(
      ConstraintViolationException exception) {
    String message =
        exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .findFirst()
            .orElse(BaseResponseStatus.INVALID_REQUEST.getMessage());

    log.warn("Constraint violation: {}", message);
    return ResponseEntity.status(BaseResponseStatus.INVALID_REQUEST.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.INVALID_REQUEST, message));
  }

  /** 타입 변환 실패 응답 처리 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception) {
    String message = exception.getName() + ": 잘못된 형식입니다.";
    log.warn("Type mismatch: {}", message);
    return ResponseEntity.status(BaseResponseStatus.INVALID_REQUEST.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.INVALID_REQUEST, message));
  }

  /** 지원하지 않는 HTTP 메서드 응답 처리 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    log.warn("Method not allowed: {}", exception.getMethod());
    return ResponseEntity.status(BaseResponseStatus.METHOD_NOT_ALLOWED.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.METHOD_NOT_ALLOWED));
  }

  /** RequestBody 파싱 실패 응답 처리 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception) {
    log.warn("Request body parse failed", exception);
    return ResponseEntity.status(BaseResponseStatus.INVALID_REQUEST.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.INVALID_REQUEST, "요청 형식이 올바르지 않습니다."));
  }

  /** 처리되지 않은 예외 응답 처리 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponseEntity<Void>> handleException(Exception exception) {
    log.error("Unhandled exception", exception);
    return ResponseEntity.status(BaseResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatusCode())
        .body(new BaseResponseEntity<>(BaseResponseStatus.INTERNAL_SERVER_ERROR));
  }
}
