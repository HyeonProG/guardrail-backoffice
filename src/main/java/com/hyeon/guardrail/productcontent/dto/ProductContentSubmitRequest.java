package com.hyeon.guardrail.productcontent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 설명 초안 승인 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 설명 초안 승인 요청")
public class ProductContentSubmitRequest {

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
