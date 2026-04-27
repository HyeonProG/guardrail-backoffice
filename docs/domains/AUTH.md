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
- 액세스 토큰은 전체 원문 저장을 피하고 식별 가능한 최소 정보만 저장한다.
- 리프레시 토큰은 서버 검증을 위해 저장하되 해시로만 저장한다.
- 로그인 실패 사유는 별도 컬럼으로 관리하지 않는다.
- 비밀번호 이력은 최근 비밀번호 재사용 방지와 임시 비밀번호 만료 추적을 위해 사용한다.
- 로그아웃 시 세션은 삭제하지 않고 상태를 `REVOKED`로 변경한다.
- 로그인 이력은 삭제보다 보존 정책으로 관리한다.
- 세션과 비밀번호 이력은 삭제보다 상태와 만료 정책으로 관리한다.

## 6. 패키지 배치
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
