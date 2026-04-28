package com.hyeon.guardrail.productcontent.domain;

/** 상품 설명 초안 검수 상태 */
public enum ProductContentStatus {
  GENERATED,
  READY_FOR_APPROVAL,
  APPROVED,
  REJECTED
}
