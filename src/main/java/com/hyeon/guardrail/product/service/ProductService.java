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
import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductSelectedOption;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductDescriptionGenerateRequest;
import com.hyeon.guardrail.product.dto.ProductHistoryResponse;
import com.hyeon.guardrail.product.dto.ProductResponse;
import com.hyeon.guardrail.product.dto.ProductSelectedOptionResponse;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.dto.ProductUpdateRequest;
import com.hyeon.guardrail.product.repository.ProductHistoryRepository;
import com.hyeon.guardrail.product.repository.ProductHistoryRepositoryQuery;
import com.hyeon.guardrail.product.repository.ProductRepository;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepository;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepositoryQuery;
import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepositoryQuery;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  private final ProductHistoryRepository productHistoryRepository;
  private final ProductHistoryRepositoryQuery productHistoryRepositoryQuery;
  private final CategoryRepositoryQuery categoryRepositoryQuery;
  private final CurrentUserService currentUserService;
  private final AiContentGenerator aiContentGenerator;
  private final ProductSelectedOptionRepository productSelectedOptionRepository;
  private final ProductSelectedOptionRepositoryQuery productSelectedOptionRepositoryQuery;
  private final FileAttachmentRepository fileAttachmentRepository;
  private final ProductOptionRepositoryQuery productOptionRepositoryQuery;
  private final ProductOptionItemRepositoryQuery productOptionItemRepositoryQuery;
  private final UserRepositoryQuery userRepositoryQuery;

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
    syncSelectedOptions(
        savedProduct.getId(), request.getCategoryId(), request.getSelectedOptionItemIds());
    saveHistory(savedProduct.getId(), request.getActorId(), ProductHistoryType.CREATED, null);

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
            && isSelectedOptionsChanged(productId, request.getSelectedOptionItemIds());

    product.updateBasicInfo(request.getCategoryId(), request.getName(), normalizedDescription);
    if (request.getSelectedOptionItemIds() != null) {
      syncSelectedOptions(productId, request.getCategoryId(), request.getSelectedOptionItemIds());
    }

    if (basicInfoChanged || selectedOptionsChanged) {
      saveHistory(productId, request.getActorId(), ProductHistoryType.UPDATED, null);
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
    saveHistory(productId, request.getActorId(), ProductHistoryType.UPDATED, "AI 설명 초안 생성");
    return toResponse(product);
  }

  /** 상품 상태 변경 */
  @Transactional
  public ProductResponse updateProductStatus(UUID productId, ProductStatusUpdateRequest request) {
    currentUserService.validateActor(request.getActorId());
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    ProductHistoryType historyType = changeStatus(product, request);

    saveHistory(productId, request.getActorId(), historyType, normalizeReason(request.getReason()));
    return toResponse(product);
  }

  /** 상품 삭제 */
  @Transactional
  public void deleteProduct(UUID productId) {
    validateProductOwnerForStaff(productId);
    Product product = findProduct(productId);
    validateProductHardDeletable(product);

    productHistoryRepository.deleteByProductId(productId);
    productSelectedOptionRepository.deleteByProductId(productId);
    fileAttachmentRepository.deleteByTargetTypeAndTargetId(FileTargetType.PRODUCT, productId);
    productRepository.delete(product);
  }

  /** 상품 처리 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductHistoryResponse> getProductHistories(UUID productId) {
    validateProductOwnerForStaff(productId);
    findProduct(productId);
    List<ProductHistory> histories = productHistoryRepositoryQuery.findAllByProductId(productId);
    Map<UUID, String> actorNameById =
        userRepositoryQuery
            .findAllByIds(histories.stream().map(ProductHistory::getActorId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(User::getId, User::getName));

    return histories.stream()
        .map(
            history ->
                ProductHistoryResponse.from(
                    history, actorNameById.getOrDefault(history.getActorId(), "-")))
        .toList();
  }

  private void validateProductOwnerForStaff(UUID productId) {
    if (currentUserService.getCurrentUserRole() != com.hyeon.guardrail.user.domain.UserRole.STAFF) {
      return;
    }

    if (!productHistoryRepositoryQuery.isProductOwner(
        productId, currentUserService.getCurrentUserId())) {
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

  private ProductHistoryType changeStatus(Product product, ProductStatusUpdateRequest request) {
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

  private void saveHistory(UUID productId, UUID actorId, ProductHistoryType type, String reason) {
    productHistoryRepository.save(new ProductHistory(productId, actorId, type, reason));
  }

  private boolean isBasicInfoChanged(
      Product product, UUID categoryId, String name, String normalizedDescription) {
    return !Objects.equals(product.getCategoryId(), categoryId)
        || !Objects.equals(product.getName(), name)
        || !Objects.equals(product.getDescription(), normalizedDescription);
  }

  private boolean isSelectedOptionsChanged(UUID productId, Collection<UUID> selectedOptionItemIds) {
    Set<UUID> currentOptionIds =
        productSelectedOptionRepositoryQuery.findAllByProductId(productId).stream()
            .map(ProductSelectedOption::getProductOptionItemId)
            .collect(Collectors.toSet());
    Set<UUID> nextOptionIds = new HashSet<>(selectedOptionItemIds);
    return !currentOptionIds.equals(nextOptionIds);
  }

  private ProductResponse toResponse(Product product) {
    return ProductResponse.from(
        product,
        productSelectedOptionRepositoryQuery.findAllByProductId(product.getId()).stream()
            .map(ProductSelectedOptionResponse::from)
            .toList());
  }

  private void syncSelectedOptions(
      UUID productId, UUID categoryId, Collection<UUID> selectedOptionItemIds) {
    productSelectedOptionRepository.deleteByProductId(productId);

    if (selectedOptionItemIds == null || selectedOptionItemIds.isEmpty()) {
      return;
    }

    List<ProductOptionItem> optionItems =
        productOptionItemRepositoryQuery.findAllByIds(selectedOptionItemIds);

    if (optionItems.size() != selectedOptionItemIds.size()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "선택한 옵션값 중 일부를 찾을 수 없습니다.");
    }

    Map<UUID, ProductOption> optionById =
        productOptionRepositoryQuery
            .findAllByIds(
                optionItems.stream().map(ProductOptionItem::getProductOptionId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

    List<ProductSelectedOption> selections =
        optionItems.stream()
            .map(
                item -> {
                  ProductOption option = optionById.get(item.getProductOptionId());
                  validateSelectedOption(categoryId, option, item);
                  return new ProductSelectedOption(
                      productId,
                      option.getId(),
                      option.getName(),
                      item.getId(),
                      item.getName(),
                      item.getSortOrder());
                })
            .toList();

    productSelectedOptionRepository.saveAll(selections);
  }

  private void validateSelectedOption(
      UUID categoryId, ProductOption option, ProductOptionItem item) {
    if (option == null
        || !option.getCategoryId().equals(categoryId)
        || option.getStatus() != ProductOptionStatus.ACTIVE) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "카테고리에 맞지 않는 옵션이 선택되었습니다.");
    }

    if (item.getStatus() != ProductOptionStatus.ACTIVE) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "비활성 옵션값은 선택할 수 없습니다.");
    }
  }
}
