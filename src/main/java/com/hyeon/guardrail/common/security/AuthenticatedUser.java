package com.hyeon.guardrail.common.security;

import com.hyeon.guardrail.user.domain.UserRole;
import java.util.UUID;

/** JWT 인증 후 SecurityContext에 저장할 로그인 사용자 정보 */
public record AuthenticatedUser(UUID userId, UserRole role) {}
