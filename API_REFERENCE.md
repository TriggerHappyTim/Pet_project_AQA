# Справочник API EVS Testing Framework

## 📚 Обзор

Этот справочник содержит полную документацию по API EVS Testing Framework. Здесь описаны все публичные классы, интерфейсы, методы и их использование.

## 🏗️ Core API

### TestConfiguration

Главная конфигурационная точка фреймворка.

```java
public final class TestConfiguration {

    // Инициализация
    public static synchronized void initialize(TestStrategyType strategyType)

    // Получение компонентов
    public static BrowserFactoryManager getBrowserFactoryManager()
    public static TestStrategyManager getStrategyManager()
    public static SoftAssert getSoftAssert()

    // Сброс конфигурации
    public static void reset()

    // Проверка типа теста
    public static boolean isUITest()
    public static boolean isApiTest()
    public static boolean isMobileTest()
}
```

### TestContext

Контекст выполнения теста с атрибутами и метаданными.

```java
public class TestContext {
    // Конструкторы
    public TestContext()
    public TestContext(String testName)

    // Метаданные теста
    public String getTestName()
    public void setTestName(String testName)

    public TestStrategyType getStrategyType()
    public void setStrategyType(TestStrategyType strategyType)

    // Атрибуты
    public void setAttribute(String key, Object value)
    public <T> T getAttribute(String key, Class<T> type)
    public Object getAttribute(String key)
    public boolean hasAttribute(String key)
    public void removeAttribute(String key)

    // Управление жизненным циклом
    public void startTimer()
    public long getExecutionTime()
    public boolean isTimeoutExceeded(long timeoutMs)

    // Сериализация
    public String toJson()
    public static TestContext fromJson(String json)
}
```

### TestResult

Результат выполнения теста или стратегии.

```java
public class TestResult {
    // Конструкторы
    public TestResult()
    public TestResult(boolean success)

    // Статус
    public boolean isSuccess()
    public void setSuccess(boolean success)

    public boolean isFailure()
    public boolean hasErrors()

    // Ошибки
    public Exception getError()
    public void setError(Exception error)
    public void setError(String message)

    // Метаданные
    public String getMessage()
    public void setMessage(String message)

    public long getExecutionTime()
    public void setExecutionTime(long executionTime)

    // Данные результата
    public void setData(String key, Object value)
    public <T> T getData(String key, Class<T> type)
    public Object getData(String key)

    // Скриншоты и артефакты
    public void addScreenshot(String path)
    public List<String> getScreenshots()

    public void addArtifact(String name, Object artifact)
    public <T> T getArtifact(String name, Class<T> type)
}
```

## 🖥️ UI Components API

### BaseComponent

Базовый класс для всех UI компонентов.

```java
public abstract class BaseComponent {
    protected final SmartElement rootElement;
    protected final String componentName;
    protected final Logger logger;

    // Проверка состояния
    public boolean isPresent()
    public boolean isVisible()
    public boolean isEnabled()

    // Ожидания
    protected void waitWithStrategy(WaitStrategy strategy)
    protected void waitForCondition(BooleanSupplier condition, Duration timeout)

    // Абстрактные методы
    public abstract boolean isValid()

    // Утилиты
    protected SmartElement findElement(String selector)
    protected SmartElementList findElements(String selector)
    protected void executeJavaScript(String script, Object... args)
}
```

### ButtonComponent

Компонент для работы с кнопками всех типов.

```java
public class ButtonComponent extends BaseComponent {

    // Статические фабрики
    public static ButtonComponent createButton(String text)
    public static ButtonComponent createPrimaryButton(String text)
    public static ButtonComponent createSecondaryButton(String text)
    public static ButtonComponent createPrimaryButtonByIndex(String buttonName, int index)  // Рекомендуется
    public static ButtonComponent createSecondaryButtonByIndex(String buttonName, int index) // Рекомендуется
    public static ButtonComponent createFooterButton(String text)
    public static ButtonComponent createSpanButton(String text)
    public static ButtonComponent createCustomButton(String selector, String name)
    
    // @Deprecated методы (используйте createPrimaryButtonByIndex/createSecondaryButtonByIndex)
    @Deprecated public static ButtonComponent createPrimaryButtonFirst(String buttonName)
    @Deprecated public static ButtonComponent createPrimaryButtonSecond(String buttonName)
    @Deprecated public static ButtonComponent createSecondaryButtonSecond(String buttonName)
    @Deprecated public static ButtonComponent createSecondaryButtonThird(String buttonName)
    @Deprecated public static ButtonComponent createSecondaryButtonFourth(String buttonName)
    @Deprecated public static ButtonComponent createSecondaryButtonFifth(String buttonName)
    @Deprecated public static ButtonComponent createSecondaryButtonSixth(String buttonName)

    // Действия
    public ButtonComponent click()
    public ButtonComponent doubleClick()
    public ButtonComponent rightClick()

    // Состояние
    @Override
    public boolean isValid()

    // Свойства
    public String getText()
    public boolean isDisabled()
    public String getButtonType()
}
```

### InputComponent

Компонент для работы с текстовыми полями ввода.

```java
public class InputComponent extends BaseComponent {

    // Статические фабрики
    public static InputComponent createLabeledInput(String label, String name)
    public static InputComponent createIdInput(String id, String name)
    public static InputComponent createNamedInput(String name, String displayName)
    public static InputComponent createCustomInput(String selector, String name)

    // Установка значений
    public InputComponent setValue(String value)
    public InputComponent clearAndSetValue(String value)
    public InputComponent appendText(String text)
    public InputComponent setNumber(long number)
    public InputComponent setNumber(double number)

    // Получение значений
    public String getValue()
    public String getPlaceholder()
    public String getInputType()

    // Проверка
    public boolean hasValue(String expected)
    public boolean isEmpty()
    public boolean isNotEmpty()

    // Специфические методы
    public InputComponent setMaskedValue(String value)  // для полей с маской
    public boolean checkMaxLength(int maxLength)

    // Состояние
    @Override
    public boolean isValid()
    public boolean isReadonly()
    public boolean isRequired()
}
```

### SelectComponent

Компонент для работы с выпадающими списками.

```java
public class SelectComponent extends BaseComponent {

    // Статические фабрики
    public static SelectComponent createMuiInput(String label, String name)
    public static SelectComponent createMuiSpan(String spanText, String name)
    public static SelectComponent createSelect(String name)
    public static SelectComponent createCustomSelect(String selector, String name)

    // Выбор значений
    public SelectComponent selectByText(String text)
    public SelectComponent selectByValue(String value)
    public SelectComponent selectByIndex(int index)

    // Получение значений
    public String getSelectedValue()
    public String getSelectedText()
    public List<String> getAllOptions()

    // Проверка
    public boolean isOptionSelected(String text)
    public boolean isValueSelected(String value)
    public boolean hasOption(String text)
    public int getOptionCount()

    // Состояние
    @Override
    public boolean isValid()
    public boolean isMultiple()
}
```

### CheckboxComponent

Компонент для работы с чекбоксами.

```java
public class CheckboxComponent extends BaseComponent {

    // Статические фабрики
    public static CheckboxComponent createByLabel(String label, String name)
    public static CheckboxComponent createById(String id, String name)
    public static CheckboxComponent createCustom(String selector, String name)

    // Управление состоянием
    public CheckboxComponent check()
    public CheckboxComponent uncheck()
    public CheckboxComponent setChecked(boolean checked)
    public CheckboxComponent toggle()

    // Проверка состояния
    public boolean isChecked()
    public boolean isSelected()

    // Свойства
    public String getValue()
    public String getLabelText()

    // Условные операции
    public CheckboxComponent ensureChecked()
    public CheckboxComponent ensureUnchecked()

    // Состояние
    @Override
    public boolean isValid()
    public boolean isEnabled()
}
```

### RadioButtonComponent

Компонент для работы с радиокнопками.

```java
public class RadioButtonComponent extends BaseComponent {

    // Статические фабрики
    public static RadioButtonComponent createByLabel(String label, String name)
    public static RadioButtonComponent createBySpanText(String spanText, String name)
    public static RadioButtonComponent createByValue(String value, String name)
    public static RadioButtonComponent createByName(String name, String value, String displayName)

    // Управление выбором
    public RadioButtonComponent select()
    public RadioButtonComponent ensureSelected()

    // Проверка состояния
    public boolean isSelected()

    // Свойства
    public String getValue()
    public String getLabelText()
    public String getGroupName()

    // Проверка в группе
    public boolean isSelectedInGroup(String groupName)

    // Состояние
    @Override
    public boolean isValid()
    public boolean isEnabled()
}
```

### DateComponent

Компонент для работы с полями дат.

```java
public class DateComponent extends BaseComponent {

    // Статические фабрики
    public static DateComponent createLabeledDate(String label, String name)
    public static DateComponent createIdDate(String id, String name)
    public static DateComponent createCustomDate(String selector, String name)

    // Установка дат
    public DateComponent setDate(String date)           // "01-01-2000"
    public DateComponent setDate(int day, int month, int year)
    public DateComponent setCurrentDate()
    public DateComponent setDateRelative(int daysOffset)

    // Получение даты
    public String getDate()

    // Проверка
    public boolean hasDate(String expected)
    public boolean isValidDate()
    public boolean isPastDate()
    public boolean isFutureDate()

    // Управление
    public DateComponent clearDate()

    // Состояние
    @Override
    public boolean isValid()
}
```

### TextareaComponent

Компонент для работы с многострочными текстовыми областями.

```java
public class TextareaComponent extends BaseComponent {

    // Статические фабрики
    public static TextareaComponent createLabeledTextarea(String labelId, String textareaId, String name)
    public static TextareaComponent createIdTextarea(String id, String name)
    public static TextareaComponent createCustomTextarea(String selector, String name)

    // Установка текста
    public TextareaComponent setText(String text)
    public TextareaComponent appendText(String text)
    public TextareaComponent setMultilineText(String... lines)

    // Получение текста
    public String getText()
    public int getLineCount()
    public int getCharacterCount()

    // Проверка
    public boolean hasText(String expected)
    public boolean containsText(String text)
    public boolean isEmpty()
    public boolean isNotEmpty()

    // Валидация
    public boolean checkMaxLength(int maxLength)

    // Свойства
    public String getPlaceholder()
    public boolean isReadonly()

    // Состояние
    @Override
    public boolean isValid()
}
```

### FileComponent

Компонент для работы с загрузкой файлов.

```java
public class FileComponent extends BaseComponent {

    // Статические фабрики
    public static FileComponent createModalFileInput(String name)
    public static FileComponent createIdFileInput(String id, String name)
    public static FileComponent createCustomFileInput(String selector, String name)

    // Загрузка файлов
    public FileComponent uploadFile(String filePath)
    public FileComponent uploadFromClasspath(String fileName)
    public FileComponent uploadMultipleFromClasspath(String... fileNames)

    // Проверка
    public boolean hasFileSelected()
    public String getSelectedFileName()

    // Управление
    public FileComponent clearSelection()

    // Свойства
    public String getAcceptedTypes()
    public boolean allowsMultipleFiles()

    // Состояние
    @Override
    public boolean isValid()
}
```

### TableComponent

Компонент для работы с табличными данными.

```java
public class TableComponent extends BaseComponent {

    // Статические фабрики
    public static TableComponent createMainTable(String name)
    public static TableComponent createResultsTable(String name)
    public static TableComponent createClassTable(String className, String name)
    public static TableComponent createCustomTable(String selector, String name)

    // Информация о таблице
    public int getRowCount()
    public int getColumnCount()
    public boolean isNotEmpty()
    public boolean isEmpty()

    // Работа со строками
    public TableComponent clickRow(int rowIndex)
    public TableComponent clickFirstRow()
    public String[] getRowText(int rowIndex)

    // Работа с ячейками
    public TableComponent clickCell(int rowIndex, int columnIndex)
    public String getCellText(int rowIndex, int columnIndex)

    // Поиск
    public int findRowByCellText(int columnIndex, String text)
    public TableComponent clickRowByText(String text)
    public TableComponent clickRowByText(String text, int columnIndex)

    // Валидация размеров
    public boolean hasExpectedRowCount(int expected)
    public boolean hasAtLeastRows(int minRowCount)

    // Заголовок и ошибки
    public String getTableTitle()
    public boolean hasError()
    public TableComponent closeErrorAlert()

    // Ожидание
    public TableComponent waitForLoad()

    // Состояние
    @Override
    public boolean isValid()
}
```

### NavigationComponent

Компонент для работы с навигацией и вкладками.

```java
public class NavigationComponent extends BaseComponent {

    // Статические фабрики
    public static NavigationComponent createMainNavigation(String name)
    public static NavigationComponent createTabNavigation(String name)
    public static NavigationComponent createCustomNavigation(String selector, String name)

    // Переходы по вкладкам
    public NavigationComponent openTab(String tabName)
    public NavigationComponent openTable()
    public NavigationComponent selectSection(String sectionName)

    // Комплексная навигация
    public NavigationComponent navigateSidebar(String parentClass, String parentText,
                                            String childClass, String childText)

    // Специфические переходы
    public NavigationComponent goToInsuredPersons()
    public NavigationComponent goToReports()
    public NavigationComponent goToInsurerAccount()

    // Проверки
    public boolean isOnTab(String tabName)
    public boolean isInSection(String sectionName)

    // Браузер
    public String getCurrentUrl()
    public NavigationComponent refreshPage()
    public NavigationComponent goBack()

    // Состояние
    @Override
    public boolean isValid()
}
```

## 🎯 Strategies API

### TestExecutionStrategy

Интерфейс для всех стратегий выполнения тестов.

```java
public interface TestExecutionStrategy {

    // Выполнение стратегии
    TestResult execute(TestContext context)

    // Метаданные
    TestStrategyType getType()
    String getDescription()
    boolean isApplicable(TestContext context)

    // Настройка
    void configure(Map<String, Object> config)
    Map<String, Object> getConfiguration()
}
```

### TestStrategyManager

Менеджер для управления стратегиями.

```java
public class TestStrategyManager {

    // Получение экземпляра
    public static TestStrategyManager getInstance()

    // Регистрация стратегий
    public void registerStrategy(TestStrategyType type, TestExecutionStrategy strategy)
    public void unregisterStrategy(TestStrategyType type)
    public boolean isStrategyRegistered(TestStrategyType type)

    // Выполнение
    public TestResult executeTestWithStrategy(TestContext context, TestStrategyType strategyType)
    public TestResult executeTestWithStrategy(String testName, TestStrategyType strategyType, SoftAssert softAssert)

    // Управление
    public List<TestStrategyType> getAvailableExecutionStrategies()
    public TestExecutionStrategy getStrategy(TestStrategyType type)
    public void clearStrategies()

    // Конфигурация
    public void setDefaultTimeout(Duration timeout)
    public Duration getDefaultTimeout()
}
```

### CryptoProValidationStrategy

Стратегия для валидации плагина КриптоПРО.

```java
public class CryptoProValidationStrategy implements TestExecutionStrategy {

    // Конструктор
    public CryptoProValidationStrategy()

    // TestExecutionStrategy implementation
    @Override
    public TestResult execute(TestContext context)

    @Override
    public TestStrategyType getType()  // возвращает UI_CRYPTO_PRO_VALIDATION

    @Override
    public String getDescription()

    // Специфические методы
    public boolean validateExtension()
    public boolean validatePlugin()
    public boolean validateCSP()
    public boolean validateObjects()
    public boolean validateCertificateSelection()

    // Конфигурация
    public void setValidationTimeout(Duration timeout)
    public void setScreenshotOnFailure(boolean enabled)
}
```

## 🔐 Security API

### CredentialManager

Менеджер для работы с учетными данными.

```java
public class CredentialManager {

    // Получение экземпляра
    public static CredentialManager getInstance()

    // Управление провайдерами
    public void registerProvider(String name, CredentialProvider provider)
    public void unregisterProvider(String name)
    public Set<String> getAvailableProviders()

    // Получение учетных данных
    public Credentials getCredentials(String username)
    public Credentials getCredentials(String username, String providerName)

    // Кеширование
    public void enableCaching(Duration ttl)
    public void disableCaching()
    public void clearCache()

    // Шифрование
    public String encrypt(String data)
    public String decrypt(String encryptedData)
}
```

### CredentialProvider

Интерфейс для провайдеров учетных данных.

```java
public interface CredentialProvider {

    // Получение учетных данных
    Credentials getCredentials(String username)

    // Метаданные
    String getProviderName()
    boolean isAvailable()
    Set<String> getSupportedCredentials()

    // Управление
    void initialize(Map<String, Object> config)
    void shutdown()
}
```

### SecureLogger

Безопасный логгер с автоматической маскировкой.

```java
public class SecureLogger {

    // Получение логгера
    public static SecureLogger getLogger(Class<?> clazz)
    public static SecureLogger getLogger(String name)

    // Логирование с маскировкой
    public void info(String message, Object... args)
    public void debug(String message, Object... args)
    public void warn(String message, Object... args)
    public void error(String message, Object... args)
    public void error(String message, Throwable throwable, Object... args)

    // Конфигурация маскировки
    public void enableMasking(boolean enabled)
    public void addMasker(DataMasker masker)
    public void removeMasker(String maskerName)

    // Проверка маскировки
    public String mask(String data)
    public boolean isSensitive(String data)
}
```

### DataMasker

Интерфейс для маскировки данных.

```java
public interface DataMasker {

    // Маскировка
    String mask(String data)
    boolean canMask(String data)

    // Метаданные
    String getName()
    String getDescription()
    MaskerType getType()

    // Конфигурация
    void configure(Map<String, Object> config)
    boolean isEnabled()
    void setEnabled(boolean enabled)
}
```

## 🧪 Test Infrastructure API

### BaseTest

Базовый класс для всех тестов.

```java
public abstract class BaseTest {

    // Защищенные поля
    protected SoftAssert softAssert;
    protected TestLogger logger;
    protected TestContext testContext;

    // Жизненный цикл
    @BeforeSuite
    public static void globalSetup()

    @BeforeTest
    public void setupTest()

    @AfterTest
    public void teardownTest()

    @AfterSuite
    public static void globalTeardown()

    // Утилиты
    protected void arrangeActAssert(Runnable arrange, Runnable act, Consumer<SoftAssert> assertConsumer)
    protected void arrangeActAssert(Runnable arrange, Runnable act, Consumer<SoftAssert> assertConsumer, String testName)
    protected void performAction(String description, Runnable action)
    protected void performCheck(String description, Runnable check)
}
```

### UITestBase

Базовый класс для UI тестов.

```java
public abstract class UITestBase extends BaseTest {

    // Дополнительная настройка UI
    @BeforeMethod
    public void setupUITestMethod()

    // Утилиты UI тестирования
    protected boolean isUIEnvironment()
    protected void takeScreenshot(String name)
    protected void waitForPageLoad()
    protected void confirmAlert()
    protected void dismissAlert()
}
```

### ApiTestBase

Базовый класс для API тестов.

```java
public abstract class ApiTestBase extends BaseTest {

    // HTTP клиент
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;

    // Настройка API
    @BeforeMethod
    public void setupApiTestMethod()

    // Утилиты API тестирования
    protected boolean isApiEnvironment()
    protected ValidatableResponse sendRequest(RequestSpecification request)
    protected void validateResponse(ValidatableResponse response)
}
```

## 🛠️ Utilities API

### ElementFactory

Фабрика для создания SmartElement'ов.

```java
public final class ElementFactory {

    // По селекторам
    public static SmartElementBuilder css(String cssSelector)
    public static SmartElementBuilder xpath(String xpathSelector)
    public static SmartElementBuilder id(String id)
    public static SmartElementBuilder name(String name)
    public static SmartElementBuilder className(String className)

    // По объектам
    public static SmartElementBuilder by(By locator)
    public static SmartElementBuilder selenide(SelenideElement element)

    // Списки элементов
    public static SmartElementListBuilder list(By locator)
    public static SmartElementListBuilder list(String cssSelector)
}
```

### SmartElement

Умный элемент с встроенными ожиданиями.

```java
public class SmartElement {
    // Конфигурация
    public SmartElement named(String name)
    public SmartElement timeout(Duration timeout)
    public SmartElement waitVisible()
    public SmartElement waitClickable()
    public SmartElement waitEnabled()

    // Действия
    public SmartElement click()
    public SmartElement doubleClick()
    public SmartElement rightClick()
    public SmartElement type(String text)
    public SmartElement clear()
    public SmartElement pressEnter()

    // Проверки
    public boolean isVisible()
    public boolean isEnabled()
    public boolean isPresent()
    public boolean hasText(String text)
    public boolean hasValue(String value)

    // Получение данных
    public String getText()
    public String getAttribute(String name)
    public String getValue()

    // Навигация
    public SmartElement find(String selector)
    public SmartElementList findAll(String selector)
    public SmartElement parent()
    public SmartElement child(int index)

    // JavaScript
    public Object executeScript(String script, Object... args)
}
```

### WaitStrategies

Стратегии ожидания элементов (используются внутри компонентов).

```java
public final class WaitStrategies {
    // Условные ожидания
    public static WaitStrategy visibility()
    public static WaitStrategy invisibility()
    public static WaitStrategy clickability()
    public static WaitStrategy presence()
    public static WaitStrategy text(String expectedText)
    public static WaitStrategy value(String expectedValue)

    // Сложные ожидания
    public static WaitStrategy and(WaitStrategy... strategies)
    public static WaitStrategy or(WaitStrategy... strategies)
    public static WaitStrategy not(WaitStrategy strategy)

    // Кастомные ожидания
    public static WaitStrategy condition(String description, BooleanSupplier condition)
    public static WaitStrategy jsCondition(String jsExpression, Object... args)
    
    // Примечание: sleep() удален, используйте StandardWaits для типовых ожиданий
}
```

**Рекомендация:** Для типовых ожиданий используйте `StandardWaits` вместо прямого использования `WaitStrategies`.

## 📊 Annotations API

### TestEnvironment

Аннотация для указания среды тестирования.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestEnvironment {

    Environment[] value() default {};

    enum Environment {
        LOCAL, DEV, STAGING, PROD, CI
    }
}
```

### AutomationStatus

Аннотация для статуса автоматизации.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutomationStatus {

    Status value() default Status.AUTOMATED;

    enum Status {
        MANUAL, AUTOMATED, DEPRECATED, BROKEN
    }
}
```

### Priority

Аннотация для приоритета тестов.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {

    Level value() default Level.MEDIUM;

    enum Level {
        CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL
    }
}
```

### Requirement

Аннотация для привязки к требованиям.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    String[] value();  // ID требований
    String description() default "";
    String link() default "";
}
```

## 📈 Exceptions API

### Framework Exceptions

```java
// Базовое исключение фреймворка
public class FrameworkException extends RuntimeException {
    public FrameworkException(String message)
    public FrameworkException(String message, Throwable cause)
}

// Исключения компонентов
public class ComponentException extends FrameworkException {
    public ComponentException(String message)
    public ComponentException(String message, Throwable cause)
}

// Исключения стратегий
public class StrategyException extends FrameworkException {
    public StrategyException(String message)
    public StrategyException(String message, Throwable cause)
}

// Исключения безопасности
public class SecurityException extends FrameworkException {
    public SecurityException(String message)
    public SecurityException(String message, Throwable cause)
}

// Таймауты
public class TimeoutException extends FrameworkException {
    public TimeoutException(String message)
    public TimeoutException(String message, Throwable cause)
}
```

## 🔧 Configuration Classes

### TestStrategyType

Перечисление типов стратегий тестирования (из пакета `com.bft.config`).

```java
public enum TestStrategyType {
    // Общие типы
    UI("UI Testing"),
    API("API Testing"),
    MOBILE("Mobile Testing");

    private final String description;

    TestStrategyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

**Примечание:** Для специфических стратегий выполнения тестов используйте `ExecutionStrategyType` из пакета `com.bft.strategy`.

### UIType

Перечисление типов UI приложений.

```java
public enum UIType {
    WEB_PORTAL("Web Portal", "https://portal.test.ecp"),
    WEB_ADMIN("Web Admin", "https://admin.test.ecp"),
    MOBILE_WEB("Mobile Web", "https://mobile.test.ecp");

    private final String displayName;
    private final String defaultUrl;

    UIType(String displayName, String defaultUrl) {
        this.displayName = displayName;
        this.defaultUrl = defaultUrl;
    }
}
```

## 📋 Data Classes

### Credentials

Класс для хранения учетных данных.

```java
public class Credentials {
    private final String username;
    private final String password;
    private final Map<String, Object> attributes;

    public Credentials(String username, String password)
    public Credentials(String username, String password, Map<String, Object> attributes)

    // Геттеры
    public String getUsername()
    public String getPassword()
    public <T> T getAttribute(String key, Class<T> type)
    public Map<String, Object> getAttributes()

    // Утилиты
    public boolean hasAttribute(String key)
    public boolean isValid()
}
```

### ValidationResult

Результат валидации.

```java
public class ValidationResult {
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> data;

    public ValidationResult()

    // Ошибки
    public void addError(String error)
    public List<String> getErrors()
    public boolean hasErrors()

    // Предупреждения
    public void addWarning(String warning)
    public List<String> getWarnings()
    public boolean hasWarnings()

    // Данные
    public void setData(String key, Object value)
    public <T> T getData(String key, Class<T> type)

    // Общий статус
    public boolean isValid()
    public void merge(ValidationResult other)
}
```

## 🎯 Usage Examples

### Простой UI тест

```java
@Test
@Story("Регистрация пользователя")
@Description("Тест регистрации нового пользователя")
public void userRegistrationTest() {
    arrangeActAssert(
        () -> logger.info("Подготовка данных пользователя"),

        () -> {
            performAction("Открытие страницы регистрации",
                () -> page.openRegistrationPage());

            performAction("Заполнение формы",
                () -> page.fillRegistrationForm("Иван", "ivan@example.com", "password123"));

            performAction("Отправка формы",
                () -> page.submitForm());
        },

        (softAssert) -> {
            performCheck("Проверка успешной регистрации",
                () -> softAssert.assertTrue(page.isSuccessMessageDisplayed()));

            performCheck("Проверка создания пользователя",
                () -> softAssert.assertTrue(userService.exists("ivan@example.com")));
        },

        "Регистрация пользователя"
    );
}
```

### Стратегическое тестирование

```java
@Test
public void cryptoProValidationTest() {
    TestContext context = new TestContext("CryptoPro Plugin Validation");
    context.setAttribute("timeout", Duration.ofSeconds(30));

    TestResult result = strategyManager.executeTestWithStrategy(
        context,
        TestStrategyType.UI_CRYPTO_PRO_VALIDATION
    );

    softAssert.assertTrue(result.isSuccess(),
        "Валидация КриптоПРО плагина должна пройти успешно");

    if (!result.isSuccess()) {
        logger.error("Ошибка валидации: {}", result.getMessage());
        takeScreenshot("cryptopro_validation_failure");
    }
}
```

### Работа с компонентами

```java
@Test
public void advancedFormTest() {
    // Использование компонентов напрямую
    InputComponent nameField = InputComponent.createLabeledInput("ФИО", "ФИО");
    SelectComponent regionSelect = SelectComponent.createMuiInput("Регион", "Регион");
    CheckboxComponent agreementCheckbox = CheckboxComponent.createByLabel("Согласие", "Согласие");
    ButtonComponent submitButton = ButtonComponent.createPrimaryButton("Отправить");

    // Комплексное взаимодействие
    nameField.setValue("Иванов Иван Иванович");
    regionSelect.selectByText("Московская область");
    agreementCheckbox.check();

    // Валидация перед отправкой
    softAssert.assertTrue(nameField.isNotEmpty());
    softAssert.assertTrue(regionSelect.isOptionSelected("Московская область"));
    softAssert.assertTrue(agreementCheckbox.isChecked());
    softAssert.assertTrue(submitButton.isEnabled());

    submitButton.click();
}
```

## 🛠️ Helpers API

### ConfigReader

Централизованное чтение конфигурации из различных источников.

```java
public class ConfigReader {
    // Получение экземпляра
    public static ConfigReader getInstance()
    public static ConfigReader getInstance(String profile)

    // Чтение свойств
    public String getProperty(String key)
    public String getProperty(String key, String defaultValue)
    public String getProperty(String key, String envKey, String defaultValue)
    public int getIntProperty(String key, int defaultValue)
    public boolean getBooleanProperty(String key, boolean defaultValue)

    // Управление профилями
    public void setProfile(String profile)
    public String getCurrentProfile()
}
```

### StandardWaits

Типовые ожидания для UI тестов.

```java
public final class StandardWaits {
    // Ожидание видимости
    public static void waitForVisible(By locator)
    public static void waitForVisible(By locator, Duration timeout)

    // Ожидание кликабельности
    public static void waitForClickable(By locator)
    public static void waitForClickable(By locator, Duration timeout)

    // Ожидание исчезновения
    public static void waitForDisappear(By locator)
    public static void waitForDisappear(By locator, Duration timeout)

    // Специфические ожидания
    public static void waitForSpinnerToDisappear()
    public static void waitForPageLoad()
    public static void waitForModal()
    public static void waitForModalToDisappear()
    public static void waitForTextChange(By locator, String oldText)
    public static void waitForText(By locator, String expectedText)
    public static void waitForAjaxToComplete()
}
```

### Specifications (API)

Управление RequestSpec и ResponseSpec для RestAssured.

```java
public final class Specifications {
    // RequestSpecification
    public static RequestSpecification defaultRequestSpec(String baseUrl)
    public static RequestSpecification authenticatedRequestSpec(String baseUrl, String bearerToken)
    public static RequestSpecification customHeadersRequestSpec(String baseUrl, Map<String, String> headers)
    public static RequestSpecification fileUploadRequestSpec(String baseUrl)

    // ResponseSpecification
    public static ResponseSpecification successResponseSpec()
    public static ResponseSpecification successResponseSpec(long maxResponseTimeMs)
    public static ResponseSpecification errorResponseSpec(int expectedStatusCode)
    public static ResponseSpecification createdResponseSpec()
    public static ResponseSpecification noContentResponseSpec()
    public static ResponseSpecification xmlResponseSpec()
    public static ResponseSpecification performanceResponseSpec(long maxResponseTimeMs)
}
```

### ApiCoreRequests

Переиспользуемые методы для API запросов с retry и логированием.

```java
public final class ApiCoreRequests {
    // Базовые методы
    public static Response get(String endpoint)
    public static Response post(String endpoint, Object body)
    public static Response put(String endpoint, Object body)
    public static Response delete(String endpoint)
    public static Response patch(String endpoint, Object body)

    // Методы с retry
    public static Response getWithRetry(String endpoint, int maxRetries)
    public static Response postWithRetry(String endpoint, Object body, int maxRetries)

    // Методы с кастомной валидацией
    public static Response get(String endpoint, Consumer<Response> validator)
    public static Response post(String endpoint, Object body, Consumer<Response> validator)
}
```

## 📊 TestData Builders API

### TestDataBuilder

Базовый класс для создания тестовых данных.

```java
public abstract class TestDataBuilder<T, B extends TestDataBuilder<T, B>> {
    // Строительство объекта
    public abstract T build()

    // Утилиты для генерации данных
    protected int randomInt(int min, int max)
    protected double randomDouble(double min, double max)
    protected String randomString(int length)
    protected String randomEmail()
    protected String randomPhone()
}
```

### UserTestDataBuilder

Builder для создания тестовых данных пользователей.

```java
public class UserTestDataBuilder extends TestDataBuilder<UserData, UserTestDataBuilder> {
    // Фабричные методы
    public static UserTestDataBuilder createDefault()
    public static UserTestDataBuilder createRandom()
    public static UserTestDataBuilder fromTestUser(TestUsers testUser)

    // Методы установки значений
    public UserTestDataBuilder withFirstName(String firstName)
    public UserTestDataBuilder withLastName(String lastName)
    public UserTestDataBuilder withEmail(String email)
    public UserTestDataBuilder withPhone(String phone)
    public UserTestDataBuilder withSnils(String snils)
    public UserTestDataBuilder withOrganization(String organization)
    public UserTestDataBuilder withUsername(String username)
    public UserTestDataBuilder withPassword(String password)
}
```

### ReportTestDataBuilder

Builder для создания тестовых данных отчетов.

```java
public class ReportTestDataBuilder extends TestDataBuilder<ReportData, ReportTestDataBuilder> {
    // Фабричные методы
    public static ReportTestDataBuilder createDefault()
    public static ReportTestDataBuilder createRandom()

    // Методы установки значений
    public ReportTestDataBuilder withReportType(ReportType reportType)
    public ReportTestDataBuilder withReportNumber(String reportNumber)
    public ReportTestDataBuilder withPeriod(LocalDate startDate, LocalDate endDate)
    public ReportTestDataBuilder withStatus(String status)
    public ReportTestDataBuilder withOrganization(String organization)
    public ReportTestDataBuilder withProcessId(String processId)
    public ReportTestDataBuilder withXmlContent(String xmlContent)
}
```

## 📈 Monitoring API

### PerformanceMonitor

Отслеживание производительности тестов.

```java
public class PerformanceMonitor {
    // Получение экземпляра
    public static PerformanceMonitor getInstance()

    // Отслеживание теста
    public void startTest(ITestResult result)
    public void endTest(ITestResult result)
    public long getTestDuration(ITestResult result)

    // Статистика
    public double getAverageDuration()
    public long getSlowTestsCount()
    public long getTotalTestsCount()
    public void printStatistics()
    public void clear()
}
```

### FlakyTestDetector

Выявление нестабильных тестов.

```java
public class FlakyTestDetector {
    // Получение экземпляра
    public static FlakyTestDetector getInstance()

    // Отслеживание результатов
    public void recordTestResult(ITestResult result)
    public boolean isFlaky(ITestResult result)
    public double getSuccessRate(ITestResult result)

    // Отчеты
    public void reportFlakyTests()
    public void clear()
}
```

## 📋 Constants API

### TimeoutConstants

Константы таймаутов для ожиданий.

```java
public final class TimeoutConstants {
    public static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);
    public static final Duration SHORT_WAIT = Duration.ofSeconds(5);
    public static final Duration LONG_WAIT = Duration.ofSeconds(30);
    public static final Duration CRYPTO_PLUGIN_WAIT = Duration.ofSeconds(15);
    // ... другие константы
}
```

### UrlConstants

Константы URL для тестов.

```java
public final class UrlConstants {
    public static final String CRYPTOPRO_DEMO_PAGE_URL = "https://cryptopro.ru/...";
    public static final String CRYPTOPRO_BASE_URL = "https://cryptopro.ru";
    public static final String API_BASE_URL_DEFAULT = "https://api.example.com";
    // ... другие константы
}
```

---

Этот справочник API предоставляет полную информацию для эффективного использования EVS Testing Framework. Для получения дополнительной информации обращайтесь к документации конкретных компонентов или примерам в директории `examples/`.</contents>
</xai:function_call">Создал подробный справочник API