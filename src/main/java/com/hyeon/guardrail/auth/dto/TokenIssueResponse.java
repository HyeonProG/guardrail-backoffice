package com.hyeon.guardrail.auth.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 내부 토큰 발급 결과 */
@Getter
@AllArgsConstructor
public class TokenIssueResponse {

  private String token;
  private LocalDateTime expiredAt;
}
