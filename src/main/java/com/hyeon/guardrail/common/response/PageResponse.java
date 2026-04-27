package com.hyeon.guardrail.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

/** 공통 페이징 응답 데이터 구조 */
@Getter
@Schema(description = "공통 페이징 응답 데이터 구조")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {

  @Schema(description = "현재 페이지 데이터 목록")
  private final List<T> content;

  @Schema(description = "현재 페이지 번호", example = "0")
  private final int page;

  @Schema(description = "페이지 크기", example = "20")
  private final int size;

  @Schema(description = "전체 데이터 수", example = "100")
  private final long totalElements;

  @Schema(description = "전체 페이지 수", example = "5")
  private final int totalPages;

  /** Spring Data Page 기반 페이징 응답 생성 */
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
