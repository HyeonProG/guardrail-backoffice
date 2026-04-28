package com.hyeon.guardrail.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 카테고리 기본 정보 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 기본 정보 수정 요청")
public class CategoryUpdateRequest {

  @Schema(description = "상위 카테고리 ID")
  private UUID parentId;

  @NotBlank
  @Schema(description = "카테고리명", example = "상의")
  private String name;
}
