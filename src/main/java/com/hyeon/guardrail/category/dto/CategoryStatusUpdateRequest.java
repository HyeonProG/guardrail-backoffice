package com.hyeon.guardrail.category.dto;

import com.hyeon.guardrail.category.domain.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 카테고리 상태 변경 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 상태 변경 요청")
public class CategoryStatusUpdateRequest {

  @NotNull
  @Schema(description = "카테고리 운영 상태", example = "ACTIVE")
  private CategoryStatus status;
}
