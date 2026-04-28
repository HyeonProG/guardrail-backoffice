# CHANGELOG

이 문서는 프로젝트의 주요 변경 이력을 기록한다.

기록은 간단하게 유지하며, `추가`, `수정`, `삭제` 단위로 작성한다.

## [0.6.0]
### 추가
- Product 도메인 엔티티, 상태 enum, 이력 엔티티, repository, Querydsl 조회 저장소 추가
- 상품 생성, 단건 조회, 목록 조회, 기본 정보 수정, 상태 변경, 삭제, 이력 조회 API 추가
- product DDL 문서 추가
- 상품 상태 전이, 카테고리 검증, 상품 이력 저장 단위 테스트 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.6.0`으로 변경
- 상품 신규 등록 시 삭제되지 않은 ACTIVE 카테고리만 사용할 수 있도록 검증 연동
- 상품 파일 첨부는 `target_type = PRODUCT`, `target_id = product.id` 규칙으로 연결되도록 DDL 기준 반영
- 상품 비활성화 이력을 `INACTIVATED`로 분리하고 product 문서 구조와 actorId 요청 정책을 현재 구현 기준으로 정리
- product 생성, 목록, 상세, 수정, 상태 변경, 삭제, 이력 조회 API 명세와 actorId, reason 규칙 문서화

## [0.5.0]
### 추가
- FileAttachment 도메인 엔티티, 상태 enum, 대상 타입 enum, repository, Querydsl 조회 저장소 추가
- 파일 첨부 생성, 단건 조회, 대상별 목록 조회, 기본 정보 수정, 삭제 API 추가
- file attachment DDL 문서 추가
- file attachment sortOrder, 대표 파일, 파일 메타데이터 검증 단위 테스트 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.5.0`으로 변경
- file attachment 허용 MIME 타입, 최대 파일 크기, 저장 경로 루트 정책 반영

## [0.4.0]
### 추가
- Category 도메인 엔티티, 상태 enum, repository, Querydsl 조회 저장소 추가
- 카테고리 생성, 목록 조회, 상세 조회, 수정, 상태 변경, 삭제, 복구 API 추가
- category DDL 문서 추가
- category 단위 테스트 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.4.0`으로 변경
- category 목록 조회, 상세 조회, 삭제, 복구 API 명세와 상태 변경 분리 기준 문서화
- category 정렬 필드 검증과 목록 조회, 복구 테스트 보강

## [0.3.0]
### 추가
- Auth 도메인 엔티티, enum, repository, Querydsl 조회 저장소 추가
- 초기 비밀번호 해시 저장 service 흐름 추가
- 비밀번호 이력, 로그인 이력, 세션 저장/조회/상태 변경 API 추가
- 사용자 조회, 수정, 삭제 API 추가
- 사용자 상태 변경 API 추가
- 로그인, 토큰 재발급, 로그아웃 API 추가
- JWT 발급과 검증 서비스 추가
- user/auth DDL 문서 추가
- user/auth/JWT 단위 테스트 추가
- 임시 메일 발송 로깅 구현 추가
### 수정
- 사용자 생성 흐름을 Auth 비밀번호 이력 저장과 연동
- 초기 임시 비밀번호 만료 기간 7일 정책 반영
- 로그인 성공/실패 이력, 세션 생성, refresh token 해시 저장 흐름 반영
- 토큰 재발급 시 세션 상태와 만료, refresh token 해시 검증 흐름 반영
- 로그아웃 시 세션 `REVOKED` 상태 변경 흐름 반영
- 프로젝트와 API 문서 노출 버전을 `0.3.0`으로 변경
- 사용자 상태 변경을 별도 API 정책으로 고정
- 초기 임시 비밀번호 만료 기간을 7일 정책으로 고정
- 로그인, 토큰 재발급, 로그아웃 API의 request/response와 토큰 정책 문서화
- 존재하지 않는 이메일 로그인 실패는 이력 저장 없이 종료하도록 정책 수정

## [0.2.0]
### 추가
- `User` 도메인 엔티티, 역할 enum, 사용 상태 enum 추가
- 사용자 생성 요청/응답 DTO 추가
- 사용자 저장소와 Querydsl 조회 저장소 추가
- 사용자 생성 API와 service 흐름 추가
### 수정
- 프로젝트 버전을 `0.2.0`으로 변경

## [0.1.20]
### 추가
- `BaseResponseEntity`, `BaseResponseStatus` 공통 응답 구조 추가
- `BaseException`, `BaseExceptionHandler`, `BaseExceptionHandlerFilter`, `AsyncExceptionHandler` 공통 예외 구조 추가
- `guardrail-domain-implementation` repo-local skill 추가
### 수정
- DTO와 공통 응답 구조의 record 사용 금지 기준 반영
- Swagger 설명 최소 기준 문서화
- 공통 성공/실패 응답 구조를 `BaseResponseEntity` 기준으로 통일
- `BaseResponseEntity` 필드 구조를 상태 코드, 성공 여부, 메시지, 코드, 결과값 기준으로 정리
- 성공 상태, 메시지, 코드 관리 기준을 `BaseResponseStatus`로 통일
- 예외 응답 구조를 `BaseResponseStatus`와 `BaseResponseEntity` 기준으로 통일
- entity `@Column(name = "...")`, all-args 생성자 기준 반영
- controller `ResponseEntity` 직접 반환 금지 기준 반영
- ArchUnit 기반 DTO `record` 금지와 entity/controller 규칙 검증 보강
- entity 생성 흐름은 service, 상태 전이와 soft delete는 entity 허용 기준으로 정리
- 공통 응답 상태에 `404 Not Found`, `409 Conflict` 추가
- 백오피스 사용자 생성 시 비밀번호 자동 생성과 범용 update 금지 기준 반영
- 사용자 생성 완료 조건에 초기 비밀번호 해시 저장과 1회 전달용 정보 반환 기준 반영
- entity 기본 정보 변경 메서드 금지와 작업 범위 외 도메인 생성 금지 기준 반영
### 삭제
- `ErrorCode`, `ErrorResponse`, `BusinessException`, `GlobalExceptionHandler` 제거

## [0.1.19]
### 추가
- 공통 성공 응답 구조 추가
- 공통 페이징 응답 구조 추가
- 공통 에러 코드 및 예외 구조 추가
- 공통 예외 응답 처리기 추가

## [0.1.18]
### 추가
- User 도메인 엔티티와 역할, 사용 상태 enum 추가

## [0.1.17]
### 추가
- OpenAPI 메타데이터 및 Bearer 보안 스키마 설정 추가
### 수정
- Springdoc 의존성을 Swagger UI 포함 모듈로 변경
- Swagger UI 경로와 문서 버전 기준 추가
- 인증 구현 전 Spring Security 기본 로그인 비활성화

## [0.1.16]
### 수정
- `build.gradle` 프로젝트 버전을 changelog 기준과 일치하도록 정리
- ArchUnit empty rule 완화 설정을 전역 설정에서 규칙별 설정으로 조정

## [0.1.15]
### 추가
-  `AGENT.md` 추가

## [0.1.14]
### 추가
- `BaseEntity`, `SoftDeleteEntity` 공통 엔티티 기반 추가
- JPA Auditing 설정 추가
- ArchUnit 엔티티 상속 규칙 검증 테스트 추가
- Spotless와 Checkstyle import 정렬 책임 충돌 방지 설정 추가

## [0.1.13]
### 추가
- ArchUnit 패키지 구조 및 계층 의존성 검증 테스트 추가
- ArchUnit 클래스 네이밍 규칙 검증 테스트 추가

## [0.1.12]
### 추가
- 테스트 전용 H2 데이터베이스 설정 추가
- 테스트 profile 기반 Spring Context 검증 기준 추가

## [0.1.11]
### 추가
- 하네스 검증 공통 스크립트 추가
- Git Hook 설치 스크립트 추가
- commit-msg, pre-commit, pre-push 검증 훅 추가
- 커밋 메시지 검증 스크립트 추가

## [0.1.10]
### 추가
- `build.gradle`에 Spotless, Checkstyle, ArchUnit 최소 설정 추가
- `config/checkstyle/checkstyle.xml` 설정 파일 추가

## [0.1.9]
### 추가
- `docs/HARNESS-RULES.md` 문서 추가
- 하네스 응답 구조, Querydsl 책임, 수동 예외 기록 기준 추가
- Spotless, Checkstyle, ArchUnit, JUnit 역할 분리 기준 추가
- build/test/hooks/CI 단계별 하네스 실행 기준 추가

## [0.1.8]
### 추가
- `docs/CODE-CONVENTIONS.md` 문서 추가
- DTO validation, entity 생성 방식, Javadoc 작성 기준 추가
- Service 트랜잭션, Optional, 컬렉션, 로그, equals/hashCode 기준 추가

## [0.1.7]
### 추가
- `docs/API-SPEC.md` 문서 추가
- API 에러 코드, 정렬 파라미터, 날짜/시간 포맷, success message 기준 추가
- API prefix, 컬렉션 응답, 응답 구조 일관성, Swagger/OpenAPI 기준 추가

## [0.1.6]
### 추가
- `docs/domains/FILE-ATTACHMENT.md` 문서 추가
- 파일 조회, 검증, 대표 파일 판단, 정렬 순서 기준 추가

## [0.1.5]
### 추가
- `docs/domains/CATEGORY.md` 문서 추가
- 카테고리 계층 및 soft delete 판단 기준 추가

## [0.1.4]
### 추가
- `docs/domains/PRODUCT.md` 문서 추가
- 상품 상태 전이 규칙 추가
- 상품 이력 사유 작성 기준 추가

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
