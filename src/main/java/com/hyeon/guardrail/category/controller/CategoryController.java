package com.hyeon.guardrail.category.controller;

import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.dto.CategoryCreateRequest;
import com.hyeon.guardrail.category.dto.CategoryResponse;
import com.hyeon.guardrail.category.dto.CategoryStatusUpdateRequest;
import com.hyeon.guardrail.category.dto.CategoryUpdateRequest;
import com.hyeon.guardrail.category.service.CategoryService;
import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

/** 카테고리 API 컨트롤러 */
@Tag(name = "Category", description = "카테고리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final CategoryService categoryService;

  /** 카테고리 생성 */
  @Operation(summary = "카테고리 생성", description = "카테고리를 생성합니다.")
  @PostMapping
  public BaseResponseEntity<CategoryResponse> createCategory(
      @Valid @RequestBody CategoryCreateRequest request) {
    CategoryResponse response = categoryService.createCategory(request);
    return BaseResponseEntity.created(response, "카테고리가 생성되었습니다.");
  }

  /** 카테고리 조회 */
  @Operation(summary = "카테고리 조회", description = "카테고리 기본 정보를 조회합니다.")
  @GetMapping("/{categoryId}")
  public BaseResponseEntity<CategoryResponse> getCategory(@PathVariable UUID categoryId) {
    return BaseResponseEntity.success(categoryService.getCategory(categoryId));
  }

  /** 카테고리 목록 조회 */
  @Operation(summary = "카테고리 목록 조회", description = "삭제되지 않은 카테고리 목록을 조회합니다.")
  @GetMapping
  public BaseResponseEntity<PageResponse<CategoryResponse>> getCategories(
      @RequestParam(required = false) UUID parentId,
      @RequestParam(required = false) CategoryStatus status,
      @PageableDefault(size = 20) Pageable pageable) {
    return BaseResponseEntity.success(categoryService.getCategories(parentId, status, pageable));
  }

  /** 카테고리 수정 */
  @Operation(summary = "카테고리 수정", description = "카테고리 기본 정보를 수정합니다.")
  @PutMapping("/{categoryId}")
  public BaseResponseEntity<CategoryResponse> updateCategory(
      @PathVariable UUID categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
    return BaseResponseEntity.success(
        categoryService.updateCategory(categoryId, request), "카테고리가 수정되었습니다.");
  }

  /** 카테고리 상태 변경 */
  @Operation(summary = "카테고리 상태 변경", description = "카테고리 상태를 ACTIVE 또는 INACTIVE로 변경합니다.")
  @PatchMapping("/{categoryId}/status")
  public BaseResponseEntity<CategoryResponse> updateCategoryStatus(
      @PathVariable UUID categoryId, @Valid @RequestBody CategoryStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        categoryService.updateCategoryStatus(categoryId, request), "카테고리 상태가 변경되었습니다.");
  }

  /** 카테고리 삭제 */
  @Operation(summary = "카테고리 삭제", description = "카테고리를 soft delete 처리합니다.")
  @DeleteMapping("/{categoryId}")
  public BaseResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
    categoryService.deleteCategory(categoryId);
    return BaseResponseEntity.success("카테고리가 삭제되었습니다.");
  }

  /** 카테고리 복구 */
  @Operation(summary = "카테고리 복구", description = "soft delete 처리된 카테고리를 복구합니다.")
  @PatchMapping("/{categoryId}/restore")
  public BaseResponseEntity<CategoryResponse> restoreCategory(@PathVariable UUID categoryId) {
    return BaseResponseEntity.success(
        categoryService.restoreCategory(categoryId), "카테고리가 복구되었습니다.");
  }
}
