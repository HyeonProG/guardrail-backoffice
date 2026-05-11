package com.hyeon.guardrail.product.service;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.generator.AiContentGenerator;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.repository.FileAttachmentRepository;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductDescriptionGenerateRequest;
import com.hyeon.guardrail.product.dto.ProductHistoryResponse;
import com.hyeon.guardrail.product.dto.ProductResponse;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.dto.ProductUpdateRequest;
import com.hyeon.guardrail.product.repository.ProductRepository;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductRepositoryQuery productRepositoryQuery;
  private final CategoryRepositoryQuery categoryRepositoryQuery;
  private final CurrentUserService currentUserService;
  private final AiContentGenerator aiContentGenerator;
  private final FileAttachmentRepository fileAttachmentRepository;
  private final ProductSelectedOptionService productSelectedOptionService;
  private final ProductHistoryService productHistoryService;
  private final ProductStatusTransitionService productStatusTransitionService;

  /** 상품 생성 */
  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    currentUserService.validateActor(request.getActorId());
    validateCategoryAvailableForCreation(request.getCategoryId());

    Product product =
        new Product(
            request.getCategoryId(),
            request.getName(),
            normalizeDescription(request.getDescription()),
            ProductStatus.DRAFT);
    Product savedProduct = productRepository.save(product);
    productSelectedOptionService.syncSelectedOptions(
        savedProduct.getId(), request.getCategoryId(), request.getSelectedOptionItemIds());
    productHistoryService.saveHistory(
        savedProduct.getId(), request.getActorId(), ProductHistoryType.CREATED, null);

    return toResponse(savedProduct);
  }

  /** 상품 단건 조회 */
  @Transactional(readOnly = true)
  public ProductResponse getProduct(UUID productId) {
    return toResponse(findProduct(productId));
  }

  /** 상품 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> getProducts(
      UUID categoryId,
      ProductStatus status,
      Boolean approvedOnly,
      Boolean myOnly,
      Pageable pageable) {
    UUID ownerId = Boolean.TRUE.equals(myOnly) ? currentUserService.getCurrentUserId() : null;
    return PageResponse.from(
        productRepositoryQuery
            .findAll(categoryId, status, approvedOnly, ownerId, pageable)
            .map(this::toResponse));
  }

  /** 상품 기본 정보 수정 */
  @Transactional
  public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
    currentUserService.validateActor(request.getActorId());
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    validateApprovedProductEditable(product);
    validateCategoryExists(request.getCategoryId());

    String normalizedDescription = normalizeDescription(request.getDescription());
    boolean basicInfoChanged =
        isBasicInfoChanged(
            product, request.getCategoryId(), request.getName(), normalizedDescription);
    boolean selectedOptionsChanged =
        request.getSelectedOptionItemIds() != null
            && productSelectedOptionService.isSelectedOptionsChanged(
                productId, request.getSelectedOptionItemIds());

    product.updateBasicInfo(request.getCategoryId(), request.getName(), normalizedDescription);
    if (request.getSelectedOptionItemIds() != null) {
      productSelectedOptionService.syncSelectedOptions(
          productId, request.getCategoryId(), request.getSelectedOptionItemIds());
    }

    if (basicInfoChanged || selectedOptionsChanged) {
      productHistoryService.saveHistory(
          productId, request.getActorId(), ProductHistoryType.UPDATED, null);
    }
    return toResponse(product);
  }

  /** 상품 설명 AI 생성 후 현재 설명에 반영 */
  @Transactional
  public ProductResponse generateProductDescription(
      UUID productId, ProductDescriptionGenerateRequest request) {
    currentUserService.validateActor(request.getActorId());
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    validateDescriptionGenerateRequest(request);

    String generatedDescription =
        aiContentGenerator
            .generateProductDescription(
                new ProductDescriptionGenerateCommand(
                    normalizeText(request.getProductName()),
                    normalizeText(request.getCategoryName()),
                    normalizeOptionSummary(request.getOptionSummary()),
                    request.getFeatureKeywords()))
            .getDescriptionText();

    product.updateDescription(normalizeDescription(generatedDescription));
    productHistoryService.saveHistory(
        productId, request.getActorId(), ProductHistoryType.UPDATED, "AI 설명 초안 생성");
    return toResponse(product);
  }

  /** 상품 상태 변경 */
  @Transactional
  public ProductResponse updateProductStatus(UUID productId, ProductStatusUpdateRequest request) {
    currentUserService.validateActor(request.getActorId());
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    ProductHistoryType historyType = productStatusTransitionService.changeStatus(product, request);

    productHistoryService.saveHistory(
        productId, request.getActorId(), historyType, normalizeReason(request.getReason()));
    return toResponse(product);
  }

  /** 상품 삭제 */
  @Transactional
  public void deleteProduct(UUID productId) {
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    validateProductHardDeletable(product);

    productHistoryService.deleteByProductId(productId);
    productSelectedOptionService.deleteByProductId(productId);
    fileAttachmentRepository.deleteByTargetTypeAndTargetId(FileTargetType.PRODUCT, productId);
    productRepository.delete(product);
  }

  /** 상품 처리 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductHistoryResponse> getProductHistories(UUID productId) {
    findProduct(productId);
    return productHistoryService.getProductHistories(productId);
  }

  private void validateProductOwnerForStaff(UUID productId) {
    if (currentUserService.getCurrentUserRole() != com.hyeon.guardrail.user.domain.UserRole.STAFF) {
      return;
    }

    if (!productHistoryService.isProductOwner(productId, currentUserService.getCurrentUserId())) {
      throw new BaseException(BaseResponseStatus.FORBIDDEN, "본인이 등록한 상품만 관리할 수 있습니다.");
    }
  }

  private void validateApprovedProductEditable(Product product) {
    if (product.getStatus() != ProductStatus.APPROVED) {
      return;
    }

    if (currentUserService.getCurrentUserRole() == com.hyeon.guardrail.user.domain.UserRole.STAFF) {
      throw new BaseException(BaseResponseStatus.FORBIDDEN, "승인 완료 상품은 관리자 또는 운영자만 수정할 수 있습니다.");
    }
  }

  private void validateProductHardDeletable(Product product) {
    if (product.getStatus() == ProductStatus.DRAFT
        || product.getStatus() == ProductStatus.PENDING
        || product.getStatus() == ProductStatus.REJECTED) {
      return;
    }

    throw new BaseException(
        BaseResponseStatus.CONFLICT, "상품은 DRAFT, PENDING 또는 REJECTED 상태에서만 완전 삭제할 수 있습니다.");
  }

  private Product findProduct(UUID productId) {
    return productRepositoryQuery
        .findById(productId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
  }

  private void validateCategoryAvailableForCreation(UUID categoryId) {
    Category category = validateCategoryExists(categoryId);
    if (category.getStatus() == CategoryStatus.INACTIVE) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "비활성 카테고리는 상품 신규 등록에 사용할 수 없습니다.");
    }
  }

  private Category validateCategoryExists(UUID categoryId) {
    return categoryRepositoryQuery
        .findById(categoryId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
  }

  private void validateDescriptionGenerateRequest(ProductDescriptionGenerateRequest request) {
    if (request.getFeatureKeywords() == null || request.getFeatureKeywords().isEmpty()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "설명 생성을 위한 키워드를 입력해 주세요.");
    }
  }

  private String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return null;
    }
    return reason;
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return "";
    }
    return description;
  }

  private String normalizeText(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return value.trim();
  }

  private String normalizeOptionSummary(String value) {
    String normalized = normalizeText(value);
    if (normalized.isBlank()) {
      return "옵션 없음";
    }
    return normalized;
  }

  private boolean isBasicInfoChanged(
      Product product, UUID categoryId, String name, String normalizedDescription) {
    return !Objects.equals(product.getCategoryId(), categoryId)
        || !Objects.equals(product.getName(), name)
        || !Objects.equals(product.getDescription(), normalizedDescription);
  }

  private ProductResponse toResponse(Product product) {
    return ProductResponse.from(
        product, productSelectedOptionService.getSelectedOptionResponses(product.getId()));
  }
}
