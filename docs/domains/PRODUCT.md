# Product 도메인

## 1. 책임
`Product`는 상품의 기본 정보와 등록 상태를 관리한다.

상품 설명 생성, 검증, 승인, 반려 흐름의 중심 데이터이며, 최종적으로 백오피스에서 관리되는 상품 정보를 표현한다.

## 2. 주요 엔티티
### Product
상품 본체 엔티티다.

관리 대상:
- 상품 UUID PK
- 카테고리 ID
- 상품명
- 상품 설명
- 수량
- 상품 상태
- 생성일시, 수정일시, 삭제 여부

### ProductHistory
상품 등록, 승인, 반려 이력을 관리한다.

관리 대상:
- 상품 이력 UUID PK
- 상품 ID
- 처리 유형
- 처리자 ID
- 반려 사유
- 생성일시, 수정일시

## 3. Enum
### ProductStatus
상품 상태를 정의한다.

값:
- `DRAFT`
- `PENDING`
- `APPROVED`
- `REJECTED`
- `INACTIVE`

### ProductHistoryType
상품 이력 유형을 정의한다.

값:
- `CREATED`
- `SUBMITTED`
- `APPROVED`
- `REJECTED`
- `UPDATED`

## 4. 필드 초안
### Product
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `categoryId` | 카테고리 UUID |
| `name` | 상품명 |
| `description` | 상품 설명 |
| `quantity` | 판매 가능 수량 |
| `status` | 상품 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

### ProductHistory
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `productId` | 상품 UUID |
| `actorId` | 처리자 사용자 UUID |
| `type` | 상품 이력 유형 |
| `reason` | 반려 사유 또는 처리 사유 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

## 5. 설계 규칙
- 모든 엔티티의 PK는 UUID를 사용한다.
- `Product`는 `SoftDeleteEntity`를 상속한다.
- `ProductHistory`는 `BaseEntity`를 상속한다.
- 상품 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- `Category`는 객체 연관관계로 참조하지 않는다.
- 카테고리 참조는 `categoryId`로 카테고리 UUID 값을 저장한다.
- `quantity`는 운영상 판매 가능 수량 기준으로 관리한다.
- 처리자 사용자는 객체 연관관계로 참조하지 않는다.
- 처리자 참조는 `actorId`로 사용자 UUID 값을 저장한다.
- 상품 승인과 반려 흐름은 `ProductHistory`에 기록한다.
- 반려 사유는 `REJECTED` 이력에서만 필수로 다룬다.
- `reason`은 `REJECTED` 이력에서는 필수이며, 그 외 이력에서는 선택 값으로 다룬다.
- AI 응답 원문은 기본적으로 별도 저장하지 않고, 최종 반영된 설명만 `Product.description`에 저장한다.

## 6. 상태 전이 규칙
상품 상태는 아래 흐름을 따른다.

```text
DRAFT -> PENDING -> APPROVED
DRAFT -> PENDING -> REJECTED
REJECTED -> PENDING
APPROVED -> INACTIVE
```

규칙:
- `DRAFT`는 운영자가 작성 중인 상태다.
- `PENDING`은 관리자 승인을 기다리는 상태다.
- `APPROVED`는 승인 완료 상태다.
- `REJECTED`는 반려 상태다.
- `INACTIVE`는 승인 이후 비활성화된 상태다.
- `REJECTED` 상태의 상품은 수정 후 다시 `PENDING` 상태로 제출할 수 있다.

## 7. 패키지 배치
`Product` 도메인은 `com.hyeon.guardrail.product` 하위에 배치한다.

```text
com.hyeon.guardrail.product
├── domain
│   ├── Product.java
│   ├── ProductStatus.java
│   ├── ProductHistory.java
│   └── ProductHistoryType.java
├── repository
│   ├── ProductRepository.java
│   ├── ProductHistoryRepository.java
│   └── ProductRepositoryQuery.java
├── service
│   ├── ProductService.java
│   └── ProductHistoryService.java
├── dto
│   ├── ProductCreateRequest.java
│   ├── ProductUpdateRequest.java
│   ├── ProductApprovalRequest.java
│   ├── ProductRejectRequest.java
│   └── ProductResponse.java
└── controller
    └── ProductController.java
```
