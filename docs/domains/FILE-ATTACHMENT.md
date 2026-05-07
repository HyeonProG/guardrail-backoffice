# FileAttachment 도메인

## 1. 책임
`FileAttachment`는 상품 이미지 같은 파일 메타데이터와 저장 위치를 관리한다.

파일은 다른 도메인과 JPA 연관관계를 맺지 않고, `targetType`과 `targetId` 값으로만 연결한다.
범위상 상품 이미지만 관리하며, 실제 바이너리 저장은 S3 구현체가 담당한다.

## 2. 주요 엔티티
### FileAttachment
파일 첨부 메타데이터 본체 엔티티다.

관리 대상:
- 파일 UUID PK
- 대상 타입
- 대상 ID
- 저장 파일명
- 원본 파일명
- 공개 접근 경로
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
파일 운영 상태를 정의한다.

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
| `filePath` | 공개 접근 경로 |
| `fileSize` | 파일 크기 |
| `contentType` | 콘텐츠 타입 |
| `sortOrder` | 표시 순서 |
| `status` | 파일 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

## 5. 설계 규칙
- PK는 UUID를 사용한다.
- `FileAttachment`는 `SoftDeleteEntity`를 상속한다.
- 파일 연결은 객체 연관관계 대신 `targetType`, `targetId` 조합으로만 표현한다.
- 상품 이미지는 `targetType = PRODUCT`로 관리한다.
- 조회 기본 조건은 `deleted = false`, `status = ACTIVE`다.
- `INACTIVE`는 운영상 비활성, `deleted = true`는 soft delete를 의미한다.
- 허용 콘텐츠 타입은 `image/jpeg`, `image/png`, `image/webp`다.
- 최대 파일 크기는 10MB다.
- 파일 업로드는 `FileStorageService` 추상화를 통해 수행하고, 운영 구현은 `S3FileStorageService`가 담당한다.
- `filePath`는 외부에서 접근 가능한 저장 경로다.
- `sortOrder = 1`인 파일을 대표 이미지로 사용한다.
- 같은 `targetType`, `targetId` 범위에서 `sortOrder`는 중복되지 않아야 한다.
- 삭제 시 메타데이터는 soft delete하고, 물리 파일 정리는 storage 구현 정책에 따른다.

## 6. API 유스케이스
### 파일 첨부 생성
- `POST /api/v1/file-attachments`
- request: `FileAttachmentCreateRequest`
- response: `BaseResponseEntity<FileAttachmentResponse>`

### 파일 업로드
- `POST /api/v1/file-attachments/upload`
- form-data:
  - `targetType`
  - `targetId`
  - `sortOrder`
  - `file`
- response: `BaseResponseEntity<FileAttachmentResponse>`

### 파일 첨부 단건 조회
- `GET /api/v1/file-attachments/{fileAttachmentId}`
- response: `BaseResponseEntity<FileAttachmentResponse>`

### 대상별 파일 목록 조회
- `GET /api/v1/file-attachments?targetType=PRODUCT&targetId={uuid}`
- response: `BaseResponseEntity<List<FileAttachmentResponse>>`

### 파일 첨부 수정
- `PUT /api/v1/file-attachments/{fileAttachmentId}`
- request: `FileAttachmentUpdateRequest`
- response: `BaseResponseEntity<FileAttachmentResponse>`

### 파일 첨부 삭제
- `DELETE /api/v1/file-attachments/{fileAttachmentId}`
- response: `BaseResponseEntity<Void>`

## 7. 패키지 배치
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
│   ├── FileAttachmentService.java
│   ├── FileStorageService.java
│   └── S3FileStorageService.java
├── support
│   └── StoredFileResult.java
├── dto
│   ├── FileAttachmentCreateRequest.java
│   ├── FileAttachmentUpdateRequest.java
│   └── FileAttachmentResponse.java
└── controller
    └── FileAttachmentController.java
```
