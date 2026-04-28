package com.hyeon.guardrail.user.dto;

import com.hyeon.guardrail.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사용자 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 생성 요청")
public class UserCreateRequest {

  @Email
  @NotBlank
  @Schema(description = "사용자 이메일", example = "staff@example.com")
  private String email;

  @NotBlank
  @Schema(description = "사용자 이름", example = "홍길동")
  private String name;

  @NotNull
  @Schema(description = "사용자 역할", example = "STAFF")
  private UserRole role;
}
