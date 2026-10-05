#!/usr/bin/env bash
# Hard-waits ratchet for test sources.
#
# Hard waits (Thread.sleep / PwSession.sleep / bare sleep()) are forbidden in test
# code except for framework polling primitives (com.bft.pw.*, SmartWaits).
# Existing occurrences are frozen in scripts/hard-waits-baseline.txt; the check
# fails when a new file appears or an existing count grows.
#
# Usage:
#   scripts/check-hard-waits.sh            # check (CI / pre-commit)
#   scripts/check-hard-waits.sh --update   # refresh baseline (review the diff!)

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

BASELINE="scripts/hard-waits-baseline.txt"
PATTERN='Thread\.sleep\(|PwSession\.sleep\(|[^A-Za-z0-9_.]sleep\('
EXCLUDE_PATH='src/test/java/com/bft/pw/|SmartWaits\.java'
EXCLUDE_COMMENTS=':[0-9]+:[[:space:]]*(//|\*|/\*)'

collect() {
  grep -rEn "$PATTERN" src/test/java --include='*.java' 2>/dev/null \
    | grep -vE "$EXCLUDE_PATH" \
    | grep -vE "$EXCLUDE_COMMENTS" \
    | cut -d: -f1 \
    | sort | uniq -c | awk '{print $2" "$1}' | sort \
    | tr -d '\r'
}

if [ "${1:-}" = "--update" ]; then
  collect > "$BASELINE"
  echo "Baseline updated: $BASELINE"
  cat "$BASELINE"
  exit 0
fi

if [ ! -f "$BASELINE" ]; then
  echo "ERROR: baseline file not found: $BASELINE" >&2
  exit 2
fi

actual="$(collect)"

if [ -z "$actual" ]; then
  echo "OK: no hard waits in test code."
  exit 0
fi

violations="$(awk '
  NR==FNR { base[$1]=$2; next }
  { act[$1]=$2 }
  END {
    bad=0
    for (f in act) {
      allowed = (f in base) ? base[f] : 0
      if (act[f] > allowed) {
        printf "  %s: %d (allowed %d)\n", f, act[f], allowed
        bad=1
      }
    }
    exit bad
  }
' <(tr -d '\r' < "$BASELINE") <(printf '%s\n' "$actual") || true)"

if [ -n "$violations" ]; then
  echo "Hard waits increased in test code:" >&2
  echo "$violations" >&2
  echo "" >&2
  echo "Use SmartWaits / PwWait / Condition-based waiting instead of sleep()." >&2
  echo "If it is intentional and temporary, update the baseline:" >&2
  echo "  scripts/check-hard-waits.sh --update" >&2
  exit 1
fi

echo "OK: hard-waits counts are within baseline."
