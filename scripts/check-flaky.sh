#!/usr/bin/env bash
# Flaky/retry gate.
#
# Правило (AGENTS.md / CONTRIBUTING.md): флейк не ретраится молча.
#   - тег `flaky` обязан идти в паре с `@DisabledByIssue("TICKET-...")`
#     или `@DisabledByInfrastructure` в том же файле;
#   - `@Retry` допустим только в файлах из scripts/retry-allowlist.txt.
#
# Usage:
#   scripts/check-flaky.sh            # check (CI / pre-commit)

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

SRC="src/test/java"
ALLOW="scripts/retry-allowlist.txt"
fail=0

# 1. @Tag("flaky") без причины отключения.
while IFS= read -r file; do
  [ -n "$file" ] || continue
  if ! grep -qE '@DisabledByIssue|@DisabledByInfrastructure' "$file"; then
    echo "ERROR: $file: тег 'flaky' без @DisabledByIssue(\"TICKET-...\") или @DisabledByInfrastructure" >&2
    fail=1
  fi
done < <(grep -rl --include='*.java' '@Tag("flaky")' "$SRC" 2>/dev/null || true)

# 2. @Retry только из allow-list (строки javadoc/комментарии не считаются).
allowed=""
if [ -f "$ALLOW" ]; then
  allowed="$(grep -vE '^[[:space:]]*(#|$)' "$ALLOW" | tr -d '\r' || true)"
fi

while IFS= read -r hit; do
  [ -n "$hit" ] || continue
  file="${hit%%:*}"
  if ! printf '%s\n' "$allowed" | grep -qxF "$file"; then
    echo "ERROR: $hit: @Retry допустим только из $ALLOW" >&2
    fail=1
  fi
done < <(grep -rnE --include='*.java' '^[[:space:]]*@Retry([[:space:](]|$)' "$SRC" 2>/dev/null || true)

if [ "$fail" -ne 0 ]; then
  echo "" >&2
  echo "Флейк не должен ретраиться молча: тег flaky + issue + @DisabledByIssue," >&2
  echo "инфраструктура — @DisabledByInfrastructure. См. CONTRIBUTING.md." >&2
  exit 1
fi

echo "OK: flaky-теги с причиной отключения, @Retry вне allow-list отсутствует."
