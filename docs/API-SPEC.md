# API 명세 기준

## 1. 목적
이 문서는 Guardrail 프로젝트의 API 설계 기준을 정의한다.

Controller, DTO, 예외 응답, 인증 헤더, 페이징 응답은 이 문서의 기준을 따른다.

## 2. 기본 원칙
- API 경로는 `/api/v1` prefix를 사용한다.
- API 응답은 일관된 공통 포맷을 사용한다.
- Entity를 응답으로 직접 반환하지 않는다.
- 요청과 응답은 DTO를 통해 처리한다.
- 식별자는 UUID 문자열을 사용한다.
- enum 값은 문자열로 주고받는다.
- 인증이 필요한 API는 Authorization 헤더를 사용한다.
- 같은 요청에 대해 동일한 조건에서는 항상 동일한 응답 구조를 반환한다.
- 컬렉션 데이터는 null 대신 빈 배열을 반환한다.

## 3. API 경로 기준
모든 API는 `/api/v1` prefix를 사용한다.

예:

```http
GET /api/v1/users/{userId}
GET /api/v1/products/{productId}
GET /api/v1/categories/{categoryId}
```

## 4. 공통 성공 응답
성공 응답은 아래 형식을 사용한다.

```json
{
  "isSuccess": true,
  "message": "요청이 정상 처리되었습니다.",
  "code": 200,
  "result": {}
}
```

필드 설명:

| 필드 | 설명 |
|------|------|
| `isSuccess` | 요청 성공 여부 |
| `message` | 응답 메시지 |
| `code` | 응답 코드 |
| `result` | 응답 데이터 |

Controller 성공 응답은 기본적으로 `BaseResponseEntity<T>`를 사용한다.

예:

```java
return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS, response);
return new BaseResponseEntity<>(BaseResponseStatus.CREATED, response, "사용자가 생성되었습니다.");
```

데이터가 없는 성공 응답은 `result`를 `null`로 반환한다.

```json
{
  "isSuccess": true,
  "message": "요청이 정상 처리되었습니다.",
  "code": 200,
  "result": null
}
```

컬렉션 데이터가 없는 경우에는 `null`이 아니라 빈 배열을 반환한다.

```json
{
  "isSuccess": true,
  "message": "요청이 정상 처리되었습니다.",
  "code": 200,
  "result": []
}
```

## 5. 공통 에러 응답
에러 응답은 아래 형식을 사용한다.

```json
{
  "isSuccess": false,
  "message": "잘못된 요청입니다.",
  "code": 400,
  "result": null
}
```

필드 설명:

| 필드 | 설명 |
|------|------|
| `isSuccess` | 요청 성공 여부 |
| `message` | 사용자 또는 개발자가 이해할 수 있는 에러 메시지 |
| `code` | 응답 코드 |
| `result` | 실패 응답 결과값, 기본적으로 `null` |

에러 코드 규칙:
- 에러 상태는 `BaseResponseStatus` enum으로 관리한다.
- 기본 공통 실패 상태는 `400`, `401`, `403`, `405`, `500`을 포함한다.
- 도메인별 실패 상태는 필요 시 `BaseResponseStatus`에 확장한다.

## 6. HTTP 상태 코드 기준
| 상태 코드 | 사용 기준 |
|-----------|----------|
| `200 OK` | 조회, 수정, 삭제 요청 성공 |
| `201 Created` | 리소스 생성 성공 |
| `400 Bad Request` | 요청 값 검증 실패 |
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | 권한 없음 |
| `405 Method Not Allowed` | 허용되지 않은 HTTP 메서드 |
| `404 Not Found` | 리소스 없음 |
| `409 Conflict` | 중복 또는 상태 충돌 |
| `500 Internal Server Error` | 서버 내부 오류 |

## 7. 인증 헤더
인증이 필요한 API는 아래 헤더를 사용한다.

```http
Authorization: Bearer {accessToken}
```

규칙:
- access token은 Authorization 헤더로 전달한다.
- refresh token은 토큰 재발급 API의 request body로 전달한다.
- 만료된 access token은 `401 Unauthorized`로 응답한다.
- 권한이 부족한 사용자는 `403 Forbidden`으로 응답한다.

## 7.1 인증 API 기준
### 로그인 API
```http
POST /api/v1/auth/login
```

request body:
- `email`
- `password`
- `deviceType`

response result:
- `accessToken`
- `refreshToken`
- `sessionId`
- `accessTokenExpiredAt`
- `refreshTokenExpiredAt`
- `userId`
- `role`

규칙:
- 로그인 요청의 IP는 서버 요청 정보에서 추출한다.
- 로그인 성공 시 로그인 이력 `SUCCESS`, 인증 세션 `ACTIVE`를 저장한다.
- 로그인 실패 시 로그인 이력 `FAIL`을 저장하고 토큰을 발급하지 않는다.
- 존재하지 않는 이메일 로그인 실패는 로그인 이력을 저장하지 않고 `401 Unauthorized`로 종료한다.

### 토큰 재발급 API
```http
POST /api/v1/auth/token/refresh
```

request body:
- `sessionId`
- `refreshToken`

response result:
- `accessToken`
- `refreshToken`
- `sessionId`
- `accessTokenExpiredAt`
- `refreshTokenExpiredAt`

규칙:
- 세션 상태가 `ACTIVE`여야 한다.
- 세션 만료 시간이 지나지 않아야 한다.
- 입력한 refresh token과 저장된 검증용 해시 값의 검증이 통과해야 한다.
- 성공 시 세션의 refresh token hash, refreshedAt, expiredAt을 갱신한다.

### 로그아웃 API
```http
POST /api/v1/auth/logout
```

request:
- Authorization 헤더의 access token
- request body의 `sessionId`

response result:
- `sessionId`
- `status`

규칙:
- 로그아웃 시 세션을 삭제하지 않고 `REVOKED` 상태로 변경한다.
- `REVOKED` 상태 세션은 토큰 재발급을 허용하지 않는다.

## 7.2 카테고리 API 기준
### 카테고리 생성 API
```http
POST /api/v1/categories
```

request body:
- `parentId`
- `name`

response result:
- `id`
- `parentId`
- `name`
- `status`
- `createdAt`
- `updatedAt`

규칙:
- 생성 시 `status`는 `ACTIVE`로 시작한다.
- `parentId`가 존재하면 부모 카테고리는 `deleted = false` 상태여야 한다.

### 카테고리 목록 조회 API
```http
GET /api/v1/categories?page=0&size=20&sort=createdAt,desc&parentId={parentId}&status=ACTIVE
```

query:
- `page`
- `size`
- `sort`
- `parentId`
- `status`

response result:
- `PageResponse<CategoryResponse>`

규칙:
- 목록 조회는 `deleted = false` 기준으로만 수행한다.
- `parentId`가 없으면 최상위 카테고리 목록 조회를 의미한다.
- `status`는 필요 시 필터로 사용한다.

### 카테고리 상세 조회 API
```http
GET /api/v1/categories/{categoryId}
```

response result:
- `CategoryResponse`

규칙:
- 삭제되지 않은 카테고리만 조회할 수 있다.

### 카테고리 기본 정보 수정 API
```http
PUT /api/v1/categories/{categoryId}
```

request body:
- `parentId`
- `name`

response result:
- `CategoryResponse`

규칙:
- 기본 정보 수정은 `name`, `parentId`만 처리한다.
- 상태 변경은 별도 API에서 처리한다.

### 카테고리 상태 변경 API
```http
PATCH /api/v1/categories/{categoryId}/status
```

request body:
- `status`

response result:
- `CategoryResponse`

규칙:
- `status`는 `ACTIVE`, `INACTIVE`만 허용한다.

### 카테고리 삭제 API
```http
DELETE /api/v1/categories/{categoryId}
```

response result:
- `null`

규칙:
- soft delete로 처리한다.

### 카테고리 복구 API
```http
PATCH /api/v1/categories/{categoryId}/restore
```

response result:
- `CategoryResponse`

규칙:
- `deleted = true` 카테고리만 복구할 수 있다.
- 하위 카테고리 복구 시 부모 카테고리는 `deleted = false` 상태여야 한다.
- 복구 후 `status`는 기존 값을 유지한다.

## 7.3 상품 API 기준
### 상품 생성 API
```http
POST /api/v1/products
```

request body:
- `categoryId`
- `name`
- `description`
- `quantity`
- `actorId`

response result:
- `id`
- `categoryId`
- `name`
- `description`
- `quantity`
- `status`
- `createdAt`
- `updatedAt`

규칙:
- 생성 시 `status`는 `DRAFT`로 시작한다.
- 생성 시 `CREATED` 이력을 저장한다.
- `actorId`는 현재 단계에서 request body로 받는다.
- 카테고리는 `deleted = false`, `status = ACTIVE` 상태여야 한다.

### 상품 목록 조회 API
```http
GET /api/v1/products?page=0&size=20&sort=createdAt,desc&categoryId={categoryId}&status=DRAFT
```

query:
- `page`
- `size`
- `sort`
- `categoryId`
- `status`

response result:
- `PageResponse<ProductResponse>`

규칙:
- 목록 조회는 `deleted = false` 기준으로만 수행한다.
- `categoryId`, `status`는 필요 시 필터로 사용한다.

### 상품 상세 조회 API
```http
GET /api/v1/products/{productId}
```

response result:
- `ProductResponse`

규칙:
- 삭제되지 않은 상품만 조회할 수 있다.

### 상품 기본 정보 수정 API
```http
PUT /api/v1/products/{productId}
```

request body:
- `categoryId`
- `name`
- `description`
- `quantity`
- `actorId`

response result:
- `ProductResponse`

규칙:
- 기본 정보 수정은 `categoryId`, `name`, `description`, `quantity`만 처리한다.
- 상태 변경은 별도 API에서 처리한다.
- 수정 시 `UPDATED` 이력을 저장한다.
- 카테고리는 `deleted = false` 상태여야 한다.

### 상품 상태 변경 API
```http
PATCH /api/v1/products/{productId}/status
```

request body:
- `status`
- `actorId`
- `reason`

response result:
- `ProductResponse`

규칙:
- `status`는 `DRAFT -> PENDING -> APPROVED`, `DRAFT -> PENDING -> REJECTED`, `REJECTED -> PENDING`, `APPROVED -> INACTIVE` 흐름만 허용한다.
- `REJECTED` 변경 시 `reason`은 필수다.
- `SUBMITTED`, `APPROVED`, `REJECTED`, `INACTIVATED` 이력을 저장한다.
- `actorId`는 현재 단계에서 request body로 받는다.

### 상품 삭제 API
```http
DELETE /api/v1/products/{productId}
```

response result:
- `null`

규칙:
- soft delete로 처리한다.

### 상품 이력 목록 조회 API
```http
GET /api/v1/products/{productId}/histories
```

response result:
- `List<ProductHistoryResponse>`

규칙:
- 상품 이력은 생성, 수정, 상태 변경 흐름 기준으로 조회한다.

## 7.4 상품 설명 생성/검수 API 기준
### 설명 초안 생성 API
```http
POST /api/v1/products/{productId}/contents/generate
```

request body:
- `actorId`
- `productName`
- `categoryName`
- `optionSummary`
- `featureKeywords`

response result:
- `ProductContentDraftResponse`

규칙:
- 생성 결과는 `ProductContentDraft`에 저장한다.
- 생성 직후 상태는 `GENERATED`다.
- 생성 출처는 `AI`로 저장한다.
- `featureKeywords`는 설명 생성에 사용할 핵심 특징 목록이다.
- 상품 이미지는 설명 생성 request에 포함하지 않는다.

### 설명 초안 목록 조회 API
```http
GET /api/v1/products/{productId}/contents?status=GENERATED
```

query:
- `status`

response result:
- `List<ProductContentDraftResponse>`

규칙:
- 목록 조회는 `deleted = false` 기준으로만 수행한다.

### 설명 초안 상세 조회 API
```http
GET /api/v1/products/{productId}/contents/{draftId}
```

response result:
- `ProductContentDraftResponse`

규칙:
- 삭제되지 않은 초안만 조회할 수 있다.

### 설명 초안 수정 API
```http
PUT /api/v1/products/{productId}/contents/{draftId}
```

request body:
- `content`
- `actorId`

response result:
- `ProductContentDraftResponse`

규칙:
- 운영자 검수 수정 흐름으로 처리한다.
- 수정 시 `EDITED` 이력을 저장한다.

### 설명 승인 요청 API
```http
PATCH /api/v1/products/{productId}/contents/{draftId}/submit
```

request body:
- `actorId`

response result:
- `ProductContentDraftResponse`

규칙:
- `GENERATED`, `REJECTED` 상태에서만 요청할 수 있다.
- 요청 시 상태를 `READY_FOR_APPROVAL`로 변경한다.
- 요청 시 `SUBMITTED` 이력을 저장한다.

### 설명 승인 API
```http
PATCH /api/v1/products/{productId}/contents/{draftId}/approve
```

request body:
- `actorId`

response result:
- `ProductContentDraftResponse`

규칙:
- `READY_FOR_APPROVAL` 상태에서만 승인할 수 있다.
- 승인 시 `Product.description`에 최종 설명을 반영한다.
- 승인 시 `APPROVED` 이력을 저장한다.

### 설명 반려 API
```http
PATCH /api/v1/products/{productId}/contents/{draftId}/reject
```

request body:
- `actorId`
- `reason`

response result:
- `ProductContentDraftResponse`

규칙:
- `READY_FOR_APPROVAL` 상태에서만 반려할 수 있다.
- `reason`은 필수다.
- 반려 시 `REJECTED` 이력을 저장한다.

### 설명 이력 조회 API
```http
GET /api/v1/products/{productId}/contents/{draftId}/histories
```

response result:
- `List<ProductContentHistoryResponse>`

규칙:
- 생성, 수정, 제출, 승인, 반려, 재생성 흐름을 시간순으로 조회한다.

## 8. UUID Path Variable
리소스 식별자는 UUID를 사용한다.

예:

```http
GET /api/v1/users/{userId}
GET /api/v1/products/{productId}
GET /api/v1/categories/{categoryId}
```

규칙:
- path variable 이름은 `{도메인명}Id` 형식을 사용한다.
- 값은 UUID 문자열이다.
- 잘못된 UUID 형식은 `400 Bad Request`로 응답한다.

## 9. 페이징 응답
목록 조회 API는 페이징 응답을 사용한다.

```json
{
  "isSuccess": true,
  "message": "요청이 정상 처리되었습니다.",
  "code": 200,
  "result": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

규칙:
- page는 0부터 시작한다.
- 기본 size는 20으로 한다.
- 최대 size는 100으로 제한한다.

## 10. 정렬 파라미터 기준
목록 조회 API는 필요한 경우 정렬 파라미터를 사용할 수 있다.

예:

```http
GET /api/v1/products?page=0&size=20&sort=createdAt,desc
```

규칙:
- 정렬 파라미터 이름은 `sort`를 사용한다.
- 형식은 `{field},{direction}`을 사용한다.
- direction은 `asc`, `desc`만 허용한다.
- 정렬 필드는 API별로 허용 목록을 정의한다.
- 허용되지 않은 정렬 필드는 `400 Bad Request`로 응답한다.

## 11. Request/Response DTO 네이밍
DTO 이름은 목적이 드러나도록 작성한다.

예:
- `UserCreateRequest`
- `UserUpdateRequest`
- `UserResponse`
- `ProductCreateRequest`
- `ProductStatusUpdateRequest`
- `ProductContentApproveRequest`
- `ProductResponse`

규칙:
- 요청 DTO는 `Request`로 끝낸다.
- 응답 DTO는 `Response`로 끝낸다.
- Entity 이름을 그대로 노출하지 않고 API 목적을 기준으로 작성한다.

## 12. Enum 응답 기준
enum은 문자열로 요청/응답한다.

예:

```json
{
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

규칙:
- enum 값은 대문자 스네이크 케이스를 사용한다.
- enum 설명이 필요한 경우 response에 별도 display 필드를 추가할 수 있다.

## 13. 날짜/시간 포맷 기준
날짜와 시간은 ISO-8601 형식의 문자열로 응답한다.

예:

```json
{
  "createdAt": "2026-04-23T10:30:00"
}
```

규칙:
- 서버 기본 타임존은 `Asia/Seoul`을 사용한다.
- 날짜/시간 응답은 `yyyy-MM-dd'T'HH:mm:ss` 형식을 사용한다.
- 날짜만 필요한 경우 `yyyy-MM-dd` 형식을 사용한다.

## 14. Success Message 사용 기준
성공 응답의 `message`는 기본적으로 상태값의 기본 메시지를 사용한다.

규칙:
- 기본 성공 메시지는 `BaseResponseStatus`를 따른다.
- 생성, 수정, 삭제처럼 사용자에게 처리 결과를 더 명확히 알려야 하는 경우 message를 재정의할 수 있다.
- message는 클라이언트 로직 분기 기준으로 사용하지 않는다.
- controller 성공 응답 반환 타입은 기본적으로 `BaseResponseEntity<T>`를 사용한다.

## 14.1 Error Message 사용 기준
실패 응답의 `message`는 기본적으로 상태값의 기본 메시지를 사용한다.

규칙:
- 실패 메시지는 `BaseResponseStatus`를 따른다.
- validation, 타입 변환 실패처럼 구체적인 원인이 필요한 경우 message를 재정의할 수 있다.
- 실패 응답의 반환 구조도 `BaseResponseEntity<T>`를 사용한다.

## 15. Swagger/OpenAPI 기준
API 문서는 Swagger/OpenAPI로 제공한다.

규칙:
- OpenAPI 문서 경로는 `/api-docs`를 사용한다.
- Swagger UI 경로는 `/swagger-ui.html`을 사용한다.
- API 문서는 controller와 DTO 기준으로 작성한다.
- Entity는 Swagger 문서에 직접 노출하지 않는다.
- 인증이 필요한 API에는 Bearer token 보안 스키마를 명시한다.
- 문서 버전은 애플리케이션 버전과 동일하게 관리한다.
- controller에는 `@Tag` 설명을 작성한다.
- endpoint에는 `@Operation(summary, description)`을 작성한다.
- request/response DTO와 공통 응답 구조에는 `@Schema(description=...)`를 작성한다.
- API 설명은 실제 동작과 다르지 않게 유지한다.

## 16. Soft Delete 조회 기준
soft delete가 적용되는 데이터는 기본 조회에서 제외한다.

규칙:
- 기본 조회 조건에는 `deleted = false`를 포함한다.
- 삭제된 데이터를 조회해야 하는 경우 별도 관리자 API로 분리한다.
- Auth 이력성 데이터는 soft delete 대신 보존, 상태, 만료 정책을 따른다.

## 17. Validation 기준
요청 값 검증은 request DTO에서 우선 수행한다.

규칙:
- 필수 값은 `@NotNull`, `@NotBlank` 등으로 검증한다.
- 길이 제한은 `@Size`로 검증한다.
- 형식 검증은 DTO 또는 service에서 수행한다.
- 도메인 생성 흐름과 유스케이스 판단은 service에서 수행한다.
- 도메인 상태 전이 검증은 service 또는 entity에서 수행한다.
