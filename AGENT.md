# AGENT

## 1. 목적
이 문서는 Guardrail 프로젝트에서 AI agent가 작업을 시작하기 전에 확인할 기준 문서의 진입점이다.

세부 규칙을 중복 작성하지 않고, 아래 문서를 우선순위대로 확인한 뒤 작업한다.

## 2. 작업 전 필수 확인 문서
1. `docs/PROJECT-OVERVIEW.md`
2. `docs/PACKAGE-STRUCTURE.md`
3. `docs/CODE-CONVENTIONS.md`
4. `docs/HARNESS-RULES.md`
5. `docs/API-SPEC.md`
6. `docs/DOMAIN-MODEL.md`
7. `docs/AI-INTEGRATION.md`
8. `docs/domains/*.md`

## 3. 문서 우선순위
문서 간 충돌 시 아래 우선순위를 따른다.

1. `AGENT.md`
2. `docs/HARNESS-RULES.md`
3. `docs/PACKAGE-STRUCTURE.md`
4. `docs/CODE-CONVENTIONS.md`
5. `docs/API-SPEC.md`
6. `docs/DOMAIN-MODEL.md`
7. `docs/AI-INTEGRATION.md`
8. `docs/domains/*.md`
9. `docs/PROJECT-OVERVIEW.md`
10. `docs/VERSION-POLICY.md`, `docs/CHANGELOG-CONVENTION.md`, `docs/COMMIT-MESSAGE.md`

## 4. 작업 유형별 참조 문서
### 도메인 구현
- `docs/DOMAIN-MODEL.md`
- `docs/domains/*.md`
- `docs/PACKAGE-STRUCTURE.md`
- `docs/CODE-CONVENTIONS.md`

### API 구현
- `docs/API-SPEC.md`
- `docs/AI-INTEGRATION.md`
- `docs/PACKAGE-STRUCTURE.md`
- `docs/CODE-CONVENTIONS.md`

### 하네스 규칙 구현
- `docs/HARNESS-RULES.md`
- `docs/PACKAGE-STRUCTURE.md`
- `docs/CODE-CONVENTIONS.md`

### 버전 및 커밋 작업
- `docs/VERSION-POLICY.md`
- `docs/CHANGELOG-CONVENTION.md`
- `docs/COMMIT-MESSAGE.md`

## 5. 작업 원칙
- 문서에 정의되지 않은 사항은 임의로 구현하지 않는다.
- 문서 간 충돌 시 문서 우선순위를 따른다.
- 작업 범위를 벗어난 수정은 하지 않는다.
- 명시된 작업 범위 밖의 패키지, 도메인, 클래스는 생성하지 않는다.
- 도메인 구현 요청 시 해당 도메인 범위 외 클래스 추가가 필요하면 먼저 문서 기준을 확인하고 범위 외 생성 없이 종료한다.
- 모든 검증을 통과한 상태에서만 작업을 종료한다.

## 6. 기본 작업 순서
1. 관련 문서 확인
2. 변경 범위 결정
3. 코드 또는 문서 수정
4. `CHANGELOG.md` 업데이트
5. `./scripts/verify.sh all` 실행
6. 실패 원인 수정
7. 커밋 가능 여부와 커밋 메시지 제안

## 7. 핵심 금지 사항
- 문서 기준과 다른 패키지 구조 임의 생성 금지
- 명시되지 않은 다른 도메인 패키지 생성 금지
- JPA 객체 연관관계 매핑 사용 금지
- Entity 직접 응답 반환 금지
- `@Data` 사용 금지
- 검증 실패 상태로 작업 완료 금지

## 8. 검증 명령
```bash
./scripts/verify.sh all
```
