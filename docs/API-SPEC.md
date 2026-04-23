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
  "success": true,
  "data": {},
  "message": null
}
```

필드 설명:

| 필드 | 설명 |
|------|------|
| `success` | 요청 성공 여부 |
| `data` | 응답 데이터 |
| `message` | 부가 메시지 |

데이터가 없는 성공 응답은 `data`를 `null`로 반환한다.

```json
{
  "success": true,
  "data": null,
  "message": "요청이 정상 처리되었습니다."
}
```

컬렉션 데이터가 없는 경우에는 `null`이 아니라 빈 배열을 반환한다.

```json
{
  "success": true,
  "data": [],
  "message": null
}
```

## 5. 공통 에러 응답
에러 응답은 아래 형식을 사용한다.

```json
{
  "success": false,
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

필드 설명:

| 필드 | 설명 |
|------|------|
| `success` | 요청 성공 여부 |
| `code` | 애플리케이션 에러 코드 |
| `message` | 사용자 또는 개발자가 이해할 수 있는 에러 메시지 |

에러 코드 규칙:
- 에러 코드는 enum으로 관리한다.
- 에러 코드는 대문자 스네이크 케이스를 사용한다.
- 에러 코드는 도메인 또는 상황을 식별할 수 있어야 한다.
- 예: `USER_NOT_FOUND`, `DUPLICATED_EMAIL`, `INVALID_PRODUCT_STATUS`

## 6. HTTP 상태 코드 기준
| 상태 코드 | 사용 기준 |
|-----------|----------|
| `200 OK` | 조회, 수정, 삭제 요청 성공 |
| `201 Created` | 리소스 생성 성공 |
| `400 Bad Request` | 요청 값 검증 실패 |
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | 권한 없음 |
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
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "message": null
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
- `ProductApprovalRequest`
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
성공 응답의 `message`는 기본적으로 `null`을 사용한다.

규칙:
- 조회 API는 `message = null`을 사용한다.
- 생성, 수정, 삭제처럼 사용자에게 처리 결과를 명확히 알려야 하는 경우에만 message를 사용한다.
- message는 클라이언트 로직 분기 기준으로 사용하지 않는다.

## 15. Swagger/OpenAPI 기준
API 문서는 Swagger/OpenAPI로 제공한다.

규칙:
- OpenAPI 문서 경로는 `/api-docs`를 사용한다.
- API 문서는 controller와 DTO 기준으로 작성한다.
- Entity는 Swagger 문서에 직접 노출하지 않는다.
- 인증이 필요한 API에는 Bearer token 보안 스키마를 명시한다.
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
- 도메인 상태 전이 검증은 service 또는 entity에서 수행한다.
