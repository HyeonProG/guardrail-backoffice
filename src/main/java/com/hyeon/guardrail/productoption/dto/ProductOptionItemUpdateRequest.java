package com.hyeon.guardrail.productoption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 옵션값 기본 정보 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 옵션값 기본 정보 수정 요청")
public class ProductOptionItemUpdateRequest {

  @NotBlank
  @Schema(description = "옵션값명", example = "블랙")
  private String name;
}
