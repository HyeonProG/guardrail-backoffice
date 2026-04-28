package com.hyeon.guardrail.productoption.controller;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.dto.ProductOptionCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemCreateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemResponse;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionItemUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionResponse;
import com.hyeon.guardrail.productoption.dto.ProductOptionStatusUpdateRequest;
import com.hyeon.guardrail.productoption.dto.ProductOptionUpdateRequest;
import com.hyeon.guardrail.productoption.service.ProductOptionService;
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

/** 상품 옵션 API 컨트롤러 */
@Tag(name = "ProductOption", description = "상품 옵션 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/options")
public class ProductOptionController {

  private final ProductOptionService productOptionService;

  /** 옵션 그룹 생성 */
  @Operation(summary = "옵션 그룹 생성", description = "상품의 옵션 그룹을 생성합니다.")
  @PostMapping
  public BaseResponseEntity<ProductOptionResponse> createProductOption(
      @PathVariable UUID productId, @Valid @RequestBody ProductOptionCreateRequest request) {
    return BaseResponseEntity.created(
        productOptionService.createProductOption(productId, request), "옵션 그룹이 생성되었습니다.");
  }

  /** 옵션 그룹 목록 조회 */
  @Operation(summary = "옵션 그룹 목록 조회", description = "삭제되지 않은 옵션 그룹 목록을 조회합니다.")
  @GetMapping
  public BaseResponseEntity<List<ProductOptionResponse>> getProductOptions(
      @PathVariable UUID productId, @RequestParam(required = false) ProductOptionStatus status) {
    return BaseResponseEntity.success(productOptionService.getProductOptions(productId, status));
  }

  /** 옵션 그룹 상세 조회 */
  @Operation(summary = "옵션 그룹 상세 조회", description = "옵션 그룹과 옵션값 목록을 조회합니다.")
  @GetMapping("/{productOptionId}")
  public BaseResponseEntity<ProductOptionResponse> getProductOption(
      @PathVariable UUID productId, @PathVariable UUID productOptionId) {
    return BaseResponseEntity.success(
        productOptionService.getProductOption(productId, productOptionId));
  }

  /** 옵션 그룹 기본 정보 수정 */
  @Operation(summary = "옵션 그룹 수정", description = "옵션 그룹 기본 정보를 수정합니다.")
  @PutMapping("/{productOptionId}")
  public BaseResponseEntity<ProductOptionResponse> updateProductOption(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @Valid @RequestBody ProductOptionUpdateRequest request) {
    return BaseResponseEntity.success(
        productOptionService.updateProductOption(productId, productOptionId, request),
        "옵션 그룹이 수정되었습니다.");
  }

  /** 옵션 그룹 상태 변경 */
  @Operation(summary = "옵션 그룹 상태 변경", description = "옵션 그룹 상태를 변경합니다.")
  @PatchMapping("/{productOptionId}/status")
  public BaseResponseEntity<ProductOptionResponse> updateProductOptionStatus(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @Valid @RequestBody ProductOptionStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        productOptionService.updateProductOptionStatus(productId, productOptionId, request),
        "옵션 그룹 상태가 변경되었습니다.");
  }

  /** 옵션 그룹 삭제 */
  @Operation(summary = "옵션 그룹 삭제", description = "옵션 그룹을 soft delete 처리합니다.")
  @DeleteMapping("/{productOptionId}")
  public BaseResponseEntity<Void> deleteProductOption(
      @PathVariable UUID productId, @PathVariable UUID productOptionId) {
    productOptionService.deleteProductOption(productId, productOptionId);
    return BaseResponseEntity.success("옵션 그룹이 삭제되었습니다.");
  }

  /** 옵션값 생성 */
  @Operation(summary = "옵션값 생성", description = "옵션 그룹에 옵션값을 생성합니다.")
  @PostMapping("/{productOptionId}/items")
  public BaseResponseEntity<ProductOptionItemResponse> createProductOptionItem(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @Valid @RequestBody ProductOptionItemCreateRequest request) {
    return BaseResponseEntity.created(
        productOptionService.createProductOptionItem(productId, productOptionId, request),
        "옵션값이 생성되었습니다.");
  }

  /** 옵션값 목록 조회 */
  @Operation(summary = "옵션값 목록 조회", description = "삭제되지 않은 옵션값 목록을 조회합니다.")
  @GetMapping("/{productOptionId}/items")
  public BaseResponseEntity<List<ProductOptionItemResponse>> getProductOptionItems(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @RequestParam(required = false) ProductOptionStatus status) {
    return BaseResponseEntity.success(
        productOptionService.getProductOptionItems(productId, productOptionId, status));
  }

  /** 옵션값 기본 정보 수정 */
  @Operation(summary = "옵션값 수정", description = "옵션값 기본 정보를 수정합니다.")
  @PutMapping("/{productOptionId}/items/{productOptionItemId}")
  public BaseResponseEntity<ProductOptionItemResponse> updateProductOptionItem(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @PathVariable UUID productOptionItemId,
      @Valid @RequestBody ProductOptionItemUpdateRequest request) {
    return BaseResponseEntity.success(
        productOptionService.updateProductOptionItem(
            productId, productOptionId, productOptionItemId, request),
        "옵션값이 수정되었습니다.");
  }

  /** 옵션값 상태 변경 */
  @Operation(summary = "옵션값 상태 변경", description = "옵션값 상태를 변경합니다.")
  @PatchMapping("/{productOptionId}/items/{productOptionItemId}/status")
  public BaseResponseEntity<ProductOptionItemResponse> updateProductOptionItemStatus(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @PathVariable UUID productOptionItemId,
      @Valid @RequestBody ProductOptionItemStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        productOptionService.updateProductOptionItemStatus(
            productId, productOptionId, productOptionItemId, request),
        "옵션값 상태가 변경되었습니다.");
  }

  /** 옵션값 삭제 */
  @Operation(summary = "옵션값 삭제", description = "옵션값을 soft delete 처리합니다.")
  @DeleteMapping("/{productOptionId}/items/{productOptionItemId}")
  public BaseResponseEntity<Void> deleteProductOptionItem(
      @PathVariable UUID productId,
      @PathVariable UUID productOptionId,
      @PathVariable UUID productOptionItemId) {
    productOptionService.deleteProductOptionItem(productId, productOptionId, productOptionItemId);
    return BaseResponseEntity.success("옵션값이 삭제되었습니다.");
  }
}
