# User 도메인

## 1. 책임
`User`는 시스템 사용자의 기본 정보를 관리한다.

인증 흐름은 `Auth` 도메인에서 담당하며, `User` 도메인은 사용자 자체의 식별 정보, 역할, 사용 여부와 사용자 자기관리 유스케이스를 관리한다.

백오피스에서는 관리자가 사용자를 생성하며, 초기 비밀번호는 시스템이 자동 생성한다.
생성된 아이디와 초기 비밀번호 전달 정책은 인증 흐름과 함께 `Auth` 도메인에서 담당한다.
사용자 생성 완료 조건은 사용자 저장, 초기 비밀번호 생성, 초기 비밀번호 해시 저장, 초기 전달용 정보 반환까지 포함한다.

## 2. 주요 엔티티
### User
사용자 본체 엔티티다.

관리 대상:
- 사용자 UUID PK
- 이메일
- 이름
- 역할
- 사용 여부
- 생성일시, 수정일시, 삭제 여부

## 3. Enum
### UserRole
사용자의 역할을 정의한다.

값:
- `STAFF`
- `OPERATOR`
- `ADMIN`

### UserStatus
사용자의 사용 상태를 정의한다.

값:
- `ACTIVE`
- `INACTIVE`

## 4. 필드 초안
### User
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `email` | 사용자 이메일 |
| `name` | 사용자 이름 |
| `role` | 사용자 역할 |
| `status` | 사용자 사용 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

## 5. 설계 규칙
- PK는 UUID를 사용한다.
- `User`는 `SoftDeleteEntity`를 상속한다.
- `status`는 운영상 사용 상태를 의미한다.
- 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- `deleted = true`는 soft delete 상태를 의미한다.
- `email`은 unique 제약을 가진다.
- 다른 도메인에서 사용자를 참조할 때 객체 연관관계를 사용하지 않는다.
- 다른 도메인에서는 `userId`로 사용자 UUID 값을 참조한다.
- 비밀번호, 세션, 로그인 이력은 `User`가 아니라 `Auth` 도메인에서 관리한다.
- 사용자 생성 시 초기 비밀번호는 시스템이 자동 생성한다.
- 사용자 생성 시 상태는 기본적으로 `ACTIVE`로 시작한다.
- 자동 생성된 초기 비밀번호는 평문 저장하지 않고 `Auth` 도메인의 비밀번호 이력에 해시로 저장한다.
- 자동 생성된 초기 비밀번호는 생성 직후 1회 전달 가능한 정보로만 사용한다.
- 사용자 생성 응답 또는 후속 인증 전달 흐름에는 초기 전달용 정보가 포함되어야 한다.
- 사용자의 비밀번호 변경과 비밀번호 이력 조회는 `User` 도메인의 API로 노출할 수 있다.
- `User` 도메인은 auth 저장 모델을 직접 소유하지 않더라도 사용자 자기관리 유스케이스를 위해 `Auth` 저장 모델을 조회/조합할 수 있다.
- 사용자 기본 정보의 범용 전체 수정은 entity가 아니라 service에서 처리한다.
- 사용자 기본 정보 수정과 사용자 상태 변경은 서로 다른 유스케이스로 분리한다.
- `email`, `name`, `role` 수정은 기본 정보 수정 API에서 처리한다.
- `status` 변경은 별도 상태 변경 API에서만 처리한다.
- 사용자 상태 변경 API는 `ACTIVE`, `INACTIVE` 전이만 허용한다.
- entity에는 `activate`, `inactivate`, `delete`, `restore` 같은 최소 상태 변경 메서드만 둔다.
- `email`, `name`, `role` 변경은 entity 메서드가 아니라 service에서 처리한다.
- `User` 도메인 구현 작업은 `com.hyeon.guardrail.user` 범위 안에서만 수행한다.

## 6. 패키지 배치
`User` 도메인은 `com.hyeon.guardrail.user` 하위에 배치한다.

```text
com.hyeon.guardrail.user
├── domain
│   ├── User.java
│   ├── UserRole.java
│   └── UserStatus.java
├── repository
│   ├── UserRepository.java
│   └── UserRepositoryQuery.java
├── service
│   └── UserService.java
├── dto
│   ├── UserCreateRequest.java
│   ├── UserUpdateRequest.java
│   ├── UserStatusUpdateRequest.java
│   ├── ChangePasswordRequest.java
│   ├── ChangePasswordResponse.java
│   ├── PasswordHistoryResponse.java
│   ├── UserTemporaryPasswordIssueResponse.java
│   └── UserResponse.java
└── controller
    └── UserController.java
```

## 7. UserController 책임
`UserController`는 아래 사용자 및 자기관리 API를 담당한다.

- `POST /api/v1/users`
- `GET /api/v1/users/{userId}`
- `GET /api/v1/users/{userId}/password-histories`
- `PATCH /api/v1/users/{userId}/password`
- `GET /api/v1/users`
- `PUT /api/v1/users/{userId}`
- `PATCH /api/v1/users/{userId}/status`
- `DELETE /api/v1/users/{userId}`
- `POST /api/v1/users/{userId}/temporary-password`
