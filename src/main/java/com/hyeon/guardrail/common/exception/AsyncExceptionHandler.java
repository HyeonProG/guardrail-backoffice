package com.hyeon.guardrail.common.exception;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

/** 비동기 예외 로깅 처리기 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

  /** 비동기 미처리 예외 로깅 */
  @Override
  public void handleUncaughtException(Throwable exception, Method method, Object... params) {
    if (exception instanceof BaseException baseException) {
      log.error(
          "BaseException -> {}({})",
          baseException.getStatus(),
          baseException.getMessage(),
          baseException);
      return;
    }

    log.error("Async exception in method: {}", method.getName(), exception);
  }
}
