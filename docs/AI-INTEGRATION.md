# AI 연동 정책

## 1. 목적
이 문서는 Guardrail 프로젝트에서 AI 기반 상품 설명 생성 기능을 어떤 방식으로 연동할지 정의한다.

정책 범위는 AI provider 선택, API 키 주입 방식, 공통 AI 클라이언트 구조, 프롬프트 입력값, 실패 처리 기준을 포함한다.

## 2. 현재 범위
- AI는 상품 설명 텍스트 초안 생성만 담당한다.
- 상품 이미지는 `FileAttachment` 도메인에서 운영자가 직접 등록한다.
- 현재 범위에서는 업로드된 이미지를 AI 입력으로 사용하지 않는다.
- 현재 범위에서는 AI가 추가 이미지를 생성하거나 이미지 위에 설명 문구를 합성하지 않는다.

## 3. Provider 기준
### 3.1 기본 Provider
현재 기본 AI provider는 `OpenAI`로 고정한다.

### 3.2 교체 가능성
- provider 선택은 설정값으로 분리한다.
- 도메인 서비스는 provider 구현체를 직접 호출하지 않고 공통 인터페이스를 호출한다.
- 이후 필요 시 다른 provider로 교체할 수 있다.

## 4. API 키 및 설정 주입 기준
### 4.1 API 키
- API 키는 `application.yml`에 작성해 관리한다.
- 실제 운영 또는 배포 시 사용하는 `application.yml`은 저장소에 직접 커밋하지 않고 secret 관리 대상으로 취급한다.
- 로컬 개발, 배포 환경, CI 환경은 각 환경별 `application.yml` 또는 secret 파일로 분리 관리한다.

### 4.2 설정 키
애플리케이션 설정은 아래 키를 사용한다.

```yaml
app:
  ai:
    enabled: true
    provider: openai
    model: gpt-4.1-mini
    timeout-seconds: 30
    base-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY:}
```

규칙:
- `enabled`는 AI 기능 전체 사용 여부를 제어한다.
- `provider`는 현재 `openai`만 허용한다.
- `model`은 설명 생성에 사용할 기본 모델명이다.
- `timeout-seconds`는 외부 호출 제한 시간이다.
- `base-url`은 provider API 기본 주소다.
- `api-key`는 provider 인증에 사용하는 실제 키다.

## 5. 공통 AI 클라이언트 구조
AI 연동 코드는 `common.ai` 하위에 둔다.

```text
com.hyeon.guardrail.common.ai
├── config
│   └── AiClientConfig.java
├── dto
│   ├── ProductDescriptionGenerateCommand.java
│   └── ProductDescriptionGenerateResult.java
├── exception
│   └── AiClientException.java
├── generator
│   ├── AiContentGenerator.java
│   └── OpenAiContentGenerator.java
└── support
    └── ProductDescriptionPromptFactory.java
```

구성 규칙:
- `AiContentGenerator`는 상품 설명 생성 공통 인터페이스다.
- `OpenAiContentGenerator`는 OpenAI 연동 구현체다.
- `ProductDescriptionPromptFactory`는 프롬프트 조합 책임만 가진다.
- 도메인 서비스는 `AiContentGenerator`만 의존한다.
- 외부 HTTP 호출 상세 구현은 도메인 패키지에 두지 않는다.

## 6. 상품 설명 생성 입력 기준
설명 생성 입력값은 아래 기준으로 고정한다.

- `productName`
- `categoryName`
- `optionSummary`
- `featureKeywords`

규칙:
- `featureKeywords`는 특징 키워드 목록이다.
- `optionSummary`는 옵션 구조를 설명 생성용 문자열로 요약한 값이다.
- 현재 범위에서는 이미지 URL, 파일 경로, 바이너리 데이터는 입력값에 포함하지 않는다.

## 7. 프롬프트 구성 기준
- 프롬프트는 상품 설명 생성 목적에만 사용한다.
- 상품명, 카테고리, 옵션 요약, 특징 키워드를 구조화해 전달한다.
- 과장 표현, 허위 성능, 확인되지 않은 효능 표현은 금지한다.
- 출력은 한국어 상품 설명 본문 형태로 제한한다.
- 필요 시 길이 제한은 `app.product.description.min-length`, `max-length` 기준으로 후처리 검증한다.

## 8. 실패 처리 기준
- AI 호출 실패는 공통 `BaseException` 또는 `AiClientException`으로 감싼다.
- 타임아웃, 인증 실패, rate limit, 응답 파싱 실패는 구분 가능한 메시지로 기록한다.
- 민감 정보인 API 키와 원문 인증 헤더는 로그에 출력하지 않는다.
- 실패한 생성 요청은 상품 설명 초안을 저장하지 않고 실패 응답으로 종료한다.

## 9. ProductContent 연계 기준
- `ProductContentService`는 `AiContentGenerator`를 호출해 설명 초안을 생성한다.
- AI 응답 원문은 `ProductContentDraft.content`에 저장한다.
- 생성 성공 시 `ProductContentSource = AI`, `ProductContentStatus = GENERATED`로 저장한다.
- 생성 후 운영자 검수와 관리자 승인 흐름은 `ProductContent` 도메인 규칙을 따른다.

## 10. 테스트 기준
- 외부 AI API를 직접 호출하는 테스트는 기본 검증 흐름에 포함하지 않는다.
- 단위 테스트에서는 `AiContentGenerator`를 mock으로 대체한다.
- 통합 테스트에서는 고정 응답 stub 또는 fake 구현을 사용한다.
- 실제 provider 연동 검증은 별도 수동 또는 전용 통합 환경에서 수행한다.
