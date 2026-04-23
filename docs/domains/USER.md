# User 도메인

## 1. 책임
`User`는 시스템 사용자의 기본 정보를 관리한다.

인증 흐름은 `Auth` 도메인에서 담당하며, `User` 도메인은 사용자 자체의 식별 정보, 역할, 사용 여부를 관리한다.

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
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |
| `deleted` | soft delete 여부, `BaseEntity` 상속 |

## 5. 설계 규칙
- PK는 UUID를 사용한다.
- 모든 엔티티는 `BaseEntity`를 상속한다.
- 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- `email`은 unique 제약을 가진다.
- 다른 도메인에서 사용자를 참조할 때 객체 연관관계를 사용하지 않는다.
- 다른 도메인에서는 `userId`로 사용자 UUID 값을 참조한다.
- 비밀번호, 세션, 로그인 이력은 `User`가 아니라 `Auth` 도메인에서 관리한다.

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
│   └── UserResponse.java
└── controller
    └── UserController.java
```
