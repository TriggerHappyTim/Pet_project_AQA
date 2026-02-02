# Docker и CI/CD для EVS Testing Framework

Этот документ описывает настройку Docker контейнеризации и CI/CD пайплайна для автоматизированного тестирования.

## 🐳 Docker поддержка

### Быстрый старт

```bash
# Клонировать репозиторий
git clone <repository-url>
cd evs-testing-framework

# Скопировать пример конфигурации
cp docker/env.example .env

# Запустить все сервисы
docker-compose up -d

# Запустить тесты
docker-compose exec evs-tests mvn clean test

# Посмотреть отчеты
open http://localhost:5050
```

### Структура Docker сервисов

```
evs-tests          # Основной контейнер с тестами
selenium-hub       # Selenium Grid Hub
chrome-node        # Chrome браузер
firefox-node       # Firefox браузер
allure-server      # Сервер отчетов Allure
postgres-db        # База данных для тестов (опционально)
```

### Конфигурация окружения

Создайте файл `.env` на основе `docker/env.example`:

```bash
# Копирование примера конфигурации
cp docker/env.example .env

# Редактирование переменных окружения
nano .env
```

#### Основные переменные окружения

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `ENVIRONMENT` | Окружение тестирования | `test` |
| `BROWSER` | Браузер для тестов | `chrome` |
| `THREAD_COUNT` | Количество параллельных потоков | `1` |
| `SELENIDE_REMOTE` | URL Selenium Grid | `http://selenium-hub:4444/wd/hub` |

#### Credentials (НЕ КОММИТТЬ!)

```bash
# EPGU
EPGU_USERNAME=your_epgu_login
EPGU_PASSWORD=your_epgu_password

# РПУ
RPU_USERNAME=your_rpu_login
RPU_PASSWORD=your_rpu_password

# И т.д.
```

### Локальная разработка

```bash
# Запуск только Selenium Grid
docker-compose up selenium-hub chrome-node

# Запуск тестов локально (не в Docker)
./scripts/run-tests.sh --local --smoke

# Запуск тестов в Docker
./scripts/run-tests.sh --docker --regression

# Генерация отчетов
./scripts/run-tests.sh --allure
```

### Продвинутое использование

```bash
# Запуск с конкретным браузером
docker-compose exec evs-tests mvn test -Dbrowser=firefox

# Запуск конкретных тестов
docker-compose exec evs-tests mvn test -Dtest="*SmokeTest*"

# Запуск с отладкой
docker-compose exec evs-tests mvn test -DforkCount=0 -DreuseForks=false

# Просмотр логов контейнеров
docker-compose logs -f evs-tests
```

## 🚀 GitLab CI/CD

### Структура пайплайна

```
validate ── test ── security ── report ── deploy
    │        │         │         │         │
    ├── build │         │         │         │
    │         ├── unit  │         │         │
    │         ├── integration    │         │
    │         └── api            │         │
    │                            │         │
    ├── code_quality             │         │
    └── security_scan            │         │
                                 │         │
                    dependency_scan      │
                                 │         │
                        generate_report ──
                                 │
                    deploy_staging ─── deploy_production
```

### Стадии пайплайна

#### 1. Validate (Валидация)
- **build_docker**: Сборка Docker образа
- **code_quality**: Проверка качества кода (Checkstyle, SpotBugs)
- **security_scan**: Сканирование секретов (Gitleaks)

#### 2. Test (Тестирование)
- **unit_tests**: Модульные тесты
- **integration_tests**: Интеграционные тесты с Selenium Grid
- **api_tests**: API тесты
- **performance_tests**: Нагрузочное тестирование (ручной запуск)

#### 3. Security (Безопасность)
- **security_tests**: Тесты безопасности
- **dependency_scan**: Сканирование уязвимостей зависимостей

#### 4. Report (Отчеты)
- **generate_report**: Генерация Allure отчетов

#### 5. Deploy (Развертывание)
- **deploy_staging**: Деплой в staging (ручной)
- **deploy_production**: Деплой в production (ручной)
- **rollback_production**: Откат production версии (ручной)

### Переменные GitLab CI

#### Обязательные переменные

```yaml
# Docker Registry
CI_REGISTRY_USER: <username>
CI_REGISTRY_PASSWORD: <password>

# Test Credentials (защищенные переменные)
EPGU_USERNAME: <epgu_login>
EPGU_PASSWORD: <epgu_password>
RPU_USERNAME: <rpu_login>
RPU_PASSWORD: <rpu_password>
# ... остальные credentials
```

#### Опциональные переменные

```yaml
# Test Configuration
ENVIRONMENT: staging
BROWSER: chrome
THREAD_COUNT: 2

# Performance
PERFORMANCE_USERS: 100
PERFORMANCE_DURATION: 300

# Notifications
SLACK_WEBHOOK: <webhook_url>
TEAMS_WEBHOOK: <webhook_url>
```

### Настройка защищенных переменных

1. Перейдите в **Settings > CI/CD > Variables**
2. Добавьте переменные с префиксом защищенных значений
3. Используйте тип "File" для сертификатов и ключей

### Секретное сканирование (Gitleaks)

Конфигурация в `.gitleaks.toml` включает:
- Поиск hardcoded credentials
- EPGU/RPU/UOS/EVS credentials
- API ключи и токены
- JWT токены

### Автоматизированные тесты

#### Smoke тесты
```bash
# Запуск ежедневно по расписанию
0 */4 * * *
```

#### Регрессионные тесты
```bash
# Запуск перед релизом
manual
```

#### Security тесты
```bash
# Запуск на MR и main ветке
```

### Деплой стратегия

#### Staging
- Автоматический деплой из `develop`
- Тестирование в staging окружении
- Ручное подтверждение для продакшена

#### Production
- Ручной деплой из `main`
- Blue-Green deployment
- Rollback capability

### Мониторинг и алерты

#### Уведомления
- Slack/Teams интеграция
- Email уведомления о падениях
- Dashboard с метриками

#### Метрики
- Время выполнения тестов
- Процент успешных тестов
- Количество найденных уязвимостей
- Производительность Selenium Grid

## 📊 Мониторинг

### Allure отчеты
```bash
# Локально
docker-compose up allure-server
open http://localhost:5050

# В CI/CD
# Доступны как артефакты пайплайна
```

### Selenium Grid
```bash
# Консоль управления
open http://localhost:4444/ui

# VNC доступ к браузерам
open http://localhost:7900
```

### Логи и отладка
```bash
# Логи контейнеров
docker-compose logs -f evs-tests

# Логи Selenium Grid
docker-compose logs -f selenium-hub

# Доступ к контейнеру
docker-compose exec evs-tests bash
```

## 🛠 Troubleshooting

### Распространенные проблемы

#### 1. Тесты не находят Selenium Grid
```bash
# Проверить статус контейнеров
docker-compose ps

# Проверить логи
docker-compose logs selenium-hub
```

#### 2. Браузеры не запускаются
```bash
# Проверить ресурсы системы
docker system df

# Увеличить ресурсы Docker
# Docker Desktop > Settings > Resources
```

#### 3. Credentials не загружаются
```bash
# Проверить .env файл
cat .env

# Проверить переменные в контейнере
docker-compose exec evs-tests env | grep -E "(USERNAME|PASSWORD)"
```

#### 4. Allure отчет пустой
```bash
# Проверить allure-results директорию
ls -la allure-results/

# Сгенерировать отчет вручную
docker-compose exec allure-server sh -c "allure generate /app/allure-results --clean -o /app/allure-report"
```

## 🔧 Расширение

### Добавление нового браузера
```yaml
# docker-compose.yml
new-browser-node:
  image: selenium/node-edge:latest
  depends_on:
    - selenium-hub
  environment:
    - SE_EVENT_BUS_HOST=selenium-hub
    - SE_NODE_MAX_SESSIONS=3
```

### Добавление новой стадии CI
```yaml
# .gitlab-ci.yml
new_stage:
  stage: custom
  script:
    - echo "Custom stage"
  only:
    - main
```

### Кастомные тесты
```bash
# Новый тип тестов
mvn test -Dtest="*CustomTest*" -Dgroups=custom

# С кастомными переменными
CUSTOM_PARAM=value mvn test
```

## 📞 Поддержка

Для вопросов и проблем:
1. Проверьте логи контейнеров
2. Посмотрите GitLab CI/CD логи
3. Проверьте документацию Allure/Selenium
4. Создайте issue в репозитории