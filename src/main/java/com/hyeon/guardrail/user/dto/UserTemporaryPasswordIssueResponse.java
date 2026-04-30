package com.hyeon.guardrail.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 사용자 임시 비밀번호 재발급 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 임시 비밀번호 재발급 응답")
public class UserTemporaryPasswordIssueResponse {

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "이메일", example = "staff@example.com")
  private String email;

  @Schema(description = "임시 비밀번호 발급 여부", example = "true")
  private boolean temporary;

  @Schema(description = "발급 일시")
  private LocalDateTime issuedAt;
}
