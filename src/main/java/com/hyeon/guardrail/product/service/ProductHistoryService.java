package com.hyeon.guardrail.product.service;

import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.dto.ProductHistoryResponse;
import com.hyeon.guardrail.product.repository.ProductHistoryRepository;
import com.hyeon.guardrail.product.repository.ProductHistoryRepositoryQuery;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 이력 저장과 조회를 담당한다. */
@Service
@RequiredArgsConstructor
public class ProductHistoryService {

  private final ProductHistoryRepository productHistoryRepository;
  private final ProductHistoryRepositoryQuery productHistoryRepositoryQuery;
  private final UserRepositoryQuery userRepositoryQuery;

  /** 상품 이력을 저장한다. */
  @Transactional
  public void saveHistory(UUID productId, UUID actorId, ProductHistoryType type, String reason) {
    productHistoryRepository.save(new ProductHistory(productId, actorId, type, reason));
  }

  /** 상품 이력 전체를 조회한다. */
  @Transactional(readOnly = true)
  public List<ProductHistoryResponse> getProductHistories(UUID productId) {
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

  /** 현재 사용자가 해당 상품의 등록자인지 확인한다. */
  @Transactional(readOnly = true)
  public boolean isProductOwner(UUID productId, UUID userId) {
    return productHistoryRepositoryQuery.isProductOwner(productId, userId);
  }

  /** 상품 이력을 전부 삭제한다. */
  @Transactional
  public void deleteByProductId(UUID productId) {
    productHistoryRepository.deleteByProductId(productId);
  }
}
