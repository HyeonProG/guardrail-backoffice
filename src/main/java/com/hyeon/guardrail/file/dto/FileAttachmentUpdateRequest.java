package com.hyeon.guardrail.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 파일 첨부 기본 정보 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "파일 첨부 기본 정보 수정 요청")
public class FileAttachmentUpdateRequest {

  @NotBlank
  @Schema(description = "저장 파일명", example = "product-main-updated.jpg")
  private String fileName;

  @NotBlank
  @Schema(description = "원본 파일명", example = "main-updated.jpg")
  private String originalFileName;

  @NotBlank
  @Schema(description = "저장 위치", example = "/uploads/products/product-main-updated.jpg")
  private String filePath;

  @Min(1)
  @Schema(description = "파일 크기", example = "1048576")
  private long fileSize;

  @NotBlank
  @Schema(description = "콘텐츠 타입", example = "image/jpeg")
  private String contentType;

  @Min(1)
  @Schema(description = "정렬 순서", example = "1")
  private int sortOrder;
}
