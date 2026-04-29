package com.hyeon.guardrail.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 생성 요청")
public class ProductCreateRequest {

  @NotNull
  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String name;

  @Schema(description = "초기 설명 메모", example = "초기 메모가 있으면 입력하고, 없으면 비워둘 수 있습니다.")
  private String description;

  @Min(0)
  @Schema(description = "판매 가능 수량", example = "100")
  private int quantity;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
