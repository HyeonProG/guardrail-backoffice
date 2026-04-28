package com.hyeon.guardrail.auth.repository;

import com.hyeon.guardrail.auth.domain.UserLoginHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자 로그인 이력 저장소 */
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, UUID> {}
