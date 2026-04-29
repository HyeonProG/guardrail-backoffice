package com.hyeon.guardrail.category.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** 카테고리 Querydsl 조회 저장소 테스트 */
@DataJpaTest
@Import(CategoryRepositoryQuery.class)
class CategoryRepositoryQueryTest {

  @Autowired private CategoryRepository categoryRepository;
  @Autowired private CategoryRepositoryQuery categoryRepositoryQuery;

  /** 허용되지 않은 정렬 필드는 잘못된 요청 예외로 차단 */
  @Test
  void findAllThrowsWhenSortFieldIsNotAllowed() {
    categoryRepository.save(new Category(null, "의류", CategoryStatus.ACTIVE));

    assertThatThrownBy(
            () ->
                categoryRepositoryQuery.findAll(
                    null,
                    false,
                    null,
                    PageRequest.of(0, 20, Sort.by(Sort.Order.asc("unknownField")))))
        .isInstanceOf(BaseException.class)
        .extracting("status")
        .isEqualTo(BaseResponseStatus.INVALID_REQUEST);
  }
}
