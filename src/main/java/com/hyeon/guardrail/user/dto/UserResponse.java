package com.hyeon.guardrail.user.dto;

import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserRole;
import com.hyeon.guardrail.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 사용자 기본 정보 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 기본 정보 응답")
public class UserResponse {

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "사용자 이메일", example = "staff@example.com")
  private String email;

  @Schema(description = "사용자 이름", example = "홍길동")
  private String name;

  @Schema(description = "사용자 역할", example = "STAFF")
  private UserRole role;

  @Schema(description = "사용자 사용 상태", example = "ACTIVE")
  private UserStatus status;

  /** 사용자 엔티티를 응답으로 변환 */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getStatus());
  }
}
