#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

MODE="${1:-all}"

case "${MODE}" in
  format)
    echo "[verify] 포맷만 검증합니다."
    ./gradlew spotlessCheck
    ;;
  static)
    echo "[verify] 포맷과 정적 분석을 검증합니다."
    ./gradlew spotlessCheck checkstyleMain checkstyleTest
    ;;
  test)
    echo "[verify] 테스트와 ArchUnit 규칙을 검증합니다."
    ./gradlew test
    ;;
  all)
    echo "[verify] 전체 하네스 규칙을 검증합니다."
    ./gradlew spotlessCheck checkstyleMain checkstyleTest test
    ;;
  *)
    echo "사용법: ./scripts/verify.sh [format|static|test|all]"
    exit 1
    ;;
esac
