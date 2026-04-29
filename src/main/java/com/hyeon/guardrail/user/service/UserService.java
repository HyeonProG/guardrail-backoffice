package com.hyeon.guardrail.user.service;

import com.hyeon.guardrail.auth.service.AuthService;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.mail.MailSender;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.response.PageResponse;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.dto.UserCreateRequest;
import com.hyeon.guardrail.user.dto.UserCreateResponse;
import com.hyeon.guardrail.user.dto.UserResponse;
import com.hyeon.guardrail.user.dto.UserStatusUpdateRequest;
import com.hyeon.guardrail.user.dto.UserUpdateRequest;
import com.hyeon.guardrail.user.repository.UserRepository;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.security.SecureRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
  private final AuthService authService;
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

  /** 사용자 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<UserResponse> getUsers(UserStatus status, Pageable pageable) {
    currentUserService.requireAdminOrOperator();
    return PageResponse.from(userRepositoryQuery.findAll(status, pageable).map(UserResponse::from));
  }

  /** 사용자 기본 정보 수정 */
  @Transactional
  public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
    if (userRepository.existsByEmailAndDeletedFalseAndIdNot(request.getEmail(), userId)) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
    }

    int updatedCount =
        userRepository.updateBasicInfo(
            userId, request.getEmail(), request.getName(), request.getRole());

    if (updatedCount == 0) {
      throw new BaseException(BaseResponseStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
    }

    return UserResponse.from(findActiveUser(userId));
  }

  /** 사용자 상태 변경 */
  @Transactional
  public UserResponse updateUserStatus(UUID userId, UserStatusUpdateRequest request) {
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
    User user = findActiveUser(userId);
    user.delete();
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
