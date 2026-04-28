package com.hyeon.guardrail.user.dto;

import com.hyeon.guardrail.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사용자 상태 변경 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 상태 변경 요청")
public class UserStatusUpdateRequest {

  @NotNull
  @Schema(description = "사용자 사용 상태", example = "ACTIVE")
  private UserStatus status;
}
