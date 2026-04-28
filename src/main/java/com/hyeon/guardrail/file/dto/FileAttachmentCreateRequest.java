package com.hyeon.guardrail.file.dto;

import com.hyeon.guardrail.file.domain.FileTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 파일 첨부 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "파일 첨부 생성 요청")
public class FileAttachmentCreateRequest {

  @NotNull
  @Schema(description = "파일 대상 타입", example = "PRODUCT")
  private FileTargetType targetType;

  @NotNull
  @Schema(description = "파일 대상 ID")
  private UUID targetId;

  @NotBlank
  @Schema(description = "저장 파일명", example = "product-main.jpg")
  private String fileName;

  @NotBlank
  @Schema(description = "원본 파일명", example = "main.jpg")
  private String originalFileName;

  @NotBlank
  @Schema(description = "저장 위치", example = "/uploads/products/product-main.jpg")
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
