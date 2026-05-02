# 패키지 구조

## 1. 목적
이 문서는 Guardrail 프로젝트의 패키지 구조 기준을 정의한다.

패키지 구조는 도메인 기준으로 나누고, 각 도메인 안에서 역할에 따라 세부 패키지를 구분한다.

## 2. 최상위 구조
기본 패키지는 아래와 같이 구성한다.

```text
com.hyeon.guardrail
├── common
├── auth
├── user
├── product
├── productoption
├── category
└── file
```

## 3. 패키지 구성 원칙
- 최상위 패키지는 도메인 기준으로 나눈다.
- 공통 기능은 `common` 하나로 관리한다.
- 공통 기능이 늘어나면 `common` 하위에 세부 패키지를 추가한다.
- 하나의 도메인 안에서 필요한 역할은 하위 패키지로 구분한다.
- 도메인 간 객체 참조는 연관관계 매핑 대신 식별자 값으로만 다룬다.
- 특정 도메인 내부 구현을 다른 도메인이 직접 참조하지 않는다.
- controller 계층에서는 비즈니스 로직을 수행하지 않는다.
- 여러 도메인을 조합하는 흐름 제어와 유스케이스 실행은 service 계층에서 수행한다.
- repository 계층은 데이터 저장 및 조회 책임만 가진다.
- dto는 계층 간 데이터 전달 목적 외 사용하지 않는다.
- entity를 controller 응답으로 직접 반환하지 않는다.

## 4. 도메인별 기본 구조
각 도메인은 아래 구조를 기본으로 한다.

```text
{domain}
├── domain
├── repository
├── service
├── dto
└── controller
```

### domain
핵심 영속 도메인 모델을 둔다.

구성:
- entity
- enum

제약:
- entity는 상태 전이와 soft delete 같은 자기 상태 변경 메서드만 가질 수 있다.
- 여러 도메인을 조합하는 흐름 제어와 유스케이스 실행은 service 계층에서 수행한다.
- entity 선언 필드는 `@Column(name = "...")`을 명시한다.
- entity 생성은 `@AllArgsConstructor`를 사용한다.
- entity에 정적 `create` 메서드를 두지 않는다.
- entity에 범용적인 전체 수정 메서드를 두지 않는다.

예:
- User
- UserRole
- UserStatus

### repository
영속성 관련 코드를 둔다.

하위 구성:
- Spring Data JPA Repository (`~Repository`)
- Querydsl 전용 단일 조회 클래스 (`~RepositoryQuery`)

제약:
- repository는 비즈니스 로직을 포함하지 않는다.
- 조회 로직은 `~RepositoryQuery` 단일 클래스로 분리한다.
- 네이밍 규칙을 반드시 준수한다.
- service는 필요에 따라 `~Repository`와 `~RepositoryQuery`를 함께 호출한다.

### service
도메인 서비스와 비즈니스 로직을 둔다.

제약:
- 여러 도메인을 조합하는 흐름 제어와 유스케이스 실행은 service 계층에서 수행한다.
- controller 또는 repository에서 비즈니스 로직을 수행하지 않는다.

### dto
dto는 계층 간 데이터 전달을 위한 객체를 둔다.

하위 구성:
- request
- response

제약:
- dto는 외부 입출력 또는 계층 간 전달 목적으로만 사용한다.
- entity를 직접 노출하지 않고 dto를 통해 변환한다.

### controller
API 입출력 코드를 둔다.

하위 구성:
- controller

제약:
- controller는 request/response 처리만 담당한다.
- 비즈니스 로직을 포함하지 않는다.
- service 계층만 호출한다.
- controller 응답은 `BaseResponseEntity<T>`만 사용한다.
- controller는 `ResponseEntity`를 직접 반환하지 않는다.

## 5. 예시 구조
예시로 `user` 도메인은 아래와 같이 구성한다.

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

## 6. 공통 패키지 기준
### common
프로젝트 전반에서 공유되는 공통 개념을 둔다.

예:
- BaseEntity
- SoftDeleteEntity
- 공통 응답 구조
- 공통 예외
- 공통 설정
- AI 연동 공통 클라이언트
- 공통 유틸
- 보안 관련 공통 기능

BaseEntity 기준:
- 모든 엔티티는 `BaseEntity` 또는 `SoftDeleteEntity`를 상속한다.
- `BaseEntity`는 `createdAt`, `updatedAt`을 가진다.
- `SoftDeleteEntity`는 `BaseEntity`를 상속하고 `deleted`를 가진다.
- soft delete가 필요한 엔티티만 `SoftDeleteEntity`를 사용한다.

제약:
- 특정 도메인에만 필요한 로직은 common에 두지 않는다.
- 두 개 이상의 도메인에서 재사용 근거가 명확할 때만 common으로 이동한다.
- 공통화보다 도메인 응집도를 우선한다.
- 외부 AI 모델 호출, 프롬프트 조합, AI 응답 매핑은 `common.ai`에 둔다.
- 도메인 서비스는 AI 모델 구현체가 아니라 `common.ai` 인터페이스만 호출한다.

## 7. 현재 도메인 기준 매핑
현재 정의된 도메인은 아래 패키지에 배치한다.

- `Auth`, `UserLoginHistory`, `UserSession`, `UserPasswordHistory` -> `auth`
- `User` -> `user`
- `Product`, `ProductHistory` -> `product`
- `ProductOption`, `ProductOptionItem` -> `productoption`
- `Category` -> `category`
- `FileAttachment` -> `file`

인증 및 세션 관련 도메인은 사용자 도메인과 분리하여 인증 경계 기준으로 auth 패키지에 배치한다.

## 8. 문서 위치 기준
도메인별 문서는 `src` 하위가 아니라 `docs` 하위에서 관리한다.

예:
- `docs/domains/USER.md`
- `docs/domains/PRODUCT.md`
- `docs/domains/CATEGORY.md`

코드와 문서는 분리하되, 패키지 구조와 문서 구조가 대응되도록 유지한다.
