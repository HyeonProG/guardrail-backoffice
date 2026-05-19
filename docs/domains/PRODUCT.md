# Product 도메인

## 1. 책임
`Product`는 직원이 작성 중인 상품 등록 요청의 기본 정보와 승인 상태를 관리한다.

직원은 상품 등록 페이지에서 상품 제목, 카테고리, 옵션, 상세 설명, 이미지를 입력하고 승인 요청을 올린다.  
관리자는 별도 승인 요청 리스트에서 이를 검토하고 승인 또는 반려한다.

## 2. 주요 엔티티
### Product
상품 등록 요청 본체 엔티티다.

관리 대상:
- 상품 UUID PK
- 카테고리 ID
- 상품명
- 상세 설명
- 상품 상태
- 생성일시, 수정일시, 삭제 여부

### ProductHistory
상품 생성, 수정, 승인 요청, 승인, 반려, 비활성화 이력을 관리한다.

관리 대상:
- 상품 이력 UUID PK
- 상품 ID
- 처리 유형
- 처리자 ID
- 처리 사유
- 생성일시, 수정일시

### ProductSelectedOption
상품 생성 시 선택된 옵션값 스냅샷을 관리한다.

관리 대상:
- 선택 스냅샷 UUID PK
- 상품 ID
- 옵션 ID
- 옵션명
- 옵션 항목 ID
- 옵션 항목명
- 정렬 순서
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
- `UPDATED`
- `SUBMITTED`
- `APPROVED`
- `REJECTED`
- `INACTIVATED`

## 4. 필드 초안
### Product
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `categoryId` | 카테고리 UUID |
| `name` | 상품명 |
| `description` | 상품 등록 폼의 상세 설명 값 |
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
| `type` | 처리 이력 유형 |
| `reason` | 반려 사유 또는 처리 사유 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

### ProductSelectedOption
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `productId` | 상품 UUID |
| `productOptionId` | 옵션 UUID |
| `productOptionName` | 옵션명 스냅샷 |
| `productOptionItemId` | 옵션 항목 UUID |
| `productOptionItemName` | 옵션 항목명 스냅샷 |
| `sortOrder` | 표시 순서 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

## 5. 설계 규칙
- 모든 엔티티의 PK는 UUID를 사용한다.
- `Product`는 `SoftDeleteEntity`를 상속한다.
- `ProductHistory`는 `BaseEntity`를 상속한다.
- `Product`는 공통 조회/상속 규칙 일관성을 위해 `deleted` 필드를 가지지만, 삭제 유스케이스는 soft delete가 아니라 hard delete로 처리한다.
- 카테고리는 객체 연관관계로 참조하지 않고 `categoryId` 값으로만 저장한다.
- 처리자 사용자는 객체 연관관계로 참조하지 않고 `actorId` 값으로만 저장한다.
- 상품 이미지는 `FileAttachment` 도메인에서 운영자가 직접 선택하고, 상품 생성 직후 이미지 메타데이터를 함께 등록한다.
- 상품 옵션은 선택한 카테고리에 연결된 `ProductOption` 마스터 데이터에서 선택한다.
- 선택된 옵션값은 상품 생성 request로 받아 `ProductSelectedOption` 스냅샷으로 영속 저장한다.
- `ProductSelectedOption`은 옵션/옵션 항목의 이름을 스냅샷으로 저장해, 기준 정보명이 바뀌어도 기존 상품 표시값을 유지한다.
- 상품명은 필수이고 최대 100자다.
- 상품 설명은 선택 값이고 최대 2000자다.
- 상세 설명은 직원이 직접 입력할 수도 있고, 상품 API에서 AI 문구를 즉시 생성해 현재 설명 필드에 반영할 수도 있다.
- AI 설명 생성은 짧은 특징 키워드도 고객 관점의 사용 장점과 구매 설득 문장으로 확장한다.
- AI는 설명 문구를 생성할 뿐이며, 최종 설명 반영 결정은 직원이 한다.
- AI는 입력에 없는 효능, 성능, 소재, 브랜드, 인증 정보를 임의로 추가할 수 없다.
- 기본 UI에서는 선택 항목을 AI 입력으로 전달하지 않는다.
- 상품 승인 전까지는 실제 게시 완료 상태가 아니다.
- 상품 승인 요청, 승인, 반려, 비활성화 흐름은 `ProductHistory`에 기록한다.
- 생성, 기본 정보 수정, 상태 변경 request에는 이력 저장을 위한 `actorId`를 포함한다.
- `actorId`는 인증 토큰에서 추출하지 않고 request body 값으로 받는다.
- `STAFF`는 상품 등록, 수정, 승인 요청만 수행한다.
- 카테고리와 상품 옵션 같은 기준 정보 관리는 `ADMIN`, `OPERATOR`만 수행한다.
- 상품 기본 정보 수정에서 실제 변경이 없으면 `UPDATED` 이력을 남기지 않는다.
- AI 설명 초안 생성으로 설명이 실제 변경되면 `UPDATED` 이력을 저장하고 `reason`은 `"AI 설명 초안 생성"`으로 기록한다.
- 승인 완료 상품의 수정, 재승인 대기, 반려, 비활성화는 `ADMIN`, `OPERATOR`만 수행한다.
- `ProductService`는 유스케이스 오케스트레이션만 담당하고, 상태 전이는 `ProductStatusTransitionService`, 선택 옵션 동기화는 `ProductSelectedOptionService`, 이력 저장은 `ProductHistoryService`로 분리한다.

## 6. 상태 전이 규칙
상품 상태는 아래 흐름을 따른다.

```text
DRAFT -> PENDING -> APPROVED
DRAFT -> PENDING -> REJECTED
REJECTED -> PENDING
APPROVED -> PENDING
APPROVED -> REJECTED
APPROVED -> INACTIVE
```

규칙:
- `DRAFT`는 직원이 상품 등록 폼을 작성 중인 상태다.
- `PENDING`은 승인 요청 리스트에 올라가 관리자의 검토를 기다리는 상태다.
- `APPROVED`는 관리자 승인 완료 상태다.
- `REJECTED`는 관리자 반려 상태다.
- `INACTIVE`는 승인 이후 비활성화된 상태다.
- 반려된 상품은 기본 정보와 설명을 수정한 뒤 다시 `PENDING`으로 승인 요청할 수 있다.
- 승인 완료 상품은 관리자 또는 운영자가 운영 검토를 위해 다시 `PENDING` 또는 `REJECTED`로 변경할 수 있다.

## 7. 백오피스 화면 흐름
### 상품 등록 페이지
- 직원은 상품 제목, 카테고리, 옵션, 상세 설명, 이미지를 입력한다.
- 카테고리는 선택 UI에서 고르고, 옵션은 선택한 카테고리에 연결된 선택 항목을 체크해 입력한다.
- 상세 설명은 직접 작성하거나, AI 문구 생성 버튼으로 즉시 반영할 수 있다.
- AI 문구를 반영해도 아직 상품이 승인된 것은 아니다.

### 승인 요청
- 상품 입력이 완료되면 직원은 승인 요청을 수행한다.
- 이 시점에 상품 상태는 `DRAFT -> PENDING`으로 변경된다.
- 승인 요청 후 상품은 관리자 승인 요청 리스트에서 검토된다.

### 관리자 승인 요청 리스트
- 관리자는 `PENDING` 상태 상품 목록을 조회한다.
- 관리자는 상품 기본 정보와 상세 설명을 검토한 뒤 승인 또는 반려한다.
- 승인 시 `APPROVED`, 반려 시 `REJECTED`로 변경된다.
- 승인 완료 상품은 관리자 또는 운영자가 재검토를 위해 다시 승인 대기 또는 반려 상태로 변경할 수 있다.

## 8. API 유스케이스
### 상품 생성
- `POST /api/v1/products`
- request: `ProductCreateRequest`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - `categoryId`, `name`, `description`, `actorId`를 request body로 받는다.
  - `description`은 선택 값이다.
  - `name`은 필수이고 최대 100자다.
  - `description`은 최대 2000자다.
  - 생성 시 상품 상태는 `DRAFT`로 시작한다.
  - 생성 시 `CREATED` 이력을 함께 저장한다.
  - 카테고리는 `deleted = false`, `status = ACTIVE` 상태여야 한다.
  - 상품명은 상품 등록 화면에서 직접 입력한다.
  - 카테고리는 사전 생성된 카테고리 목록에서 선택한다.
  - 옵션은 선택된 카테고리에 연결된 목록에서 화면에서 선택한다.
  - 선택한 옵션값 ID 목록은 `selectedOptionItemIds`로 request body에 포함한다.
  - 이미지는 선택 값이며, 생성 직후 `FileAttachment` 메타데이터를 별도 등록한다.

### 상품 목록 조회
- `GET /api/v1/products`
- query:
  - `page`
  - `size`
  - `sort`
  - `categoryId`
  - `status`
  - `approvedOnly`
  - `myOnly`
- response: `BaseResponseEntity<PageResponse<ProductResponse>>`
- 규칙:
  - 목록 조회는 `deleted = false` 기준으로만 수행한다.
  - `categoryId`, `status`는 필요 시 필터로 사용한다.
  - `approvedOnly=true`면 승인 완료 상품만 조회한다.
  - `myOnly=true`면 현재 로그인한 사용자가 생성한 상품만 조회한다.

### 상품 상세 조회
- `GET /api/v1/products/{productId}`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - 삭제되지 않은 상품만 조회할 수 있다.

### 상품 기본 정보 수정
- `PUT /api/v1/products/{productId}`
- request: `ProductUpdateRequest`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - `categoryId`, `name`, `description`, `actorId`를 request body로 받는다.
  - `description`은 선택 값이다.
  - `name`은 필수이고 최대 100자다.
  - `description`은 최대 2000자다.
  - `selectedOptionItemIds`가 포함되면 선택 항목 스냅샷도 함께 갱신한다.
  - 상태 변경은 이 API에서 처리하지 않는다.
  - 실제 변경이 있을 때만 `UPDATED` 이력을 함께 저장한다.
  - 카테고리는 `deleted = false` 상태여야 한다.

### 상품 승인 요청
- `PATCH /api/v1/products/{productId}/status`
- request: `ProductStatusUpdateRequest`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - 직원은 `status = PENDING`으로 요청한다.
  - 직원은 `DRAFT`, `REJECTED` 상태에서만 승인 요청할 수 있다.
  - 관리자 또는 운영자는 `APPROVED` 상태 상품을 다시 `PENDING`으로 변경할 수 있다.
  - `SUBMITTED` 이력을 함께 저장한다.

### 상품 설명 AI 초안 생성
- `POST /api/v1/products/{productId}/description/generate`
- request: `ProductDescriptionGenerateRequest`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - request는 `actorId`, `productName`, `categoryName`, `optionSummary`, `featureKeywords`를 사용한다.
  - `featureKeywords`는 비어 있을 수 없다.
  - 기본 프론트 흐름에서는 `optionSummary`를 빈 문자열로 전달할 수 있다.
  - 생성된 설명이 실제 반영되면 `UPDATED` 이력을 남기고 `reason`은 `"AI 설명 초안 생성"`으로 저장한다.

### 상품 승인 또는 반려
- `PATCH /api/v1/products/{productId}/status`
- request: `ProductStatusUpdateRequest`
- response: `BaseResponseEntity<ProductResponse>`
- 규칙:
  - 관리자는 `PENDING` 상태 상품을 `APPROVED` 또는 `REJECTED`로 처리할 수 있다.
  - 관리자 또는 운영자는 `APPROVED` 상태 상품을 `REJECTED`로 변경할 수 있다.
  - `REJECTED` 변경 시 `reason`은 필수다.
  - `APPROVED`, `REJECTED`, `INACTIVATED` 이력을 함께 저장한다.

### 승인 요청 리스트 조회
- `GET /api/v1/products?status=PENDING`
- response: `BaseResponseEntity<PageResponse<ProductResponse>>`
- 규칙:
  - 관리자는 이 목록을 별도 승인 요청 관리 화면에서 조회한다.

### 상품 삭제
- `DELETE /api/v1/products/{productId}`
- response: `BaseResponseEntity<Void>`
- 규칙:
  - hard delete로 처리한다.
  - `DRAFT`, `PENDING`, `REJECTED` 상태에서만 삭제할 수 있다.
  - 삭제 시 상품 이력, 선택 항목 스냅샷, 상품 대상 파일 메타데이터를 함께 정리한다.

### 상품 이력 목록 조회
- `GET /api/v1/products/{productId}/histories`
- response: `BaseResponseEntity<List<ProductHistoryResponse>>`
- 규칙:
  - 상품 이력은 생성, 수정, 상태 변경 흐름을 시간순으로 조회한다.

## 9. 패키지 배치
`Product` 도메인은 `com.hyeon.guardrail.product` 하위에 배치한다.

```text
com.hyeon.guardrail.product
├── domain
│   ├── Product.java
│   ├── ProductStatus.java
│   ├── ProductHistory.java
│   ├── ProductHistoryType.java
│   └── ProductSelectedOption.java
├── repository
│   ├── ProductRepository.java
│   ├── ProductHistoryRepository.java
│   ├── ProductRepositoryQuery.java
│   ├── ProductHistoryRepositoryQuery.java
│   ├── ProductSelectedOptionRepository.java
│   └── ProductSelectedOptionRepositoryQuery.java
├── service
│   ├── ProductService.java
│   ├── ProductHistoryService.java
│   ├── ProductSelectedOptionService.java
│   └── ProductStatusTransitionService.java
├── dto
│   ├── ProductCreateRequest.java
│   ├── ProductUpdateRequest.java
│   ├── ProductStatusUpdateRequest.java
│   ├── ProductResponse.java
│   ├── ProductHistoryResponse.java
│   ├── ProductSelectedOptionResponse.java
│   └── ProductDescriptionGenerateRequest.java
└── controller
    └── ProductController.java
```
