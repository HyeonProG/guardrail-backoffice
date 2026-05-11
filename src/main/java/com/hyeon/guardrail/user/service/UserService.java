package com.hyeon.guardrail.user.service;

import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import com.hyeon.guardrail.auth.repository.AuthRepositoryQuery;
import com.hyeon.guardrail.auth.repository.UserPasswordHistoryRepository;
import com.hyeon.guardrail.auth.service.AuthService;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.mail.MailSender;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.dto.ChangePasswordRequest;
import com.hyeon.guardrail.user.dto.ChangePasswordResponse;
import com.hyeon.guardrail.user.dto.PasswordHistoryResponse;
import com.hyeon.guardrail.user.dto.UserCreateRequest;
import com.hyeon.guardrail.user.dto.UserCreateResponse;
import com.hyeon.guardrail.user.dto.UserResponse;
import com.hyeon.guardrail.user.dto.UserStatusUpdateRequest;
import com.hyeon.guardrail.user.dto.UserTemporaryPasswordIssueResponse;
import com.hyeon.guardrail.user.dto.UserUpdateRequest;
import com.hyeon.guardrail.user.repository.UserRepository;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사용자 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class UserService {

  private static final char[] INITIAL_PASSWORD_CHARS =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%".toCharArray();
  private static final int INITIAL_PASSWORD_LENGTH = 12;

  private final UserRepository userRepository;
  private final UserRepositoryQuery userRepositoryQuery;
  private final AuthRepositoryQuery authRepositoryQuery;
  private final UserPasswordHistoryRepository passwordHistoryRepository;
  private final AuthService authService;
  private final PasswordEncoder passwordEncoder;
  private final MailSender mailSender;
  private final CurrentUserService currentUserService;
  private final SecureRandom secureRandom = new SecureRandom();

  /** 사용자 생성과 초기 비밀번호 발급 */
  @Transactional
  public UserCreateResponse createUser(UserCreateRequest request) {
    if (userRepository.count() > 0) {
      currentUserService.requireAdminOrOperator();
    }

    if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
    }

    String initialPassword = generateInitialPassword();

    User user =
        new User(request.getEmail(), request.getName(), request.getRole(), UserStatus.ACTIVE);
    User savedUser = userRepository.save(user);
    authService.saveInitialPassword(savedUser.getId(), initialPassword);
    mailSender.send(
        savedUser.getEmail(),
        "Guardrail 초기 계정 정보",
        "email=" + savedUser.getEmail() + ", initialPassword=" + initialPassword);

    return UserCreateResponse.of(savedUser, initialPassword);
  }

  /** 사용자 단건 조회 */
  @Transactional(readOnly = true)
  public UserResponse getUser(UUID userId) {
    return UserResponse.from(findActiveUser(userId));
  }

  /** 사용자 비밀번호 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<PasswordHistoryResponse> getPasswordHistories(UUID userId) {
    currentUserService.validateActor(userId);
    return authRepositoryQuery.findPasswordHistoriesByUserId(userId).stream()
        .map(PasswordHistoryResponse::from)
        .toList();
  }

  /** 현재 로그인 사용자의 비밀번호를 변경한다. */
  @Transactional
  public ChangePasswordResponse changePassword(UUID userId, ChangePasswordRequest request) {
    currentUserService.validateActor(userId);

    UserPasswordHistory latestPasswordHistory =
        authRepositoryQuery
            .findLatestValidPasswordHistory(userId, LocalDateTime.now())
            .orElseThrow(
                () -> new BaseException(BaseResponseStatus.NOT_FOUND, "현재 비밀번호 정보를 찾을 수 없습니다."));

    if (!passwordEncoder.matches(
        request.getCurrentPassword(), latestPasswordHistory.getPasswordHash())) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
    }

    if (passwordEncoder.matches(
        request.getNewPassword(), latestPasswordHistory.getPasswordHash())) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    UserPasswordHistory changedPasswordHistory =
        new UserPasswordHistory(
            userId, passwordEncoder.encode(request.getNewPassword()), false, null);

    UserPasswordHistory savedPasswordHistory =
        passwordHistoryRepository.save(changedPasswordHistory);
    return new ChangePasswordResponse(
        savedPasswordHistory.getUserId(), false, savedPasswordHistory.getCreatedAt());
  }

  /** 사용자 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<UserResponse> getUsers(UserStatus status, Pageable pageable) {
    currentUserService.requireAdminOrOperator();
    return PageResponse.from(userRepositoryQuery.findAll(status, pageable).map(UserResponse::from));
  }

  /** 사용자 기본 정보 수정 */
  @Transactional
  public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
    currentUserService.validateActor(userId);

    if (userRepository.existsByEmailAndDeletedFalseAndIdNot(request.getEmail(), userId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
    }

    User user = findActiveUser(userId);
    user.updateBasicInfo(request.getEmail(), request.getName(), user.getRole());

    return UserResponse.from(user);
  }

  /** 사용자 상태 변경 */
  @Transactional
  public UserResponse updateUserStatus(UUID userId, UserStatusUpdateRequest request) {
    currentUserService.requireAdminOrOperator();
    User user = findActiveUser(userId);

    if (request.getStatus() == UserStatus.ACTIVE) {
      user.activate();
    } else {
      user.inactivate();
    }

    return UserResponse.from(user);
  }

  /** 사용자 삭제 */
  @Transactional
  public void deleteUser(UUID userId) {
    currentUserService.requireAdminOrOperator();
    User user = findActiveUser(userId);
    user.delete();
  }

  /** 사용자 임시 비밀번호 재발급 */
  @Transactional
  public UserTemporaryPasswordIssueResponse issueTemporaryPassword(UUID userId) {
    currentUserService.requireAdminOrOperator();
    User user = findActiveUser(userId);
    String temporaryPassword = generateInitialPassword();

    authService.saveInitialPassword(user.getId(), temporaryPassword);
    mailSender.send(
        user.getEmail(),
        "Guardrail 임시 비밀번호 발급",
        "email=" + user.getEmail() + ", temporaryPassword=" + temporaryPassword);

    return new UserTemporaryPasswordIssueResponse(
        user.getId(), user.getEmail(), true, LocalDateTime.now());
  }

  private String generateInitialPassword() {
    StringBuilder password = new StringBuilder(INITIAL_PASSWORD_LENGTH);

    for (int index = 0; index < INITIAL_PASSWORD_LENGTH; index++) {
      int charIndex = secureRandom.nextInt(INITIAL_PASSWORD_CHARS.length);
      password.append(INITIAL_PASSWORD_CHARS[charIndex]);
    }

    return password.toString();
  }

  private User findActiveUser(UUID userId) {
    return userRepositoryQuery
        .findById(userId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
  }
}
