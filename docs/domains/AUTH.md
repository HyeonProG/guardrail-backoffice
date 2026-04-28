# Auth 도메인

## 1. 책임
`Auth`는 인증 흐름과 인증 이력을 관리한다.

로그인, 로그아웃, 토큰 재발급, 세션 관리, 로그인 이력, 비밀번호 이력을 담당한다.

## 2. 주요 엔티티
### UserLoginHistory
사용자의 로그인 이력을 관리한다.

관리 대상:
- 로그인 이력 UUID PK
- 사용자 ID
- 로그인 유형
- 로그인 결과
- IP
- 로그인 일시
- 생성일시, 수정일시

### UserSession
사용자의 인증 세션과 토큰 정보를 관리한다.

관리 대상:
- 세션 UUID PK
- 사용자 ID
- 액세스 토큰 식별 정보
- 리프레시 토큰 해시
- 디바이스 유형
- IP
- 갱신일시
- 만료일시
- 생성일시, 수정일시

### UserPasswordHistory
사용자의 비밀번호 변경 및 임시 비밀번호 이력을 관리한다.

관리 대상:
- 비밀번호 이력 UUID PK
- 사용자 ID
- 비밀번호 해시
- 임시 비밀번호 여부
- 만료일시
- 생성일시, 수정일시

## 3. Enum
### LoginType
로그인 인증 유형을 정의한다.

값:
- `PASSWORD`
- `TEMP_PASSWORD`

### LoginResult
로그인 결과를 정의한다.

값:
- `SUCCESS`
- `FAIL`

### DeviceType
인증 요청 디바이스 유형을 정의한다.

값:
- `WEB`
- `MOBILE`
- `UNKNOWN`

### SessionStatus
세션 상태를 정의한다.

값:
- `ACTIVE`
- `EXPIRED`
- `REVOKED`

## 4. 필드 초안
### UserLoginHistory
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `userId` | 사용자 UUID |
| `loginType` | 로그인 유형 |
| `loginResult` | 로그인 결과 |
| `ipAddress` | 요청 IP |
| `loggedInAt` | 로그인 일시 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

### UserSession
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `userId` | 사용자 UUID |
| `accessTokenId` | 액세스 토큰 식별 정보 |
| `refreshTokenHash` | 리프레시 토큰 해시 |
| `deviceType` | 디바이스 유형 |
| `ipAddress` | 요청 IP |
| `status` | 세션 상태 |
| `refreshedAt` | 갱신일시 |
| `expiredAt` | 만료일시 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

### UserPasswordHistory
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `userId` | 사용자 UUID |
| `passwordHash` | 비밀번호 해시 |
| `temporary` | 임시 비밀번호 여부 |
| `expiredAt` | 만료일시 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

## 5. 설계 규칙
- 모든 엔티티의 PK는 UUID를 사용한다.
- Auth 엔티티는 `BaseEntity`를 상속한다.
- Auth 엔티티는 soft delete를 기본 적용하지 않는다.
- `User`는 객체 연관관계로 참조하지 않는다.
- 사용자 참조는 `userId`로 사용자 UUID 값을 저장한다.
- 토큰 값과 비밀번호는 평문으로 저장하지 않는다.
- 백오피스 사용자 생성 시 초기 비밀번호는 시스템이 자동 생성한다.
- 자동 생성된 초기 비밀번호 전달 정책은 인증 흐름에서 관리한다.
- 자동 생성된 초기 비밀번호는 해시 저장 전에만 평문으로 존재할 수 있다.
- 초기 비밀번호는 생성 직후 1회 전달용 정보로만 사용하고 저장하지 않는다.
- 현재 단계에서는 사용자 생성 응답 또는 별도 초기 전달용 응답으로만 초기 비밀번호를 노출할 수 있다.
- 초기 임시 비밀번호의 만료 기간은 생성 시점부터 7일이다.
- 초기 임시 비밀번호 이력은 `expiredAt`을 반드시 저장한다.
- 초기 임시 비밀번호가 만료되면 재로그인에 사용할 수 없고, 재발급 시 새로운 비밀번호 이력을 추가한다.
- 액세스 토큰은 전체 원문 저장을 피하고 식별 가능한 최소 정보만 저장한다.
- 리프레시 토큰은 서버 검증을 위해 저장하되 해시로만 저장한다.
- 로그인 요청은 `email`, `password`, `deviceType`을 사용한다.
- 로그인 요청의 IP는 request body가 아니라 서버 요청 정보에서 추출해 저장한다.
- 로그인 성공 응답은 `accessToken`, `refreshToken`, `sessionId`, `accessTokenExpiredAt`, `refreshTokenExpiredAt`, `userId`, `role`을 포함한다.
- access token에는 `userId`, `role`, `sessionId`, `accessTokenId`를 포함한다.
- refresh token에는 `userId`, `sessionId`를 포함한다.
- 로그인 성공 시 `UserSession`을 `ACTIVE` 상태로 생성하고 refresh token은 해시로 저장한다.
- 토큰 재발급 요청은 `sessionId`, `refreshToken`을 사용한다.
- 토큰 재발급 성공 시 새로운 access token, refresh token, `accessTokenExpiredAt`, `refreshTokenExpiredAt`, `sessionId`를 응답한다.
- 토큰 재발급 시 세션 상태가 `ACTIVE`이고 세션 만료 시간이 지나지 않았으며 refresh token 해시 검증이 통과해야 한다.
- 토큰 재발급 성공 시 세션의 refresh token hash, refreshedAt, expiredAt을 갱신한다.
- 로그아웃 요청은 Authorization 헤더의 access token과 `sessionId`를 함께 사용한다.
- 로그아웃 성공 응답은 `sessionId`, `status`를 포함한다.
- 로그아웃 시 대상 세션을 삭제하지 않고 `REVOKED` 상태로 변경한다.
- 존재하지 않는 이메일 로그인 실패는 로그인 이력을 저장하지 않고 즉시 인증 실패로 종료한다.
- 로그인 실패 사유는 별도 컬럼으로 관리하지 않는다.
- 비밀번호 이력은 최근 비밀번호 재사용 방지와 임시 비밀번호 만료 추적을 위해 사용한다.
- 로그아웃 시 세션은 삭제하지 않고 상태를 `REVOKED`로 변경한다.
- 로그인 이력은 삭제보다 보존 정책으로 관리한다.
- 세션과 비밀번호 이력은 삭제보다 상태와 만료 정책으로 관리한다.

## 6. 인증 흐름 기준
### 6.1 로그인
1. `email` 기준으로 삭제되지 않은 사용자를 조회한다.
2. 사용자 상태가 `ACTIVE`인지 확인한다.
3. 입력한 비밀번호와 최근 유효 비밀번호 이력을 검증한다.
4. 성공 시 로그인 이력을 `SUCCESS`로 저장한다.
5. 존재하지 않는 이메일이면 로그인 이력을 저장하지 않고 즉시 인증 실패로 종료한다.
6. 식별된 사용자의 비밀번호 불일치 또는 비활성 상태 실패는 로그인 이력을 `FAIL`로 저장하고 토큰을 발급하지 않는다.
7. access token, refresh token을 발급한다.
8. `UserSession`을 `ACTIVE` 상태로 저장한다.
9. 토큰 정보와 사용자 기본 정보를 응답한다.

### 6.2 토큰 재발급
1. `sessionId` 기준으로 세션을 조회한다.
2. 세션 상태가 `ACTIVE`인지 확인한다.
3. 세션 만료 여부를 확인한다.
4. 입력한 refresh token과 저장된 해시를 검증한다.
5. 성공 시 access token과 refresh token을 새로 발급한다.
6. 세션의 refresh token hash, refreshedAt, expiredAt을 갱신한다.
7. 새 토큰 정보와 세션 식별 정보를 응답한다.

### 6.3 로그아웃
1. Authorization 헤더의 access token과 `sessionId`를 검증한다.
2. 대상 세션을 조회한다.
3. 세션 상태를 `REVOKED`로 변경한다.
4. 로그아웃 후 해당 세션으로는 재발급을 허용하지 않는다.

## 7. 패키지 배치
`Auth` 도메인은 `com.hyeon.guardrail.auth` 하위에 배치한다.

```text
com.hyeon.guardrail.auth
├── domain
│   ├── UserLoginHistory.java
│   ├── UserSession.java
│   ├── UserPasswordHistory.java
│   ├── LoginType.java
│   ├── LoginResult.java
│   ├── DeviceType.java
│   └── SessionStatus.java
├── repository
│   ├── UserLoginHistoryRepository.java
│   ├── UserSessionRepository.java
│   ├── UserPasswordHistoryRepository.java
│   └── AuthRepositoryQuery.java
├── service
│   └── AuthService.java
├── dto
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── TokenRefreshRequest.java
│   └── TokenRefreshResponse.java
└── controller
    └── AuthController.java
```
