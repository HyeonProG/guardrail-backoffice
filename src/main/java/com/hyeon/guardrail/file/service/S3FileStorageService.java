package com.hyeon.guardrail.file.service;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.support.StoredFileResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** S3 기반 파일 저장소 업로드 처리 */
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

  private final S3Client s3Client;

  @Value("${app.storage.s3.bucket}")
  private String bucket;

  @Value("${app.storage.s3.public-base-url}")
  private String publicBaseUrl;

  @Override
  public StoredFileResult upload(FileTargetType targetType, UUID targetId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "업로드할 파일이 필요합니다.");
    }

    String originalFileName =
        file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
    String sanitizedOriginalName = originalFileName.replaceAll("\\s+", "-");
    String extension = extractExtension(sanitizedOriginalName);
    String storedFileName = UUID.randomUUID() + extension;
    String objectKey = buildObjectKey(targetType, targetId, storedFileName);
    String filePath = buildPublicFileUrl(objectKey);

    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(file.getContentType())
            .build();

    try (InputStream inputStream = file.getInputStream()) {
      s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
      return new StoredFileResult(storedFileName, filePath, objectKey);
    } catch (IOException exception) {
      throw new UncheckedIOException("파일을 읽는 중 오류가 발생했습니다.", exception);
    } catch (S3Exception exception) {
      throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
    }
  }

  private String buildObjectKey(FileTargetType targetType, UUID targetId, String storedFileName) {
    String folder = "products";
    return "%s/%s/%s".formatted(folder, targetId, storedFileName);
  }

  private String buildPublicFileUrl(String objectKey) {
    return "%s/%s".formatted(trimTrailingSlash(publicBaseUrl), objectKey);
  }

  private String trimTrailingSlash(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }

  private String extractExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return "." + fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
  }
}
