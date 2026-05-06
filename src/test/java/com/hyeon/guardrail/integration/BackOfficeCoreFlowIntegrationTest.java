package com.hyeon.guardrail.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.auth.domain.DeviceType;
import com.hyeon.guardrail.auth.domain.LoginResult;
import com.hyeon.guardrail.auth.domain.SessionStatus;
import com.hyeon.guardrail.auth.dto.LoginRequest;
import com.hyeon.guardrail.auth.repository.UserPasswordHistoryRepository;
import com.hyeon.guardrail.auth.service.AuthService;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.dto.CategoryCreateRequest;
import com.hyeon.guardrail.category.dto.CategoryStatusUpdateRequest;
import com.hyeon.guardrail.category.service.CategoryService;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.generator.AiContentGenerator;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.AuthenticatedUser;
import com.hyeon.guardrail.file.domain.FileTargetType;
import com.hyeon.guardrail.file.dto.FileAttachmentCreateRequest;
import com.hyeon.guardrail.file.service.FileAttachmentService;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.dto.ProductCreateRequest;
import com.hyeon.guardrail.product.dto.ProductDescriptionGenerateRequest;
import com.hyeon.guardrail.product.dto.ProductStatusUpdateRequest;
import com.hyeon.guardrail.product.service.ProductService;
import com.hyeon.guardrail.user.domain.UserRole;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.dto.UserCreateRequest;
import com.hyeon.guardrail.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** 백오피스 핵심 도메인 연결 흐름 통합 테스트 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BackOfficeCoreFlowIntegrationTest {

  @Autowired private UserService userService;
  @Autowired private AuthService authService;
  @Autowired private CategoryService categoryService;
  @Autowired private FileAttachmentService fileAttachmentService;
  @Autowired private ProductService productService;
  @Autowired private UserPasswordHistoryRepository passwordHistoryRepository;
  @MockitoBean private AiContentGenerator aiContentGenerator;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  /** 관리자 생성부터 로그인, 분류, 파일, 상품 승인과 비활성화 흐름을 검증 */
  @Test
  public void backOfficeCoreFlowApprovesAndInactivatesProduct() {
    var admin =
        userService.createUser(new UserCreateRequest(uniqueEmail("admin"), "관리자", UserRole.ADMIN));
    authenticate(admin.getUserId(), admin.getRole());

    assertThat(admin.getUserId()).isNotNull();
    assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(admin.getInitialPassword()).isNotBlank();

    var passwordHistory =
        passwordHistoryRepository.findAll().stream()
            .filter(history -> history.getUserId().equals(admin.getUserId()))
            .findFirst()
            .orElseThrow();
    assertThat(passwordHistory.isTemporary()).isTrue();
    assertThat(passwordHistory.getPasswordHash()).isNotBlank();
    assertThat(passwordHistory.getPasswordHash()).isNotEqualTo(admin.getInitialPassword());
    assertThat(passwordHistory.getExpiredAt()).isNotNull();

    var login =
        authService.login(
            new LoginRequest(admin.getEmail(), admin.getInitialPassword(), DeviceType.WEB),
            "127.0.0.1");

    assertThat(login.getAccessToken()).isNotBlank();
    assertThat(login.getRefreshToken()).isNotBlank();
    assertThat(login.getSessionId()).isNotNull();
    assertThat(login.getUserId()).isEqualTo(admin.getUserId());
    assertThat(login.getRole()).isEqualTo(UserRole.ADMIN);
    assertThat(authService.getSessions(admin.getUserId()))
        .singleElement()
        .satisfies(
            session -> {
              assertThat(session.getSessionId()).isEqualTo(login.getSessionId());
              assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
              assertThat(session.getAccessTokenId()).isNotBlank();
            });
    assertThat(authService.getLoginHistories(admin.getUserId()))
        .singleElement()
        .satisfies(history -> assertThat(history.getLoginResult()).isEqualTo(LoginResult.SUCCESS));

    var rootCategory = categoryService.createCategory(new CategoryCreateRequest(null, "의류"));
    var childCategory =
        categoryService.createCategory(new CategoryCreateRequest(rootCategory.getId(), "상의"));

    assertThat(rootCategory.getParentId()).isNull();
    assertThat(rootCategory.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
    assertThat(childCategory.getParentId()).isEqualTo(rootCategory.getId());
    assertThat(childCategory.getStatus()).isEqualTo(CategoryStatus.ACTIVE);

    UUID fileTargetId = UUID.randomUUID();
    var representativeFile =
        fileAttachmentService.createFileAttachment(
            new FileAttachmentCreateRequest(
                FileTargetType.PRODUCT,
                fileTargetId,
                "product-main.jpg",
                "main.jpg",
                "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main.jpg",
                1024L,
                "image/jpeg",
                1));

    assertThat(representativeFile.getTargetType()).isEqualTo(FileTargetType.PRODUCT);
    assertThat(representativeFile.getTargetId()).isEqualTo(fileTargetId);
    assertThat(representativeFile.getSortOrder()).isEqualTo(1);
    assertThat(representativeFile.isRepresentative()).isTrue();
    assertThat(fileAttachmentService.getFileAttachments(FileTargetType.PRODUCT, fileTargetId))
        .singleElement()
        .satisfies(file -> assertThat(file.getId()).isEqualTo(representativeFile.getId()));
    assertThatThrownBy(
            () ->
                fileAttachmentService.createFileAttachment(
                    new FileAttachmentCreateRequest(
                        FileTargetType.PRODUCT,
                        fileTargetId,
                        "product-main-copy.jpg",
                        "main-copy.jpg",
                        "https://guardrail-test-assets.s3.ap-northeast-2.amazonaws.com/products/product-main-copy.jpg",
                        1024L,
                        "image/jpeg",
                        1)))
        .isInstanceOf(BaseException.class);

    var product =
        productService.createProduct(
            new ProductCreateRequest(
                childCategory.getId(), "가드레일 티셔츠", "판매 페이지 상품 설명", admin.getUserId()));

    assertThat(product.getCategoryId()).isEqualTo(childCategory.getId());
    assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
    assertThat(productService.getProductHistories(product.getId()))
        .singleElement()
        .satisfies(history -> assertThat(history.getType()).isEqualTo(ProductHistoryType.CREATED));

    var submitted =
        productService.updateProductStatus(
            product.getId(),
            new ProductStatusUpdateRequest(ProductStatus.PENDING, admin.getUserId(), null));
    var approved =
        productService.updateProductStatus(
            product.getId(),
            new ProductStatusUpdateRequest(ProductStatus.APPROVED, admin.getUserId(), null));
    var inactive =
        productService.updateProductStatus(
            product.getId(),
            new ProductStatusUpdateRequest(ProductStatus.INACTIVE, admin.getUserId(), null));

    assertThat(submitted.getStatus()).isEqualTo(ProductStatus.PENDING);
    assertThat(approved.getStatus()).isEqualTo(ProductStatus.APPROVED);
    assertThat(inactive.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    assertThat(productService.getProductHistories(product.getId()))
        .extracting("type")
        .containsExactly(
            ProductHistoryType.CREATED,
            ProductHistoryType.SUBMITTED,
            ProductHistoryType.APPROVED,
            ProductHistoryType.INACTIVATED);
  }

  /** 없는 이메일 로그인 실패와 상품 반려 후 재제출 흐름을 검증 */
  @Test
  public void backOfficeRejectionFlowRequiresReasonAndAllowsResubmission() {
    var admin =
        userService.createUser(
            new UserCreateRequest(uniqueEmail("reject-admin"), "반려 관리자", UserRole.ADMIN));
    authenticate(admin.getUserId(), admin.getRole());

    assertThatThrownBy(
            () ->
                authService.login(
                    new LoginRequest(uniqueEmail("missing"), "wrong-password", DeviceType.WEB),
                    "127.0.0.1"))
        .isInstanceOf(BaseException.class);
    assertThat(authService.getLoginHistories(admin.getUserId())).isEmpty();

    var category = categoryService.createCategory(new CategoryCreateRequest(null, "잡화"));
    var product =
        productService.createProduct(
            new ProductCreateRequest(
                category.getId(), "검수 상품", "승인 검수 대상 상품 설명", admin.getUserId()));
    productService.updateProductStatus(
        product.getId(),
        new ProductStatusUpdateRequest(ProductStatus.PENDING, admin.getUserId(), null));

    assertThatThrownBy(
            () ->
                productService.updateProductStatus(
                    product.getId(),
                    new ProductStatusUpdateRequest(ProductStatus.REJECTED, admin.getUserId(), " ")))
        .isInstanceOf(BaseException.class);
    assertThat(productService.getProductHistories(product.getId()))
        .extracting("type")
        .containsExactly(ProductHistoryType.CREATED, ProductHistoryType.SUBMITTED);

    var rejected =
        productService.updateProductStatus(
            product.getId(),
            new ProductStatusUpdateRequest(ProductStatus.REJECTED, admin.getUserId(), "설명 보완 필요"));
    var resubmitted =
        productService.updateProductStatus(
            product.getId(),
            new ProductStatusUpdateRequest(ProductStatus.PENDING, admin.getUserId(), null));

    assertThat(rejected.getStatus()).isEqualTo(ProductStatus.REJECTED);
    assertThat(resubmitted.getStatus()).isEqualTo(ProductStatus.PENDING);
    assertThat(productService.getProductHistories(product.getId()))
        .extracting("type")
        .containsExactly(
            ProductHistoryType.CREATED,
            ProductHistoryType.SUBMITTED,
            ProductHistoryType.REJECTED,
            ProductHistoryType.SUBMITTED);
    assertThat(productService.getProductHistories(product.getId()))
        .filteredOn(history -> history.getType() == ProductHistoryType.REJECTED)
        .singleElement()
        .satisfies(history -> assertThat(history.getReason()).isEqualTo("설명 보완 필요"));
  }

  /** 비활성 카테고리 상품 생성 실패 흐름을 검증 */
  @Test
  public void backOfficeProductCreationRequiresActiveCategory() {
    var admin =
        userService.createUser(
            new UserCreateRequest(uniqueEmail("inactive-admin"), "비활성 관리자", UserRole.ADMIN));
    authenticate(admin.getUserId(), admin.getRole());
    var category = categoryService.createCategory(new CategoryCreateRequest(null, "비활성 분류"));
    categoryService.updateCategoryStatus(
        category.getId(), new CategoryStatusUpdateRequest(CategoryStatus.INACTIVE));

    assertThatThrownBy(
            () ->
                productService.createProduct(
                    new ProductCreateRequest(
                        category.getId(), "등록 실패 상품", "비활성 분류 상품 설명", admin.getUserId())))
        .isInstanceOf(BaseException.class);
  }

  /** 상품 설명 AI 생성은 현재 상품 설명에 즉시 반영된다 */
  @Test
  public void productDescriptionGenerationAppliesDirectlyToProduct() {
    var admin =
        userService.createUser(
            new UserCreateRequest(uniqueEmail("content-admin"), "설명 관리자", UserRole.ADMIN));
    authenticate(admin.getUserId(), admin.getRole());
    var category = categoryService.createCategory(new CategoryCreateRequest(null, "생활용품"));
    var product =
        productService.createProduct(
            new ProductCreateRequest(
                category.getId(), "스테인리스 텀블러", "승인 전 기존 설명", admin.getUserId()));

    when(aiContentGenerator.generateProductDescription(any()))
        .thenReturn(new ProductDescriptionGenerateResult("AI가 생성한 상품 설명 초안입니다."));

    var generated =
        productService.generateProductDescription(
            product.getId(),
            new ProductDescriptionGenerateRequest(
                admin.getUserId(), "스테인리스 텀블러", "생활용품", "용량: 500ml", List.of("보온", "휴대성")));

    assertThat(generated.getDescription()).isEqualTo("AI가 생성한 상품 설명 초안입니다.");
    assertThat(productService.getProduct(product.getId()).getDescription())
        .isEqualTo("AI가 생성한 상품 설명 초안입니다.");
    assertThat(productService.getProductHistories(product.getId()))
        .extracting("type")
        .containsExactly(ProductHistoryType.CREATED, ProductHistoryType.UPDATED);
  }

  private String uniqueEmail(String prefix) {
    return prefix + "-" + UUID.randomUUID() + "@example.com";
  }

  private void authenticate(UUID userId, UserRole role) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, role),
                "test-token",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
  }
}
