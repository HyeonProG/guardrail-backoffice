# ProductOption 도메인

## 1. 책임
`ProductOption`은 상품 등록 전에 미리 관리되는 옵션 마스터 데이터를 관리한다.

옵션은 특정 상품에 종속되지 않는다.  
운영자는 `상품 옵션 관리` 1depth 메뉴에서 옵션 그룹과 옵션값을 먼저 등록하고, 이후 `상품 등록` 화면에서 등록된 옵션을 선택해 상품 설명 생성과 상품 구성에 활용한다.

## 2. 주요 엔티티
### ProductOption
옵션 그룹 본체 엔티티다.

관리 대상:
- 옵션 그룹 UUID PK
- 옵션 그룹명
- 정렬 순서
- 옵션 그룹 상태
- 생성일시, 수정일시, 삭제 여부

예:
- 색상
- 사이즈
- 용량

### ProductOptionItem
옵션 그룹에 속한 선택값 엔티티다.

관리 대상:
- 옵션 선택값 UUID PK
- 옵션 그룹 ID
- 옵션값명
- 추가 금액
- 정렬 순서
- 옵션값 상태
- 생성일시, 수정일시, 삭제 여부

예:
- 색상 -> 블랙, 화이트
- 사이즈 -> 255, 260, 265

## 3. Enum
### ProductOptionStatus
옵션 그룹과 옵션값 상태를 정의한다.

값:
- `ACTIVE`
- `INACTIVE`

## 4. 필드 초안
### ProductOption
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `name` | 옵션 그룹명 |
| `sortOrder` | 옵션 그룹 정렬 순서 |
| `status` | 옵션 그룹 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

### ProductOptionItem
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `productOptionId` | 옵션 그룹 UUID |
| `name` | 옵션값명 |
| `additionalPrice` | 추가 금액 |
| `sortOrder` | 옵션값 정렬 순서 |
| `status` | 옵션값 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

## 5. 설계 규칙
- 모든 엔티티의 PK는 UUID를 사용한다.
- `ProductOption`, `ProductOptionItem`은 `SoftDeleteEntity`를 상속한다.
- 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- `ProductOption`은 특정 `Product`를 직접 참조하지 않는다.
- `ProductOption`은 객체 연관관계로 `ProductOptionItem`을 참조하지 않는다.
- 옵션값 참조는 `productOptionId`로 옵션 그룹 UUID 값을 저장한다.
- 같은 옵션 그룹명은 미삭제 기준으로 중복될 수 없다.
- 같은 옵션 그룹 `sortOrder`는 미삭제 기준으로 중복될 수 없다.
- 같은 옵션 그룹 안에서 옵션값명은 미삭제 기준으로 중복될 수 없다.
- 같은 옵션 그룹 안에서 옵션값 `sortOrder`는 미삭제 기준으로 중복될 수 없다.
- `INACTIVE`는 운영상 비활성 상태를 의미하고, `deleted = true`는 soft delete 상태를 의미한다.
- 옵션 그룹 기본 정보 수정과 상태 변경은 별도 유스케이스로 분리한다.
- 옵션값 기본 정보 수정과 상태 변경은 별도 유스케이스로 분리한다.
- `additionalPrice`는 0 이상만 허용한다.
- `ACTIVE` 상태 옵션과 옵션값만 상품 등록/상품 설명 생성 화면에서 선택 대상으로 사용한다.
- 상품은 옵션 마스터를 직접 영속 연결하지 않는다.
- 현재 범위에서는 상품 등록/상품 설명 생성 화면에서 선택한 옵션값을 조합해 화면과 설명 생성 입력값에만 사용한다.

## 6. 백오피스 화면 흐름 기준
### 상품 옵션 관리
- 1depth 메뉴에서 접근한다.
- 운영자는 여기서 옵션 그룹과 옵션값을 미리 생성한다.
- 이 화면은 상품 등록 화면과 분리한다.

### 상품 등록
- 상품명은 상품 등록 화면에서 직접 입력한다.
- 카테고리는 드롭다운 목록에서 선택한다.
- 옵션은 미리 등록된 옵션 그룹/옵션값 목록에서 선택한다.
- 선택한 옵션은 상품 설명 생성 입력의 `optionSummary`와 상품 화면 표시용 데이터로 사용한다.

### 상품 설명 생성
- 키워드는 상품 등록 화면 또는 설명 생성 팝업에서 입력한다.
- 옵션은 텍스트 수동 입력이 아니라 등록된 옵션 목록에서 선택한다.
- 선택된 옵션을 조합해 AI 설명 생성 입력값으로 사용한다.

## 7. API 유스케이스
### 옵션 그룹 생성
- `POST /api/v1/product-options`
- request: `ProductOptionCreateRequest`
- response: `BaseResponseEntity<ProductOptionResponse>`
- 규칙:
  - `name`, `sortOrder`, `status`, `actorId`를 request body로 받는다.
  - 옵션 그룹명과 `sortOrder`는 미삭제 기준으로 중복될 수 없다.

### 옵션 그룹 목록 조회
- `GET /api/v1/product-options`
- query:
  - `status`
- response: `BaseResponseEntity<List<ProductOptionResponse>>`
- 규칙:
  - 목록 조회는 미삭제 기준으로만 수행한다.
  - `status`는 필요 시 필터로 사용한다.

### 옵션 그룹 상세 조회
- `GET /api/v1/product-options/{productOptionId}`
- response: `BaseResponseEntity<ProductOptionResponse>`
- 규칙:
  - 삭제되지 않은 옵션 그룹만 조회할 수 있다.
  - 옵션 그룹 조회 시 해당 옵션값 목록을 함께 포함할 수 있다.

### 옵션 그룹 기본 정보 수정
- `PUT /api/v1/product-options/{productOptionId}`
- request: `ProductOptionUpdateRequest`
- response: `BaseResponseEntity<ProductOptionResponse>`
- 규칙:
  - `name`, `sortOrder`, `actorId`를 request body로 받는다.
  - 상태 변경은 이 API에서 처리하지 않는다.

### 옵션 그룹 상태 변경
- `PATCH /api/v1/product-options/{productOptionId}/status`
- request: `ProductOptionStatusUpdateRequest`
- response: `BaseResponseEntity<ProductOptionResponse>`
- 규칙:
  - `status`, `actorId`를 request body로 받는다.
  - `ACTIVE`, `INACTIVE`만 허용한다.

### 옵션 그룹 삭제
- `DELETE /api/v1/product-options/{productOptionId}`
- response: `BaseResponseEntity<Void>`
- 규칙:
  - soft delete로 처리한다.

### 옵션값 생성
- `POST /api/v1/product-options/{productOptionId}/items`
- request: `ProductOptionItemCreateRequest`
- response: `BaseResponseEntity<ProductOptionItemResponse>`
- 규칙:
  - `name`, `additionalPrice`, `sortOrder`, `status`, `actorId`를 request body로 받는다.
  - 같은 옵션 그룹 안에서 옵션값명과 `sortOrder`는 중복될 수 없다.
  - `additionalPrice`는 0 이상이어야 한다.

### 옵션값 목록 조회
- `GET /api/v1/product-options/{productOptionId}/items`
- query:
  - `status`
- response: `BaseResponseEntity<List<ProductOptionItemResponse>>`
- 규칙:
  - 목록 조회는 미삭제 기준으로만 수행한다.

### 옵션값 기본 정보 수정
- `PUT /api/v1/product-options/{productOptionId}/items/{productOptionItemId}`
- request: `ProductOptionItemUpdateRequest`
- response: `BaseResponseEntity<ProductOptionItemResponse>`
- 규칙:
  - `name`, `additionalPrice`, `sortOrder`, `actorId`를 request body로 받는다.
  - 상태 변경은 이 API에서 처리하지 않는다.

### 옵션값 상태 변경
- `PATCH /api/v1/product-options/{productOptionId}/items/{productOptionItemId}/status`
- request: `ProductOptionItemStatusUpdateRequest`
- response: `BaseResponseEntity<ProductOptionItemResponse>`
- 규칙:
  - `status`, `actorId`를 request body로 받는다.
  - `ACTIVE`, `INACTIVE`만 허용한다.

### 옵션값 삭제
- `DELETE /api/v1/product-options/{productOptionId}/items/{productOptionItemId}`
- response: `BaseResponseEntity<Void>`
- 규칙:
  - soft delete로 처리한다.

## 8. 패키지 배치
`ProductOption` 도메인은 `com.hyeon.guardrail.productoption` 하위에 배치한다.

```text
com.hyeon.guardrail.productoption
├── domain
│   ├── ProductOption.java
│   ├── ProductOptionItem.java
│   └── ProductOptionStatus.java
├── repository
│   ├── ProductOptionRepository.java
│   ├── ProductOptionItemRepository.java
│   ├── ProductOptionRepositoryQuery.java
│   └── ProductOptionItemRepositoryQuery.java
├── service
│   └── ProductOptionService.java
├── dto
│   ├── ProductOptionCreateRequest.java
│   ├── ProductOptionUpdateRequest.java
│   ├── ProductOptionStatusUpdateRequest.java
│   ├── ProductOptionResponse.java
│   ├── ProductOptionItemCreateRequest.java
│   ├── ProductOptionItemUpdateRequest.java
│   ├── ProductOptionItemStatusUpdateRequest.java
│   └── ProductOptionItemResponse.java
└── controller
    └── ProductOptionController.java
```
