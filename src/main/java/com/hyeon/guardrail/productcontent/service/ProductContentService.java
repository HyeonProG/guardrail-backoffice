package com.hyeon.guardrail.productcontent.service;

import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.generator.AiContentGenerator;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.product.repository.ProductRepository;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.productcontent.domain.ProductContentDraft;
import com.hyeon.guardrail.productcontent.domain.ProductContentHistory;
import com.hyeon.guardrail.productcontent.domain.ProductContentHistoryType;
import com.hyeon.guardrail.productcontent.domain.ProductContentSource;
import com.hyeon.guardrail.productcontent.domain.ProductContentStatus;
import com.hyeon.guardrail.productcontent.dto.ProductContentApplyRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentApproveRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentDraftResponse;
import com.hyeon.guardrail.productcontent.dto.ProductContentGenerateRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentHistoryResponse;
import com.hyeon.guardrail.productcontent.dto.ProductContentRejectRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentSubmitRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentUpdateRequest;
import com.hyeon.guardrail.productcontent.repository.ProductContentDraftRepository;
import com.hyeon.guardrail.productcontent.repository.ProductContentDraftRepositoryQuery;
import com.hyeon.guardrail.productcontent.repository.ProductContentHistoryRepository;
import com.hyeon.guardrail.productcontent.repository.ProductContentHistoryRepositoryQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 상품 설명 생성과 검수 승인 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class ProductContentService {

  private final ProductContentDraftRepository productContentDraftRepository;
  private final ProductContentDraftRepositoryQuery productContentDraftRepositoryQuery;
  private final ProductContentHistoryRepository productContentHistoryRepository;
  private final ProductContentHistoryRepositoryQuery productContentHistoryRepositoryQuery;
  private final ProductRepository productRepository;
  private final ProductRepositoryQuery productRepositoryQuery;
  private final AiContentGenerator aiContentGenerator;

  /** AI 상품 설명 초안 생성 */
  @Transactional
  public ProductContentDraftResponse generateDraft(
      UUID productId, ProductContentGenerateRequest request) {
    validateProductExists(productId);

    ProductContentHistoryType historyType =
        productContentDraftRepositoryQuery.existsByProductId(productId)
            ? ProductContentHistoryType.REGENERATED
            : ProductContentHistoryType.GENERATED;
    String generatedContent =
        aiContentGenerator
            .generateProductDescription(
                new ProductDescriptionGenerateCommand(
                    request.getProductName(),
                    request.getCategoryName(),
                    request.getOptionSummary(),
                    request.getFeatureKeywords()))
            .getDescriptionText();
    validateContent(generatedContent);

    ProductContentDraft draft =
        new ProductContentDraft(
            productId,
            generatedContent,
            ProductContentSource.AI,
            ProductContentStatus.GENERATED,
            request.getActorId(),
            null,
            null,
            null);
    ProductContentDraft savedDraft = productContentDraftRepository.save(draft);
    saveHistory(savedDraft.getId(), productId, request.getActorId(), historyType, null);

    return ProductContentDraftResponse.from(savedDraft);
  }

  /** 상품 설명 초안 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductContentDraftResponse> getDrafts(UUID productId, ProductContentStatus status) {
    validateProductExists(productId);
    return productContentDraftRepositoryQuery.findAllByProductId(productId, status).stream()
        .map(ProductContentDraftResponse::from)
        .toList();
  }

  /** 상품 설명 초안 단건 조회 */
  @Transactional(readOnly = true)
  public ProductContentDraftResponse getDraft(UUID productId, UUID draftId) {
    return ProductContentDraftResponse.from(findDraft(productId, draftId));
  }

  /** 상품 설명 초안 본문 수정 */
  @Transactional
  public ProductContentDraftResponse updateDraft(
      UUID productId, UUID draftId, ProductContentUpdateRequest request) {
    ProductContentDraft draft = findDraft(productId, draftId);
    validateDraftEditable(draft);
    int updatedCount =
        productContentDraftRepository.updateContent(draftId, productId, request.getContent());

    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "상품 설명 초안을 찾을 수 없습니다.");
    }

    saveHistory(draftId, productId, request.getActorId(), ProductContentHistoryType.EDITED, null);
    return ProductContentDraftResponse.from(findDraft(productId, draftId));
  }

  /** 상품 설명 초안 승인 요청 */
  @Transactional
  public ProductContentDraftResponse submitDraft(
      UUID productId, UUID draftId, ProductContentSubmitRequest request) {
    ProductContentDraft draft = findDraft(productId, draftId);
    draft.submit(request.getActorId());
    saveHistory(
        draftId, productId, request.getActorId(), ProductContentHistoryType.SUBMITTED, null);

    return ProductContentDraftResponse.from(draft);
  }

  /** 상품 설명 초안 적용 */
  @Transactional
  public ProductContentDraftResponse applyDraft(
      UUID productId, UUID draftId, ProductContentApplyRequest request) {
    ProductContentDraft draft = findDraft(productId, draftId);

    int updatedCount = productRepository.updateDescription(productId, draft.getContent());
    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
    }

    saveHistory(draftId, productId, request.getActorId(), ProductContentHistoryType.APPLIED, null);
    return ProductContentDraftResponse.from(draft);
  }

  /** 상품 설명 초안 승인 처리 */
  @Transactional
  public ProductContentDraftResponse approveDraft(
      UUID productId, UUID draftId, ProductContentApproveRequest request) {
    ProductContentDraft draft = findDraft(productId, draftId);
    draft.approve(request.getActorId());

    int updatedCount = productRepository.updateDescription(productId, draft.getContent());
    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
    }

    saveHistory(draftId, productId, request.getActorId(), ProductContentHistoryType.APPROVED, null);
    return ProductContentDraftResponse.from(draft);
  }

  /** 상품 설명 초안 반려 처리 */
  @Transactional
  public ProductContentDraftResponse rejectDraft(
      UUID productId, UUID draftId, ProductContentRejectRequest request) {
    ProductContentDraft draft = findDraft(productId, draftId);
    draft.reject(request.getReason());
    saveHistory(
        draftId,
        productId,
        request.getActorId(),
        ProductContentHistoryType.REJECTED,
        request.getReason());

    return ProductContentDraftResponse.from(draft);
  }

  /** 상품 설명 처리 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<ProductContentHistoryResponse> getHistories(UUID productId, UUID draftId) {
    findDraft(productId, draftId);
    return productContentHistoryRepositoryQuery.findAllByDraftId(productId, draftId).stream()
        .map(ProductContentHistoryResponse::from)
        .toList();
  }

  /** 상품 설명 초안 삭제 */
  @Transactional
  public void deleteDraft(UUID productId, UUID draftId) {
    ProductContentDraft draft = findDraft(productId, draftId);
    draft.delete();
  }

  private ProductContentDraft findDraft(UUID productId, UUID draftId) {
    return productContentDraftRepositoryQuery
        .findById(productId, draftId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "상품 설명 초안을 찾을 수 없습니다."));
  }

  private void validateProductExists(UUID productId) {
    productRepositoryQuery
        .findById(productId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
  }

  private void validateDraftEditable(ProductContentDraft draft) {
    if (draft.getStatus() == ProductContentStatus.APPROVED) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "승인 완료된 상품 설명은 상품 수정 API에서 변경해야 합니다.");
    }
  }

  private void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "생성된 상품 설명이 비어 있습니다.");
    }
    if (content.length() > 2000) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "상품 설명은 2000자를 초과할 수 없습니다.");
    }
  }

  private void saveHistory(
      UUID draftId, UUID productId, UUID actorId, ProductContentHistoryType type, String reason) {
    productContentHistoryRepository.save(
        new ProductContentHistory(draftId, productId, actorId, type, normalizeReason(reason)));
  }

  private String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return null;
    }
    return reason;
  }
}
