package com.hyeon.guardrail.file.dto;

import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileStatus;
import com.hyeon.guardrail.file.domain.FileTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 파일 첨부 기본 정보 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "파일 첨부 기본 정보 응답")
public class FileAttachmentResponse {

  @Schema(description = "파일 첨부 ID")
  private UUID id;

  @Schema(description = "파일 대상 타입", example = "PRODUCT")
  private FileTargetType targetType;

  @Schema(description = "파일 대상 ID")
  private UUID targetId;

  @Schema(description = "저장 파일명", example = "product-main.jpg")
  private String fileName;

  @Schema(description = "원본 파일명", example = "main.jpg")
  private String originalFileName;

  @Schema(description = "저장 위치", example = "https://cdn.guardrail.com/products/product-main.jpg")
  private String filePath;

  @Schema(description = "파일 크기", example = "1048576")
  private long fileSize;

  @Schema(description = "콘텐츠 타입", example = "image/jpeg")
  private String contentType;

  @Schema(description = "정렬 순서", example = "1")
  private int sortOrder;

  @Schema(description = "대표 파일 여부", example = "true")
  private boolean representative;

  @Schema(description = "파일 운영 상태", example = "ACTIVE")
  private FileStatus status;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 파일 첨부 엔티티를 응답으로 변환 */
  public static FileAttachmentResponse from(FileAttachment fileAttachment) {
    return new FileAttachmentResponse(
        fileAttachment.getId(),
        fileAttachment.getTargetType(),
        fileAttachment.getTargetId(),
        fileAttachment.getFileName(),
        fileAttachment.getOriginalFileName(),
        fileAttachment.getFilePath(),
        fileAttachment.getFileSize(),
        fileAttachment.getContentType(),
        fileAttachment.getSortOrder(),
        fileAttachment.getSortOrder() == 1,
        fileAttachment.getStatus(),
        fileAttachment.getCreatedAt(),
        fileAttachment.getUpdatedAt());
  }
}
