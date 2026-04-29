package com.hyeon.guardrail.category.repository;

import com.hyeon.guardrail.category.domain.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 카테고리 기본 영속성 저장소 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  /** 삭제되지 않은 최상위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdIsNullAndNameAndDeletedFalse(String name);

  /** 삭제되지 않은 하위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdAndNameAndDeletedFalse(UUID parentId, String name);

  /** 특정 카테고리를 제외한 삭제되지 않은 최상위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdIsNullAndNameAndDeletedFalseAndIdNot(String name, UUID categoryId);

  /** 특정 카테고리를 제외한 삭제되지 않은 하위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdAndNameAndDeletedFalseAndIdNot(
      UUID parentId, String name, UUID categoryId);
}
