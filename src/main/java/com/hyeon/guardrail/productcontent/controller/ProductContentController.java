package com.hyeon.guardrail.productcontent.controller;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.productcontent.domain.ProductContentStatus;
import com.hyeon.guardrail.productcontent.dto.ProductContentApplyRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentApproveRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentDraftResponse;
import com.hyeon.guardrail.productcontent.dto.ProductContentGenerateRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentHistoryResponse;
import com.hyeon.guardrail.productcontent.dto.ProductContentRejectRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentSubmitRequest;
import com.hyeon.guardrail.productcontent.dto.ProductContentUpdateRequest;
import com.hyeon.guardrail.productcontent.service.ProductContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 상품 설명 생성과 검수 승인 API 컨트롤러 */
@Tag(name = "ProductContent", description = "상품 설명 생성과 검수 승인 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/contents")
public class ProductContentController {

  private final ProductContentService productContentService;

  /** 상품 설명 초안 생성 */
  @Operation(summary = "상품 설명 초안 생성", description = "AI를 호출해 상품 설명 초안을 생성합니다.")
  @PostMapping("/generate")
  public BaseResponseEntity<ProductContentDraftResponse> generateDraft(
      @PathVariable UUID productId, @Valid @RequestBody ProductContentGenerateRequest request) {
    return BaseResponseEntity.created(
        productContentService.generateDraft(productId, request), "상품 설명 초안이 생성되었습니다.");
  }

  /** 상품 설명 초안 목록 조회 */
  @Operation(summary = "상품 설명 초안 목록 조회", description = "삭제되지 않은 상품 설명 초안 목록을 조회합니다.")
  @GetMapping
  public BaseResponseEntity<List<ProductContentDraftResponse>> getDrafts(
      @PathVariable UUID productId, @RequestParam(required = false) ProductContentStatus status) {
    return BaseResponseEntity.success(productContentService.getDrafts(productId, status));
  }

  /** 상품 설명 초안 상세 조회 */
  @Operation(summary = "상품 설명 초안 상세 조회", description = "상품 설명 초안 단건을 조회합니다.")
  @GetMapping("/{draftId}")
  public BaseResponseEntity<ProductContentDraftResponse> getDraft(
      @PathVariable UUID productId, @PathVariable UUID draftId) {
    return BaseResponseEntity.success(productContentService.getDraft(productId, draftId));
  }

  /** 상품 설명 초안 수정 */
  @Operation(summary = "상품 설명 초안 수정", description = "운영자 검수 결과로 상품 설명 초안 본문을 수정합니다.")
  @PutMapping("/{draftId}")
  public BaseResponseEntity<ProductContentDraftResponse> updateDraft(
      @PathVariable UUID productId,
      @PathVariable UUID draftId,
      @Valid @RequestBody ProductContentUpdateRequest request) {
    return BaseResponseEntity.success(
        productContentService.updateDraft(productId, draftId, request), "상품 설명 초안이 수정되었습니다.");
  }

  /** 상품 설명 초안 승인 요청 */
  @Operation(summary = "상품 설명 초안 승인 요청", description = "상품 설명 초안을 관리자 승인 대기 상태로 변경합니다.")
  @PatchMapping("/{draftId}/submit")
  public BaseResponseEntity<ProductContentDraftResponse> submitDraft(
      @PathVariable UUID productId,
      @PathVariable UUID draftId,
      @Valid @RequestBody ProductContentSubmitRequest request) {
    return BaseResponseEntity.success(
        productContentService.submitDraft(productId, draftId, request), "상품 설명 초안이 승인 요청되었습니다.");
  }

  /** 상품 설명 초안 적용 */
  @Operation(summary = "상품 설명 초안 적용", description = "선택한 상품 설명 초안을 현재 상품 상세 설명에 반영합니다.")
  @PatchMapping("/{draftId}/apply")
  public BaseResponseEntity<ProductContentDraftResponse> applyDraft(
      @PathVariable UUID productId,
      @PathVariable UUID draftId,
      @Valid @RequestBody ProductContentApplyRequest request) {
    return BaseResponseEntity.success(
        productContentService.applyDraft(productId, draftId, request), "상품 설명 초안이 적용되었습니다.");
  }

  /** 상품 설명 초안 승인 */
  @Operation(summary = "상품 설명 초안 승인", description = "상품 설명 초안을 승인하고 상품 설명에 반영합니다.")
  @PatchMapping("/{draftId}/approve")
  public BaseResponseEntity<ProductContentDraftResponse> approveDraft(
      @PathVariable UUID productId,
      @PathVariable UUID draftId,
      @Valid @RequestBody ProductContentApproveRequest request) {
    return BaseResponseEntity.success(
        productContentService.approveDraft(productId, draftId, request), "상품 설명 초안이 승인되었습니다.");
  }

  /** 상품 설명 초안 반려 */
  @Operation(summary = "상품 설명 초안 반려", description = "상품 설명 초안을 반려 상태로 변경합니다.")
  @PatchMapping("/{draftId}/reject")
  public BaseResponseEntity<ProductContentDraftResponse> rejectDraft(
      @PathVariable UUID productId,
      @PathVariable UUID draftId,
      @Valid @RequestBody ProductContentRejectRequest request) {
    return BaseResponseEntity.success(
        productContentService.rejectDraft(productId, draftId, request), "상품 설명 초안이 반려되었습니다.");
  }

  /** 상품 설명 처리 이력 목록 조회 */
  @Operation(summary = "상품 설명 처리 이력 목록 조회", description = "상품 설명 초안 처리 이력 목록을 조회합니다.")
  @GetMapping("/{draftId}/histories")
  public BaseResponseEntity<List<ProductContentHistoryResponse>> getHistories(
      @PathVariable UUID productId, @PathVariable UUID draftId) {
    return BaseResponseEntity.success(productContentService.getHistories(productId, draftId));
  }

  /** 상품 설명 초안 삭제 */
  @Operation(summary = "상품 설명 초안 삭제", description = "상품 설명 초안을 soft delete 처리합니다.")
  @DeleteMapping("/{draftId}")
  public BaseResponseEntity<Void> deleteDraft(
      @PathVariable UUID productId, @PathVariable UUID draftId) {
    productContentService.deleteDraft(productId, draftId);
    return BaseResponseEntity.success("상품 설명 초안이 삭제되었습니다.");
  }
}
