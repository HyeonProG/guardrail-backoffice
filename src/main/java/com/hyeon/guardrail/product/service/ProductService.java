package com.hyeon.guardrail.product.service;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductHistoryResponse;
import com.hyeon.guardrail.product.dto.ProductResponse;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.dto.ProductUpdateRequest;
import com.hyeon.guardrail.product.repository.ProductHistoryRepository;
import com.hyeon.guardrail.product.repository.ProductHistoryRepositoryQuery;
import com.hyeon.guardrail.product.repository.ProductRepository;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import java.util.List;
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
  private final ProductHistoryRepository productHistoryRepository;
  private final ProductHistoryRepositoryQuery productHistoryRepositoryQuery;
  private final CategoryRepositoryQuery categoryRepositoryQuery;

  /** 상품 생성 */
  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    validateCategoryAvailableForCreation(request.getCategoryId());

    Product product =
        new Product(
            request.getCategoryId(),
            request.getName(),
            request.getDescription(),
            request.getQuantity(),
            ProductStatus.DRAFT);
    Product savedProduct = productRepository.save(product);
    saveHistory(savedProduct.getId(), request.getActorId(), ProductHistoryType.CREATED, null);

    return ProductResponse.from(savedProduct);
  }

  /** 상품 단건 조회 */
  @Transactional(readOnly = true)
  public ProductResponse getProduct(UUID productId) {
    return ProductResponse.from(findProduct(productId));
  }

  /** 상품 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> getProducts(
      UUID categoryId, ProductStatus status, Pageable pageable) {
    return PageResponse.from(
        productRepositoryQuery.findAll(categoryId, status, pageable).map(ProductResponse::from));
  }

  /** 상품 기본 정보 수정 */
  @Transactional
  public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
    findProduct(productId);
    validateCategoryExists(request.getCategoryId());

    int updatedCount =
        productRepository.updateBasicInfo(
            productId,
            request.getCategoryId(),
            request.getName(),
            request.getDescription(),
            request.getQuantity());

    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
    }

    saveHistory(productId, request.getActorId(), ProductHistoryType.UPDATED, null);
    return ProductResponse.from(findProduct(productId));
  }

  /** 상품 상태 변경 */
  @Transactional
  public ProductResponse updateProductStatus(UUID productId, ProductStatusUpdateRequest request) {
    Product product = findProduct(productId);
    ProductHistoryType historyType = changeStatus(product, request);

    saveHistory(productId, request.getActorId(), historyType, normalizeReason(request.getReason()));
    return ProductResponse.from(product);
  }

  /** 상품 삭제 */
  @Transactional
  public void deleteProduct(UUID productId) {
    Product product = findProduct(productId);
    product.delete();
  }

  /** 상품 처리 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductHistoryResponse> getProductHistories(UUID productId) {
    findProduct(productId);
    return productHistoryRepositoryQuery.findAllByProductId(productId).stream()
        .map(ProductHistoryResponse::from)
        .toList();
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
      product.approve();
      return ProductHistoryType.APPROVED;
    }

    if (status == ProductStatus.REJECTED) {
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

  private String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return null;
    }
    return reason;
  }

  private void saveHistory(UUID productId, UUID actorId, ProductHistoryType type, String reason) {
    productHistoryRepository.save(new ProductHistory(productId, actorId, type, reason));
  }
}
