package com.hyeon.guardrail.productoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.dto.ProductOptionCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionUpdateRequest;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepository;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepository;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepositoryQuery;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 상품 옵션 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class ProductOptionServiceTest {

  @Mock private ProductOptionRepository productOptionRepository;
  @Mock private ProductOptionItemRepository productOptionItemRepository;
  @Mock private ProductOptionRepositoryQuery productOptionRepositoryQuery;
  @Mock private ProductOptionItemRepositoryQuery productOptionItemRepositoryQuery;
  @Mock private ProductRepositoryQuery productRepositoryQuery;

  @InjectMocks private ProductOptionService productOptionService;

  /** 옵션 그룹 생성은 삭제되지 않은 상품만 허용 */
  @Test
  void createProductOptionRequiresExistingProduct() {
    UUID productId = UUID.randomUUID();
    ProductOptionCreateRequest request =
        new ProductOptionCreateRequest("색상", 1, ProductOptionStatus.ACTIVE, UUID.randomUUID());

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productOptionService.createProductOption(productId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionRepository, never()).save(any(ProductOption.class));
  }

  /** 같은 상품 안에서 미삭제 옵션명 중복은 실패 */
  @Test
  void createProductOptionRejectsDuplicatedNameInProduct() {
    UUID productId = UUID.randomUUID();
    ProductOptionCreateRequest request =
        new ProductOptionCreateRequest("색상", 1, ProductOptionStatus.ACTIVE, UUID.randomUUID());

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product(productId)));
    when(productOptionRepositoryQuery.existsByProductIdAndName(productId, "색상", null))
        .thenReturn(true);

    assertThatThrownBy(() -> productOptionService.createProductOption(productId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionRepository, never()).save(any(ProductOption.class));
  }

  /** 같은 상품 안에서 미삭제 옵션 그룹 정렬 순서 중복은 실패 */
  @Test
  void updateProductOptionRejectsDuplicatedSortOrderInProduct() {
    UUID productId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    ProductOption productOption = new ProductOption(productId, "색상", 1, ProductOptionStatus.ACTIVE);
    ProductOptionUpdateRequest request = new ProductOptionUpdateRequest("색상", 2, UUID.randomUUID());

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product(productId)));
    when(productOptionRepositoryQuery.findById(productId, productOptionId))
        .thenReturn(Optional.of(productOption));
    when(productOptionRepositoryQuery.existsByProductIdAndName(productId, "색상", productOptionId))
        .thenReturn(false);
    when(productOptionRepositoryQuery.existsByProductIdAndSortOrder(productId, 2, productOptionId))
        .thenReturn(true);

    assertThatThrownBy(
            () -> productOptionService.updateProductOption(productId, productOptionId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionRepository, never())
        .updateBasicInfo(any(UUID.class), any(UUID.class), any(String.class), any(Integer.class));
  }

  /** 같은 옵션 그룹 안에서 미삭제 옵션값명 중복은 실패 */
  @Test
  void createProductOptionItemRejectsDuplicatedNameInOption() {
    UUID productId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    ProductOptionItemCreateRequest request =
        new ProductOptionItemCreateRequest(
            "블랙", 1000, 1, ProductOptionStatus.ACTIVE, UUID.randomUUID());

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product(productId)));
    when(productOptionRepositoryQuery.findById(productId, productOptionId))
        .thenReturn(Optional.of(new ProductOption(productId, "색상", 1, ProductOptionStatus.ACTIVE)));
    when(productOptionItemRepositoryQuery.existsByProductOptionIdAndName(
            productOptionId, "블랙", null))
        .thenReturn(true);

    assertThatThrownBy(
            () -> productOptionService.createProductOptionItem(productId, productOptionId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionItemRepository, never()).save(any(ProductOptionItem.class));
  }

  /** 추가 금액은 0 이상만 허용 */
  @Test
  void createProductOptionItemRejectsNegativeAdditionalPrice() {
    UUID productId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    ProductOptionItemCreateRequest request =
        new ProductOptionItemCreateRequest(
            "블랙", -1, 1, ProductOptionStatus.ACTIVE, UUID.randomUUID());

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product(productId)));
    when(productOptionRepositoryQuery.findById(productId, productOptionId))
        .thenReturn(Optional.of(new ProductOption(productId, "색상", 1, ProductOptionStatus.ACTIVE)));

    assertThatThrownBy(
            () -> productOptionService.createProductOptionItem(productId, productOptionId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionItemRepository, never()).save(any(ProductOptionItem.class));
  }

  /** 옵션값 상태 변경은 기본 정보 수정과 분리되어 상태만 변경 */
  @Test
  void updateProductOptionItemStatusChangesOnlyStatus() {
    UUID productId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    UUID productOptionItemId = UUID.randomUUID();
    ProductOption productOption = new ProductOption(productId, "색상", 1, ProductOptionStatus.ACTIVE);
    ProductOptionItem productOptionItem =
        new ProductOptionItem(productOptionId, "블랙", 1000, 1, ProductOptionStatus.ACTIVE);

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product(productId)));
    when(productOptionRepositoryQuery.findById(productId, productOptionId))
        .thenReturn(Optional.of(productOption));
    when(productOptionItemRepositoryQuery.findById(productOptionId, productOptionItemId))
        .thenReturn(Optional.of(productOptionItem));

    var response =
        productOptionService.updateProductOptionItemStatus(
            productId,
            productOptionId,
            productOptionItemId,
            new ProductOptionItemStatusUpdateRequest(
                ProductOptionStatus.INACTIVE, UUID.randomUUID()));

    assertThat(response.getStatus()).isEqualTo(ProductOptionStatus.INACTIVE);
    assertThat(response.getName()).isEqualTo("블랙");
    assertThat(response.getAdditionalPrice()).isEqualTo(1000);
  }

  private Product product(UUID productId) {
    Product product = new Product(UUID.randomUUID(), "티셔츠", "상품 설명", 10, ProductStatus.DRAFT);
    ReflectionTestUtils.setField(product, "id", productId);
    return product;
  }
}
