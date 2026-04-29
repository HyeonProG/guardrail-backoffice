package com.hyeon.guardrail.auth.repository;

import com.hyeon.guardrail.auth.domain.UserSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자 인증 세션 저장소 */
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {}
