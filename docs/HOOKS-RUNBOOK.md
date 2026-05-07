# 훅 실행 런북

## 1. 목적
이 문서는 Guardrail 프로젝트의 실제 Git Hook 설치 방식과 검증 흐름을 기록한다.

하네스 엔지니어링 관점에서 이 문서는 "문서 -> 훅 -> 검증 스크립트" 연결을 재현 가능한 형태로 고정하기 위한 실행 런북이다.

## 2. 실제 훅 경로
이 프로젝트는 Git 기본 `.git/hooks`를 직접 수정하지 않고, 저장소 내부 `.githooks` 디렉터리를 사용한다.

```text
.githooks/
├── commit-msg
├── pre-commit
└── pre-push
```

훅 설치는 아래 스크립트로 수행한다.

```bash
./scripts/install-git-hooks.sh
```

## 3. 설치 방식
`./scripts/install-git-hooks.sh`는 아래 작업을 수행한다.

1. `.githooks/*`에 실행 권한을 부여한다.
2. `git config core.hooksPath .githooks`를 설정한다.

즉, 이 프로젝트에서 훅 기준을 다시 만들 때는 `.githooks` 파일과 설치 스크립트를 함께 복원해야 한다.

## 4. 훅별 실행 규칙
### 4.1 commit-msg
실행 파일:

```text
.githooks/commit-msg
```

실행 명령:

```bash
./scripts/validate-commit-message.sh "$1"
```

역할:
- 커밋 메시지가 프로젝트 규칙을 따르는지 검증한다.

### 4.2 pre-commit
실행 파일:

```text
.githooks/pre-commit
```

실행 명령:

```bash
./scripts/verify.sh static
```

역할:
- Spotless, Checkstyle 같은 정적 규칙을 커밋 전에 차단한다.

### 4.3 pre-push
실행 파일:

```text
.githooks/pre-push
```

실행 명령:

```bash
./scripts/verify.sh all
```

역할:
- 테스트를 포함한 전체 검증을 push 전에 차단한다.

## 5. 검증 스크립트 기준
실행 파일:

```text
scripts/verify.sh
```

지원 모드:

- `format`
  - `./gradlew spotlessCheck`
- `static`
  - `./gradlew spotlessCheck checkstyleMain checkstyleTest`
- `test`
  - `./gradlew test`
- `all`
  - `./gradlew spotlessCheck checkstyleMain checkstyleTest test`

## 6. 재현 순서
이 프로젝트를 문서, 스킬, 훅만으로 최대한 비슷하게 다시 재구성하려면 아래 순서를 따른다.

1. `AGENT.md`를 읽고 기준 문서 진입점을 고정한다.
2. `docs/HARNESS-RULES.md`로 자동 검증 대상과 차단 규칙을 확정한다.
3. `docs/HOOKS-RUNBOOK.md`로 실제 훅 경로와 실행 명령을 복원한다.
4. `scripts/install-git-hooks.sh`로 `.githooks`를 Git 훅 경로에 연결한다.
5. `docs/PACKAGE-STRUCTURE.md`, `docs/DOMAIN-MODEL.md`, `docs/API-SPEC.md`를 따라 코드 구조와 API 계약을 복원한다.
6. `.codex/skills/guardrail-domain-implementation/SKILL.md`의 읽기 순서와 작업 절차를 따라 도메인 구현을 진행한다.
7. 마지막에 `./scripts/verify.sh all`을 통과시킨다.

## 7. 유지 규칙
- 훅 동작을 바꾸면 반드시 이 문서와 `docs/HARNESS-RULES.md`를 함께 수정한다.
- `.githooks`와 `scripts/verify.sh`가 실제 기준이며, 문서는 항상 코드와 동일해야 한다.
- 하네스 관련 자동화는 "문서에 먼저 규칙 정의 -> 훅/검증으로 차단" 순서를 지킨다.
