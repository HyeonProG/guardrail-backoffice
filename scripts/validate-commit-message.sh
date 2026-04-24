#!/usr/bin/env bash

set -euo pipefail

COMMIT_MESSAGE_FILE="${1:-}"

if [[ -z "${COMMIT_MESSAGE_FILE}" || ! -f "${COMMIT_MESSAGE_FILE}" ]]; then
  echo "사용법: ./scripts/validate-commit-message.sh <commit-message-file>"
  exit 1
fi

COMMIT_MESSAGE="$(grep -v '^\s*#' "${COMMIT_MESSAGE_FILE}" | sed '/^\s*$/d' | head -n 1)"
ALLOWED_TYPES="feat|fix|refactor|chore|docs|test|style|perf"
COMMIT_PATTERN="^(${ALLOWED_TYPES}): .+"

if [[ "${COMMIT_MESSAGE}" =~ ^(Merge|Revert) ]]; then
  exit 0
fi

if [[ ! "${COMMIT_MESSAGE}" =~ ${COMMIT_PATTERN} ]]; then
  echo "커밋 메시지 형식이 올바르지 않습니다."
  echo ""
  echo "사용 형식: <type>: <설명>"
  echo "허용 type: feat, fix, refactor, chore, docs, test, style, perf"
  echo "예시: docs: 프로젝트 문서 구조 정리"
  echo ""
  echo "입력된 메시지: ${COMMIT_MESSAGE}"
  exit 1
fi
