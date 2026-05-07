# Guardrail Backoffice Frontend

React, Vite, TypeScript 기반 백오피스 SPA입니다.

## 실행 방법

```bash
cd frontend
npm install
npm run dev
```

기본 개발 서버는 `http://localhost:5173`에서 실행됩니다.

## 환경 변수

`.env.example`을 참고해 `.env`를 생성합니다.

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=Guardrail Backoffice
```

## 인증 저장 정책

- 로그인 요청은 `email`, `password`, `deviceType=WEB`를 전송합니다.
- 로그인 성공 시 `accessToken`, `refreshToken`, `sessionId`, `userId`, `role`, `temporaryPassword`를 localStorage에 저장합니다.
- 현재 보호 라우트는 `accessToken` 존재 여부로만 접근을 제어합니다.
- 로그아웃은 `/api/v1/auth/logout` 호출 후 저장된 인증 정보를 삭제합니다.

## 구현된 화면

- `/login`: `/api/v1/auth/login` 기반 로그인
- `/dashboard`: 승인 완료 상품, 승인 요청 현황, 최근 등록 상품 미리보기를 보여주는 메인 대시보드
- `/categories`: 카테고리 목록, 생성, 수정, 상태 변경, 삭제, 복구
- `/product-options`: 카테고리별 옵션과 선택 항목 관리
- `/products`: 승인 전 단계 상품 목록
- `/products/new`: 상품 등록과 승인 요청
- `/products/:productId`: 상품 상세 조회
- `/products/:productId/edit`: 상품 수정
- `/products/approved`: 승인 완료 상품 목록
- `/approval-requests`: 승인 요청 검토와 승인/반려
- `/users`: 사용자 관리
- `/my-account`: 내 정보 수정과 비밀번호 변경

모든 API 응답은 `BaseResponseEntity.result`를 기준으로 사용합니다.

## 기본 테스트 순서

1. 백엔드를 `http://localhost:8080`에서 실행합니다.
2. 프론트 `.env`의 `VITE_API_BASE_URL`을 확인합니다.
3. `/login`에서 백엔드 계정으로 로그인합니다.
4. `ADMIN` 또는 `OPERATOR`로 `/categories`와 `/product-options`에서 기준 정보를 준비합니다.
5. `STAFF`로 `/products/new`에서 상품을 등록하고 AI 문구 생성 후 승인 요청을 보냅니다.
6. `ADMIN` 또는 `OPERATOR`로 `/approval-requests`에서 상품을 승인 또는 반려합니다.
7. `/products/approved`와 `/dashboard`에서 승인 완료 상품을 확인합니다.

## 주요 스크립트

- `npm run dev`: 개발 서버 실행
- `npm run build`: 타입 체크 후 프로덕션 빌드
- `npm run preview`: 빌드 결과 미리보기
- `npm run typecheck`: TypeScript 타입 체크

## 구조

- `src/app`: 앱 진입 구성, 라우터, provider
- `src/pages`: 라우트 단위 페이지
- `src/widgets`: 화면을 구성하는 레이아웃 위젯
- `src/features`: 로그인 같은 사용자 행동 단위 기능
- `src/entities`: 도메인 모델과 타입
- `src/shared`: API 클라이언트, 설정, 공통 UI
- `src/styles`: 전역 스타일

도메인별 API와 타입은 `src/entities` 아래에 배치했고, 라우트 단위 화면은 `src/pages`에서 조합합니다. 파일/옵션 화면은 이후 `src/entities/productOption`, `src/entities/file`과 해당 페이지를 추가해 같은 구조로 확장할 수 있습니다.
