package com.hyeon.guardrail.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.dto.CategoryCreateRequest;
import com.hyeon.guardrail.category.dto.CategoryStatusUpdateRequest;
import com.hyeon.guardrail.category.repository.CategoryRepository;
import com.hyeon.guardrail.category.repository.CategoryRepositoryQuery;
import com.hyeon.guardrail.common.exception.BaseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

/** 카테고리 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private CategoryRepositoryQuery categoryRepositoryQuery;

  @InjectMocks private CategoryService categoryService;

  /** 하위 카테고리 생성 시 삭제되지 않은 부모 카테고리 존재를 검증 */
  @Test
  void createCategoryRequiresExistingParentWhenParentIdExists() {
    UUID parentId = UUID.randomUUID();
    CategoryCreateRequest request = new CategoryCreateRequest(parentId, "상의");

    when(categoryRepositoryQuery.findById(parentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(BaseException.class);
    verify(categoryRepository, never()).save(any(Category.class));
  }

  /** 같은 상위 카테고리 안의 미삭제 카테고리명 중복이면 생성에 실패 */
  @Test
  void createCategoryThrowsWhenNameDuplicatedInSameParent() {
    UUID parentId = UUID.randomUUID();
    Category parent = new Category(null, "의류", CategoryStatus.ACTIVE);
    CategoryCreateRequest request = new CategoryCreateRequest(parentId, "상의");

    when(categoryRepositoryQuery.findById(parentId)).thenReturn(Optional.of(parent));
    when(categoryRepository.existsByParentIdAndNameAndDeletedFalse(parentId, request.getName()))
        .thenReturn(true);

    assertThatThrownBy(() -> categoryService.createCategory(request))
        .isInstanceOf(BaseException.class);
    verify(categoryRepository, never()).save(any(Category.class));
  }

  /** 상태 변경 요청은 deleted 값과 분리해 status만 변경 */
  @Test
  void updateCategoryStatusChangesStatusOnly() {
    UUID categoryId = UUID.randomUUID();
    Category category = new Category(null, "상의", CategoryStatus.ACTIVE);
    ReflectionTestUtils.setField(category, "id", categoryId);
    when(categoryRepositoryQuery.findById(categoryId)).thenReturn(Optional.of(category));

    var response =
        categoryService.updateCategoryStatus(
            categoryId, new CategoryStatusUpdateRequest(CategoryStatus.INACTIVE));

    assertThat(response.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
    assertThat(category.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
    assertThat(category.isDeleted()).isFalse();
  }

  /** 목록 조회는 parentId, status 조건으로 조회 저장소를 호출 */
  @Test
  void getCategoriesDelegatesParentAndStatusFilter() {
    UUID parentId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 20);
    Category category = new Category(parentId, "상의", CategoryStatus.ACTIVE);
    ReflectionTestUtils.setField(category, "id", UUID.randomUUID());

    when(categoryRepositoryQuery.findAll(parentId, true, CategoryStatus.ACTIVE, pageable))
        .thenReturn(new PageImpl<>(List.of(category), pageable, 1));

    var response = categoryService.getCategories(parentId, true, CategoryStatus.ACTIVE, pageable);

    assertThat(response.getContent()).hasSize(1);
    assertThat(response.getContent().get(0).getName()).isEqualTo("상의");
    verify(categoryRepositoryQuery).findAll(parentId, true, CategoryStatus.ACTIVE, pageable);
  }

  /** 삭제된 카테고리 복구 시 부모가 삭제되지 않았고 이름이 중복되지 않아야 한다 */
  @Test
  void restoreCategoryRestoresDeletedCategoryWhenParentAndNameAreValid() {
    UUID categoryId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Category deletedCategory = new Category(parentId, "상의", CategoryStatus.INACTIVE);
    Category parent = new Category(null, "의류", CategoryStatus.ACTIVE);
    ReflectionTestUtils.setField(deletedCategory, "id", categoryId);
    deletedCategory.delete();
    ReflectionTestUtils.setField(parent, "id", parentId);

    when(categoryRepositoryQuery.findDeletedById(categoryId))
        .thenReturn(Optional.of(deletedCategory));
    when(categoryRepositoryQuery.findById(parentId)).thenReturn(Optional.of(parent));
    when(categoryRepository.existsByParentIdAndNameAndDeletedFalseAndIdNot(
            parentId, "상의", categoryId))
        .thenReturn(false);

    var response = categoryService.restoreCategory(categoryId);

    assertThat(response.getId()).isEqualTo(categoryId);
    assertThat(deletedCategory.isDeleted()).isFalse();
    assertThat(response.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
  }

  /** 삭제된 카테고리 복구 시 같은 상위 카테고리 안의 이름 중복이면 실패 */
  @Test
  void restoreCategoryThrowsWhenNameDuplicatedInSameParent() {
    UUID categoryId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();
    Category deletedCategory = new Category(parentId, "상의", CategoryStatus.ACTIVE);
    Category parent = new Category(null, "의류", CategoryStatus.ACTIVE);
    ReflectionTestUtils.setField(deletedCategory, "id", categoryId);
    deletedCategory.delete();

    when(categoryRepositoryQuery.findDeletedById(categoryId))
        .thenReturn(Optional.of(deletedCategory));
    when(categoryRepositoryQuery.findById(parentId)).thenReturn(Optional.of(parent));
    when(categoryRepository.existsByParentIdAndNameAndDeletedFalseAndIdNot(
            eq(parentId), eq("상의"), eq(categoryId)))
        .thenReturn(true);

    assertThatThrownBy(() -> categoryService.restoreCategory(categoryId))
        .isInstanceOf(BaseException.class);
  }
}
