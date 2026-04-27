package com.hyeon.guardrail.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 공통 예외 응답 처리 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(ErrorResponse.from(errorCode, exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining(", "));

    return ResponseEntity.badRequest()
        .body(ErrorResponse.from(ErrorCode.INVALID_INPUT_VALUE, message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException exception) {
    String message =
        exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));

    return ResponseEntity.badRequest()
        .body(ErrorResponse.from(ErrorCode.INVALID_INPUT_VALUE, message));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception) {
    String message = exception.getName() + ": 잘못된 형식입니다.";
    return ResponseEntity.badRequest()
        .body(ErrorResponse.from(ErrorCode.INVALID_TYPE_VALUE, message));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
        .body(ErrorResponse.from(ErrorCode.METHOD_NOT_ALLOWED));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
        .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  private String formatFieldError(FieldError fieldError) {
    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
  }
}
