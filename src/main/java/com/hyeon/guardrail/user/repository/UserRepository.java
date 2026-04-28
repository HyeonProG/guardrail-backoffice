package com.hyeon.guardrail.user.repository;

import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserRole;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 사용자 기본 영속성 저장소 */
public interface UserRepository extends JpaRepository<User, UUID> {

  /** 삭제되지 않은 사용자 이메일 존재 여부 조회 */
  boolean existsByEmailAndDeletedFalse(String email);

  /** 특정 사용자를 제외한 삭제되지 않은 사용자 이메일 존재 여부 조회 */
  boolean existsByEmailAndDeletedFalseAndIdNot(String email, UUID userId);

  /** 사용자 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update User user
         set user.email = :email,
             user.name = :name,
             user.role = :role
       where user.id = :userId
         and user.deleted = false
      """)
  int updateBasicInfo(
      @Param("userId") UUID userId,
      @Param("email") String email,
      @Param("name") String name,
      @Param("role") UserRole role);
}
