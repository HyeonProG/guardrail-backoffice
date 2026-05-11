package com.hyeon.guardrail.common.security;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.user.domain.UserRole;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** 현재 로그인 사용자 정보를 조회하고 역할 기반 접근을 검증한다. */
@Service
public class CurrentUserService {

  /** 현재 로그인 사용자 ID를 반환한다. */
  public UUID getCurrentUserId() {
    return getAuthenticatedUser().userId();
  }

  /** 현재 로그인 사용자 역할을 반환한다. */
  public UserRole getCurrentUserRole() {
    return getAuthenticatedUser().role();
  }

  /** 현재 로그인 사용자가 ADMIN 또는 OPERATOR인지 검증한다. */
  public void requireAdminOrOperator() {
    if (!isAdminOrOperator()) {
      throw new BaseException(BaseResponseStatus.FORBIDDEN, "해당 작업은 관리자 또는 운영자만 수행할 수 있습니다.");
    }
  }

  /** 현재 로그인 사용자가 ADMIN 또는 OPERATOR인지 반환한다. */
  public boolean isAdminOrOperator() {
    UserRole role = getCurrentUserRole();
    return role == UserRole.ADMIN || role == UserRole.OPERATOR;
  }

  /** 요청 본문의 actorId가 현재 로그인 사용자와 일치하는지 검증한다. */
  public void validateActor(UUID actorId) {
    if (actorId == null || !getCurrentUserId().equals(actorId)) {
      throw new BaseException(BaseResponseStatus.FORBIDDEN, "현재 로그인 사용자와 요청 처리자가 일치하지 않습니다.");
    }
  }

  private AuthenticatedUser getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    return user;
  }
}
