package com.hyeon.guardrail.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

/** 공통 페이징 응답 데이터 구조 */
public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
