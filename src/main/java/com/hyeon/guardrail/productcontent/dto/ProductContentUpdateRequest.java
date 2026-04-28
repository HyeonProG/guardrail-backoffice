package com.hyeon.guardrail.productcontent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 설명 초안 본문 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 설명 초안 본문 수정 요청")
public class ProductContentUpdateRequest {

  @NotBlank
  @Size(max = 2000)
  @Schema(description = "상품 설명 초안 본문")
  private String content;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
