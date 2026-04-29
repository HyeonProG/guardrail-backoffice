package com.hyeon.guardrail.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.auth.service.AuthService;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.mail.MailSender;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserRole;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.dto.UserCreateRequest;
import com.hyeon.guardrail.user.dto.UserCreateResponse;
import com.hyeon.guardrail.user.dto.UserStatusUpdateRequest;
import com.hyeon.guardrail.user.repository.UserRepository;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** 사용자 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserRepositoryQuery userRepositoryQuery;
  @Mock private AuthService authService;
  @Mock private MailSender mailSender;
  @Mock private CurrentUserService currentUserService;

  @InjectMocks private UserService userService;

  /** 사용자 생성 시 초기 비밀번호 저장과 메일 발송을 수행 */
  @Test
  void createUserCreatesInitialPasswordAndSendsMail() {
    UserCreateRequest request = new UserCreateRequest("staff@example.com", "홍길동", UserRole.STAFF);
    User savedUser = new User("staff@example.com", "홍길동", UserRole.STAFF, UserStatus.ACTIVE);
    ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());

    when(userRepository.existsByEmailAndDeletedFalse(request.getEmail())).thenReturn(false);
    when(userRepository.count()).thenReturn(0L);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserCreateResponse response = userService.createUser(request);

    assertThat(response.getUserId()).isEqualTo(savedUser.getId());
    assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(response.getInitialPassword()).isNotBlank();
    verify(authService).saveInitialPassword(savedUser.getId(), response.getInitialPassword());
    verify(mailSender)
        .send(
            savedUser.getEmail(),
            "Guardrail 초기 계정 정보",
            "email=" + savedUser.getEmail() + ", initialPassword=" + response.getInitialPassword());
  }

  /** 중복 이메일이면 사용자 생성에 실패 */
  @Test
  void createUserThrowsWhenEmailAlreadyExists() {
    UserCreateRequest request = new UserCreateRequest("staff@example.com", "홍길동", UserRole.STAFF);

    when(userRepository.existsByEmailAndDeletedFalse(request.getEmail())).thenReturn(true);
    when(userRepository.count()).thenReturn(0L);

    assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(BaseException.class);
    verify(authService, never()).saveInitialPassword(any(), any());
    verify(mailSender, never()).send(any(), any(), any());
  }

  /** 사용자 상태 변경 요청에 따라 상태를 변경 */
  @Test
  void updateUserStatusChangesUserState() {
    UUID userId = UUID.randomUUID();
    User user = new User("staff@example.com", "홍길동", UserRole.STAFF, UserStatus.ACTIVE);
    ReflectionTestUtils.setField(user, "id", userId);
    when(userRepositoryQuery.findById(userId)).thenReturn(java.util.Optional.of(user));

    UserStatusUpdateRequest request = new UserStatusUpdateRequest(UserStatus.INACTIVE);

    var response = userService.updateUserStatus(userId, request);

    assertThat(response.getStatus()).isEqualTo(UserStatus.INACTIVE);
    assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
  }
}
