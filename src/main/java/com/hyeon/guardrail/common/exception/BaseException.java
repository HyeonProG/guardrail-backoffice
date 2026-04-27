package com.hyeon.guardrail.common.exception;

import com.hyeon.guardrail.common.response.BaseResponseStatus;
import lombok.Getter;

/** 공통 비즈니스 예외 구조 */
@Getter
public class BaseException extends RuntimeException {

  private final BaseResponseStatus status;

  /** 상태 기반 예외 생성 */
  public BaseException(BaseResponseStatus status) {
    super(status.getMessage());
    this.status = status;
  }

  /** 상태와 메시지 재정의 기반 예외 생성 */
  public BaseException(BaseResponseStatus status, String message) {
    super(message);
    this.status = status;
  }
}
