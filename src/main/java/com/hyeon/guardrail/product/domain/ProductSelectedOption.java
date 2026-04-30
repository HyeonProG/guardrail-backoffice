package com.hyeon.guardrail.product.domain;

import com.hyeon.guardrail.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품에 선택된 옵션값 스냅샷 */
@Getter
@Entity
@Table(name = "product_selected_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductSelectedOption extends BaseEntity {

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "product_option_id", nullable = false)
  private UUID productOptionId;

  @Column(name = "product_option_name", nullable = false)
  private String productOptionName;

  @Column(name = "product_option_item_id", nullable = false)
  private UUID productOptionItemId;

  @Column(name = "product_option_item_name", nullable = false)
  private String productOptionItemName;

  @Column(name = "additional_price", nullable = false)
  private int additionalPrice;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;
}
