package com.hyeon.guardrail.product.service;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 상품 상태 전이 규칙과 권한 검증을 담당한다. */
@Service
@RequiredArgsConstructor
public class ProductStatusTransitionService {

  private final CurrentUserService currentUserService;

  /** 요청에 따라 상품 상태를 전이하고 대응 이력 타입을 반환한다. */
  public ProductHistoryType changeStatus(Product product, ProductStatusUpdateRequest request) {
    ProductStatus status = request.getStatus();

    if (status == ProductStatus.PENDING) {
      product.submit();
      return ProductHistoryType.SUBMITTED;
    }

    if (status == ProductStatus.APPROVED) {
      currentUserService.requireAdminOrOperator();
      product.approve();
      return ProductHistoryType.APPROVED;
    }

    if (status == ProductStatus.REJECTED) {
      currentUserService.requireAdminOrOperator();
      validateRejectReason(request.getReason());
      product.reject();
      return ProductHistoryType.REJECTED;
    }

    if (status == ProductStatus.INACTIVE) {
      currentUserService.requireAdminOrOperator();
      product.inactivate();
      return ProductHistoryType.INACTIVATED;
    }

    throw new BaseException(BaseResponseStatus.CONFLICT, "허용되지 않은 상품 상태 변경입니다.");
  }

  private void validateRejectReason(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "반려 사유는 필수입니다.");
    }
  }
}
