package com.hyeon.guardrail.product.dto;

import com.hyeon.guardrail.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 상태 변경 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 상태 변경 요청")
public class ProductStatusUpdateRequest {

  @NotNull
  @Schema(description = "변경할 상품 상태", example = "PENDING")
  private ProductStatus status;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;

  @Schema(description = "반려 또는 상태 변경 사유")
  private String reason;
}
