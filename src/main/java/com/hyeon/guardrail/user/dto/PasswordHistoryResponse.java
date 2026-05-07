package com.hyeon.guardrail.user.dto;

import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 사용자 비밀번호 이력 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "사용자 비밀번호 이력 응답")
public class PasswordHistoryResponse {

  @Schema(description = "비밀번호 이력 ID")
  private UUID passwordHistoryId;

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "임시 비밀번호 여부", example = "true")
  private boolean temporary;

  @Schema(description = "만료 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime expiredAt;

  @Schema(description = "생성 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime createdAt;

  /** 비밀번호 이력 엔티티를 응답으로 변환 */
  public static PasswordHistoryResponse from(UserPasswordHistory passwordHistory) {
    return new PasswordHistoryResponse(
        passwordHistory.getId(),
        passwordHistory.getUserId(),
        passwordHistory.isTemporary(),
        passwordHistory.getExpiredAt(),
        passwordHistory.getCreatedAt());
  }
}
