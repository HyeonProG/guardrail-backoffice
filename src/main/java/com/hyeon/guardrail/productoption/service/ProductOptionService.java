package com.hyeon.guardrail.productoption.service;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.dto.ProductOptionCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemResponse;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionResponse;
import com.hyeon.guardrail.productoption.dto.ProductOptionStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionUpdateRequest;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepository;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepository;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepositoryQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 옵션 그룹과 옵션값 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class ProductOptionService {

  private final ProductOptionRepository productOptionRepository;
  private final ProductOptionItemRepository productOptionItemRepository;
  private final ProductOptionRepositoryQuery productOptionRepositoryQuery;
  private final ProductOptionItemRepositoryQuery productOptionItemRepositoryQuery;
  private final ProductRepositoryQuery productRepositoryQuery;

  /** 옵션 그룹 생성 */
  @Transactional
  public ProductOptionResponse createProductOption(
      UUID productId, ProductOptionCreateRequest request) {
    validateProductExists(productId);
    validateProductOptionNameNotDuplicated(productId, request.getName(), null);
    validateProductOptionSortOrderNotDuplicated(productId, request.getSortOrder(), null);

    ProductOption productOption =
        new ProductOption(
            productId, request.getName(), request.getSortOrder(), request.getStatus());

    return ProductOptionResponse.from(productOptionRepository.save(productOption));
  }

  /** 옵션 그룹 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductOptionResponse> getProductOptions(UUID productId, ProductOptionStatus status) {
    validateProductExists(productId);
    return productOptionRepositoryQuery.findAllByProductId(productId, status).stream()
        .map(ProductOptionResponse::from)
        .toList();
  }

  /** 옵션 그룹 단건 조회 */
  @Transactional(readOnly = true)
  public ProductOptionResponse getProductOption(UUID productId, UUID productOptionId) {
    validateProductExists(productId);
    ProductOption productOption = findProductOption(productId, productOptionId);
    List<ProductOptionItemResponse> items =
        productOptionItemRepositoryQuery.findAllByProductOptionId(productOptionId, null).stream()
            .map(ProductOptionItemResponse::from)
            .toList();

    return ProductOptionResponse.from(productOption, items);
  }

  /** 옵션 그룹 기본 정보 수정 */
  @Transactional
  public ProductOptionResponse updateProductOption(
      UUID productId, UUID productOptionId, ProductOptionUpdateRequest request) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    validateProductOptionNameNotDuplicated(productId, request.getName(), productOptionId);
    validateProductOptionSortOrderNotDuplicated(productId, request.getSortOrder(), productOptionId);

    int updatedCount =
        productOptionRepository.updateBasicInfo(
            productId, productOptionId, request.getName(), request.getSortOrder());
    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다.");
    }

    return ProductOptionResponse.from(findProductOption(productId, productOptionId));
  }

  /** 옵션 그룹 상태 변경 */
  @Transactional
  public ProductOptionResponse updateProductOptionStatus(
      UUID productId, UUID productOptionId, ProductOptionStatusUpdateRequest request) {
    validateProductExists(productId);
    ProductOption productOption = findProductOption(productId, productOptionId);
    changeStatus(productOption, request.getStatus());
    return ProductOptionResponse.from(productOption);
  }

  /** 옵션 그룹 삭제 */
  @Transactional
  public void deleteProductOption(UUID productId, UUID productOptionId) {
    validateProductExists(productId);
    ProductOption productOption = findProductOption(productId, productOptionId);
    productOption.delete();
  }

  /** 옵션값 생성 */
  @Transactional
  public ProductOptionItemResponse createProductOptionItem(
      UUID productId, UUID productOptionId, ProductOptionItemCreateRequest request) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    validateAdditionalPrice(request.getAdditionalPrice());
    validateProductOptionItemNameNotDuplicated(productOptionId, request.getName(), null);
    validateProductOptionItemSortOrderNotDuplicated(productOptionId, request.getSortOrder(), null);

    ProductOptionItem productOptionItem =
        new ProductOptionItem(
            productOptionId,
            request.getName(),
            request.getAdditionalPrice(),
            request.getSortOrder(),
            request.getStatus());

    return ProductOptionItemResponse.from(productOptionItemRepository.save(productOptionItem));
  }

  /** 옵션값 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductOptionItemResponse> getProductOptionItems(
      UUID productId, UUID productOptionId, ProductOptionStatus status) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    return productOptionItemRepositoryQuery
        .findAllByProductOptionId(productOptionId, status)
        .stream()
        .map(ProductOptionItemResponse::from)
        .toList();
  }

  /** 옵션값 기본 정보 수정 */
  @Transactional
  public ProductOptionItemResponse updateProductOptionItem(
      UUID productId,
      UUID productOptionId,
      UUID productOptionItemId,
      ProductOptionItemUpdateRequest request) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    findProductOptionItem(productOptionId, productOptionItemId);
    validateAdditionalPrice(request.getAdditionalPrice());
    validateProductOptionItemNameNotDuplicated(
        productOptionId, request.getName(), productOptionItemId);
    validateProductOptionItemSortOrderNotDuplicated(
        productOptionId, request.getSortOrder(), productOptionItemId);

    int updatedCount =
        productOptionItemRepository.updateBasicInfo(
            productOptionId,
            productOptionItemId,
            request.getName(),
            request.getAdditionalPrice(),
            request.getSortOrder());
    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "옵션값을 찾을 수 없습니다.");
    }

    return ProductOptionItemResponse.from(
        findProductOptionItem(productOptionId, productOptionItemId));
  }

  /** 옵션값 상태 변경 */
  @Transactional
  public ProductOptionItemResponse updateProductOptionItemStatus(
      UUID productId,
      UUID productOptionId,
      UUID productOptionItemId,
      ProductOptionItemStatusUpdateRequest request) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    ProductOptionItem productOptionItem =
        findProductOptionItem(productOptionId, productOptionItemId);
    changeStatus(productOptionItem, request.getStatus());
    return ProductOptionItemResponse.from(productOptionItem);
  }

  /** 옵션값 삭제 */
  @Transactional
  public void deleteProductOptionItem(
      UUID productId, UUID productOptionId, UUID productOptionItemId) {
    validateProductExists(productId);
    findProductOption(productId, productOptionId);
    ProductOptionItem productOptionItem =
        findProductOptionItem(productOptionId, productOptionItemId);
    productOptionItem.delete();
  }

  private void validateProductExists(UUID productId) {
    productRepositoryQuery
        .findById(productId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
  }

  private ProductOption findProductOption(UUID productId, UUID productOptionId) {
    return productOptionRepositoryQuery
        .findById(productId, productOptionId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다."));
  }

  private ProductOptionItem findProductOptionItem(UUID productOptionId, UUID productOptionItemId) {
    return productOptionItemRepositoryQuery
        .findById(productOptionId, productOptionItemId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "옵션값을 찾을 수 없습니다."));
  }

  private void validateProductOptionNameNotDuplicated(
      UUID productId, String name, UUID excludedId) {
    if (productOptionRepositoryQuery.existsByProductIdAndName(productId, name, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션명입니다.");
    }
  }

  private void validateProductOptionSortOrderNotDuplicated(
      UUID productId, int sortOrder, UUID excludedId) {
    if (productOptionRepositoryQuery.existsByProductIdAndSortOrder(
        productId, sortOrder, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션 그룹 정렬 순서입니다.");
    }
  }

  private void validateProductOptionItemNameNotDuplicated(
      UUID productOptionId, String name, UUID excludedId) {
    if (productOptionItemRepositoryQuery.existsByProductOptionIdAndName(
        productOptionId, name, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션값명입니다.");
    }
  }

  private void validateProductOptionItemSortOrderNotDuplicated(
      UUID productOptionId, int sortOrder, UUID excludedId) {
    if (productOptionItemRepositoryQuery.existsByProductOptionIdAndSortOrder(
        productOptionId, sortOrder, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션값 정렬 순서입니다.");
    }
  }

  private void validateAdditionalPrice(int additionalPrice) {
    if (additionalPrice < 0) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "추가 금액은 0 이상이어야 합니다.");
    }
  }

  private void changeStatus(ProductOption productOption, ProductOptionStatus status) {
    if (status == ProductOptionStatus.ACTIVE) {
      productOption.activate();
      return;
    }
    productOption.inactivate();
  }

  private void changeStatus(ProductOptionItem productOptionItem, ProductOptionStatus status) {
    if (status == ProductOptionStatus.ACTIVE) {
      productOptionItem.activate();
      return;
    }
    productOptionItem.inactivate();
  }
}
