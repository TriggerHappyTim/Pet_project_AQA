# 🚀 Быстрый запуск тестов с Allure отчётом

## ⚡ Самый простой способ

### Вариант 1: Через двойной клик (РЕКОМЕНДУЕТСЯ)

1. Откройте папку в проводнике:
   ```
   <путь_к_проекту>\evs-testing-framework
   ```

2. **Двойной клик** на файл:
   ```
   run-tests-with-allure.bat
   ```

3. Дождитесь окончания (3-5 минут)

4. Браузер откроется автоматически с Allure отчётом

---

## Вариант 2: Через командную строку (CMD)

Откройте **Командную строку** (не PowerShell!):

```cmd
cd /d <путь_к_проекту>\evs-testing-framework

run-tests-with-allure.bat
```

---

## Вариант 3: Через IntelliJ IDEA Terminal

В IDEA откройте Terminal и выполните:

```bash
./run-tests-with-allure.bat
```

Или выберите **CMD** в выпадающем списке Terminal (справа вверху) вместо PowerShell.

---

## 📋 Что делает скрипт?

1. ✅ Очищает старые результаты
2. ✅ Запускает тест `Efs1#efs_1_xml` в **Firefox**
3. ✅ Генерирует Allure отчёт
4. ✅ Открывает отчёт в браузере

---

## ⏱️ Ожидаемое время выполнения

- Очистка: 1 сек
- Maven сборка: 30-60 сек
- Выполнение теста: 60-120 сек
- Генерация отчёта: 10-20 сек

**Итого:** 2-4 минуты

---

## 🎯 Альтернативные команды

### Только запуск тестов (без отчёта)

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml
```

### Только генерация отчёта (если тесты уже запущены)

```cmd
mvn allure:serve
```

### Запуск всех тестов EFS-1

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1
```

### Запуск с конкретным пользователем

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_krivonosov
```

---

## 🐛 Troubleshooting

### Проблема: "Firefox не найден"

**Решение:**
1. Установите Firefox: https://www.mozilla.org/firefox/
2. Или используйте Chrome: замените в команде `browser_firefox_local` на `browser_chrome_local`

### Проблема: "Maven не найден"

**Решение:**
1. Откройте IntelliJ IDEA
2. Maven встроен в IDEA - используйте IDEA Terminal

### Проблема: "Allure отчёт не генерируется"

**Решение:**
```cmd
mvn io.qameta.allure:allure-maven:2.29.0:serve
```

---

## 📊 Просмотр отчёта

После запуска откроется браузер с Allure отчётом на адресе:
```
http://localhost:[случайный_порт]/
```

Отчёт покажет:
- ✅ Статус теста (passed/failed)
- 📸 Скриншоты при ошибках
- 📄 Page source
- 🔍 Подробные шаги теста
- ⏱️ Время выполнения
- 📊 Графики и статистику

---

## 🎉 Готово!

Теперь просто **дважды кликните** на `run-tests-with-allure.bat` и наслаждайтесь красивым отчётом! 🚀

---

## 🔧 Локальный запуск в стиле GitLab CI/CD

Для запуска тестов локально с теми же параметрами, что используются в GitLab CI/CD, создайте файлы:

### Windows (BAT)
Создайте файл `scripts/run-local-gitlab-style.bat`:

```batch
@echo off
REM Скрипт для локального запуска тестов, имитирующий настройки GitLab CI/CD
REM Использование: run-local-gitlab-style.bat [ENVIRONMENT] [TEST_SCRIPT] [USER_ACCOUNT] [SINGLE_TEST] [SUITE]

setlocal enabledelayedexpansion

REM Установка значений по умолчанию (как в GitLab CI/CD)
set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=test

set TEST_SCRIPT=%2
if "%TEST_SCRIPT%"=="" set TEST_SCRIPT=smoke_web

set USER_ACCOUNT=%3
if "%USER_ACCOUNT%"=="" set USER_ACCOUNT=user_krivonosov

set SINGLE_TEST=%4
set SUITE=%5

echo ========================================
echo Локальный запуск тестов (GitLab CI/CD стиль)
echo ========================================
echo ENVIRONMENT: %ENVIRONMENT%
echo TEST_SCRIPT: %TEST_SCRIPT%
echo USER_ACCOUNT: %USER_ACCOUNT%
if not "%SINGLE_TEST%"=="" echo SINGLE_TEST: %SINGLE_TEST%
if not "%SUITE%"=="" echo SUITE: %SUITE%
echo ========================================
echo.

REM Переход в директорию проекта
cd /d "%~dp0.."

REM Формирование команды Maven
set MVN_CMD=mvn clean test --activate-profiles %ENVIRONMENT%,%TEST_SCRIPT%,%USER_ACCOUNT%

REM Добавление параметров если указаны
if not "%SINGLE_TEST%"=="" set MVN_CMD=!MVN_CMD! -Dtest=%SINGLE_TEST%
if not "%SUITE%"=="" set MVN_CMD=!MVN_CMD! -Dsuite=%SUITE%

REM Добавление параметров для имитации GitLab CI/CD
set MVN_CMD=!MVN_CMD! --batch-mode --errors --fail-at-end --show-version

echo Выполняется команда:
echo !MVN_CMD!
echo.

REM Запуск тестов
!MVN_CMD!

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Тесты успешно завершены!
    echo ========================================
    echo Для просмотра Allure отчета выполните:
    echo mvn allure:serve
) else (
    echo.
    echo ========================================
    echo Тесты завершились с ошибками!
    echo ========================================
    exit /b %ERRORLEVEL%
)
```

### Linux/Mac (Bash)
Создайте файл `scripts/run-local-gitlab-style.sh`:

```bash
#!/bin/bash
# Скрипт для локального запуска тестов, имитирующий настройки GitLab CI/CD
# Использование: ./run-local-gitlab-style.sh [ENVIRONMENT] [TEST_SCRIPT] [USER_ACCOUNT] [SINGLE_TEST] [SUITE]

ENVIRONMENT=${1:-test}
TEST_SCRIPT=${2:-smoke_web}
USER_ACCOUNT=${3:-user_krivonosov}
SINGLE_TEST=${4:-}
SUITE=${5:-}

echo "========================================"
echo "Локальный запуск тестов (GitLab CI/CD стиль)"
echo "========================================"
echo "ENVIRONMENT: $ENVIRONMENT"
echo "TEST_SCRIPT: $TEST_SCRIPT"
echo "USER_ACCOUNT: $USER_ACCOUNT"
[ -n "$SINGLE_TEST" ] && echo "SINGLE_TEST: $SINGLE_TEST"
[ -n "$SUITE" ] && echo "SUITE: $SUITE"
echo "========================================"
echo ""

cd "$(dirname "$0")/.." || exit 1

MVN_CMD="mvn clean test --activate-profiles $ENVIRONMENT,$TEST_SCRIPT,$USER_ACCOUNT"

[ -n "$SINGLE_TEST" ] && MVN_CMD="$MVN_CMD -Dtest=$SINGLE_TEST"
[ -n "$SUITE" ] && MVN_CMD="$MVN_CMD -Dsuite=$SUITE"

MVN_CMD="$MVN_CMD --batch-mode --errors --fail-at-end --show-version"

echo "Выполняется команда:"
echo "$MVN_CMD"
echo ""

eval "$MVN_CMD"

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Тесты успешно завершены!"
    echo "========================================"
    echo "Для просмотра Allure отчета выполните:"
    echo "mvn allure:serve"
else
    echo ""
    echo "========================================"
    echo "Тесты завершились с ошибками!"
    echo "========================================"
    exit 1
fi
```

### Примеры использования:

```cmd
REM Базовый запуск
scripts\run-local-gitlab-style.bat

REM Запуск одного теста
scripts\run-local-gitlab-style.bat test smoke_web user_krivonosov "CryptoProCertificateTest#verifyCryptoProPluginLoaded"

REM Запуск EFS-1 тестов
scripts\run-local-gitlab-style.bat test smoke_efs user_krivonosov
```
