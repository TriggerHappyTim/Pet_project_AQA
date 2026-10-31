#!/usr/bin/env bash
# XPath ratchet for test sources.
#
# The codebase is migrating from XPath to CSS/Playwright selectors
# (data-testid -> role/text -> CSS -> XPath as a last resort).
# To stop the number of XPath usages from growing, existing occurrences are
# frozen in scripts/xpath-baseline.txt; the check fails when a new file appears
# or an existing count grows.
#
# The framework adapter (src/test/java/com/bft/pw) is excluded: it legitimately
# implements $x()/By.xpath() as part of the wrapper.
#
# Usage:
#   scripts/check-xpath.sh            # check (CI / pre-commit)
#   scripts/check-xpath.sh --update   # refresh baseline (only when the count DECREASES!)

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

BASELINE="scripts/xpath-baseline.txt"
PATTERN='\$x\(|By\.xpath\(|"//|"\.//'
EXCLUDE_PATH='src/test/java/com/bft/pw/'
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
  echo "total XPath lines: $(awk '{s+=$2} END {print s+0}' "$BASELINE")"
  exit 0
fi

if [ ! -f "$BASELINE" ]; then
  echo "ERROR: baseline file not found: $BASELINE" >&2
  exit 2
fi

actual="$(collect)"

if [ -z "$actual" ]; then
  echo "OK: no XPath in test code."
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
  echo "XPath usages increased in test code:" >&2
  echo "$violations" >&2
  echo "" >&2
  echo "Prefer data-testid -> role/text -> CSS (:has-text / :text-is) over XPath." >&2
  echo "See docs/PW_GUIDE.md and CONTRIBUTING.md." >&2
  echo "The baseline may only be updated when the total count DECREASES:" >&2
  echo "  scripts/check-xpath.sh --update" >&2
  exit 1
fi

echo "OK: XPath counts are within baseline ($(awk '{s+=$2} END {print s+0}' "$BASELINE") lines)."
