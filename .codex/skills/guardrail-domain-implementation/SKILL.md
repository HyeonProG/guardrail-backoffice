---
name: guardrail-domain-implementation
description: Guardrail 프로젝트의 AGENT.md와 docs 규칙을 따라 백엔드 도메인을 구현하거나 다시 생성한다. user, auth, product, category, file 같은 도메인 패키지를 domain, repository, service, dto, controller 구조로 만들거나 수정할 때 사용한다.
---

# Guardrail 도메인 구현

## 개요

프로젝트 문서를 지정된 순서대로 읽고, 요청 범위를 고정한 뒤, 허용된 계층만 구현한다. 작업은 `CHANGELOG.md` 업데이트와 `./scripts/verify.sh all` 통과까지 완료된 경우에만 종료한다.

문서에 없는 규칙은 임의로 만들지 않는다. 요청된 동작이 문서에 정의되어 있지 않으면 문서 기준 경계까지만 구현하고 누락 사항을 보고한다.

## 필수 확인 순서

수정 전에 아래 파일을 순서대로 확인한다.

1. `AGENT.md`
2. `docs/HARNESS-RULES.md`
3. `docs/PACKAGE-STRUCTURE.md`
4. `docs/CODE-CONVENTIONS.md`
5. `docs/API-SPEC.md`
6. `docs/DOMAIN-MODEL.md`
7. `docs/domains/<TARGET>.md`

요청 대상 도메인에 맞는 상세 문서를 함께 사용한다. 예: `docs/domains/USER.md`, `docs/domains/AUTH.md`

## 작업 절차

1. 요청된 도메인과 허용된 패키지 범위를 확인한다.
2. 필수 문서를 순서대로 읽는다.
3. 요청된 도메인 패키지만 구현한다.
4. `CHANGELOG.md`는 현재 작업 버전 아래만 수정하고 새 버전을 임의로 만들지 않는다.
5. `./scripts/verify.sh all`을 실행한다.
6. 수정 파일, 검증 결과, 문서상 누락된 사항을 함께 보고한다.

## 작업 범위 고정

- 요청된 도메인 밖의 패키지를 생성하지 않는다.
- 현재 도메인이 다른 도메인을 참조하더라도, 그 이유만으로 다른 도메인 패키지를 생성하지 않는다.
- 교차 도메인 의존이 필요하지만 대상 도메인이 범위 밖이라면 문서에 정의된 계약까지만 반영하고 중단 후 보고한다.
- 코드 변경은 요청된 계층 범위 안에서만 수행한다.

## 강제 규칙

- `docs/PACKAGE-STRUCTURE.md`의 패키지 구조를 따른다.
- `domain`, `repository`, `service`, `dto`, `controller`만 사용한다.
- entity는 `{domain}.domain`에 둔다.
- PK는 UUID를 사용하고 JPA 연관관계 매핑은 사용하지 않는다.
- entity 선언 필드는 `@Column(name = "...")`을 명시한다.
- entity는 `@AllArgsConstructor`를 사용한다.
- entity에 정적 `create` 메서드를 추가하지 않는다.
- entity에 범용적인 전체 수정 메서드를 추가하지 않는다.
- entity 메서드는 최소 상태 변경만 허용한다.
- 기본 정보 수정과 상태 변경 유스케이스를 문서 기준대로 분리한다.
- DTO에 `record`를 사용하지 않는다.
- controller에서 `ResponseEntity`를 반환하지 않는다.
- 응답은 `BaseResponseEntity<T>`만 사용한다.
- 실패 처리는 `BaseException`, `BaseResponseStatus`를 사용한다.
- 자동 생성 값은 request DTO로 받지 않는다.
- 자동 생성 값에 만료 정책이 정의되어 있으면 코드에 그대로 반영한다.
- 로그인, JWT, 로그아웃 구현 시 `docs/domains/AUTH.md`, `docs/API-SPEC.md`의 request/response와 토큰 정책을 그대로 따른다.
- 클래스와 필요한 public 메서드에는 한 줄 Javadoc을 작성한다.

## 도메인 생성 체크리스트

작업 종료 전에 아래 항목을 확인한다.

- 생성된 파일이 요청 범위와 정확히 일치한다.
- `docs/domains/<TARGET>.md`의 규칙이 코드에 반영되어 있다.
- DTO 이름이 `Request` 또는 `Response`로 끝난다.
- Querydsl 조회 클래스가 `~RepositoryQuery` 네이밍을 사용한다.
- controller 경로가 `/api/v1`를 사용한다.
- 필요한 controller endpoint와 DTO에 Swagger annotation이 있다.
- 문서에 정의된 상태 변경 API와 만료 정책이 빠지지 않았다.
- 로그인, JWT, 로그아웃 API가 문서의 request/response와 토큰 정책을 그대로 반영했다.
- 존재하지 않는 이메일 로그인 실패 처리 방식이 문서 기준과 일치한다.
- `CHANGELOG.md`에 불필요한 새 버전을 추가하지 않았다.
- `./scripts/verify.sh all`이 통과한다.

## 결과 보고 형식

작업 완료 보고 시 아래를 포함한다.

1. 수정한 파일 목록
2. `./scripts/verify.sh all` 통과 여부
3. 구현을 막은 문서상 누락 사항
4. 문서에 정의된 상태 변경 정책, 만료 정책, 인증 API 정책 반영 여부

## 사용 예시

`$guardrail-domain-implementation`을 사용해서 `docs/domains/USER.md` 기준으로 `src/main/java/com/hyeon/guardrail/user`의 `user` 도메인을 구현하고, `CHANGELOG.md`를 업데이트한 뒤 `./scripts/verify.sh all` 통과까지 완료한다.
