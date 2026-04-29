package com.hyeon.guardrail.user.repository;

import com.hyeon.guardrail.user.domain.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자 기본 영속성 저장소 */
public interface UserRepository extends JpaRepository<User, UUID> {

  /** 삭제되지 않은 사용자 이메일 존재 여부 조회 */
  boolean existsByEmailAndDeletedFalse(String email);

  /** 특정 사용자를 제외한 삭제되지 않은 사용자 이메일 존재 여부 조회 */
  boolean existsByEmailAndDeletedFalseAndIdNot(String email, UUID userId);
}
