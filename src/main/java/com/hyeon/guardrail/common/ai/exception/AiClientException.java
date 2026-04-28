package com.hyeon.guardrail.common.ai.exception;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;

/** AI provider 호출 실패 예외 */
public class AiClientException extends BaseException {

  /** AI 실패 상태와 메시지 기반 예외 생성 */
  public AiClientException(BaseResponseStatus status, String message) {
    super(status, message);
  }

  /** AI 실패 상태, 메시지, 원인 기반 예외 생성 */
  public AiClientException(BaseResponseStatus status, String message, Throwable cause) {
    super(status, message);
    initCause(cause);
  }
}
