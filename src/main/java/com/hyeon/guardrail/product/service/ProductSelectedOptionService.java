package com.hyeon.guardrail.product.service;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.product.domain.ProductSelectedOption;
import com.hyeon.guardrail.product.dto.ProductSelectedOptionResponse;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepository;
import com.hyeon.guardrail.product.repository.ProductSelectedOptionRepositoryQuery;
import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.repository.ProductOptionItemRepositoryQuery;
import com.hyeon.guardrail.productoption.repository.ProductOptionRepositoryQuery;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 선택 옵션 스냅샷을 동기화하고 조회한다. */
@Service
@RequiredArgsConstructor
public class ProductSelectedOptionService {

  private final ProductSelectedOptionRepository productSelectedOptionRepository;
  private final ProductSelectedOptionRepositoryQuery productSelectedOptionRepositoryQuery;
  private final ProductOptionRepositoryQuery productOptionRepositoryQuery;
  private final ProductOptionItemRepositoryQuery productOptionItemRepositoryQuery;

  /** 상품에 저장된 선택 옵션이 변경되었는지 확인한다. */
  @Transactional(readOnly = true)
  public boolean isSelectedOptionsChanged(UUID productId, Collection<UUID> selectedOptionItemIds) {
    Set<UUID> currentOptionIds =
        productSelectedOptionRepositoryQuery.findAllByProductId(productId).stream()
            .map(ProductSelectedOption::getProductOptionItemId)
            .collect(Collectors.toSet());
    Set<UUID> nextOptionIds = new HashSet<>(selectedOptionItemIds);
    return !currentOptionIds.equals(nextOptionIds);
  }

  /** 상품 선택 옵션 스냅샷을 다시 저장한다. */
  @Transactional
  public void syncSelectedOptions(
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

  /** 상품 선택 옵션 응답을 조회한다. */
  @Transactional(readOnly = true)
  public List<ProductSelectedOptionResponse> getSelectedOptionResponses(UUID productId) {
    return productSelectedOptionRepositoryQuery.findAllByProductId(productId).stream()
        .map(ProductSelectedOptionResponse::from)
        .toList();
  }

  /** 상품 선택 옵션을 모두 삭제한다. */
  @Transactional
  public void deleteByProductId(UUID productId) {
    productSelectedOptionRepository.deleteByProductId(productId);
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
