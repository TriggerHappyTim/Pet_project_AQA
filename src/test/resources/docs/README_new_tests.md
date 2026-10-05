# Написание новых тестов

Руководство по созданию нового UI-теста в проекте. Краткая справка — обязательно
прочитайте перед добавлением новой функциональности.

## Структура проекта

```
src/test/java/com/bft/
├── LK_Archive/                    # Тесты ЛК Архива
├── LK_Insurence/                  # Тесты ЛК Страхователя
│   ├── EFS_1/                     #   подмодуль ЕФС-1
│   ├── SZV_ISH/                   #   подмодуль СЗВ-ИСХ
│   ├── SZV_M/                     #   подмодуль СЗВ-М
│   └── SZV_TD/                    #   подмодуль СЗВ-ТД
├── steps/                         # Шаги (AuthSteps, ReportNavigationSteps, ...)
├── ui/pages/                      # Page Objects (LoginPage, MainPage, ...)
├── ui/component/                  # UI-компоненты (Button, Input, Table, DatePicker, ...)
├── ui/core/                       # SmartElement, ClickHelper и пр.
├── jupiter/                       # JUnit 5 аннотации и extensions
│   ├── annotation/                #   @DisabledOnEnvironment, @DisabledByInfrastructure и др.
│   └── extension/                 #   MakeScreenshotsExtension, TestResultWatcher и др.
└── test/
    ├── base/                      # BaseTest, UITestBase
    ├── TestAssertions.java        # Мягкие проверки (auto-verify после каждого теста)
    └── template/                  # Шаблон нового теста (NewFeatureTestTemplate)
```

## Минимальный каркас теста

1. **Скопируйте** `com.bft.test.template.NewFeatureTestTemplate` в пакет
   `com.bft.LK_Insurence.<МОДУЛЬ>` (или `com.bft.LK_Archive`) и переименуйте класс.
2. **Наследуйте** `BaseTest` (для всех тестов) или `UITestBase` (для UI с arrangeActAssert).
3. **Добавьте шаги** как поля класса (не создавайте экземпляры внутри методов):
   ```java
   private final AuthSteps authSteps = new AuthSteps();
   ```
4. **Опишите тест** через `@Test` + Allure-аннотации + теги:
   ```java
   @Test
   @Tag("web")
   @Tag("smoke")
   @Story("История")
   @Description("Детальное описание")
   @Severity(SeverityLevel.CRITICAL)
   public void myFeatureTest() { ... }
   ```
5. **Используйте Arrange-Act-Assert**: авторизация (Arrange) -> шаги через steps (Act) ->
   проверки через `assertions` (Assert).

## Правила

- **Проверки** выполняйте через `assertions` (`com.bft.test.TestAssertions`). `assertAll()`
  вызывается автоматически после каждого теста (`BaseTest` via `TestResultWatcher`),
  явный вызов не требуется.
- **Пользователи**: `com.bft.security.TestUsers` (enum). Учётные данные берутся из
  переменных окружения или `credentials.properties`, пароли в коде не хранятся.
- **Контур**: `UITypeSelector.getSelectedUIType()` (задаётся свойством `evs.ui.type`).
- **Теги** (`@Tag`): обязательны. Существующие примеры — `web`, `smoke`,
  `regression`, `xml-upload`, `manual-creation`, `user-specific`, `efs`, `archive`, `negative`.
- **Логика в тестах**: тест вызывает шаги, шаги — страницы и компоненты. Прямая работа
  с селекторами в тесте допустима для коротких проверок (например,
  `$x("//div[contains(text(), 'ЗЛ сохранено')]").shouldBe(exist)`).
- **Новые селекторы/действия** добавляйте в существующие Page Object / компоненты, а не
  в тестовый метод.
- **Клики**: для устойчивости используйте `ClickHelper` / методы компонентов
  (`ButtonComponent.click`), а не голый `Selenide.click`.
- **Аннотация `@AllureId`**: уникальная, формат `ПРЕФИКС-NNN`.
- **Скриншоты при падении**: автоматически через `MakeScreenshotsExtension`
  (подключён в `BaseTest`).

## Запуск

```bash
# Отдельный тест (JDK 11 обязателен: JAVA_HOME -> corretto-11)
mvn test -Dtest=Efs1#efs_1_xml_krivonosov

# По тегам (JUnit 5)
mvn test -Dgroups=smoke
mvn test -Dgroups=web
mvn test -Dgroups="smoke,web"
mvn test -DexcludedGroups=negative

# Без сети (офлайн), если зависимости уже скачаны
mvn -o test-compile
```

## Чек-лист перед pull request

- [ ] Тест компилируется: `mvn -o test-compile` (exit code 0).
- [ ] Используются шаги и компоненты, а не дублирование селекторов.
- [ ] Есть `@Test` с `@Tag`, `@Story`, `@Description`.
- [ ] Уникальный `@AllureId`.
- [ ] Нет `sleep(...)` и «магических» задержек — используются явные ожидания Selenide.
- [ ] Проверки через `assertions`, а не `System.out`/`assert` JVM.