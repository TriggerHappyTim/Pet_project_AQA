# com.bft.test — Базовые классы, аннотации, хелперы

Модуль тестирования: базовые классы, кастомные аннотации, хелперы, логирование, негативные тесты и retry.

**Всего файлов:** 16

## Структура

### base/
- **UITestBase** — базовый класс UI-тестов (Arrange-Act-Assert)
- **BaseTest** — базовый класс всех тестов (junit-platform, Allure)

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
- **RetryAnalyzer** — JUnit 5 Extension для повторного запуска тестов

BaseTest.java — базовый класс всех тестов (корень com.bft)

### examples/
- ~~ImprovedTestExamples~~ *(закомментирован)*
- ~~AdvancedReportingExample~~ *(закомментирован)*
