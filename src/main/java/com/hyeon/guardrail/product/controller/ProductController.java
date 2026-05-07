package com.hyeon.guardrail.product.controller;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductDescriptionGenerateRequest;
import com.hyeon.guardrail.product.dto.ProductHistoryResponse;
import com.hyeon.guardrail.product.dto.ProductResponse;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.dto.ProductUpdateRequest;
import com.hyeon.guardrail.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/** 상품 API 컨트롤러 */
@Tag(name = "Product", description = "상품 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  /** 상품 생성 */
  @Operation(summary = "상품 생성", description = "상품을 생성합니다.")
  @PostMapping
  public BaseResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody ProductCreateRequest request) {
    return BaseResponseEntity.created(productService.createProduct(request), "상품이 생성되었습니다.");
  }

  /** 상품 조회 */
  @Operation(summary = "상품 조회", description = "상품 기본 정보를 조회합니다.")
  @GetMapping("/{productId}")
  public BaseResponseEntity<ProductResponse> getProduct(@PathVariable UUID productId) {
    return BaseResponseEntity.success(productService.getProduct(productId));
  }

  /** 상품 목록 조회 */
  @Operation(summary = "상품 목록 조회", description = "삭제되지 않은 상품 목록을 조회합니다.")
  @GetMapping
  public BaseResponseEntity<PageResponse<ProductResponse>> getProducts(
      @RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) ProductStatus status,
      @RequestParam(required = false) Boolean approvedOnly,
      @RequestParam(required = false) Boolean myOnly,
      @PageableDefault(size = 20) Pageable pageable) {
    return BaseResponseEntity.success(
        productService.getProducts(categoryId, status, approvedOnly, myOnly, pageable));
  }

  /** 상품 수정 */
  @Operation(summary = "상품 수정", description = "상품 기본 정보를 수정합니다.")
  @PutMapping("/{productId}")
  public BaseResponseEntity<ProductResponse> updateProduct(
      @PathVariable UUID productId, @Valid @RequestBody ProductUpdateRequest request) {
    return BaseResponseEntity.success(
        productService.updateProduct(productId, request), "상품이 수정되었습니다.");
  }

  /** 상품 설명 AI 생성 */
  @Operation(summary = "상품 설명 AI 생성", description = "상품 정보를 기반으로 AI 설명을 생성해 현재 상품 설명에 반영합니다.")
  @PostMapping("/{productId}/description/generate")
  public BaseResponseEntity<ProductResponse> generateProductDescription(
      @PathVariable UUID productId, @Valid @RequestBody ProductDescriptionGenerateRequest request) {
    return BaseResponseEntity.success(
        productService.generateProductDescription(productId, request), "상품 설명 AI 초안이 반영되었습니다.");
  }

  /** 상품 상태 변경 */
  @Operation(summary = "상품 상태 변경", description = "상품 상태를 문서에 정의된 전이 규칙에 따라 변경합니다.")
  @PatchMapping("/{productId}/status")
  public BaseResponseEntity<ProductResponse> updateProductStatus(
      @PathVariable UUID productId, @Valid @RequestBody ProductStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        productService.updateProductStatus(productId, request), "상품 상태가 변경되었습니다.");
  }

  /** 상품 삭제 */
  @Operation(summary = "상품 삭제", description = "상품을 완전히 삭제합니다.")
  @DeleteMapping("/{productId}")
  public BaseResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
    productService.deleteProduct(productId);
    return BaseResponseEntity.success("상품이 삭제되었습니다.");
  }

  /** 상품 이력 목록 조회 */
  @Operation(summary = "상품 이력 목록 조회", description = "상품 처리 이력 목록을 조회합니다.")
  @GetMapping("/{productId}/histories")
  public BaseResponseEntity<List<ProductHistoryResponse>> getProductHistories(
      @PathVariable UUID productId) {
    return BaseResponseEntity.success(productService.getProductHistories(productId));
  }
}
