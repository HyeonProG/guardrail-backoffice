package com.hyeon.guardrail.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 필터 단계 공통 예외 응답 처리기 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseExceptionHandlerFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  /** 필터 체인 예외 응답 처리 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (BaseException exception) {
      log.error(
          "BaseException -> {}({})", exception.getStatus(), exception.getMessage(), exception);
      writeErrorResponse(
          response, new BaseResponseEntity<>(exception.getStatus(), exception.getMessage()));
    } catch (AuthenticationException exception) {
      log.error("AuthenticationException", exception);
      writeErrorResponse(response, new BaseResponseEntity<>(BaseResponseStatus.UNAUTHORIZED));
    }
  }

  private void writeErrorResponse(
      HttpServletResponse response, BaseResponseEntity<Void> baseResponseEntity)
      throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(baseResponseEntity.getHttpStatusCode().value());
    response.getWriter().write(objectMapper.writeValueAsString(baseResponseEntity));
  }
}
