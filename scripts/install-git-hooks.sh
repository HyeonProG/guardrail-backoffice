#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

git config core.hooksPath .githooks
chmod +x .githooks/commit-msg .githooks/pre-commit .githooks/pre-push

echo "Git hooks 경로를 .githooks로 설정했습니다."
