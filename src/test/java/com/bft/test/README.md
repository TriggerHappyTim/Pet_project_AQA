# com.bft.test — Базовые классы, аннотации, хелперы

Модуль тестирования: базовые классы, кастомные аннотации, хелперы, логирование, негативные тесты и retry.

**Всего файлов:** 16

## Структура

### base/
- **UITestBase** — базовый класс UI-тестов (Arrange-Act-Assert)
- ~~ApiTestBase~~ *(закомментирован)*
- **DataDrivenTestBase** — параметризованные тесты

### annotations/
- **TestType** — тип теста (UI, API и т.д.)
- **TestPriority** — приоритет теста
- **TestEnvironment** — требования к окружению
- **Requirement** — связь с требованиями
- **AutomationAction** — описание действий
- ~~ZephyrTest~~ *(закомментирован)*
- **AllureAnnotationProcessor** — обработчик аннотаций для Allure

### helpers/
- **TestSetupHelper** — настройка тестов
- **LoginHelper** — вспомогательные методы авторизации
- **PageObjectHelper** — работа с Page Object
- **AssertionHelper** — мягкие и расширенные проверки
- **SmartWaits** — умные ожидания

### logging/
- **TestLogger** — структурированное логирование
- **AllureIntegration** — прикрепление данных к Allure

### negative/
- **NegativeTestCases** — негативные UI-тесты
- ~~ApiNegativeTestCases~~ *(закомментирован)*

### retry/
- **RetryAnalyzer** — анализатор повторов для TestNG
- **Retry** — аннотация повтора теста

### examples/
- ~~ImprovedTestExamples~~ *(закомментирован)*
- ~~AdvancedReportingExample~~ *(закомментирован)*
