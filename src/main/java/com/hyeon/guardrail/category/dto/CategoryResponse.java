package com.hyeon.guardrail.category.dto;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 카테고리 기본 정보 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "카테고리 기본 정보 응답")
public class CategoryResponse {

  @Schema(description = "카테고리 ID")
  private UUID id;

  @Schema(description = "상위 카테고리 ID")
  private UUID parentId;

  @Schema(description = "카테고리명", example = "상의")
  private String name;

  @Schema(description = "카테고리 운영 상태", example = "ACTIVE")
  private CategoryStatus status;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 카테고리 엔티티를 응답으로 변환 */
  public static CategoryResponse from(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getParentId(),
        category.getName(),
        category.getStatus(),
        category.getCreatedAt(),
        category.getUpdatedAt());
  }
}
