# AI 연동 정책

## 1. 목적
이 문서는 Guardrail 프로젝트에서 AI 기반 상품 설명 생성 기능을 어떤 방식으로 연동할지 정의한다.

정책 범위는 OpenAI 연결 방식, Spring AI 공통 클라이언트 구조, 프롬프트 입력값, 실패 처리 기준을 포함한다.

## 2. 현재 범위
- AI는 상품 설명 텍스트 생성만 담당한다.
- 상품 이미지는 `FileAttachment` 도메인에서 운영자가 직접 등록한다.
- 현재 범위에서는 업로드된 이미지를 AI 입력으로 사용하지 않는다.
- 현재 범위에서는 AI가 추가 이미지를 생성하거나 이미지 위에 설명 문구를 합성하지 않는다.

## 3. 실행 기준
### 3.1 기본 실행 대상
현재 기본 AI 실행 대상은 `OpenAI API`다.

규칙:
- 현재 구현 범위는 `Spring AI + OpenAI` 조합으로 고정한다.
- 도메인 서비스는 OpenAI HTTP 호출 세부 구현을 직접 다루지 않는다.

## 4. 설정 주입 기준
### 4.1 설정 키
애플리케이션 설정은 아래 키를 사용한다.

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com
      chat:
        options:
          model: gpt-5.4-mini

app:
  ai:
    enabled: true
```

규칙:
- `enabled`는 AI 기능 전체 사용 여부를 제어한다.
- `spring.ai.openai.api-key`는 OpenAI API 키다.
- `spring.ai.openai.base-url`은 OpenAI API 기본 주소다.
- `spring.ai.openai.chat.options.model`은 설명 생성에 사용할 기본 모델명이다.
- `app.ai.enabled`는 애플리케이션 레벨 AI 기능 사용 여부를 제어한다.

### 4.2 OpenAI 기본 기준
배포와 시연 환경은 아래 기준으로 고정한다.

- base-url: `https://api.openai.com`
- model: `gpt-5.4-mini`

설명:
- 실제 실행 전에는 OpenAI API 키를 발급해 `OPENAI_API_KEY` 또는 `spring.ai.openai.api-key`로 주입한다.
- 애플리케이션은 Spring AI가 제공하는 OpenAI 연동 기능을 사용한다.

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
- `OpenAiContentGenerator`는 Spring AI OpenAI 연동 구현체다.
- `ProductDescriptionPromptFactory`는 프롬프트 조합 책임만 가진다.
- `common.ai.dto`는 외부 API request/response DTO가 아니라 내부 AI 호출 command/result를 표현한다.
- 도메인 서비스는 `AiContentGenerator`만 의존한다.
- 외부 OpenAI 호출 상세 구현은 도메인 패키지에 두지 않는다.

## 6. 상품 설명 생성 입력 기준
설명 생성 입력값은 아래 기준으로 고정한다.

- `productName`
- `categoryName`
- `optionSummary`
- `featureKeywords`

규칙:
- `featureKeywords`는 특징 키워드 목록이다.
- `optionSummary`는 옵션 구조를 설명 생성용 문자열로 요약한 값이다.
- 현재 기본 프론트 UX에서는 `optionSummary`를 비워서 전달하며, 선택 항목은 AI 입력에 사용하지 않는다.
- 현재 범위에서는 이미지 URL, 파일 경로, 바이너리 데이터는 입력값에 포함하지 않는다.

## 7. 프롬프트 구성 기준
- 프롬프트는 상품 설명 생성 목적에만 사용한다.
- 상품명, 카테고리, 옵션 요약, 특징 키워드를 구조화해 전달한다.
- 과장 표현, 허위 성능, 확인되지 않은 효능 표현은 금지한다.
- 출력은 한국어 상품 설명 본문 형태로 제한한다.
- 필요 시 길이 제한은 `app.product.description.min-length`, `max-length` 기준으로 후처리 검증한다.

## 8. 실패 처리 기준
- AI 호출 실패는 공통 `BaseException` 또는 `AiClientException`으로 감싼다.
- 모델 연결 실패, 응답 파싱 실패는 구분 가능한 메시지로 기록한다.
- 실패한 생성 요청은 상품 설명을 변경하지 않고 실패 응답으로 종료한다.

## 9. Product 연계 기준
- `ProductService`는 `AiContentGenerator`를 호출해 상품 설명 문구를 생성한다.
- AI 응답은 별도 초안 테이블에 저장하지 않고 현재 상품의 `description` 필드에 바로 반영한다.
- 생성 후 직원이 설명을 검토하고, 최종 승인 책임은 상품 승인 요청 흐름에서 관리한다.

## 10. 실행 기준
- 배포 환경에서는 `OpenAI API`를 사용한다.
- 권장 기본 모델은 `gpt-5.4-mini`다.
- 예시 실행 순서:
  1. OpenAI API 키 발급
  2. `OPENAI_API_KEY` 또는 `application.yml`의 `spring.ai.openai.api-key` 설정
  3. `application.yml`의 `spring.ai.openai`, `app.ai.enabled` 설정 확인
  4. 상품 설명 생성 API 또는 UI에서 설명 생성 테스트

## 11. 테스트 기준
- 외부 AI API를 직접 호출하는 테스트는 기본 검증 흐름에 포함하지 않는다.
- 단위 테스트에서는 `AiContentGenerator`를 mock으로 대체한다.
- 통합 테스트에서는 고정 응답 stub 또는 fake 구현을 사용한다.
- 실제 OpenAI 연동 검증은 별도 수동 또는 전용 통합 환경에서 수행한다.
