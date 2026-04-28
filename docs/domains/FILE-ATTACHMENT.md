# FileAttachment 도메인

## 1. 책임
`FileAttachment`는 상품 이미지 포함 파일 자원을 관리한다.

파일은 특정 도메인 엔티티와 객체 연관관계를 맺지 않고, `targetType`과 `targetId`를 통해 연결한다.

현재 범위에서 상품 이미지는 운영자가 직접 업로드하고 관리한다.

## 2. 주요 엔티티
### FileAttachment
파일 첨부 본체 엔티티다.

관리 대상:
- 파일 UUID PK
- 대상 타입
- 대상 ID
- 파일명
- 원본 파일명
- 저장 위치
- 파일 크기
- 콘텐츠 타입
- 정렬 순서
- 파일 상태
- 생성일시, 수정일시, 삭제 여부

## 3. Enum
### FileTargetType
파일이 연결되는 대상 타입을 정의한다.

값:
- `PRODUCT`

### FileStatus
파일 상태를 정의한다.

값:
- `ACTIVE`
- `INACTIVE`

## 4. 필드 초안
### FileAttachment
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `targetType` | 파일 대상 타입 |
| `targetId` | 파일 대상 UUID |
| `fileName` | 저장 파일명 |
| `originalFileName` | 원본 파일명 |
| `filePath` | 저장 위치 |
| `fileSize` | 파일 크기 |
| `contentType` | 콘텐츠 타입 |
| `sortOrder` | 정렬 순서 |
| `status` | 파일 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

## 5. 설계 규칙
- PK는 UUID를 사용한다.
- `FileAttachment`는 `SoftDeleteEntity`를 상속한다.
- 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- 파일은 대상 도메인과 객체 연관관계로 참조하지 않는다.
- 파일 연결은 `targetType`과 `targetId` 조합으로 표현한다.
- 상품 이미지는 `targetType = PRODUCT`로 관리한다.
- `targetId`에는 대상 도메인의 UUID 값을 저장한다.
- 파일 조회는 기본적으로 `targetType`, `targetId`, `deleted = false`, `status = ACTIVE`를 기준으로 한다.
- `INACTIVE`는 운영상 비활성 상태를 의미하고, `deleted = true`는 soft delete 상태를 의미한다.
- 파일 크기와 콘텐츠 타입 검증은 service 계층에서 수행한다.
- 허용할 콘텐츠 타입은 `image/jpeg`, `image/png`, `image/webp`로 제한한다.
- 최대 파일 크기는 10MB로 제한한다.
- 파일 업로드 저장 루트 경로는 `/uploads/products`를 사용한다.
- `filePath`는 반드시 `/uploads/products` 하위 경로여야 한다.
- 파일 순서는 `sortOrder`로 관리한다.
- `sortOrder = 1`인 파일을 대표 파일로 본다.
- 같은 `targetType`, `targetId` 안에서 `sortOrder`는 중복될 수 없다.
- 파일 메타데이터 삭제와 실제 물리 파일 삭제는 별도 정책으로 다룬다.
- 현재 범위에서는 상품 이미지를 AI 생성 입력값으로 사용하지 않는다.
- 현재 범위에서는 AI가 추가 상품 이미지를 생성하거나 이미지 위에 설명 문구를 합성하지 않는다.

## 6. 패키지 배치
`FileAttachment` 도메인은 `com.hyeon.guardrail.file` 하위에 배치한다.

```text
com.hyeon.guardrail.file
├── domain
│   ├── FileAttachment.java
│   ├── FileTargetType.java
│   └── FileStatus.java
├── repository
│   ├── FileAttachmentRepository.java
│   └── FileAttachmentRepositoryQuery.java
├── service
│   └── FileAttachmentService.java
├── dto
│   ├── FileUploadRequest.java
│   └── FileResponse.java
└── controller
    └── FileAttachmentController.java
```
