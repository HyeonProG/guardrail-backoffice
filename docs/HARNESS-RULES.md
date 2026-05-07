# 하네스 규칙

## 1. 목적
이 문서는 Guardrail 프로젝트에서 자동 검증할 하네스 규칙의 기준을 정의한다.

하네스 규칙은 사람이 합의한 문서 기준을 코드, 테스트, 훅, CI에서 반복 가능하게 검증하기 위한 기준이다.

## 2. 하네스 적용 원칙
- 하네스는 개발 편의를 위한 권고가 아니라, 위반 시 차단 가능한 규칙을 목표로 한다.
- 문서로 먼저 기준을 정의한 뒤 자동 검증으로 연결한다.
- 구조, 네이밍, 의존 방향, API 규칙, 코드 스타일을 자동 검증한다.
- 사람 리뷰 전에 기계적으로 걸러낼 수 있는 규칙부터 우선 적용한다.

## 3. 검증 대상
하네스로 검증할 주요 대상은 아래와 같다.

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
- entity 선언 필드는 `@Column(name = "...")`을 명시해야 한다.
- entity는 정적 `create` 메서드를 가질 수 없다.
- entity는 선언 필드 기준 `@AllArgsConstructor`를 가져야 한다.
- entity는 범용적인 전체 수정 메서드를 가질 수 없다.
- JPA 연관관계 매핑은 사용하지 않는다.
- 다른 도메인 참조는 `{domain}Id` 필드로만 표현한다.

### 4.5 API 규칙
- 모든 API 경로는 `/api/v1` prefix를 사용한다.
- 모든 성공 응답은 공통 응답 구조를 따른다.
- 모든 에러 응답은 공통 에러 응답 구조를 따른다.
- controller는 `BaseResponseEntity<T>`만 반환한다.
- controller는 `ResponseEntity`를 직접 반환하지 않는다.
- 컬렉션 응답은 null 대신 빈 배열을 반환한다.
- 같은 요청에 대해 동일한 조건에서는 항상 동일한 응답 구조를 반환한다.
- enum은 문자열로 응답한다.

### 4.6 보안 규칙
- 민감 정보는 로그에 출력하지 않는다.
- 비밀번호와 토큰은 평문으로 저장하지 않는다.
- Auth 도메인 엔티티는 soft delete 대신 상태/만료 정책을 우선한다.

## 5. 자동 검증 도구
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
- controller, service, config, common 진입점의 Javadoc 작성 여부 검증
- `@Data` 사용 금지 같은 기본 제한 규칙 검증

### ArchUnit
- 패키지 구조 및 계층 의존 규칙 검증
- controller -> service -> repository 방향 검증
- controller의 repository/domain 직접 접근 차단과 entity 직접 노출 금지 같은 구조 규칙 검증
- UUID `id` 필드와 soft delete 대상 상속 규칙 검증
- JPA 연관관계 금지 규칙 검증
- `~Repository`, `~RepositoryQuery`, `Request`, `Response` 네이밍 검증
- 현재 규칙은 메서드 호출/필드 접근 기준의 직접 의존 차단에 초점을 둔다.
- controller가 domain enum을 요청 파라미터나 쿼리 파라미터 타입으로 사용하는 형태는 현재 허용한다.

### JUnit
- 상태 전이, 도메인 규칙, 비즈니스 검증 테스트
- 상품 상태 전이 검증
- 상품 이력 reason 규칙 검증
- 카테고리 parentId 규칙 검증
- 파일 sortOrder 규칙 검증
- Auth 상태/만료 규칙 검증

### Git Hooks
- `.githooks/commit-msg`에서 커밋 메시지 형식 검증
- `.githooks/pre-commit`에서 `./scripts/verify.sh static` 실행
- `.githooks/pre-push`에서 `./scripts/verify.sh all` 실행
- 실제 설치는 `./scripts/install-git-hooks.sh`와 `git config core.hooksPath .githooks` 기준으로 수행

### CI
- 훅과 동일한 규칙을 서버에서 다시 검증
- 로컬 우회 시에도 최종 차단

## 6. 자동화 규칙
- Spotless 기반 포맷 검증
- Checkstyle 기반 스타일 검증
- Checkstyle 기반 공개 진입점 Javadoc 검증
- `@Data` 사용 금지 검증
- ArchUnit 기반 패키지 구조 검증
- ArchUnit 기반 계층 의존 방향 검증
- ArchUnit 기반 클래스 네이밍 검증
- ArchUnit 기반 엔티티 상속 규칙 검증
- ArchUnit 기반 soft delete 대상 엔티티 상속 검증
- ArchUnit 기반 DTO `record` 사용 금지 검증
- ArchUnit 기반 controller `BaseResponseEntity` 반환 강제 검증
- ArchUnit 기반 entity `@Column(name = "...")` 명시 검증
- ArchUnit 기반 entity 정적 `create` 메서드 금지 검증
- ArchUnit 기반 entity `@AllArgsConstructor` 검증
- ArchUnit 기반 entity UUID `id` 필드 검증
- ArchUnit 기반 API 경로 `/api/v1` prefix 검증
- ArchUnit 기반 JPA 연관관계 금지 검증
- `.githooks` 기반 commit 메시지 형식 검증
- `.githooks` 기반 pre-commit, pre-push 검증 실행
- GitHub Actions 기반 `./scripts/verify.sh all` CI 검증
- JUnit 기반 상품 상태 전이 규칙 검증

## 7. 수동 확인 규칙
아래 내용은 자동화 전까지 수동 리뷰로 확인한다.

- 도메인 경계 변경
- 상태 전이 정책 변경
- 파일 정책 변경
- Auth 저장 정책 변경
- 문서와 코드 기준 불일치 여부
- 수동으로 허용한 예외 규칙은 문서에 기록한다.

## 8. 문서 연계
하네스 규칙은 아래 문서를 기준으로 삼는다.

- `docs/PACKAGE-STRUCTURE.md`
- `docs/API-SPEC.md`
- `docs/CODE-CONVENTIONS.md`
- `docs/DOMAIN-MODEL.md`
- `docs/HOOKS-RUNBOOK.md`
- `docs/domains/*.md`
