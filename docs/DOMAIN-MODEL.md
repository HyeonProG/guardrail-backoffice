# 도메인 모델

## 1. 목적
이 문서는 Guardrail 프로젝트의 핵심 도메인을 한눈에 확인하기 위한 인덱스 문서다.

상세 필드, 상태값, 규칙은 각 도메인 문서에서 정의한다.

## 2. 도메인 기준
- 도메인 이름은 상태보다 본질 개념 중심으로 정의한다.
- 실제로 관리해야 하는 데이터만 영속 도메인으로 둔다.
- 모든 테이블의 PK는 UUID를 사용한다.
- 모든 엔티티는 `BaseEntity` 또는 `SoftDeleteEntity`를 상속한다.
- `BaseEntity`는 `createdAt`, `updatedAt`을 가진다.
- `SoftDeleteEntity`는 `BaseEntity`를 상속하고 `deleted`를 가진다.
- soft delete가 필요한 엔티티만 `SoftDeleteEntity`를 사용한다.
- 모든 테이블 간 연관관계 매핑은 사용하지 않는다.
- 테이블 간 참조가 필요해도 객체 연관관계 대신 식별자 값으로만 다룬다.
- AI 관련 데이터는 기본적으로 별도 테이블에 영속 저장하지 않으며, 별도 도메인 문서에서 명시한 경우에만 영속 저장한다.

## 3. 핵심 도메인 목록
### User
사용자를 관리한다.

### Auth
로그인, 로그아웃, 토큰 재발급 등 인증 흐름을 관리한다.
`Auth`는 `User`를 객체 연관관계 없이 `userId`로 참조한다.

### Product
상품의 기본 정보와 상태를 관리한다.
`Product`는 `Category`를 객체 연관관계 없이 `categoryId`로 참조한다.
상품 등록 시 선택된 옵션은 선택한 카테고리에 연결된 `ProductOption` 마스터 데이터를 사용한다.

### ProductContent
AI 설명 생성, 운영자 검수, 관리자 승인 반영 흐름을 관리한다.
`ProductContentDraft`와 `ProductContentHistory`는 `Product`를 객체 연관관계 없이 `productId`로 참조한다.
현재 범위에서 AI는 텍스트 설명만 생성하고, 상품 이미지는 `FileAttachment`로 직접 등록한다.

### ProductOption
카테고리별로 미리 관리되는 옵션 그룹과 옵션 선택값 마스터 데이터를 관리한다.
`ProductOptionItem`은 `ProductOption`을 객체 연관관계 없이 `productOptionId`로 참조한다.

### FileAttachment
상품 이미지 포함 파일 자원을 관리한다.
`FileAttachment`는 대상 도메인을 `targetType`, `targetId`로 참조한다.

### Category
상품 분류 체계를 관리한다.

### ProductHistory
상품 등록, 승인, 반려 이력을 관리한다.
`ProductHistory`는 `Product`와 처리자 `User`를 각각 `productId`, `actorId`로 참조한다.

## 4. 보조 개념
### Auth 하위 개념
인증 도메인 안에서 아래 개념을 함께 관리한다.

- UserLoginHistory
- UserSession
- UserPasswordHistory

### 설명 생성 결과
설명 생성 결과는 `ProductContentDraft`에 영속 저장한다.

### 검증 결과
운영자 검수와 관리자 승인 결과는 `ProductContentDraft.status`와 `ProductContentHistory`로 관리한다.
