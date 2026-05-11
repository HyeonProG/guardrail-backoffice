package com.hyeon.guardrail.productoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.dto.ProductOptionCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemResponse;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemUpdateRequest;
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
  @Mock private CategoryRepositoryQuery categoryRepositoryQuery;
  @Mock private CurrentUserService currentUserService;

  @InjectMocks private ProductOptionService productOptionService;

  /** 옵션 그룹 생성은 삭제되지 않은 카테고리에서만 허용한다. */
  @Test
  void createProductOptionRequiresExistingCategory() {
    UUID categoryId = UUID.randomUUID();
    ProductOptionCreateRequest request =
        new ProductOptionCreateRequest("색상", ProductOptionStatus.ACTIVE);

    when(categoryRepositoryQuery.findById(categoryId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productOptionService.createProductOption(categoryId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionRepository, never()).save(any(ProductOption.class));
  }

  /** 같은 카테고리 안에서 미삭제 옵션명 중복은 실패한다. */
  @Test
  void createProductOptionRejectsDuplicatedNameInCategory() {
    UUID categoryId = UUID.randomUUID();
    ProductOptionCreateRequest request =
        new ProductOptionCreateRequest("색상", ProductOptionStatus.ACTIVE);

    when(categoryRepositoryQuery.findById(categoryId))
        .thenReturn(Optional.of(category(categoryId)));
    when(productOptionRepositoryQuery.existsByCategoryIdAndName(categoryId, "색상", null))
        .thenReturn(true);

    assertThatThrownBy(() -> productOptionService.createProductOption(categoryId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionRepository, never()).save(any(ProductOption.class));
  }

  /** 옵션 그룹 기본 정보 수정은 이름만 수정하고 정렬 순서는 유지한다. */
  @Test
  void updateProductOptionKeepsSortOrder() {
    UUID categoryId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    ProductOption productOption =
        new ProductOption(categoryId, "색상", 1, ProductOptionStatus.ACTIVE);
    ProductOptionUpdateRequest request = new ProductOptionUpdateRequest("컬러");

    when(categoryRepositoryQuery.findById(categoryId))
        .thenReturn(Optional.of(category(categoryId)));
    when(productOptionRepositoryQuery.findById(categoryId, productOptionId))
        .thenReturn(Optional.of(productOption));
    when(productOptionRepositoryQuery.existsByCategoryIdAndName(categoryId, "컬러", productOptionId))
        .thenReturn(false);

    var response = productOptionService.updateProductOption(categoryId, productOptionId, request);

    assertThat(response.getName()).isEqualTo("컬러");
    assertThat(productOption.getSortOrder()).isEqualTo(1);
  }

  /** 같은 옵션 그룹 안에서 미삭제 옵션값명 중복은 실패한다. */
  @Test
  void createProductOptionItemRejectsDuplicatedNameInOption() {
    UUID categoryId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    ProductOptionItemCreateRequest request =
        new ProductOptionItemCreateRequest("블랙", ProductOptionStatus.ACTIVE);

    when(categoryRepositoryQuery.findById(categoryId))
        .thenReturn(Optional.of(category(categoryId)));
    when(productOptionRepositoryQuery.findById(categoryId, productOptionId))
        .thenReturn(
            Optional.of(new ProductOption(categoryId, "색상", 1, ProductOptionStatus.ACTIVE)));
    when(productOptionItemRepositoryQuery.existsByProductOptionIdAndName(
            productOptionId, "블랙", null))
        .thenReturn(true);

    assertThatThrownBy(
            () ->
                productOptionService.createProductOptionItem(categoryId, productOptionId, request))
        .isInstanceOf(BaseException.class);
    verify(productOptionItemRepository, never()).save(any(ProductOptionItem.class));
  }

  /** 옵션값 상태 변경은 기본 정보 수정과 분리되어 상태만 변경한다. */
  @Test
  void updateProductOptionItemStatusChangesOnlyStatus() {
    UUID categoryId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    UUID productOptionItemId = UUID.randomUUID();
    ProductOption productOption =
        new ProductOption(categoryId, "색상", 1, ProductOptionStatus.ACTIVE);
    ProductOptionItem productOptionItem =
        new ProductOptionItem(productOptionId, "블랙", 1, ProductOptionStatus.ACTIVE);

    when(categoryRepositoryQuery.findById(categoryId))
        .thenReturn(Optional.of(category(categoryId)));
    when(productOptionRepositoryQuery.findById(categoryId, productOptionId))
        .thenReturn(Optional.of(productOption));
    when(productOptionItemRepositoryQuery.findById(productOptionId, productOptionItemId))
        .thenReturn(Optional.of(productOptionItem));

    var response =
        productOptionService.updateProductOptionItemStatus(
            categoryId,
            productOptionId,
            productOptionItemId,
            new ProductOptionItemStatusUpdateRequest(ProductOptionStatus.INACTIVE));

    assertThat(response.getStatus()).isEqualTo(ProductOptionStatus.INACTIVE);
    assertThat(response.getName()).isEqualTo("블랙");
  }

  /** 옵션값 기본 정보 수정은 같은 옵션 그룹 안에서 자기 자신을 제외하고 이름 중복만 검사한다. */
  @Test
  void updateProductOptionItemExcludesCurrentItemFromDuplicateChecks() {
    UUID categoryId = UUID.randomUUID();
    UUID productOptionId = UUID.randomUUID();
    UUID productOptionItemId = UUID.randomUUID();
    ProductOption productOption =
        new ProductOption(categoryId, "색상", 1, ProductOptionStatus.ACTIVE);
    ProductOptionItem productOptionItem =
        new ProductOptionItem(productOptionId, "블랙", 1, ProductOptionStatus.ACTIVE);

    when(categoryRepositoryQuery.findById(categoryId))
        .thenReturn(Optional.of(category(categoryId)));
    when(productOptionRepositoryQuery.findById(categoryId, productOptionId))
        .thenReturn(Optional.of(productOption));
    when(productOptionItemRepositoryQuery.findById(productOptionId, productOptionItemId))
        .thenReturn(Optional.of(productOptionItem));
    when(productOptionItemRepositoryQuery.existsByProductOptionIdAndName(
            productOptionId, "블랙", productOptionItemId))
        .thenReturn(false);

    ProductOptionItemResponse response =
        productOptionService.updateProductOptionItem(
            categoryId,
            productOptionId,
            productOptionItemId,
            new ProductOptionItemUpdateRequest("블랙"));

    assertThat(response.getName()).isEqualTo("블랙");
    assertThat(response.getSortOrder()).isEqualTo(1);
    verify(productOptionItemRepositoryQuery)
        .existsByProductOptionIdAndName(productOptionId, "블랙", productOptionItemId);
  }

  private Category category(UUID categoryId) {
    Category category = new Category(null, "카테고리1", CategoryStatus.ACTIVE);
    ReflectionTestUtils.setField(category, "id", categoryId);
    return category;
  }
}
