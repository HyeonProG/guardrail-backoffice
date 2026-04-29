# ProductContent 도메인

## 1. 책임
`ProductContent`는 상품 등록 페이지에서 사용할 AI 기반 상세 설명 초안 생성과 초안 이력 관리를 담당한다.

이 도메인은 AI가 생성한 설명 초안과 운영자의 수정 이력을 저장한다.  
최종 승인 책임은 `Product` 도메인의 승인 요청 흐름에 있으며, `ProductContent`는 설명 초안을 만들어 상품 폼에 적용하는 보조 역할을 가진다.

현재 범위에서 AI는 이미지 생성이나 이미지 편집을 수행하지 않고, 상품 설명 텍스트 초안 생성만 담당한다.

## 2. 주요 엔티티
### ProductContentDraft
상품 설명 초안 엔티티다.

관리 대상:
- 상품 설명 초안 UUID PK
- 상품 ID
- 설명 원문
- 설명 생성 출처
- 설명 초안 상태
- 생성 요청자 ID
- 생성일시, 수정일시, 삭제 여부

### ProductContentHistory
상품 설명 생성, 수정, 적용, 재생성 이력을 관리한다.

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

규칙:
- 현재 원하는 백오피스 플로우의 기본 사용 상태는 `GENERATED`다.
- `READY_FOR_APPROVAL`, `APPROVED`, `REJECTED`는 기존 구현과 호환을 위해 유지하지만, 1차 등록 화면 플로우에서는 상품 승인 상태보다 우선하지 않는다.

### ProductContentHistoryType
상품 설명 이력 유형을 정의한다.

값:
- `GENERATED`
- `EDITED`
- `APPLIED`
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
| `approvedByActorId` | 최종 승인자 또는 반려자 UUID |
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
- 상품은 객체 연관관계로 참조하지 않고 `productId` 값으로만 저장한다.
- 이력은 객체 연관관계로 초안과 사용자를 참조하지 않고 `draftId`, `actorId` 값으로만 저장한다.
- AI 생성 결과는 `ProductContentDraft`에 영속 저장한다.
- 설명 생성 입력값은 상품명, 카테고리, 옵션 요약, 특징 키워드로 고정한다.
- 상품 이미지는 `FileAttachment` 도메인에서 운영자가 직접 등록한다.
- 현재 범위에서는 업로드된 이미지를 AI 설명 생성 입력으로 사용하지 않는다.
- AI가 생성한 결과를 그대로 저장하지 않고, 운영자가 검토 후 직접 적용 여부를 결정한다.
- `적용하기`는 선택한 초안 내용을 현재 `Product.description`에 반영하는 동작이다.
- 초안을 적용해도 상품 승인 요청이 자동으로 발생하지 않는다.
- 최종 승인 책임은 `Product` 도메인의 승인 요청 흐름에 있다.
- `ProductContentHistory`는 생성, 수정, 적용, 재생성 흐름을 기록한다.

## 6. 백오피스 화면 흐름
### AI 설명 생성 팝업
- 직원이 상품 등록 화면의 상세 설명 영역에서 AI 설명 생성 팝업을 연다.
- 팝업에서 특징 키워드를 입력한다.
- 시스템은 상품명, 카테고리명, 선택 옵션 요약, 특징 키워드를 기준으로 설명 초안을 생성한다.

### AI 설명 결과 확인
- 직원은 생성된 설명 초안을 확인한다.
- 직원은 초안을 그대로 사용하거나 수정한 뒤 사용할 수 있다.

### 적용하기
- 직원이 `적용하기`를 누르면 선택한 초안 내용이 상품 등록 폼의 상세 설명 필드에 반영된다.
- 이 시점에도 상품은 아직 승인 요청되지 않은 상태다.

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
  - 생성 시 `GENERATED` 또는 `REGENERATED` 이력을 함께 저장한다.

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

### 설명 초안 적용
- `PATCH /api/v1/products/{productId}/contents/{draftId}/apply`
- request: `ProductContentApplyRequest`
- response: `BaseResponseEntity<ProductContentDraftResponse>`
- 규칙:
  - `actorId`를 request body로 받는다.
  - 선택한 초안 내용을 현재 `Product.description`에 반영한다.
  - 적용 시 `APPLIED` 이력을 함께 저장한다.
  - 적용은 승인 요청이 아니며, 상품 상태를 변경하지 않는다.

### 설명 이력 조회
- `GET /api/v1/products/{productId}/contents/{draftId}/histories`
- response: `BaseResponseEntity<List<ProductContentHistoryResponse>>`
- 규칙:
  - 생성, 수정, 적용, 재생성 흐름을 시간순으로 조회한다.

### 설명 초안 삭제
- `DELETE /api/v1/products/{productId}/contents/{draftId}`
- response: `BaseResponseEntity<Void>`
- 규칙:
  - soft delete로 처리한다.

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
│   ├── ProductContentApplyRequest.java
│   ├── ProductContentSubmitRequest.java
│   ├── ProductContentApproveRequest.java
│   ├── ProductContentRejectRequest.java
│   ├── ProductContentDraftResponse.java
│   └── ProductContentHistoryResponse.java
└── controller
    └── ProductContentController.java
```
