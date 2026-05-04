# CHANGELOG

이 문서는 프로젝트의 주요 변경 이력을 기록한다.

기록은 간단하게 유지하며, `추가`, `수정`, `삭제` 단위로 작성한다.

## [0.9.3]
### 수정
- AI 연동 구현과 설정을 `Spring AI + OpenAI API` 기준으로 전환
- 배포용 환경 변수와 Docker Compose 설정에 OpenAI 관련 변수 추가
- AI 연동 정책 문서와 프로젝트 개요를 OpenAI 기반 배포 구조에 맞게 정리
- 배포용 `.env.example` 기준을 추가

## [0.9.2]
### 수정
- 로그아웃 요청을 인증 토큰 필수 검증 경로에서 제외하고 `sessionId` 기준 세션 종료로 정리
- 로그아웃 시 UI는 정상 종료되지만 백엔드에 불필요한 인증 실패 로그가 남던 문제 수정
- 로그아웃 API, JWT 필터, 보안 설정, 인증 서비스와 테스트를 현재 로그아웃 흐름 기준으로 정합성 보강

## [0.9.1]
### 수정
- 상품, 승인 요청, 사용자, 카테고리, 상품 옵션 화면의 사용자 관점 문구와 확인 모달 흐름을 최종 정리
- 상품 등록, 상품 수정, 승인 요청, 승인 완료 상품, 상품 상세 화면 UX를 현재 운영 플로우 기준으로 정리
- 상품 설명 생성, 이미지 업로드/미리보기, 권한별 화면 노출, 오류 페이지 이동 흐름을 현재 구현 기준으로 마감 정리
- `frontend/README`, `CHANGELOG`, Swagger 설명을 실제 구현과 맞게 정합성 보강

## [0.9.0]
### 수정
- 상품, 승인 요청, 오류 페이지, 내 정보, 카테고리, 상품 옵션 화면의 사용자 문구와 UX를 현재 구현 기준으로 정리
- 인증 토큰 만료 시 로그인 페이지로 이동하고, 권한 없음/존재하지 않는 경로는 전용 안내 페이지로 분리
- `PROJECT-OVERVIEW`, `API-SPEC`, `PRODUCT`, `CATEGORY`, `AI-INTEGRATION` 문서를 실제 권한 정책, 상품 삭제 정책, 선택 항목 저장 구조, AI 설명 생성 흐름 기준으로 정합성 보강

## [0.8.5]
### 추가
- 임시 비밀번호 발급 추가
- 내 정보 화면 비밀번호 변경 API와 프론트 비밀번호 변경 폼을 추가
- 임시 비밀번호 사용자에게 변경 안내 모달과 상태 표시를 추가
### 수정
- 상품 생성 화면에 선택 이미지 메타데이터 등록 흐름을 추가
- 카테고리별 상품 옵션과 옵션값 정렬 순서를 생성 시 자동 부여하도록 변경
- 더 이상 사용하지 않는 `ProductContent` 관련 문서와 DDL을 제거하고 상품 설명 생성 기준을 `Product` 중심으로 정리

## [0.8.3]
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.8.3`으로 변경
- product option 도메인을 전역 옵션 마스터 구조에서 카테고리별 옵션 마스터 구조로 재설계
- product option 백엔드 API를 `/api/v1/categories/{categoryId}/options` 구조로 변경
- product option 중복 규칙과 DDL을 카테고리 기준으로 변경
- 프론트 상품 옵션 관리 화면과 상품 설명 옵션 선택 흐름을 카테고리 기준으로 정리

## [0.8.2]
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.8.2`로 변경
- product option 백엔드 API를 `/api/v1/products/{productId}/options` 구조에서 `/api/v1/product-options` 마스터 데이터 구조로 재설계
- product option 엔티티, Querydsl 중복 규칙, 서비스, DDL을 상품 종속 구조에서 전역 옵션 마스터 구조로 변경
- 프론트 상품 옵션 관리 화면과 상품 설명 옵션 선택 화면을 독립 옵션 마스터 API 기준으로 정리

## [0.8.1]
### 수정
- AI 기본 연동 설정을 OpenAI 클라우드 예시에서 Ollama 로컬 실행 기준으로 정리
- AI 연동 문서를 OpenAI 호환 API 기준과 Ollama 사용 흐름에 맞게 보강
- 상품 옵션 도메인을 상품 종속 구조에서 마스터 데이터 구조로 재정의
- 상품 등록/설명 생성/승인 요청 화면 흐름을 카테고리, 상품 옵션, 상품 등록, 승인 요청 관리 기준으로 문서화

## [0.8.0]
### 추가
- ProductContent 도메인 엔티티, 상태/출처/이력 enum, repository, Querydsl 조회 저장소 추가
- AI 상품 설명 초안 생성, 목록/상세 조회, 수정, 승인 요청, 승인, 반려, 이력 조회, 삭제 API 추가
- product content DDL 문서 추가
- 상품 설명 승인 반영, 재생성 신규 초안 생성, AI 생성 결과 저장 흐름 테스트 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.8.0`으로 변경
- 상품 설명 승인 시에만 `Product.description`에 최종 설명이 반영되도록 product repository 연동 추가
- 승인 완료된 상품 설명 초안은 `productcontent`에서 수정하지 않고 `product` 기본 정보 수정 API로 변경하도록 정책과 테스트 보강

## [0.7.1]
### 추가
- `common.ai` 기반 OpenAI 상품 설명 생성 공통 연동 구조 추가
- 상품 설명 생성 명령/결과 DTO, 프롬프트 팩토리, AI 전용 예외 추가
- AI 비활성화, 미지원 provider, API 키 누락, provider 호출 실패 단위 테스트 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.7.1`로 변경
- AI API 키 기본값을 환경 변수 기반 빈 기본값으로 정리
- AI 정책 문서의 `Command`/`Result` DTO 명명 기준을 하네스 예외로 반영

## [0.7.0]
### 추가
- ProductOption 도메인 엔티티, 상태 enum, repository, Querydsl 조회 저장소 추가
- 옵션 그룹 생성, 목록 조회, 상세 조회, 기본 정보 수정, 상태 변경, 삭제 API 추가
- 옵션값 생성, 목록 조회, 기본 정보 수정, 상태 변경, 삭제 API 추가
- product option DDL 문서 추가
- 옵션 그룹과 옵션값의 중복 검증, productId 검증, 추가 금액 검증 단위 테스트 추가
- `docs/domains/PRODUCT-CONTENT.md` 문서 추가
- `docs/AI-INTEGRATION.md` 문서 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.7.0`으로 변경
- 상품 옵션과 옵션값을 객체 연관관계 없이 `productId`, `productOptionId` 값으로 연결
- 도메인 인덱스와 상품 문서에 AI 설명 생성/검수/승인 영속 도메인 기준 반영
- 상품 설명 생성/검수/승인 API 명세 추가
- 상품 이미지는 직접 등록하고 AI는 텍스트 설명만 생성하는 현재 범위 정책 반영
- 도메인 모델, 패키지 구조, DTO 예시, 프로젝트 개요 문서를 현재 구현 기준으로 정합성 보강
- 공통 AI 클라이언트 구조와 OpenAI 기반 API 키 주입 정책 문서화

## [0.6.1]
### 추가
- user/auth/category/file/product 도메인 연결 기준 백오피스 핵심 흐름 통합 테스트 추가
- 관리자 생성, 초기 비밀번호 해시 저장, 로그인 세션, 카테고리, 파일 메타데이터, 상품 상태 전이와 반려 재제출 시나리오 검증 추가
- `docs/domains/PRODUCT-OPTION.md` 문서 추가
### 수정
- 프로젝트와 API 문서 노출 버전을 `0.6.1`로 변경
- refresh token 저장 정책을 평문 금지와 검증 가능한 해시 값 기준으로 문서 정합성 보강
- 도메인 인덱스에 product option 도메인 추가

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
