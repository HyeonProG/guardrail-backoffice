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
핵심 도메인 모델을 둔다.

구성:
- entity
- enum

예:
- User
- UserRole
- UserStatus

### repository
영속성 관련 코드를 둔다.

하위 구성:
- repository
- Querydsl 구현체 (`~RepositoryQuery`)

### service
도메인 서비스와 비즈니스 로직을 둔다.

하위 구성:
- service

### dto
계층 간 데이터 전달 객체를 둔다.

하위 구성:
- request dto
- response dto
- command/query dto

### controller
API 입출력 코드를 둔다.

하위 구성:
- controller

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
- 공통 응답 구조
- 공통 예외
- 공통 설정
- 공통 유틸
- 보안 관련 공통 기능

## 7. 현재 도메인 기준 매핑
현재 정의된 도메인은 아래 패키지에 배치한다.

- `Auth`, `UserLoginHistory`, `UserSession`, `UserPasswordHistory` -> `auth`
- `User` -> `user`
- `Product`, `ProductHistory` -> `product`
- `Category` -> `category`
- `FileAttachment` -> `file`

## 8. 문서 위치 기준
도메인별 문서는 `src` 하위가 아니라 `docs` 하위에서 관리한다.

예:
- `docs/domains/USER.md`
- `docs/domains/PRODUCT.md`
- `docs/domains/CATEGORY.md`

코드와 문서는 분리하되, 패키지 구조와 문서 구조가 대응되도록 유지한다.
