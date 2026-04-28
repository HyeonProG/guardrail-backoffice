# ProductContent 도메인

## 1. 책임
`ProductContent`는 AI 기반 상품 설명 생성, 운영자 검수, 관리자 승인 반영 흐름을 관리한다.

이 도메인은 AI가 생성한 설명 초안과 검수/승인 이력을 저장하고, 최종 승인된 설명을 `Product.description`에 반영하는 책임을 가진다.

현재 범위에서 AI는 이미지 생성이나 이미지 편집을 수행하지 않고, 상품 설명 텍스트 초안 생성만 담당한다.

## 2. 주요 엔티티
### ProductContentDraft
상품 설명 초안 엔티티다.

관리 대상:
- 상품 설명 초안 UUID PK
- 상품 ID
- 설명 원문
- 설명 생성 출처
- 설명 상태
- 생성 요청자 ID
- 검수 제출자 ID
- 최종 승인자 ID
- 반려 사유
- 생성일시, 수정일시, 삭제 여부

### ProductContentHistory
상품 설명 생성/검수/승인 이력을 관리한다.

관리 대상:
- 상품 설명 이력 UUID PK
- 상품 설명 초안 ID
- 상품 ID
- 처리 유형
- 처리자 ID
- 처리 사유
- 생성일시, 수정일시

## 3. Enum
### ProductContentSource
상품 설명 초안 생성 출처를 정의한다.

값:
- `AI`
- `MANUAL`

규칙:
- `AI`는 설명 생성 요청으로 만들어진 초안을 의미한다.
- `MANUAL`은 운영자가 설명 초안을 처음부터 직접 작성한 경우에만 사용한다.
- 현재 1차 범위의 기본 흐름은 `AI` 초안 생성 기준으로 설계한다.

### ProductContentStatus
상품 설명 초안 상태를 정의한다.

값:
- `GENERATED`
- `READY_FOR_APPROVAL`
- `APPROVED`
- `REJECTED`

### ProductContentHistoryType
상품 설명 이력 유형을 정의한다.

값:
- `GENERATED`
- `EDITED`
- `SUBMITTED`
- `APPROVED`
- `REJECTED`
- `REGENERATED`

## 4. 필드 초안
### ProductContentDraft
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `productId` | 상품 UUID |
| `content` | 상품 설명 초안 본문 |
| `source` | 설명 생성 출처 |
| `status` | 설명 초안 상태 |
| `requestedByActorId` | 생성 요청자 UUID |
| `submittedByActorId` | 검수 제출자 UUID |
| `approvedByActorId` | 최종 승인자 UUID |
| `rejectReason` | 반려 사유 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

### ProductContentHistory
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `draftId` | 상품 설명 초안 UUID |
| `productId` | 상품 UUID |
| `actorId` | 처리자 UUID |
| `type` | 설명 이력 유형 |
| `reason` | 처리 사유 또는 반려 사유 |
| `createdAt` | 생성일시, `BaseEntity` 상속 |
| `updatedAt` | 수정일시, `BaseEntity` 상속 |

## 5. 설계 규칙
- 모든 엔티티의 PK는 UUID를 사용한다.
- `ProductContentDraft`는 `SoftDeleteEntity`를 상속한다.
- `ProductContentHistory`는 `BaseEntity`를 상속한다.
- 상품은 객체 연관관계로 참조하지 않는다.
- 상품 참조는 `productId`로 상품 UUID 값을 저장한다.
- 이력은 객체 연관관계로 초안과 사용자를 참조하지 않는다.
- `draftId`, `actorId`는 UUID 값으로만 저장한다.
- AI 생성 결과는 `ProductContentDraft`에 영속 저장한다.
- 설명 생성 입력값은 상품명, 카테고리, 옵션 요약, 특징 키워드로 고정한다.
- 상품 이미지는 `FileAttachment` 도메인에서 운영자가 직접 등록한다.
- 현재 범위에서는 업로드된 이미지를 AI 설명 생성 입력으로 사용하지 않는다.
- 현재 범위에서는 AI가 추가 이미지를 생성하거나 이미지 위에 설명 문구를 합성하지 않는다.
- 최종 승인 전까지 AI 생성 초안은 `Product.description`에 반영하지 않는다.
- 관리자 승인 완료 시 승인된 초안 내용을 `Product.description`에 반영한다.
- 상품 하나에는 여러 개의 설명 초안이 누적될 수 있다.
- 현재 승인 대기 또는 최종 승인 기준 초안은 서비스 규칙으로 판별한다.
- 운영자는 생성된 초안을 수정한 뒤 승인 요청할 수 있다.
- 관리자는 승인 또는 반려만 수행한다.
- `REJECTED` 상태에서는 `rejectReason`이 필수다.
- 반려된 초안은 재수정 후 다시 승인 요청할 수 있다.
- 재생성 요청은 새로운 초안 엔티티를 추가하는 방식으로 처리한다.
- 재생성 시 기존 승인 초안 상태를 변경하지 않고, 동일 상품 기준으로 새 `GENERATED` 초안을 추가한다.
- `ProductContentHistory`는 생성, 수정, 제출, 승인, 반려, 재생성 흐름을 기록한다.

## 6. 상태 전이 규칙
상품 설명 초안 상태는 아래 흐름을 따른다.

```text
GENERATED -> READY_FOR_APPROVAL -> APPROVED
GENERATED -> READY_FOR_APPROVAL -> REJECTED
REJECTED -> READY_FOR_APPROVAL
```

규칙:
- `GENERATED`는 AI 생성 직후 또는 재생성 직후 상태다.
- `READY_FOR_APPROVAL`은 운영자가 검수 후 관리자 승인 요청한 상태다.
- `APPROVED`는 관리자 승인이 끝났고 상품 설명에 반영된 상태다.
- `REJECTED`는 관리자 반려 상태다.
- 동일 상품에 대한 재생성 요청은 기존 초안 상태 전이가 아니라 새 `GENERATED` 초안 추가로 처리한다.

## 7. API 유스케이스
### 설명 초안 생성
- `POST /api/v1/products/{productId}/contents/generate`
- request: `ProductContentGenerateRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `actorId`, `productName`, `categoryName`, `optionSummary`, `featureKeywords`를 request body로 받는다.
  - `featureKeywords`는 설명 생성에 사용할 핵심 특징 목록이다.
  - 상품 이미지는 설명 생성 request에 포함하지 않는다.
  - 생성 출처는 `AI`로 저장한다.
  - 생성 시 `GENERATED` 상태로 시작한다.
  - 생성 시 `GENERATED` 이력을 함께 저장한다.

### 설명 초안 목록 조회
- `GET /api/v1/products/{productId}/contents`
- query:
  - `status`
- response: `BaseResponseEntity<List<ProductContentDraftResponse>>`
- 규칙:
  - 목록 조회는 미삭제 기준으로만 수행한다.
  - 목록은 생성일시 내림차순으로 조회한다.

### 설명 초안 상세 조회
- `GET /api/v1/products/{productId}/contents/{draftId}`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - 삭제되지 않은 초안만 조회할 수 있다.

### 설명 초안 수정
- `PUT /api/v1/products/{productId}/contents/{draftId}`
- request: `ProductContentUpdateRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `content`, `actorId`를 request body로 받는다.
  - 운영자 검수 수정 흐름으로 처리한다.
  - 수정 시 `EDITED` 이력을 함께 저장한다.

### 설명 승인 요청
- `PATCH /api/v1/products/{productId}/contents/{draftId}/submit`
- request: `ProductContentSubmitRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `actorId`를 request body로 받는다.
  - `GENERATED`, `REJECTED` 상태에서만 승인 요청할 수 있다.
  - 요청 시 상태를 `READY_FOR_APPROVAL`로 변경한다.
  - `SUBMITTED` 이력을 함께 저장한다.

### 설명 승인
- `PATCH /api/v1/products/{productId}/contents/{draftId}/approve`
- request: `ProductContentApproveRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `actorId`를 request body로 받는다.
  - `READY_FOR_APPROVAL` 상태에서만 승인할 수 있다.
  - 승인 시 상태를 `APPROVED`로 변경한다.
  - 승인된 `content`를 `Product.description`에 반영한다.
  - `APPROVED` 이력을 함께 저장한다.

### 설명 반려
- `PATCH /api/v1/products/{productId}/contents/{draftId}/reject`
- request: `ProductContentRejectRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `actorId`, `reason`을 request body로 받는다.
  - `READY_FOR_APPROVAL` 상태에서만 반려할 수 있다.
  - 반려 시 상태를 `REJECTED`로 변경한다.
  - `reason`은 필수다.
  - `REJECTED` 이력을 함께 저장한다.

### 설명 이력 조회
- `GET /api/v1/products/{productId}/contents/{draftId}/histories`
- response: `BaseResponseEntity<List<ProductContentHistoryResponse>>`
- 규칙:
  - 생성, 수정, 제출, 승인, 반려, 재생성 흐름을 시간순으로 조회한다.

### 설명 초안 삭제
- `DELETE /api/v1/products/{productId}/contents/{draftId}`
- response: `BaseResponseEntity<Void>`
- 규칙:
  - soft delete로 처리한다.
  - 최종 승인 반영본 삭제와 상품 설명 원복은 별도 정책으로 다룬다.

## 8. 패키지 배치
`ProductContent` 도메인은 `com.hyeon.guardrail.productcontent` 하위에 배치한다.

```text
com.hyeon.guardrail.productcontent
├── domain
│   ├── ProductContentDraft.java
│   ├── ProductContentHistory.java
│   ├── ProductContentSource.java
│   ├── ProductContentStatus.java
│   └── ProductContentHistoryType.java
├── repository
│   ├── ProductContentDraftRepository.java
│   ├── ProductContentHistoryRepository.java
│   ├── ProductContentDraftRepositoryQuery.java
│   └── ProductContentHistoryRepositoryQuery.java
├── service
│   └── ProductContentService.java
├── dto
│   ├── ProductContentGenerateRequest.java
│   ├── ProductContentUpdateRequest.java
│   ├── ProductContentSubmitRequest.java
│   ├── ProductContentApproveRequest.java
│   ├── ProductContentRejectRequest.java
│   ├── ProductContentDraftResponse.java
│   └── ProductContentHistoryResponse.java
└── controller
    └── ProductContentController.java
```
