package com.hyeon.guardrail.file.controller;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.dto.FileAttachmentCreateRequest;
import com.hyeon.guardrail.file.dto.FileAttachmentResponse;
import com.hyeon.guardrail.file.dto.FileAttachmentUpdateRequest;
import com.hyeon.guardrail.file.service.FileAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 파일 첨부 API 컨트롤러 */
@Tag(name = "FileAttachment", description = "파일 첨부 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file-attachments")
public class FileAttachmentController {

  private final FileAttachmentService fileAttachmentService;

  /** 파일 첨부 생성 */
  @Operation(summary = "파일 첨부 생성", description = "파일 첨부 메타데이터를 생성합니다.")
  @PostMapping
  public BaseResponseEntity<FileAttachmentResponse> createFileAttachment(
      @Valid @RequestBody FileAttachmentCreateRequest request) {
    FileAttachmentResponse response = fileAttachmentService.createFileAttachment(request);
    return BaseResponseEntity.created(response, "파일 첨부가 생성되었습니다.");
  }

  /** 파일 업로드 */
  @Operation(summary = "파일 업로드", description = "실제 파일 업로드와 파일 첨부 메타데이터 생성을 함께 처리합니다.")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public BaseResponseEntity<FileAttachmentResponse> uploadFileAttachment(
      @RequestParam FileTargetType targetType,
      @RequestParam UUID targetId,
      @RequestParam int sortOrder,
      @RequestParam MultipartFile file) {
    FileAttachmentResponse response =
        fileAttachmentService.uploadFileAttachment(targetType, targetId, file, sortOrder);
    return BaseResponseEntity.created(response, "파일 업로드가 완료되었습니다.");
  }

  /** 파일 첨부 조회 */
  @Operation(summary = "파일 첨부 조회", description = "파일 첨부 기본 정보를 조회합니다.")
  @GetMapping("/{fileAttachmentId}")
  public BaseResponseEntity<FileAttachmentResponse> getFileAttachment(
      @PathVariable UUID fileAttachmentId) {
    return BaseResponseEntity.success(fileAttachmentService.getFileAttachment(fileAttachmentId));
  }

  /** 대상별 파일 첨부 목록 조회 */
  @Operation(summary = "대상별 파일 첨부 목록 조회", description = "targetType과 targetId 기준 파일 목록을 조회합니다.")
  @GetMapping
  public BaseResponseEntity<List<FileAttachmentResponse>> getFileAttachments(
      @RequestParam FileTargetType targetType, @RequestParam UUID targetId) {
    return BaseResponseEntity.success(
        fileAttachmentService.getFileAttachments(targetType, targetId));
  }

  /** 파일 첨부 수정 */
  @Operation(summary = "파일 첨부 수정", description = "파일 첨부 기본 정보를 수정합니다.")
  @PutMapping("/{fileAttachmentId}")
  public BaseResponseEntity<FileAttachmentResponse> updateFileAttachment(
      @PathVariable UUID fileAttachmentId,
      @Valid @RequestBody FileAttachmentUpdateRequest request) {
    return BaseResponseEntity.success(
        fileAttachmentService.updateFileAttachment(fileAttachmentId, request), "파일 첨부가 수정되었습니다.");
  }

  /** 파일 첨부 삭제 */
  @Operation(summary = "파일 첨부 삭제", description = "파일 첨부를 soft delete 처리합니다.")
  @DeleteMapping("/{fileAttachmentId}")
  public BaseResponseEntity<Void> deleteFileAttachment(@PathVariable UUID fileAttachmentId) {
    fileAttachmentService.deleteFileAttachment(fileAttachmentId);
    return BaseResponseEntity.success("파일 첨부가 삭제되었습니다.");
  }
}
