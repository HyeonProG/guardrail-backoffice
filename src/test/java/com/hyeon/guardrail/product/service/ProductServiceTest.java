package com.hyeon.guardrail.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.generator.AiContentGenerator;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.file.repository.FileAttachmentRepository;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductDescriptionGenerateRequest;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.repository.ProductHistoryRepository;
import com.hyeon.guardrail.product.repository.ProductHistoryRepositoryQuery;
import com.hyeon.guardrail.product.repository.ProductRepository;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepository;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepositoryQuery;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 상품 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductRepositoryQuery productRepositoryQuery;
  @Mock private ProductHistoryRepository productHistoryRepository;
  @Mock private ProductHistoryRepositoryQuery productHistoryRepositoryQuery;
  @Mock private ProductSelectedOptionRepository productSelectedOptionRepository;
  @Mock private ProductSelectedOptionRepositoryQuery productSelectedOptionRepositoryQuery;
  @Mock private CategoryRepositoryQuery categoryRepositoryQuery;
  @Mock private ProductOptionRepositoryQuery productOptionRepositoryQuery;
  @Mock private ProductOptionItemRepositoryQuery productOptionItemRepositoryQuery;
  @Mock private CurrentUserService currentUserService;
  @Mock private AiContentGenerator aiContentGenerator;
  @Mock private FileAttachmentRepository fileAttachmentRepository;
  @Mock private UserRepositoryQuery userRepositoryQuery;

  @InjectMocks private ProductService productService;

  /** 상품 생성은 삭제되지 않은 ACTIVE 카테고리에서만 허용 */
  @Test
  void createProductRequiresActiveCategory() {
    UUID categoryId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Category category = new Category(null, "상의", CategoryStatus.INACTIVE);
    ProductCreateRequest request = new ProductCreateRequest(categoryId, "티셔츠", "상품 설명", actorId);

    when(categoryRepositoryQuery.findById(categoryId)).thenReturn(Optional.of(category));

    assertThatThrownBy(() -> productService.createProduct(request))
        .isInstanceOf(BaseException.class);
    verify(productRepository, never()).save(any(Product.class));
    verify(productHistoryRepository, never()).save(any(ProductHistory.class));
  }

  /** 상품 생성은 DRAFT 상태 상품과 CREATED 이력을 저장 */
  @Test
  void createProductStoresDraftProductAndCreatedHistory() {
    UUID categoryId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    Category category = new Category(null, "상의", CategoryStatus.ACTIVE);
    ProductCreateRequest request = new ProductCreateRequest(categoryId, "티셔츠", "상품 설명", actorId);

    when(categoryRepositoryQuery.findById(categoryId)).thenReturn(Optional.of(category));
    when(productSelectedOptionRepositoryQuery.findAllByProductId(productId)).thenReturn(List.of());
    when(productRepository.save(any(Product.class)))
        .thenAnswer(
            invocation -> {
              Product product = invocation.getArgument(0);
              ReflectionTestUtils.setField(product, "id", productId);
              return product;
            });

    var response = productService.createProduct(request);

    assertThat(response.getId()).isEqualTo(productId);
    assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);

    ArgumentCaptor<ProductHistory> historyCaptor = ArgumentCaptor.forClass(ProductHistory.class);
    verify(productHistoryRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getProductId()).isEqualTo(productId);
    assertThat(historyCaptor.getValue().getActorId()).isEqualTo(actorId);
    assertThat(historyCaptor.getValue().getType()).isEqualTo(ProductHistoryType.CREATED);
  }

  /** 문서에 정의되지 않은 상품 상태 전이는 실패 */
  @Test
  void updateProductStatusRejectsInvalidTransition() {
    UUID productId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Product product = new Product(UUID.randomUUID(), "티셔츠", "상품 설명", ProductStatus.DRAFT);
    ReflectionTestUtils.setField(product, "id", productId);

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product));

    assertThatThrownBy(
            () ->
                productService.updateProductStatus(
                    productId,
                    new ProductStatusUpdateRequest(ProductStatus.APPROVED, actorId, null)))
        .isInstanceOf(BaseException.class);
    verify(productHistoryRepository, never()).save(any(ProductHistory.class));
  }

  /** 반려 상태 변경은 reason을 필수로 요구 */
  @Test
  void updateProductStatusRequiresReasonWhenRejected() {
    UUID productId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Product product = new Product(UUID.randomUUID(), "티셔츠", "상품 설명", ProductStatus.PENDING);
    ReflectionTestUtils.setField(product, "id", productId);

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product));

    assertThatThrownBy(
            () ->
                productService.updateProductStatus(
                    productId, new ProductStatusUpdateRequest(ProductStatus.REJECTED, actorId, "")))
        .isInstanceOf(BaseException.class);
    verify(productHistoryRepository, never()).save(any(ProductHistory.class));
  }

  /** 승인 대기 상품 반려는 REJECTED 이력과 사유를 저장 */
  @Test
  void updateProductStatusStoresRejectedHistoryWithReason() {
    UUID productId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Product product = new Product(UUID.randomUUID(), "티셔츠", "상품 설명", ProductStatus.PENDING);
    ReflectionTestUtils.setField(product, "id", productId);

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product));
    when(productSelectedOptionRepositoryQuery.findAllByProductId(productId)).thenReturn(List.of());

    var response =
        productService.updateProductStatus(
            productId, new ProductStatusUpdateRequest(ProductStatus.REJECTED, actorId, "설명 보완 필요"));

    assertThat(response.getStatus()).isEqualTo(ProductStatus.REJECTED);

    ArgumentCaptor<ProductHistory> historyCaptor = ArgumentCaptor.forClass(ProductHistory.class);
    verify(productHistoryRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getType()).isEqualTo(ProductHistoryType.REJECTED);
    assertThat(historyCaptor.getValue().getReason()).isEqualTo("설명 보완 필요");
  }

  /** 승인 완료 상품 비활성화는 INACTIVATED 이력을 저장 */
  @Test
  void updateProductStatusStoresInactivatedHistory() {
    UUID productId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Product product = new Product(UUID.randomUUID(), "티셔츠", "상품 설명", ProductStatus.APPROVED);
    ReflectionTestUtils.setField(product, "id", productId);

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product));
    when(productSelectedOptionRepositoryQuery.findAllByProductId(productId)).thenReturn(List.of());

    var response =
        productService.updateProductStatus(
            productId, new ProductStatusUpdateRequest(ProductStatus.INACTIVE, actorId, null));

    assertThat(response.getStatus()).isEqualTo(ProductStatus.INACTIVE);

    ArgumentCaptor<ProductHistory> historyCaptor = ArgumentCaptor.forClass(ProductHistory.class);
    verify(productHistoryRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getType()).isEqualTo(ProductHistoryType.INACTIVATED);
    assertThat(historyCaptor.getValue().getReason()).isNull();
  }

  /** 상품 설명 AI 생성은 결과를 상품 설명에 반영하고 UPDATED 이력을 남긴다 */
  @Test
  void generateProductDescriptionUpdatesDescriptionAndStoresHistory() {
    UUID productId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    Product product = new Product(UUID.randomUUID(), "티셔츠", "기존 설명", ProductStatus.DRAFT);
    ReflectionTestUtils.setField(product, "id", productId);
    ProductDescriptionGenerateRequest request =
        new ProductDescriptionGenerateRequest(
            actorId, "티셔츠", "상의", "색상: 블랙 / 사이즈: M", List.of("가벼움", "출퇴근용"));

    when(productRepositoryQuery.findById(productId)).thenReturn(Optional.of(product));
    when(aiContentGenerator.generateProductDescription(any()))
        .thenReturn(new ProductDescriptionGenerateResult("AI가 생성한 설명입니다."));
    when(productSelectedOptionRepositoryQuery.findAllByProductId(productId)).thenReturn(List.of());

    var response = productService.generateProductDescription(productId, request);

    assertThat(response.getDescription()).isEqualTo("AI가 생성한 설명입니다.");
    assertThat(product.getDescription()).isEqualTo("AI가 생성한 설명입니다.");
    ArgumentCaptor<ProductHistory> historyCaptor = ArgumentCaptor.forClass(ProductHistory.class);
    verify(productHistoryRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getType()).isEqualTo(ProductHistoryType.UPDATED);
    assertThat(historyCaptor.getValue().getReason()).isEqualTo("AI 설명 초안 생성");
  }
}
