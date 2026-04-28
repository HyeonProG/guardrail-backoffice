package com.hyeon.guardrail.auth.domain;

/** 인증 세션 상태 정의 */
public enum SessionStatus {
  ACTIVE,
  EXPIRED,
  REVOKED
}
