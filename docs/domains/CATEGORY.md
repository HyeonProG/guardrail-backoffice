# Category 도메인

## 1. 책임
`Category`는 상품 분류 체계를 관리한다.

상품은 카테고리 UUID를 값으로 참조하며, 카테고리와 상품 간 객체 연관관계는 사용하지 않는다.

## 2. 주요 엔티티
### Category
카테고리 본체 엔티티다.

관리 대상:
- 카테고리 UUID PK
- 상위 카테고리 ID
- 카테고리명
- 카테고리 상태
- 생성일시, 수정일시, 삭제 여부

## 3. Enum
### CategoryStatus
카테고리 상태를 정의한다.

값:
- `ACTIVE`
- `INACTIVE`

## 4. 필드 초안
### Category
| 필드 | 설명 |
|------|------|
| `id` | UUID PK |
| `parentId` | 상위 카테고리 UUID |
| `name` | 카테고리명 |
| `status` | 카테고리 상태 |
| `createdAt` | 생성일시, `SoftDeleteEntity` 상속 |
| `updatedAt` | 수정일시, `SoftDeleteEntity` 상속 |
| `deleted` | soft delete 여부, `SoftDeleteEntity` 상속 |

## 5. 설계 규칙
- PK는 UUID를 사용한다.
- `Category`는 `SoftDeleteEntity`를 상속한다.
- 삭제는 `deleted` 값을 사용하는 soft delete로 처리한다.
- 계층 구조는 `parentId`로 표현한다.
- 최상위 카테고리의 `parentId`는 null이다.
- 하위 카테고리 생성 시 부모 카테고리는 존재해야 하며 삭제되지 않은 상태여야 한다.
- 상위 카테고리는 객체 연관관계로 참조하지 않는다.
- 같은 상위 카테고리 안에서 카테고리명은 중복될 수 없다.
- 카테고리명 중복 제약은 미삭제 카테고리 기준으로 판단한다.
- `INACTIVE`는 운영상 비활성 상태를 의미하고, `deleted = true`는 soft delete 상태를 의미한다.
- 비활성 카테고리는 상품 신규 등록 시 선택할 수 없다.

## 6. 패키지 배치
`Category` 도메인은 `com.hyeon.guardrail.category` 하위에 배치한다.

```text
com.hyeon.guardrail.category
├── domain
│   ├── Category.java
│   └── CategoryStatus.java
├── repository
│   ├── CategoryRepository.java
│   └── CategoryRepositoryQuery.java
├── service
│   └── CategoryService.java
├── dto
│   ├── CategoryCreateRequest.java
│   ├── CategoryUpdateRequest.java
│   └── CategoryResponse.java
└── controller
    └── CategoryController.java
```
