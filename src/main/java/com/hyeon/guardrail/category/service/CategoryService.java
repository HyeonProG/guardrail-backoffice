package com.hyeon.guardrail.category.service;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.dto.CategoryCreateRequest;
import com.hyeon.guardrail.category.dto.CategoryResponse;
import com.hyeon.guardrail.category.dto.CategoryStatusUpdateRequest;
import com.hyeon.guardrail.category.dto.CategoryUpdateRequest;
import com.hyeon.guardrail.category.repository.CategoryRepository;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.common.security.CurrentUserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 카테고리 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryRepositoryQuery categoryRepositoryQuery;
  private final CurrentUserService currentUserService;

  /** 카테고리 생성 */
  @Transactional
  public CategoryResponse createCategory(CategoryCreateRequest request) {
    validateParentExists(request.getParentId());
    validateNameNotDuplicated(request.getParentId(), request.getName());

    Category category =
        new Category(request.getParentId(), request.getName(), CategoryStatus.ACTIVE);
    return CategoryResponse.from(categoryRepository.save(category));
  }

  /** 카테고리 단건 조회 */
  @Transactional(readOnly = true)
  public CategoryResponse getCategory(UUID categoryId) {
    return CategoryResponse.from(findActiveCategory(categoryId));
  }

  /** 카테고리 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<CategoryResponse> getCategories(
      UUID parentId, boolean filterByParent, CategoryStatus status, Pageable pageable) {
    return PageResponse.from(
        categoryRepositoryQuery
            .findAll(parentId, filterByParent, status, pageable)
            .map(CategoryResponse::from));
  }

  /** 삭제된 카테고리 목록 조회 */
  @Transactional(readOnly = true)
  public List<CategoryResponse> getDeletedCategories() {
    currentUserService.requireAdminOrOperator();
    return categoryRepositoryQuery.findDeletedAll().stream().map(CategoryResponse::from).toList();
  }

  /** 카테고리 기본 정보 수정 */
  @Transactional
  public CategoryResponse updateCategory(UUID categoryId, CategoryUpdateRequest request) {
    validateParentExists(request.getParentId());
    validateNameNotDuplicated(categoryId, request.getParentId(), request.getName());

    Category category = findActiveCategory(categoryId);
    category.updateBasicInfo(request.getParentId(), request.getName());
    return CategoryResponse.from(category);
  }

  /** 카테고리 상태 변경 */
  @Transactional
  public CategoryResponse updateCategoryStatus(
      UUID categoryId, CategoryStatusUpdateRequest request) {
    currentUserService.requireAdminOrOperator();
    Category category = findActiveCategory(categoryId);

    if (request.getStatus() == CategoryStatus.ACTIVE) {
      category.activate();
    } else {
      category.inactivate();
    }

    return CategoryResponse.from(category);
  }

  /** 카테고리 삭제 */
  @Transactional
  public void deleteCategory(UUID categoryId) {
    currentUserService.requireAdminOrOperator();
    Category category = findActiveCategory(categoryId);
    category.delete();
  }

  /** 카테고리 복구 */
  @Transactional
  public CategoryResponse restoreCategory(UUID categoryId) {
    currentUserService.requireAdminOrOperator();
    Category category = findDeletedCategory(categoryId);
    validateParentExists(category.getParentId());
    validateNameNotDuplicated(category.getId(), category.getParentId(), category.getName());

    category.restore();
    return CategoryResponse.from(category);
  }

  private Category findActiveCategory(UUID categoryId) {
    return categoryRepositoryQuery
        .findById(categoryId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
  }

  private Category findDeletedCategory(UUID categoryId) {
    return categoryRepositoryQuery
        .findDeletedById(categoryId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "삭제된 카테고리를 찾을 수 없습니다."));
  }

  private void validateParentExists(UUID parentId) {
    if (parentId == null) {
      return;
    }

    findActiveCategory(parentId);
  }

  private void validateNameNotDuplicated(UUID parentId, String name) {
    boolean duplicated =
        parentId == null
            ? categoryRepository.existsByParentIdIsNullAndNameAndDeletedFalse(name)
            : categoryRepository.existsByParentIdAndNameAndDeletedFalse(parentId, name);

    if (duplicated) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 카테고리명입니다.");
    }
  }

  private void validateNameNotDuplicated(UUID categoryId, UUID parentId, String name) {
    boolean duplicated =
        parentId == null
            ? categoryRepository.existsByParentIdIsNullAndNameAndDeletedFalseAndIdNot(
                name, categoryId)
            : categoryRepository.existsByParentIdAndNameAndDeletedFalseAndIdNot(
                parentId, name, categoryId);

    if (duplicated) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 카테고리명입니다.");
    }
  }
}
