package com.hyeon.guardrail.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 사용자 비밀번호 변경 요청 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 비밀번호 변경 요청")
public class ChangePasswordRequest {

  @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
  @Schema(description = "현재 비밀번호", example = "admin1234!")
  private String currentPassword;

  @NotBlank(message = "새 비밀번호를 입력해 주세요.")
  @Schema(description = "새 비밀번호", example = "newPassword123!")
  private String newPassword;
}
