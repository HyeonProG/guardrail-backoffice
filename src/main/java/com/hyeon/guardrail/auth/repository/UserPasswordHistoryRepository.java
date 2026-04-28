package com.hyeon.guardrail.auth.repository;

import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자 비밀번호 이력 저장소 */
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, UUID> {}
