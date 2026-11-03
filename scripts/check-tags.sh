#!/usr/bin/env bash
# Tag consistency gate.
#
# Проверяет, что список тегов в документации, фильтры <groups> в pom.xml и
# фактические @Tag в тестах согласованы между собой:
#   1. каждый тег из <groups> описан в CONTRIBUTING.md (конвенция «Теги обязательны»);
#   2. каждый тег из <groups> реально используется хотя бы в одном тесте;
#   3. каждый фильтр <groups> (конъюнкция) выбирает хотя бы один класс,
#      иначе профиль завершается "BUILD SUCCESS", не выполнив ни одного теста.
#
# Так ловится класс дефекта "тег regress в правилах ≠ фильтр regression в профилях".
#
# Usage:
#   scripts/check-tags.sh            # check (CI / pre-commit)

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

POM="pom.xml"
DOC="CONTRIBUTING.md"
TEST_SRC="src/test/java"

fail=0

# --- 1. Теги, описанные в документации (блок "Теги обязательны") --------------
doc_block="$(awk '
  /Теги обязательны/ { inside = 1 }
  inside && /^[0-9]+\. / && !/Теги обязательны/ { exit }
  inside
' "$DOC")"

doc_tags="$(printf '%s\n' "$doc_block" \
  | grep -oE '`[^`]+`' \
  | tr -d '`' \
  | grep -E '^[a-z][a-z0-9-]*$' \
  | sort -u || true)"

if [ -z "$doc_tags" ]; then
  echo "ERROR: не удалось прочитать список тегов из $DOC (блок «Теги обязательны»)" >&2
  exit 2
fi

# --- 2. Теги из фильтров <groups> в pom.xml -----------------------------------
group_exprs="$(grep -oE '<groups>[^<]+</groups>' "$POM" \
  | sed -E 's#</?groups>##g; s/&amp;/\&/g; s/^[[:space:]]+//; s/[[:space:]]+$//' \
  | sort -u)"

pom_tags="$(printf '%s\n' "$group_exprs" \
  | tr '&|!()' '\n' \
  | tr -d ' ' \
  | grep -E '^[a-z][a-z0-9-]*$' \
  | sort -u || true)"

# --- 3. Теги, реально используемые в тестах -----------------------------------
code_tags="$(grep -rhoE --include='*.java' '@Tag\("[^"]+"\)' "$TEST_SRC" \
  | sed -E 's/.*"([^"]+)".*/\1/' \
  | sort -u || true)"

# 3.1 Тег из pom.xml должен быть описан в документации и использован в коде.
for tag in $pom_tags; do
  if ! grep -qx "$tag" <<< "$doc_tags"; then
    echo "ERROR: pom.xml фильтрует по тегу '$tag', но он не описан в $DOC" >&2
    echo "       Правила и сборка должны называть тег одинаково." >&2
    fail=1
  fi
  if ! grep -qx "$tag" <<< "$code_tags"; then
    echo "ERROR: pom.xml фильтрует по тегу '$tag', но ни один тест не помечен @Tag(\"$tag\")" >&2
    echo "       Такой профиль выполнит 0 тестов и завершится успешно." >&2
    fail=1
  fi
done

# 3.2 Каждый простой фильтр (конъюнкция) должен выбирать хотя бы один класс.
test_files="$(grep -rlE --include='*.java' '@Tag\(' "$TEST_SRC" || true)"

while IFS= read -r expr; do
  [ -n "$expr" ] || continue

  if printf '%s' "$expr" | grep -qE '[|!()]'; then
    echo "SKIP: сложное выражение <groups>$expr</groups> (проверяются только конъюнкции)" >&2
    continue
  fi

  tokens="$(printf '%s' "$expr" \
    | tr '&' '\n' \
    | tr -d ' ' \
    | grep -E '^[a-z][a-z0-9-]*$' || true)"
  [ -n "$tokens" ] || continue

  matched=0
  for file in $test_files; do
    file_tags="$(grep -oE '@Tag\("[^"]+"\)' "$file" | sed -E 's/.*"([^"]+)".*/\1/')"
    ok=1
    while IFS= read -r token; do
      if ! grep -qx "$token" <<< "$file_tags"; then
        ok=0
        break
      fi
    done <<< "$tokens"
    if [ "$ok" -eq 1 ]; then
      matched=1
      break
    fi
  done

  if [ "$matched" -eq 0 ]; then
    echo "ERROR: <groups>$expr</groups> не выбирает ни одного класса — профиль выполнит 0 тестов" >&2
    fail=1
  fi
done <<< "$group_exprs"

# --- Итог ---------------------------------------------------------------------
if [ "$fail" -ne 0 ]; then
  echo "" >&2
  echo "Теги в документации, pom.xml и тестах расходятся. Почини одно из трёх." >&2
  echo "Список тегов: $DOC (конвенция «Теги обязательны»)." >&2
  exit 1
fi

echo "OK: теги согласованы (pom: $(printf '%s\n' $pom_tags | wc -l | tr -d ' ') тега(ов), в коде: $(printf '%s\n' $code_tags | wc -l | tr -d ' '))."
