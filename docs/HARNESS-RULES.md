# 하네스 규칙

## 1. 목적
이 문서는 Guardrail 프로젝트에서 자동 검증할 하네스 규칙의 기준을 정의한다.

하네스 규칙은 사람이 합의한 문서 기준을 코드, 테스트, 훅, CI에서 반복 가능하게 검증하기 위한 기준이다.

## 2. 하네스 적용 원칙
- 하네스는 개발 편의를 위한 권고가 아니라, 위반 시 차단 가능한 규칙을 목표로 한다.
- 문서로 먼저 기준을 정의한 뒤 자동 검증으로 연결한다.
- 구조, 네이밍, 의존 방향, API 규칙, 코드 스타일을 점진적으로 검증한다.
- 사람 리뷰 전에 기계적으로 걸러낼 수 있는 규칙부터 우선 적용한다.

## 3. 검증 대상
현재 프로젝트에서 하네스로 검증할 주요 대상은 아래와 같다.

- 패키지 구조
- 클래스 네이밍
- 계층 책임
- DTO/Entity 노출 규칙
- Repository/Querydsl 규칙
- API 경로 규칙
- soft delete 및 공통 엔티티 기준
- 로그 및 보안 규칙

## 4. 필수 차단 규칙
### 4.1 패키지 구조
- 최상위 패키지는 도메인 기준으로 나눈다.
- 도메인 하위 구조는 `domain`, `repository`, `service`, `dto`, `controller`만 사용한다.
- 공통 기능은 `common` 패키지에 둔다.

### 4.2 계층 책임
- controller는 service만 호출할 수 있다.
- controller는 repository를 직접 호출할 수 없다.
- controller는 entity를 응답으로 직접 반환할 수 없다.
- repository는 비즈니스 로직을 가질 수 없다.
- Querydsl 조회 로직은 허용하되, 유스케이스 판단 로직은 repository에 둘 수 없다.
- service는 유스케이스 흐름과 비즈니스 조합을 담당한다.

### 4.3 네이밍 규칙
- Spring Data JPA Repository는 `~Repository`로 끝낸다.
- Querydsl 조회 클래스는 `~RepositoryQuery`로 끝낸다.
- 요청 DTO는 `Request`로 끝낸다.
- 응답 DTO는 `Response`로 끝낸다.
- controller는 `Controller`, service는 `Service`로 끝낸다.

### 4.4 Entity 규칙
- 모든 엔티티의 PK 필드는 `id`여야 한다.
- PK 타입은 UUID여야 한다.
- soft delete 대상 엔티티는 `SoftDeleteEntity`를 상속한다.
- soft delete 미적용 엔티티는 `BaseEntity`를 상속한다.
- JPA 연관관계 매핑은 사용하지 않는다.
- 다른 도메인 참조는 `{domain}Id` 필드로만 표현한다.

### 4.5 API 규칙
- 모든 API 경로는 `/api/v1` prefix를 사용한다.
- 모든 성공 응답은 공통 응답 구조를 따른다.
- 모든 에러 응답은 공통 에러 응답 구조를 따른다.
- 컬렉션 응답은 null 대신 빈 배열을 반환한다.
- 같은 요청에 대해 동일한 조건에서는 항상 동일한 응답 구조를 반환한다.
- enum은 문자열로 응답한다.

### 4.6 보안 규칙
- 민감 정보는 로그에 출력하지 않는다.
- 비밀번호와 토큰은 평문으로 저장하지 않는다.
- Auth 도메인 엔티티는 soft delete 대신 상태/만료 정책을 우선한다.

## 5. 자동 검증 도구 후보
### 도구별 책임 분리
| 도구 | 역할 | 검증 대상 |
|------|------|-----------|
| `Spotless` | 코드 포맷 통일 | 포맷, import 정리 |
| `Checkstyle` | 코드 스타일 및 기본 규칙 검증 | 네이밍, Javadoc, import, 스타일 |
| `ArchUnit` | 구조 및 의존 방향 검증 | 패키지 구조, 계층 책임, 네이밍, 엔티티 규칙 |
| `JUnit` | 도메인 및 비즈니스 규칙 검증 | 상태 전이, 이력 규칙, soft delete, 서비스 규칙 |

### 실행 단계 기준
- `Spotless`, `Checkstyle`는 build 단계에서 실패를 발생시킨다.
- `ArchUnit`, `JUnit`은 test 단계에서 실패를 발생시킨다.
- commit/push 단계에서는 Git Hooks로 1차 차단한다.
- CI에서는 모든 규칙을 다시 검증하고, 실패 시 병합을 차단한다.
- 구조 위반은 `ArchUnit`으로 검증한다.
- 비즈니스 규칙은 `JUnit`으로 검증한다.

### Spotless
- 코드 포맷 검증
- import 정렬
- 줄바꿈, 공백, 포맷 일관성 검증

### Checkstyle
- 네이밍, import, 스타일 규칙 검증
- Javadoc 작성 여부 검증
- `@Data` 사용 금지 같은 기본 제한 규칙 검증

### ArchUnit
- 패키지 구조 및 계층 의존 규칙 검증
- controller -> service -> repository 방향 검증
- entity 직접 노출 금지 같은 구조 규칙 검증
- JPA 연관관계 금지 규칙 검증
- `~Repository`, `~RepositoryQuery`, `Request`, `Response` 네이밍 검증

### JUnit
- 상태 전이, 도메인 규칙, 비즈니스 검증 테스트
- 상품 상태 전이 검증
- 상품 이력 reason 규칙 검증
- 카테고리 parentId 규칙 검증
- 파일 sortOrder 규칙 검증
- Auth 상태/만료 규칙 검증

### Git Hooks
- commit 전 포맷/정적 검증 차단
- push 전 테스트 및 규칙 검증 차단

### CI
- 훅과 동일한 규칙을 서버에서 다시 검증
- 로컬 우회 시에도 최종 차단

## 6. 현재 자동화 상태
현재 자동화가 적용된 규칙은 아래와 같다.

### 자동화 완료
- Spotless 기반 포맷 검증
- Checkstyle 기반 스타일 검증
- `@Data` 사용 금지 검증
- ArchUnit 기반 패키지 구조 검증
- ArchUnit 기반 계층 의존 방향 검증
- ArchUnit 기반 클래스 네이밍 검증
- ArchUnit 기반 엔티티 상속 규칙 검증
- Git Hook 기반 commit 메시지 형식 검증
- Git Hook 기반 pre-commit, pre-push 검증 실행

### 문서 기준은 존재하지만 자동화가 남아 있는 항목
- API 경로 `/api/v1` prefix 검증
- controller 응답 구조 직접 검증
- JPA 연관관계 금지 규칙 검증
- PK 필드명과 UUID 타입 직접 검증
- soft delete 적용 대상 검증
- 로그 민감 정보 출력 금지 검증
- 도메인 상태 전이 규칙 검증

## 7. 점진 적용 순서
### 1단계
- Spotless
- Checkstyle
- 기본 테스트 실행

### 2단계
- ArchUnit 패키지 구조 규칙
- Repository/Controller/DTO 네이밍 검증

### 3단계
- API 응답 형식 테스트
- soft delete 및 공통 엔티티 규칙 테스트
- 민감 정보 로그 검증 보강

## 8. 우선 구현할 규칙
초기에는 아래 규칙부터 자동화한다.

1. controller가 repository를 직접 호출하지 못하게 한다.
2. entity를 controller 응답으로 직접 반환하지 못하게 한다.
3. Querydsl 조회 클래스는 `~RepositoryQuery` 네이밍만 허용한다.
4. API 경로는 `/api/v1` prefix만 허용한다.
5. DTO는 `Request`, `Response` 네이밍만 허용한다.
6. `@Data` 사용을 금지한다.

## 9. 수동 확인 규칙
아래 내용은 자동화 전까지 수동 리뷰로 확인한다.

- 도메인 경계 변경
- 상태 전이 정책 변경
- 파일 정책 변경
- Auth 저장 정책 변경
- 문서와 코드 기준 불일치 여부
- 수동으로 허용한 예외 규칙은 문서에 기록하고, 추후 자동화 대상으로 편입한다.

## 10. 문서 연계
하네스 규칙은 아래 문서를 기준으로 삼는다.

- `docs/PACKAGE-STRUCTURE.md`
- `docs/API-SPEC.md`
- `docs/CODE-CONVENTIONS.md`
- `docs/DOMAIN-MODEL.md`
- `docs/domains/*.md`
