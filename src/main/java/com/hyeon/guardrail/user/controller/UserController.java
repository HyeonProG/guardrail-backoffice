package com.hyeon.guardrail.user.controller;

import com.hyeon.guardrail.common.response.BaseResponseEntity;
import com.hyeon.guardrail.user.dto.UserCreateRequest;
import com.hyeon.guardrail.user.dto.UserCreateResponse;
import com.hyeon.guardrail.user.dto.UserResponse;
import com.hyeon.guardrail.user.dto.UserStatusUpdateRequest;
import com.hyeon.guardrail.user.dto.UserUpdateRequest;
import com.hyeon.guardrail.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 사용자 API 컨트롤러 */
@Tag(name = "User", description = "사용자 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  /** 사용자 생성 */
  @Operation(summary = "사용자 생성", description = "사용자를 생성하고 초기 전달용 비밀번호를 반환합니다.")
  @PostMapping
  public BaseResponseEntity<UserCreateResponse> createUser(
      @Valid @RequestBody UserCreateRequest request) {
    UserCreateResponse response = userService.createUser(request);
    return BaseResponseEntity.created(response, "사용자가 생성되었습니다.");
  }

  /** 사용자 조회 */
  @Operation(summary = "사용자 조회", description = "사용자 기본 정보를 조회합니다.")
  @GetMapping("/{userId}")
  public BaseResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
    return BaseResponseEntity.success(userService.getUser(userId));
  }

  /** 사용자 수정 */
  @Operation(summary = "사용자 수정", description = "사용자 기본 정보를 수정합니다.")
  @PutMapping("/{userId}")
  public BaseResponseEntity<UserResponse> updateUser(
      @PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
    return BaseResponseEntity.success(userService.updateUser(userId, request), "사용자가 수정되었습니다.");
  }

  /** 사용자 상태 변경 */
  @Operation(summary = "사용자 상태 변경", description = "사용자 상태를 ACTIVE 또는 INACTIVE로 변경합니다.")
  @PatchMapping("/{userId}/status")
  public BaseResponseEntity<UserResponse> updateUserStatus(
      @PathVariable UUID userId, @Valid @RequestBody UserStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        userService.updateUserStatus(userId, request), "사용자 상태가 변경되었습니다.");
  }

  /** 사용자 삭제 */
  @Operation(summary = "사용자 삭제", description = "사용자를 soft delete 처리합니다.")
  @DeleteMapping("/{userId}")
  public BaseResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.deleteUser(userId);
    return BaseResponseEntity.success("사용자가 삭제되었습니다.");
  }
}
