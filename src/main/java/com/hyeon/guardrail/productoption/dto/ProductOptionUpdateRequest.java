package com.hyeon.guardrail.productoption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 옵션 그룹 기본 정보 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 옵션 그룹 기본 정보 수정 요청")
public class ProductOptionUpdateRequest {

  @NotBlank
  @Schema(description = "옵션명", example = "색상")
  private String name;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
