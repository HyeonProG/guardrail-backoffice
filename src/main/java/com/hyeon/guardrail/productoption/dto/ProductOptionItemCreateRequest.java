package com.hyeon.guardrail.productoption.dto;

import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 옵션값 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 옵션값 생성 요청")
public class ProductOptionItemCreateRequest {

  @NotBlank
  @Schema(description = "옵션값명", example = "블랙")
  private String name;

  @Min(0)
  @Schema(description = "추가 금액", example = "1000")
  private int additionalPrice;

  @Min(0)
  @Schema(description = "옵션값 정렬 순서", example = "1")
  private int sortOrder;

  @NotNull
  @Schema(description = "옵션값 상태", example = "ACTIVE")
  private ProductOptionStatus status;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
