package com.hyeon.guardrail.productoption.service;

import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.security.CurrentUserService;
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
  private final CategoryRepositoryQuery categoryRepositoryQuery;
  private final CurrentUserService currentUserService;

  /** 옵션 그룹 생성 */
  @Transactional
  public ProductOptionResponse createProductOption(
      UUID categoryId, ProductOptionCreateRequest request) {
    validateCategoryExists(categoryId);
    validateProductOptionNameNotDuplicated(categoryId, request.getName(), null);
    int nextSortOrder = productOptionRepositoryQuery.findNextSortOrderByCategoryId(categoryId);

    ProductOption productOption =
        new ProductOption(categoryId, request.getName(), nextSortOrder, request.getStatus());

    return ProductOptionResponse.from(productOptionRepository.save(productOption));
  }

  /** 옵션 그룹 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductOptionResponse> getProductOptions(
      UUID categoryId, ProductOptionStatus status) {
    validateCategoryExists(categoryId);
    return productOptionRepositoryQuery.findAllByCategoryId(categoryId, status).stream()
        .map(
            productOption ->
                ProductOptionResponse.from(
                    productOption,
                    productOptionItemRepositoryQuery
                        .findAllByProductOptionId(productOption.getId(), status)
                        .stream()
                        .map(ProductOptionItemResponse::from)
                        .toList()))
        .toList();
  }

  /** 옵션 그룹 단건 조회 */
  @Transactional(readOnly = true)
  public ProductOptionResponse getProductOption(UUID categoryId, UUID productOptionId) {
    validateCategoryExists(categoryId);
    ProductOption productOption = findProductOption(categoryId, productOptionId);
    List<ProductOptionItemResponse> items =
        productOptionItemRepositoryQuery.findAllByProductOptionId(productOptionId, null).stream()
            .map(ProductOptionItemResponse::from)
            .toList();

    return ProductOptionResponse.from(productOption, items);
  }

  /** 옵션 그룹 기본 정보 수정 */
  @Transactional
  public ProductOptionResponse updateProductOption(
      UUID categoryId, UUID productOptionId, ProductOptionUpdateRequest request) {
    validateCategoryExists(categoryId);
    ProductOption productOption = findProductOption(categoryId, productOptionId);
    validateProductOptionNameNotDuplicated(categoryId, request.getName(), productOptionId);
    productOption.updateBasicInfo(request.getName());

    return ProductOptionResponse.from(productOption);
  }

  /** 옵션 그룹 상태 변경 */
  @Transactional
  public ProductOptionResponse updateProductOptionStatus(
      UUID categoryId, UUID productOptionId, ProductOptionStatusUpdateRequest request) {
    currentUserService.requireAdminOrOperator();
    validateCategoryExists(categoryId);
    ProductOption productOption = findProductOption(categoryId, productOptionId);
    changeStatus(productOption, request.getStatus());
    return ProductOptionResponse.from(productOption);
  }

  /** 옵션 그룹 삭제 */
  @Transactional
  public void deleteProductOption(UUID categoryId, UUID productOptionId) {
    currentUserService.requireAdminOrOperator();
    validateCategoryExists(categoryId);
    ProductOption productOption = findProductOption(categoryId, productOptionId);
    productOption.delete();
  }

  /** 옵션값 생성 */
  @Transactional
  public ProductOptionItemResponse createProductOptionItem(
      UUID categoryId, UUID productOptionId, ProductOptionItemCreateRequest request) {
    validateCategoryExists(categoryId);
    findProductOption(categoryId, productOptionId);
    validateAdditionalPrice(request.getAdditionalPrice());
    validateProductOptionItemNameNotDuplicated(productOptionId, request.getName(), null);
    int nextSortOrder =
        productOptionItemRepositoryQuery.findNextSortOrderByProductOptionId(productOptionId);

    ProductOptionItem productOptionItem =
        new ProductOptionItem(
            productOptionId,
            request.getName(),
            request.getAdditionalPrice(),
            nextSortOrder,
            request.getStatus());

    return ProductOptionItemResponse.from(productOptionItemRepository.save(productOptionItem));
  }

  /** 옵션값 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductOptionItemResponse> getProductOptionItems(
      UUID categoryId, UUID productOptionId, ProductOptionStatus status) {
    validateCategoryExists(categoryId);
    findProductOption(categoryId, productOptionId);
    return productOptionItemRepositoryQuery
        .findAllByProductOptionId(productOptionId, status)
        .stream()
        .map(ProductOptionItemResponse::from)
        .toList();
  }

  /** 옵션값 기본 정보 수정 */
  @Transactional
  public ProductOptionItemResponse updateProductOptionItem(
      UUID categoryId,
      UUID productOptionId,
      UUID productOptionItemId,
      ProductOptionItemUpdateRequest request) {
    validateCategoryExists(categoryId);
    findProductOption(categoryId, productOptionId);
    ProductOptionItem productOptionItem =
        findProductOptionItem(productOptionId, productOptionItemId);
    validateAdditionalPrice(request.getAdditionalPrice());
    validateProductOptionItemNameNotDuplicated(
        productOptionId, request.getName(), productOptionItemId);
    productOptionItem.updateBasicInfo(request.getName(), request.getAdditionalPrice());

    return ProductOptionItemResponse.from(productOptionItem);
  }

  /** 옵션값 상태 변경 */
  @Transactional
  public ProductOptionItemResponse updateProductOptionItemStatus(
      UUID categoryId,
      UUID productOptionId,
      UUID productOptionItemId,
      ProductOptionItemStatusUpdateRequest request) {
    currentUserService.requireAdminOrOperator();
    validateCategoryExists(categoryId);
    findProductOption(categoryId, productOptionId);
    ProductOptionItem productOptionItem =
        findProductOptionItem(productOptionId, productOptionItemId);
    changeStatus(productOptionItem, request.getStatus());
    return ProductOptionItemResponse.from(productOptionItem);
  }

  /** 옵션값 삭제 */
  @Transactional
  public void deleteProductOptionItem(
      UUID categoryId, UUID productOptionId, UUID productOptionItemId) {
    currentUserService.requireAdminOrOperator();
    validateCategoryExists(categoryId);
    findProductOption(categoryId, productOptionId);
    ProductOptionItem productOptionItem =
        findProductOptionItem(productOptionId, productOptionItemId);
    productOptionItem.delete();
  }

  private void validateCategoryExists(UUID categoryId) {
    categoryRepositoryQuery
        .findById(categoryId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
  }

  private ProductOption findProductOption(UUID categoryId, UUID productOptionId) {
    return productOptionRepositoryQuery
        .findById(categoryId, productOptionId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다."));
  }

  private ProductOptionItem findProductOptionItem(UUID productOptionId, UUID productOptionItemId) {
    return productOptionItemRepositoryQuery
        .findById(productOptionId, productOptionItemId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "옵션값을 찾을 수 없습니다."));
  }

  private void validateProductOptionNameNotDuplicated(
      UUID categoryId, String name, UUID excludedId) {
    if (productOptionRepositoryQuery.existsByCategoryIdAndName(categoryId, name, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션명입니다.");
    }
  }

  private void validateProductOptionItemNameNotDuplicated(
      UUID productOptionId, String name, UUID excludedId) {
    if (productOptionItemRepositoryQuery.existsByProductOptionIdAndName(
        productOptionId, name, excludedId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 옵션값명입니다.");
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
