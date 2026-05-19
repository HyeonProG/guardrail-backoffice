package com.hyeon.guardrail.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 상품 상태 전이 규칙 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class ProductStatusTransitionServiceTest {

  @Mock private CurrentUserService currentUserService;

  @InjectMocks private ProductStatusTransitionService productStatusTransitionService;

  /** 작성 중 상품은 승인 대기로 전이할 수 있다. */
  @Test
  void draftCanBeSubmitted() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.DRAFT);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(ProductStatus.PENDING, UUID.randomUUID(), null));

    assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    assertThat(historyType).isEqualTo(ProductHistoryType.SUBMITTED);
    verify(currentUserService, never()).requireAdminOrOperator();
  }

  /** 반려 상품은 다시 승인 대기로 전이할 수 있다. */
  @Test
  void rejectedCanBeResubmitted() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.REJECTED);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(ProductStatus.PENDING, UUID.randomUUID(), null));

    assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    assertThat(historyType).isEqualTo(ProductHistoryType.SUBMITTED);
  }

  /** 승인 완료 상품은 운영자 또는 관리자 권한 검증 후 다시 승인 대기로 전이할 수 있다. */
  @Test
  void approvedCanBeResubmittedByAdminOrOperator() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.APPROVED);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(ProductStatus.PENDING, UUID.randomUUID(), null));

    verify(currentUserService).requireAdminOrOperator();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
    assertThat(historyType).isEqualTo(ProductHistoryType.SUBMITTED);
  }

  /** 승인 완료는 운영자 또는 관리자 권한 검증 후 처리한다. */
  @Test
  void approveRequiresAdminOrOperator() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.PENDING);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(ProductStatus.APPROVED, UUID.randomUUID(), null));

    verify(currentUserService).requireAdminOrOperator();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.APPROVED);
    assertThat(historyType).isEqualTo(ProductHistoryType.APPROVED);
  }

  /** 반려는 운영자 또는 관리자 권한과 반려 사유를 함께 요구한다. */
  @Test
  void rejectRequiresAdminOrOperatorAndReason() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.PENDING);

    assertThatThrownBy(
            () ->
                productStatusTransitionService.changeStatus(
                    product,
                    new ProductStatusUpdateRequest(ProductStatus.REJECTED, UUID.randomUUID(), " ")))
        .isInstanceOf(BaseException.class);

    verify(currentUserService).requireAdminOrOperator();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
  }

  /** 승인 완료 상품도 운영자 또는 관리자 권한과 반려 사유 검증 후 반려할 수 있다. */
  @Test
  void approvedCanBeRejectedByAdminOrOperator() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.APPROVED);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(
                ProductStatus.REJECTED, UUID.randomUUID(), "승인 이후 보완 필요"));

    verify(currentUserService).requireAdminOrOperator();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.REJECTED);
    assertThat(historyType).isEqualTo(ProductHistoryType.REJECTED);
  }

  /** 승인 완료 상품 비활성화도 운영자 또는 관리자 권한 검증 후 처리한다. */
  @Test
  void inactivateRequiresAdminOrOperator() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.APPROVED);

    ProductHistoryType historyType =
        productStatusTransitionService.changeStatus(
            product,
            new ProductStatusUpdateRequest(ProductStatus.INACTIVE, UUID.randomUUID(), null));

    verify(currentUserService).requireAdminOrOperator();
    assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    assertThat(historyType).isEqualTo(ProductHistoryType.INACTIVATED);
  }

  /** 허용되지 않은 상태 전이는 예외로 차단한다. */
  @Test
  void unsupportedStatusTransitionFails() {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", ProductStatus.DRAFT);

    assertThatThrownBy(
            () ->
                productStatusTransitionService.changeStatus(
                    product,
                    new ProductStatusUpdateRequest(
                        ProductStatus.APPROVED, UUID.randomUUID(), null)))
        .isInstanceOf(BaseException.class);
  }
}
