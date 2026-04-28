package com.hyeon.guardrail.user.dto;

import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserRole;
import com.hyeon.guardrail.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 사용자 생성 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 생성 응답")
public class UserCreateResponse {

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

  @Schema(description = "초기 전달용 비밀번호")
  private String initialPassword;

  /** 사용자 엔티티와 초기 비밀번호로 생성 응답 변환 */
  public static UserCreateResponse of(User user, String initialPassword) {
    return new UserCreateResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.getStatus(),
        initialPassword);
  }
}
