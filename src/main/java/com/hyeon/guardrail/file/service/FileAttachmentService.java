package com.hyeon.guardrail.file.service;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileStatus;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.dto.FileAttachmentCreateRequest;
import com.hyeon.guardrail.file.dto.FileAttachmentResponse;
import com.hyeon.guardrail.file.dto.FileAttachmentUpdateRequest;
import com.hyeon.guardrail.file.repository.FileAttachmentRepository;
import com.hyeon.guardrail.file.repository.FileAttachmentRepositoryQuery;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 파일 첨부 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class FileAttachmentService {

  private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

  private final FileAttachmentRepository fileAttachmentRepository;
  private final FileAttachmentRepositoryQuery fileAttachmentRepositoryQuery;
  private final CurrentUserService currentUserService;

  @Value("${app.upload.max-file-size-mb}")
  private long maxFileSizeMb;

  @Value("${app.upload.storage-root-path}")
  private String storageRootPath;

  @Value("${app.upload.public-url-prefix}")
  private String publicUrlPrefix;

  @Value("#{'${app.upload.allowed-content-types}'.split(',')}")
  private List<String> allowedContentTypes;

  /** 파일 첨부 생성 */
  @Transactional
  public FileAttachmentResponse createFileAttachment(FileAttachmentCreateRequest request) {
    validateFileMetadata(request.getFileSize(), request.getContentType());
    validateStoragePath(request.getFilePath());
    validateSortOrderNotDuplicated(
        request.getTargetType(), request.getTargetId(), request.getSortOrder());

    FileAttachment fileAttachment =
        new FileAttachment(
            request.getTargetType(),
            request.getTargetId(),
            request.getFileName(),
            request.getOriginalFileName(),
            request.getFilePath(),
            request.getFileSize(),
            request.getContentType(),
            request.getSortOrder(),
            FileStatus.ACTIVE);

    return FileAttachmentResponse.from(fileAttachmentRepository.save(fileAttachment));
  }

  /** 파일 업로드와 메타데이터 생성을 함께 처리 */
  @Transactional
  public FileAttachmentResponse uploadFileAttachment(
      FileTargetType targetType, UUID targetId, MultipartFile file, int sortOrder) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "업로드할 파일이 필요합니다.");
    }

    String originalFileName =
        file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
    String sanitizedOriginalName = originalFileName.replaceAll("\\s+", "-");
    String extension = extractExtension(sanitizedOriginalName);
    String generatedFileName = UUID.randomUUID() + extension;
    String filePath = buildPublicFilePath(targetId, generatedFileName);

    validateFileMetadata(file.getSize(), file.getContentType());
    validateStoragePath(filePath);
    validateSortOrderNotDuplicated(targetType, targetId, sortOrder);

    Path absoluteDirectory = resolveStorageDirectory(targetId);
    Path absoluteFilePath = absoluteDirectory.resolve(generatedFileName).normalize();

    try {
      Files.createDirectories(absoluteDirectory);
      file.transferTo(absoluteFilePath);
    } catch (IOException exception) {
      throw new UncheckedIOException("파일을 저장하는 중 오류가 발생했습니다.", exception);
    }

    FileAttachment fileAttachment =
        new FileAttachment(
            targetType,
            targetId,
            generatedFileName,
            originalFileName,
            filePath,
            file.getSize(),
            file.getContentType(),
            sortOrder,
            FileStatus.ACTIVE);

    return FileAttachmentResponse.from(fileAttachmentRepository.save(fileAttachment));
  }

  /** 파일 첨부 단건 조회 */
  @Transactional(readOnly = true)
  public FileAttachmentResponse getFileAttachment(UUID fileAttachmentId) {
    return FileAttachmentResponse.from(findFileAttachment(fileAttachmentId));
  }

  /** 대상별 파일 첨부 목록 조회 */
  @Transactional(readOnly = true)
  public List<FileAttachmentResponse> getFileAttachments(FileTargetType targetType, UUID targetId) {
    return fileAttachmentRepositoryQuery.findAllByTarget(targetType, targetId).stream()
        .map(FileAttachmentResponse::from)
        .toList();
  }

  /** 파일 첨부 기본 정보 수정 */
  @Transactional
  public FileAttachmentResponse updateFileAttachment(
      UUID fileAttachmentId, FileAttachmentUpdateRequest request) {
    FileAttachment fileAttachment = findFileAttachment(fileAttachmentId);
    validateFileMetadata(request.getFileSize(), request.getContentType());
    validateStoragePath(request.getFilePath());
    validateSortOrderNotDuplicated(
        fileAttachment.getTargetType(),
        fileAttachment.getTargetId(),
        request.getSortOrder(),
        fileAttachmentId);

    fileAttachment.updateBasicInfo(
        request.getFileName(),
        request.getOriginalFileName(),
        request.getFilePath(),
        request.getFileSize(),
        request.getContentType(),
        request.getSortOrder());

    return FileAttachmentResponse.from(fileAttachment);
  }

  /** 파일 첨부 삭제 */
  @Transactional
  public void deleteFileAttachment(UUID fileAttachmentId) {
    currentUserService.requireAdminOrOperator();
    FileAttachment fileAttachment = findFileAttachment(fileAttachmentId);
    fileAttachment.delete();
  }

  private FileAttachment findFileAttachment(UUID fileAttachmentId) {
    return fileAttachmentRepositoryQuery
        .findById(fileAttachmentId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "파일 첨부를 찾을 수 없습니다."));
  }

  private void validateFileMetadata(long fileSize, String contentType) {
    if (fileSize <= 0) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "파일 크기는 1바이트 이상이어야 합니다.");
    }

    long maxFileSize = maxFileSizeMb * BYTES_PER_MEGABYTE;
    if (fileSize > maxFileSize) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "파일 크기가 허용 범위를 초과했습니다.");
    }

    if (contentType == null || contentType.isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "콘텐츠 타입은 필수입니다.");
    }

    if (!allowedContentTypes.contains(contentType)) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "허용되지 않은 콘텐츠 타입입니다.");
    }
  }

  private void validateStoragePath(String filePath) {
    if (filePath == null || filePath.isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "저장 위치는 필수입니다.");
    }

    if (!filePath.startsWith(publicUrlPrefix)) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "허용되지 않은 저장 위치입니다.");
    }
  }

  private Path resolveStorageDirectory(UUID targetId) {
    return Paths.get(storageRootPath).toAbsolutePath().normalize().resolve(targetId.toString());
  }

  private String buildPublicFilePath(UUID targetId, String fileName) {
    return "%s/%s/%s".formatted(publicUrlPrefix, targetId, fileName);
  }

  private String extractExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return "." + fileName.substring(fileName.lastIndexOf('.') + 1);
  }

  private void validateSortOrderNotDuplicated(
      FileTargetType targetType, UUID targetId, int sortOrder) {
    boolean duplicated =
        fileAttachmentRepository.existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalse(
            targetType, targetId, sortOrder);

    if (duplicated) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "같은 대상 안에서 정렬 순서는 중복될 수 없습니다.");
    }
  }

  private void validateSortOrderNotDuplicated(
      FileTargetType targetType, UUID targetId, int sortOrder, UUID fileAttachmentId) {
    boolean duplicated =
        fileAttachmentRepository.existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalseAndIdNot(
            targetType, targetId, sortOrder, fileAttachmentId);

    if (duplicated) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "같은 대상 안에서 정렬 순서는 중복될 수 없습니다.");
    }
  }
}
