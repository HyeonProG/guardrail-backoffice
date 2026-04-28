package com.hyeon.guardrail.productoption.dto;

import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 옵션값 상태 변경 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 옵션값 상태 변경 요청")
public class ProductOptionItemStatusUpdateRequest {

  @NotNull
  @Schema(description = "옵션값 상태", example = "ACTIVE")
  private ProductOptionStatus status;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
