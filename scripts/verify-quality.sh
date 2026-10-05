#!/usr/bin/env bash
# Local quality gates runner (same checks as CI):
#   1. Checkstyle (test sources included, fail on violation)
#   2. Hard-waits ratchet
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "== Checkstyle =="
mvn -B -DskipTests validate

echo "== Hard-waits ratchet =="
bash scripts/check-hard-waits.sh

echo "OK: all quality gates passed."
