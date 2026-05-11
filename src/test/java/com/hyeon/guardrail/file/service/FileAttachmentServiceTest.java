package com.hyeon.guardrail.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.file.domain.FileAttachment;
import com.hyeon.guardrail.file.domain.FileStatus;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.dto.FileAttachmentCreateRequest;
import com.hyeon.guardrail.file.dto.FileAttachmentUpdateRequest;
import com.hyeon.guardrail.file.repository.FileAttachmentRepository;
import com.hyeon.guardrail.file.repository.FileAttachmentRepositoryQuery;
import com.hyeon.guardrail.file.support.StoredFileResult;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.repository.ProductRepositoryQuery;
import com.hyeon.guardrail.product.service.ProductHistoryService;
import com.hyeon.guardrail.user.domain.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

/** 파일 첨부 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class FileAttachmentServiceTest {

  @Mock private FileAttachmentRepository fileAttachmentRepository;
  @Mock private FileAttachmentRepositoryQuery fileAttachmentRepositoryQuery;
  @Mock private CurrentUserService currentUserService;
  @Mock private FileStorageService fileStorageService;
  @Mock private ProductRepositoryQuery productRepositoryQuery;
  @Mock private ProductHistoryService productHistoryService;

  @InjectMocks private FileAttachmentService fileAttachmentService;

  /** 테스트 기본 업로드 용량 설정 */
  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(fileAttachmentService, "maxFileSizeMb", 10L);
    ReflectionTestUtils.setField(
        fileAttachmentService,
        "publicBaseUrl",
        "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com");
    ReflectionTestUtils.setField(
        fileAttachmentService,
        "allowedContentTypes",
        List.of("image/jpeg", "image/png", "image/webp"));
  }

  /** 같은 대상 안의 sortOrder 중복이면 생성에 실패 */
  @Test
  void createFileAttachmentThrowsWhenSortOrderDuplicatedInSameTarget() {
    UUID targetId = UUID.randomUUID();
    FileAttachmentCreateRequest request =
        new FileAttachmentCreateRequest(
            FileTargetType.PRODUCT,
            targetId,
            "product-main.jpg",
            "main.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
            1024L,
            "image/jpeg",
            1);

    mockProductTarget(targetId, ProductStatus.DRAFT);
    when(fileAttachmentRepository.existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalse(
            FileTargetType.PRODUCT, targetId, 1))
        .thenReturn(true);

    assertThatThrownBy(() -> fileAttachmentService.createFileAttachment(request))
        .isInstanceOf(BaseException.class);
    verify(fileAttachmentRepository, never()).save(any(FileAttachment.class));
  }

  /** 파일 크기가 업로드 제한을 초과하면 생성에 실패 */
  @Test
  void createFileAttachmentThrowsWhenFileSizeExceedsLimit() {
    FileAttachmentCreateRequest request =
        new FileAttachmentCreateRequest(
            FileTargetType.PRODUCT,
            UUID.randomUUID(),
            "product-main.jpg",
            "main.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
            11L * 1024L * 1024L,
            "image/jpeg",
            1);

    assertThatThrownBy(() -> fileAttachmentService.createFileAttachment(request))
        .isInstanceOf(BaseException.class);
    verify(fileAttachmentRepository, never()).save(any(FileAttachment.class));
  }

  /** 허용되지 않은 MIME 타입이면 생성에 실패 */
  @Test
  void createFileAttachmentThrowsWhenContentTypeIsNotAllowed() {
    FileAttachmentCreateRequest request =
        new FileAttachmentCreateRequest(
            FileTargetType.PRODUCT,
            UUID.randomUUID(),
            "product-main.gif",
            "main.gif",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.gif",
            1024L,
            "image/gif",
            1);

    assertThatThrownBy(() -> fileAttachmentService.createFileAttachment(request))
        .isInstanceOf(BaseException.class);
    verify(fileAttachmentRepository, never()).save(any(FileAttachment.class));
  }

  /** 허용된 저장 경로 루트 밖의 파일이면 생성에 실패 */
  @Test
  void createFileAttachmentThrowsWhenStoragePathIsNotAllowed() {
    FileAttachmentCreateRequest request =
        new FileAttachmentCreateRequest(
            FileTargetType.PRODUCT,
            UUID.randomUUID(),
            "product-main.jpg",
            "main.jpg",
            "https://example.com/product-main.jpg",
            1024L,
            "image/jpeg",
            1);

    assertThatThrownBy(() -> fileAttachmentService.createFileAttachment(request))
        .isInstanceOf(BaseException.class);
    verify(fileAttachmentRepository, never()).save(any(FileAttachment.class));
  }

  /** sortOrder가 1인 파일은 대표 파일로 응답 */
  @Test
  void getFileAttachmentMarksSortOrderOneAsRepresentative() {
    UUID fileAttachmentId = UUID.randomUUID();
    FileAttachment fileAttachment =
        new FileAttachment(
            FileTargetType.PRODUCT,
            UUID.randomUUID(),
            "product-main.jpg",
            "main.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
            1024L,
            "image/jpeg",
            1,
            FileStatus.ACTIVE);
    ReflectionTestUtils.setField(fileAttachment, "id", fileAttachmentId);

    when(fileAttachmentRepositoryQuery.findById(fileAttachmentId))
        .thenReturn(Optional.of(fileAttachment));

    var response = fileAttachmentService.getFileAttachment(fileAttachmentId);

    assertThat(response.isRepresentative()).isTrue();
    assertThat(response.getSortOrder()).isEqualTo(1);
  }

  /** 대상별 목록 조회는 targetType과 targetId 조건으로 조회 저장소를 호출 */
  @Test
  void getFileAttachmentsDelegatesTargetTypeAndTargetIdFilter() {
    UUID targetId = UUID.randomUUID();
    FileAttachment fileAttachment =
        new FileAttachment(
            FileTargetType.PRODUCT,
            targetId,
            "product-sub.jpg",
            "sub.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-sub.jpg",
            1024L,
            "image/jpeg",
            2,
            FileStatus.ACTIVE);
    ReflectionTestUtils.setField(fileAttachment, "id", UUID.randomUUID());

    when(fileAttachmentRepositoryQuery.findAllByTarget(FileTargetType.PRODUCT, targetId))
        .thenReturn(List.of(fileAttachment));

    var response = fileAttachmentService.getFileAttachments(FileTargetType.PRODUCT, targetId);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).getTargetType()).isEqualTo(FileTargetType.PRODUCT);
    assertThat(response.get(0).getTargetId()).isEqualTo(targetId);
    assertThat(response.get(0).isRepresentative()).isFalse();
    verify(fileAttachmentRepositoryQuery).findAllByTarget(FileTargetType.PRODUCT, targetId);
  }

  /** 수정 시 자기 자신을 제외하고 같은 대상의 sortOrder 중복을 검증 */
  @Test
  void updateFileAttachmentValidatesSortOrderExceptItself() {
    UUID fileAttachmentId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    FileAttachment fileAttachment =
        new FileAttachment(
            FileTargetType.PRODUCT,
            targetId,
            "product-sub.jpg",
            "sub.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-sub.jpg",
            1024L,
            "image/jpeg",
            2,
            FileStatus.ACTIVE);
    FileAttachmentUpdateRequest request =
        new FileAttachmentUpdateRequest(
            "product-main.jpg",
            "main.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
            1024L,
            "image/jpeg",
            1);
    ReflectionTestUtils.setField(fileAttachment, "id", fileAttachmentId);

    when(fileAttachmentRepositoryQuery.findById(fileAttachmentId))
        .thenReturn(Optional.of(fileAttachment));
    mockProductTarget(targetId, ProductStatus.DRAFT);
    when(fileAttachmentRepository.existsByTargetTypeAndTargetIdAndSortOrderAndDeletedFalseAndIdNot(
            FileTargetType.PRODUCT, targetId, 1, fileAttachmentId))
        .thenReturn(true);

    assertThatThrownBy(() -> fileAttachmentService.updateFileAttachment(fileAttachmentId, request))
        .isInstanceOf(BaseException.class);
  }

  /** 삭제는 파일 첨부 메타데이터를 soft delete 처리 */
  @Test
  void deleteFileAttachmentSoftDeletesMetadata() {
    UUID fileAttachmentId = UUID.randomUUID();
    FileAttachment fileAttachment =
        new FileAttachment(
            FileTargetType.PRODUCT,
            UUID.randomUUID(),
            "product-main.jpg",
            "main.jpg",
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
            1024L,
            "image/jpeg",
            1,
            FileStatus.ACTIVE);
    ReflectionTestUtils.setField(fileAttachment, "id", fileAttachmentId);
    when(fileAttachmentRepositoryQuery.findById(fileAttachmentId))
        .thenReturn(Optional.of(fileAttachment));
    mockProductTarget(fileAttachment.getTargetId(), ProductStatus.DRAFT);

    fileAttachmentService.deleteFileAttachment(fileAttachmentId);

    assertThat(fileAttachment.isDeleted()).isTrue();
  }

  /** 업로드는 S3 저장 결과를 파일 첨부 메타데이터로 저장 */
  @Test
  void uploadFileAttachmentStoresUploadedFileMetadata() {
    UUID targetId = UUID.randomUUID();
    MockMultipartFile file =
        new MockMultipartFile("file", "main.jpg", "image/jpeg", "sample".getBytes());
    mockProductTarget(targetId, ProductStatus.DRAFT);
    when(fileStorageService.upload(FileTargetType.PRODUCT, targetId, file))
        .thenReturn(
            new StoredFileResult(
                "stored-main.jpg",
                "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/%s/stored-main.jpg"
                    .formatted(targetId),
                "products/%s/stored-main.jpg".formatted(targetId)));
    when(fileAttachmentRepository.save(any(FileAttachment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        fileAttachmentService.uploadFileAttachment(FileTargetType.PRODUCT, targetId, file, 1);

    assertThat(response.getFileName()).isEqualTo("stored-main.jpg");
    assertThat(response.getFilePath())
        .isEqualTo(
            "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/%s/stored-main.jpg"
                .formatted(targetId));
    verify(fileStorageService).upload(FileTargetType.PRODUCT, targetId, file);
  }

  private void mockProductTarget(UUID targetId, ProductStatus status) {
    Product product = new Product(UUID.randomUUID(), "상품", "설명", status);
    ReflectionTestUtils.setField(product, "id", targetId);
    when(productRepositoryQuery.findById(targetId)).thenReturn(Optional.of(product));
    when(currentUserService.getCurrentUserRole()).thenReturn(UserRole.ADMIN);
  }
}
