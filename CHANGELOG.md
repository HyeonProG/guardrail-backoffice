# CHANGELOG

이 문서는 프로젝트의 주요 변경 이력을 기록한다.

기록은 간단하게 유지하며, `추가`, `수정`, `삭제` 단위로 작성한다.

## [0.1.3]
### 추가
- `docs/domains/AUTH.md` 문서 추가
- 인증 토큰과 비밀번호 저장 기준 추가
- `BaseEntity`와 `SoftDeleteEntity` 분리 기준 추가
- Auth 도메인 soft delete 제외 기준 추가

## [0.1.2]
### 추가
- `docs/domains/USER.md` 문서 추가
- UUID PK 기준 추가
- `BaseEntity` 공통 필드 기준 추가
- soft delete 기준 추가
- `User.email` unique 제약 기준 추가
- 패키지 구조의 책임 분리 기준 보강

## [0.1.1]
### 추가
- `docs/DOMAIN-MODEL.md` 문서 추가
- `docs/PACKAGE-STRUCTURE.md` 문서 추가
- `Auth` 도메인 및 패키지 기준 추가
- 인증 관련 이력을 `auth` 기준으로 재배치

## [0.1.0]
### 추가
- Spring Boot 3.5.13 기반 백엔드 프로젝트 초기 구조 추가
- JWT, Querydsl, PostgreSQL 기반 초기 의존성 추가
- `application.yml` 기본 설정 및 한국어 주석 추가
- `springdoc` 의존성 구성 추가
- `.gitignore` 로컬 개발 환경 기준 추가
- 문서 파일 네이밍 규칙 대문자 기준 추가
- `docs/PROJECT-OVERVIEW.md` 문서 추가
- `docs/VERSION-POLICY.md` 문서 추가
- `docs/CHANGELOG-CONVENTION.md` 문서 추가
- `docs/COMMIT-MESSAGE.md` 문서 추가
- `CHANGELOG.md` 경량 형식 추가
