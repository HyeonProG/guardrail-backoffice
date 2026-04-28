package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

  @Email
  @NotBlank
  @Schema(description = "사용자 이메일", example = "staff@example.com")
  private String email;

  @NotBlank
  @Schema(description = "비밀번호")
  private String password;

  @NotNull
  @Schema(description = "디바이스 유형", example = "WEB")
  private DeviceType deviceType;
}
