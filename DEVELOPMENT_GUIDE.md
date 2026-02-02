# Руководство разработчика EVS Testing Framework

## 🎯 Обзор

Это руководство предназначено для разработчиков, которые будут расширять, модифицировать или поддерживать EVS Testing Framework. Здесь описаны лучшие практики, паттерны проектирования и рекомендации по разработке.

## 🏗️ Архитектурные принципы

### SOLID принципы

#### 1. Single Responsibility Principle (SRP)
Каждый класс должен иметь одну ответственность.

```java
// ❌ Плохо: класс делает слишком много
public class TestUtils {
    public void fillForm(User user) { /* ... */ }
    public void validateResponse(Response response) { /* ... */ }
    public void takeScreenshot() { /* ... */ }
    public void sendEmail(String to, String subject, String body) { /* ... */ }
}

// ✅ Хорошо: разделить на специализированные классы
public class FormFiller {
    public void fillUserForm(User user) { /* ... */ }
}

public class ResponseValidator {
    public void validateApiResponse(Response response) { /* ... */ }
}

public class ScreenshotTaker {
    public void takeScreenshot() { /* ... */ }
}

public class EmailService {
    public void sendNotification(String to, String subject, String body) { /* ... */ }
}
```

#### 2. Open/Closed Principle (OCP)
Классы должны быть открыты для расширения, но закрыты для модификации.

```java
// ✅ Хорошо: расширение через наследование
public abstract class BaseComponent {
    protected abstract boolean isValid();
}

public class ButtonComponent extends BaseComponent {
    @Override
    protected boolean isValid() {
        return isPresent() && isVisible() && isEnabled();
    }
}

// ✅ Хорошо: расширение через композицию
public class ValidationService {
    private final List<Validator> validators = new ArrayList<>();

    public void addValidator(Validator validator) {
        this.validators.add(validator);
    }

    public ValidationResult validate(Object target) {
        ValidationResult result = new ValidationResult();
        for (Validator validator : validators) {
            result.merge(validator.validate(target));
        }
        return result;
    }
}
```

#### 3. Liskov Substitution Principle (LSP)
Подклассы должны быть заменяемыми на базовые классы.

```java
// ✅ Хорошо: подкласс полностью заменяет базовый класс
public class SecureLogger implements Logger {
    private final Logger delegate;

    @Override
    public void info(String message) {
        String maskedMessage = maskSensitiveData(message);
        delegate.info(maskedMessage);
    }

    @Override
    public void error(String message) {
        delegate.error(message);
    }
}
```

#### 4. Interface Segregation Principle (ISP)
Клиенты не должны зависеть от интерфейсов, которые они не используют.

```java
// ❌ Плохо: толстый интерфейс
public interface TestRunner {
    void runUnitTests();
    void runIntegrationTests();
    void runUITests();
    void runPerformanceTests();
    void generateReport();
    void sendNotification();
    void cleanup();
}

// ✅ Хорошо: разделенные интерфейсы
public interface UnitTestRunner {
    void runUnitTests();
}

public interface UITestRunner {
    void runUITests();
}

public interface ReportGenerator {
    void generateReport();
}

public interface NotificationService {
    void sendNotification();
}

// Композитный интерфейс для комплексных случаев
public interface FullTestRunner extends UnitTestRunner, UITestRunner,
                                       ReportGenerator, NotificationService {
    void cleanup();
}
```

#### 5. Dependency Inversion Principle (DIP)
Зависимости от абстракций, а не от конкретных реализаций.

```java
// ✅ Хорошо: зависимость от абстракции
public class UserService {
    private final CredentialProvider credentialProvider;

    public UserService(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public User authenticate(String username, String password) {
        Credentials creds = credentialProvider.getCredentials(username);
        // логика аутентификации
    }
}

// Интерфейс для разных реализаций
public interface CredentialProvider {
    Credentials getCredentials(String username);
}

public class DatabaseCredentialProvider implements CredentialProvider {
    @Override
    public Credentials getCredentials(String username) {
        // получение из БД
    }
}

public class MockCredentialProvider implements CredentialProvider {
    @Override
    public Credentials getCredentials(String username) {
        // mock данные для тестов
    }
}
```

## 🧪 Написание тестов

### Структура тестов

#### 1. Arrange-Act-Assert (AAA) паттерн

```java
@Test
public void testUserRegistration() {
    // Arrange - подготовка данных и состояния
    User testUser = createTestUser();
    RegistrationPage registrationPage = new RegistrationPage();

    // Act - выполнение действий
    registrationPage
        .openRegistrationForm()
        .fillUserData(testUser)
        .submitForm();

    // Assert - проверка результатов
    softAssert.assertTrue(registrationPage.isSuccessMessageDisplayed());
    softAssert.assertTrue(userService.isUserRegistered(testUser.getEmail()));
    softAssert.assertAll();
}
```

#### 2. Given-When-Then (BDD стиль)

```java
@Test
@Description("Регистрация нового пользователя через веб-интерфейс")
public void userCanRegisterThroughWebInterface() {
    given("пользователь открывает страницу регистрации")
        .when("заполняет форму валидными данными")
        .and("нажимает кнопку 'Зарегистрироваться'")
        .then("видит сообщение об успешной регистрации")
        .and("получает email с подтверждением");
}

private UserCanRegisterThroughWebInterface given(String description) {
    logger.info("Given: {}", description);
    registrationPage.open();
    return this;
}

private UserCanRegisterThroughWebInterface when(String description) {
    logger.info("When: {}", description);
    // действия
    return this;
}

private void then(String description) {
    logger.info("Then: {}", description);
    // проверки
}
```

### Использование компонентов

#### Создание Page Object с компонентами

```java
public class LoginPage extends BasePage<LoginPage> {
    private final InputComponent usernameField;
    private final InputComponent passwordField;
    private final ButtonComponent loginButton;
    private final TextComponent errorMessage;

    public LoginPage() {
        this.usernameField = InputComponent.createLabeledInput("Логин", "Поле логина");
        this.passwordField = InputComponent.createLabeledInput("Пароль", "Поле пароля");
        this.loginButton = ButtonComponent.createPrimaryButton("Войти");
        this.errorMessage = TextComponent.createClass("error-message", "Сообщение об ошибке");
    }

    public LoginPage login(String username, String password) {
        usernameField.setValue(username);
        passwordField.setValue(password);
        loginButton.click();
        waitForLoading();
        return this;
    }

    public boolean isErrorDisplayed() {
        return errorMessage.isVisible();
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    private void waitForLoading() {
        // логика ожидания загрузки
    }
}
```

#### Композиция компонентов

```java
public class UserManagementPage extends BasePage<UserManagementPage> {
    private final UserSearchComponent searchComponent;
    private final UserTableComponent userTable;
    private final UserFormComponent userForm;

    public UserManagementPage() {
        this.searchComponent = new UserSearchComponent();
        this.userTable = new UserTableComponent();
        this.userForm = new UserFormComponent();
    }

    public UserManagementPage searchUser(String query) {
        searchComponent.search(query);
        return this;
    }

    public UserManagementPage selectUser(int index) {
        userTable.clickRow(index);
        return this;
    }

    public UserManagementPage editUser(User updatedUser) {
        userForm.fillUserData(updatedUser);
        userForm.save();
        return this;
    }
}
```

### Тестовые данные и фикстуры

#### 1. Test Data Builder паттерн

```java
public class UserBuilder {
    private String firstName = "Иван";
    private String lastName = "Иванов";
    private String email = "ivan@example.com";
    private String phone = "+7-999-123-45-67";
    private UserRole role = UserRole.USER;

    public UserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder asAdmin() {
        this.role = UserRole.ADMIN;
        return this;
    }

    public User build() {
        return new User(firstName, lastName, email, phone, role);
    }

    // Статические фабричные методы
    public static User createDefault() {
        return new UserBuilder().build();
    }

    public static User createAdmin() {
        return new UserBuilder().asAdmin().build();
    }

    public static User createRandom() {
        return new UserBuilder()
            .withFirstName(RandomStringUtils.randomAlphabetic(5))
            .withLastName(RandomStringUtils.randomAlphabetic(8))
            .withEmail(RandomStringUtils.randomAlphabetic(5) + "@example.com")
            .build();
    }
}
```

#### 2. Data Provider для параметризованных тестов

```java
@Test(dataProvider = "userDataProvider")
public void testUserRegistrationWithDifferentData(User user) {
    registrationPage
        .open()
        .fillUserData(user)
        .submit();

    assertUserRegistered(user);
}

@DataProvider(name = "userDataProvider")
public Object[][] userDataProvider() {
    return new Object[][] {
        { UserBuilder.createDefault() },
        { UserBuilder.createAdmin() },
        { UserBuilder.createRandom() },
        { UserBuilder.createDefault().withEmail("invalid-email") },
        { UserBuilder.createDefault().withFirstName("") }
    };
}
```

#### 3. Fixture управление

```java
public class UserFixture {
    private final UserService userService;
    private final List<User> createdUsers = new ArrayList<>();

    public UserFixture(UserService userService) {
        this.userService = userService;
    }

    public User createUser() {
        User user = UserBuilder.createRandom();
        userService.createUser(user);
        createdUsers.add(user);
        return user;
    }

    public User createUser(UserBuilder builder) {
        User user = builder.build();
        userService.createUser(user);
        createdUsers.add(user);
        return user;
    }

    public void cleanup() {
        for (User user : createdUsers) {
            try {
                userService.deleteUser(user.getId());
            } catch (Exception e) {
                logger.warn("Failed to cleanup user: {}", user.getEmail());
            }
        }
        createdUsers.clear();
    }
}

// Использование в тестах
public class UserManagementTest extends BaseTest {
    private UserFixture userFixture;

    @BeforeMethod
    public void setup() {
        userFixture = new UserFixture(userService);
    }

    @AfterMethod
    public void cleanup() {
        if (userFixture != null) {
            userFixture.cleanup();
        }
    }

    @Test
    public void testUserCreation() {
        User user = userFixture.createUser();

        // тестовая логика
    }
}
```

## 🔧 Расширение фреймворка

### Создание нового компонента

#### Шаг 1: Определить интерфейс компонента

```java
public interface Searchable {
    void search(String query);
    boolean hasResults();
    int getResultCount();
    void clearSearch();
}
```

#### Шаг 2: Создать базовую реализацию

```java
public abstract class BaseSearchComponent extends BaseComponent implements Searchable {
    protected final InputComponent searchInput;
    protected final ButtonComponent searchButton;
    protected final ButtonComponent clearButton;

    protected BaseSearchComponent(String selector, String name) {
        super(ElementFactory.css(selector).named(name).build(), name);

        this.searchInput = InputComponent.createCustom(
            selector + " input[type='search']",
            "Поисковый запрос"
        );

        this.searchButton = ButtonComponent.createCustom(
            selector + " button.search",
            "Кнопка поиска"
        );

        this.clearButton = ButtonComponent.createCustom(
            selector + " button.clear",
            "Кнопка очистки"
        );
    }

    @Override
    public void search(String query) {
        logger.info("Выполняем поиск: {}", query);
        searchInput.setValue(query);
        searchButton.click();
        waitForSearchCompletion();
    }

    @Override
    public boolean hasResults() {
        // абстрактная логика для проверки результатов
        return getResultCount() > 0;
    }

    @Override
    public void clearSearch() {
        if (clearButton.isVisible()) {
            clearButton.click();
        } else {
            searchInput.clear();
        }
    }

    protected abstract int getResultCount();
    protected abstract void waitForSearchCompletion();
}
```

#### Шаг 3: Создать конкретную реализацию

```java
public class UserSearchComponent extends BaseSearchComponent {
    private final TableComponent resultsTable;

    public UserSearchComponent() {
        super(".user-search", "Поиск пользователей");
        this.resultsTable = TableComponent.createCustom(
            ".user-search-results table",
            "Результаты поиска пользователей"
        );
    }

    @Override
    protected int getResultCount() {
        return resultsTable.getRowCount();
    }

    @Override
    protected void waitForSearchCompletion() {
        resultsTable.waitForLoad();
    }

    // Специфические методы для поиска пользователей
    public UserSearchComponent searchByName(String name) {
        searchInput.setValue(name);
        searchButton.click();
        waitForSearchCompletion();
        return this;
    }

    public UserSearchComponent searchByEmail(String email) {
        searchInput.setValue(email);
        searchButton.click();
        waitForSearchCompletion();
        return this;
    }

    public User selectUserByEmail(String email) {
        int rowIndex = resultsTable.findRowByCellText(2, email); // email в колонке 2
        if (rowIndex >= 0) {
            resultsTable.clickRow(rowIndex);
            return getSelectedUser();
        }
        throw new NoSuchElementException("User with email " + email + " not found");
    }

    private User getSelectedUser() {
        // логика получения данных выбранного пользователя
        return new User(/* данные из формы */);
    }
}
```

#### Шаг 4: Интеграция в Page Object

```java
public class UserManagementPage extends BasePage<UserManagementPage> {
    private final UserSearchComponent searchComponent;
    private final UserFormComponent userForm;
    private final ButtonComponent createUserButton;

    public UserManagementPage() {
        this.searchComponent = new UserSearchComponent();
        this.userForm = new UserFormComponent();
        this.createUserButton = ButtonComponent.createPrimaryButton("Создать пользователя");
    }

    public UserManagementPage searchUser(String query) {
        searchComponent.search(query);
        return this;
    }

    public UserManagementPage selectUser(String email) {
        searchComponent.selectUserByEmail(email);
        return this;
    }

    public UserManagementPage createUser(User user) {
        createUserButton.click();
        userForm.fillUserData(user);
        userForm.save();
        return this;
    }

    public boolean isUserFound(String email) {
        return searchComponent.searchByEmail(email).hasResults();
    }
}
```

### Создание новой стратегии

#### Шаг 1: Определить интерфейс стратегии

```java
public interface DataPreparationStrategy extends TestExecutionStrategy {
    void prepareTestData(TestContext context);
    void cleanupTestData(TestContext context);
    boolean isDataReady(TestContext context);
}
```

#### Шаг 2: Создать базовую реализацию

```java
public abstract class BaseDataPreparationStrategy implements DataPreparationStrategy {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DataService dataService;

    protected BaseDataPreparationStrategy(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public final TestResult execute(TestContext context) {
        TestResult result = new TestResult();

        try {
            logger.info("Подготовка тестовых данных для: {}", context.getTestName());

            prepareTestData(context);

            if (!isDataReady(context)) {
                throw new DataPreparationException("Тестовые данные не готовы");
            }

            // Выполнение основного теста
            result = executeTestLogic(context);

        } catch (Exception e) {
            logger.error("Ошибка подготовки данных: {}", e.getMessage());
            result.setSuccess(false);
            result.setError(e);
        } finally {
            try {
                cleanupTestData(context);
            } catch (Exception e) {
                logger.warn("Ошибка очистки данных: {}", e.getMessage());
            }
        }

        return result;
    }

    protected abstract TestResult executeTestLogic(TestContext context);
    protected abstract void prepareTestData(TestContext context);
    protected abstract void cleanupTestData(TestContext context);
    protected abstract boolean isDataReady(TestContext context);
}
```

#### Шаг 3: Создать конкретную стратегию

```java
public class UserDataPreparationStrategy extends BaseDataPreparationStrategy {

    private final UserService userService;
    private final List<User> createdUsers = new ArrayList<>();

    public UserDataPreparationStrategy(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @Override
    protected void prepareTestData(TestContext context) {
        // Создание тестовых пользователей
        User adminUser = createTestUser("admin", UserRole.ADMIN);
        User regularUser = createTestUser("user", UserRole.USER);

        createdUsers.add(adminUser);
        createdUsers.add(regularUser);

        // Сохранение в контекст для использования в тестах
        context.setAttribute("adminUser", adminUser);
        context.setAttribute("regularUser", regularUser);
    }

    @Override
    protected boolean isDataReady(TestContext context) {
        User adminUser = (User) context.getAttribute("adminUser");
        User regularUser = (User) context.getAttribute("regularUser");

        return adminUser != null && regularUser != null &&
               userService.exists(adminUser.getId()) &&
               userService.exists(regularUser.getId());
    }

    @Override
    protected void cleanupTestData(TestContext context) {
        for (User user : createdUsers) {
            try {
                userService.deleteUser(user.getId());
                logger.debug("Удален тестовый пользователь: {}", user.getEmail());
            } catch (Exception e) {
                logger.warn("Не удалось удалить пользователя {}: {}", user.getEmail(), e.getMessage());
            }
        }
        createdUsers.clear();
    }

    @Override
    protected TestResult executeTestLogic(TestContext context) {
        // Делегирование выполнения конкретному тесту
        TestStrategy delegateStrategy = getDelegateStrategy(context);
        return delegateStrategy.execute(context);
    }

    private User createTestUser(String prefix, UserRole role) {
        String email = prefix + "_" + System.currentTimeMillis() + "@test.com";
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test" + prefix);
        user.setLastName("User" + prefix);
        user.setRole(role);

        return userService.createUser(user);
    }

    private TestStrategy getDelegateStrategy(TestContext context) {
        // Определение какой стратегии делегировать выполнение
        String testType = context.getAttribute("testType", String.class);
        switch (testType) {
            case "ui": return new UITestStrategy();
            case "api": return new ApiTestStrategy();
            default: return new DefaultTestStrategy();
        }
    }
}
```

#### Шаг 4: Регистрация и использование

```java
// Регистрация стратегии
TestStrategyManager strategyManager = TestStrategyManager.getInstance();
strategyManager.registerStrategy(
    TestStrategyType.USER_DATA_PREPARATION,
    new UserDataPreparationStrategy(userService)
);

// Использование в тестах
@Test
public void testUserManagement() {
    TestContext context = new TestContext();
    context.setAttribute("testType", "ui");
    context.setAttribute("testName", "userManagementTest");

    TestResult result = strategyManager.executeTestWithStrategy(
        context,
        TestStrategyType.USER_DATA_PREPARATION
    );

    assertTrue(result.isSuccess());
}
```

## 🐛 Обработка ошибок и устойчивость

### Graceful degradation

```java
public class ResilientComponent extends BaseComponent {
    private final int maxRetries = 3;
    private final Duration retryDelay = Duration.ofSeconds(1);

    public ResilientComponent clickWithRetry() {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                click();
                return this;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new ComponentException("Не удалось выполнить клик после " + maxRetries + " попыток", e);
                }
                logger.warn("Попытка {} клика не удалась, повтор через {} мс: {}",
                           attempt, retryDelay.toMillis(), e.getMessage());
                // Используем умное ожидание вместо sleep
                StandardWaits.waitForClickable(element, retryDelay);
            }
        }
        return this;
    }

    public ResilientComponent waitForStability() {
        return waitForStability(Duration.ofSeconds(5));
    }

    public ResilientComponent waitForStability(Duration timeout) {
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).compareTo(timeout) < 0) {
            if (isStable()) {
                return this;
            }
            // Используем умное ожидание вместо sleep
            StandardWaits.waitForStability(element, Duration.ofMillis(100));
        }
        throw new TimeoutException("Компонент не стабилизировался за " + timeout);
    }

    private boolean isStable() {
        // Проверка стабильности состояния компонента
        boolean currentVisible = isVisible();
        // Используем умное ожидание вместо sleep
        StandardWaits.waitForElementReady(element, Duration.ofMillis(50));
        return currentVisible == isVisible();
    }
}
```

### Recovery strategies

```java
public class RecoveryManager {
    private final List<RecoveryStrategy> strategies;

    public RecoveryManager() {
        this.strategies = Arrays.asList(
            new PageRefreshRecovery(),
            new BrowserRestartRecovery(),
            new DataResetRecovery()
        );
    }

    public boolean recoverFromFailure(TestFailure failure) {
        for (RecoveryStrategy strategy : strategies) {
            if (strategy.canHandle(failure)) {
                try {
                    logger.info("Попытка восстановления с помощью: {}", strategy.getName());
                    strategy.recover(failure);
                    return true;
                } catch (Exception e) {
                    logger.warn("Стратегия {} не сработала: {}", strategy.getName(), e.getMessage());
                }
            }
        }
        return false;
    }
}

public interface RecoveryStrategy {
    String getName();
    boolean canHandle(TestFailure failure);
    void recover(TestFailure failure) throws RecoveryException;
}

public class PageRefreshRecovery implements RecoveryStrategy {
    @Override
    public String getName() { return "Page Refresh"; }

    @Override
    public boolean canHandle(TestFailure failure) {
        return failure.getType() == FailureType.STALE_ELEMENT_REFERENCE;
    }

    @Override
    public void recover(TestFailure failure) {
        Selenide.refresh();
        waitForPageLoad();
    }
}
```

## 📊 Производительность и оптимизация

### Lazy initialization

```java
public class LazyComponent extends BaseComponent {
    private volatile SmartElement heavyElement;
    private final Object lock = new Object();

    public SmartElement getHeavyElement() {
        if (heavyElement == null) {
            synchronized (lock) {
                if (heavyElement == null) {
                    heavyElement = ElementFactory.css(".heavy-element")
                        .named("Тяжелый элемент")
                        .waitVisible()
                        .build();
                }
            }
        }
        return heavyElement;
    }
}
```

### Connection pooling

```java
@Configuration
public class DatabaseConfig {
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb");
        config.setUsername("testuser");
        config.setPassword("testpass");

        // Оптимизация пула соединений
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000); // 5 минут
        config.setMaxLifetime(600000); // 10 минут
        config.setConnectionTimeout(30000); // 30 секунд

        return new HikariDataSource(config);
    }
}
```

### Caching

```java
public class ComponentCache {
    private final Cache<String, BaseComponent> componentCache =
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public <T extends BaseComponent> T getComponent(String key, Supplier<T> factory) {
        try {
            return (T) componentCache.get(key, () -> factory.get());
        } catch (ExecutionException e) {
            throw new ComponentException("Ошибка создания компонента", e);
        }
    }

    public void invalidate(String key) {
        componentCache.invalidate(key);
    }

    public void invalidateAll() {
        componentCache.invalidateAll();
    }
}
```

## 🧪 Тестирование кода

### Unit тестирование компонентов

```java
@ExtendWith(MockitoExtension.class)
public class ButtonComponentTest {

    @Mock
    private SmartElement mockElement;

    @Mock
    private SoftAssert mockSoftAssert;

    private ButtonComponent buttonComponent;

    @BeforeEach
    void setUp() {
        buttonComponent = new ButtonComponent(mockElement, "Test Button");
    }

    @Test
    void click_shouldCallElementClick() {
        // Given
        when(mockElement.isPresent()).thenReturn(true);
        when(mockElement.isVisible()).thenReturn(true);
        when(mockElement.isEnabled()).thenReturn(true);

        // When
        buttonComponent.click();

        // Then
        verify(mockElement).click();
    }

    @Test
    void isValid_shouldReturnTrue_whenAllConditionsMet() {
        // Given
        when(mockElement.isPresent()).thenReturn(true);
        when(mockElement.isVisible()).thenReturn(true);
        when(mockElement.isEnabled()).thenReturn(true);

        // When
        boolean result = buttonComponent.isValid();

        // Then
        assertTrue(result);
    }
}
```

### Integration тестирование

```java
@SpringBootTest
@ExtendWith(SelenideExtension.class)
public class LoginPageIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void login_shouldAuthenticateUser(@TempUser User user) {
        // Given
        LoginPage loginPage = new LoginPage();

        // When
        loginPage.open()
                .authorize(user.getEmail(), user.getPassword())
                .waitForDashboard();

        // Then
        assertTrue(loginPage.isLoggedIn());
        assertEquals(user.getEmail(), loginPage.getCurrentUserEmail());
    }
}
```

### E2E тестирование

```java
@Test
@Order(1)
public void completeUserWorkflow() {
    // Регистрация
    User user = UserBuilder.createRandom();
    registrationPage.register(user);

    // Активация аккаунта
    activationPage.activate(user.getActivationToken());

    // Авторизация
    loginPage.login(user.getEmail(), user.getPassword());

    // Работа с профилем
    profilePage.updateProfile(user);

    // Выход
    header.logout();

    // Проверка что пользователь может снова войти
    loginPage.login(user.getEmail(), user.getPassword());
    assertTrue(dashboardPage.isDisplayed());
}
```

## 📝 Лучшие практики

### 1. Принципы именования

```java
// ✅ Хорошо: описательные имена
public class UserProfileUpdatePage extends BasePage<UserProfileUpdatePage> {
    private final InputComponent firstNameField;
    private final ButtonComponent saveProfileButton;

    public UserProfileUpdatePage updateFirstName(String firstName) {
        firstNameField.setValue(firstName);
        return this;
    }
}

// ❌ Плохо: неясные имена
public class Page extends BasePage<Page> {
    private final InputComponent f1;
    private final ButtonComponent b1;

    public Page m1(String p1) {
        f1.setValue(p1);
        return this;
    }
}
```

### 2. Обработка исключений

```java
// ✅ Хорошо: специфичные исключения с контекстом
public User findUserById(Long id) throws UserNotFoundException {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(
            String.format("Пользователь с ID %d не найден", id)));
}

// ✅ Хорошо: recovery в компонентах
public ButtonComponent safeClick() {
    try {
        return click();
    } catch (ElementNotInteractableException e) {
        logger.warn("Кнопка не доступна, пытаемся прокрутить: {}", e.getMessage());
        scrollIntoView();
        return click();
    }
}
```

### 3. Логирование

```java
// ✅ Хорошо: структурированное логирование
public User createUser(CreateUserRequest request) {
    logger.info("Создание пользователя",
               keyValue("email", request.getEmail()),
               keyValue("role", request.getRole()));

    User user = new User(request.getEmail(), request.getRole());
    User savedUser = userRepository.save(user);

    logger.info("Пользователь создан",
               keyValue("userId", savedUser.getId()),
               keyValue("email", savedUser.getEmail()));

    return savedUser;
}

// ✅ Хорошо: логирование в компонентах
public InputComponent setValue(String value) {
    logger.debug("Установка значения '{}' в поле '{}'", value, componentName);

    try {
        rootElement.type(value);
        logger.debug("Значение успешно установлено");
    } catch (Exception e) {
        logger.error("Ошибка установки значения в поле '{}': {}", componentName, e.getMessage());
        throw new ComponentException("Не удалось установить значение", e);
    }

    return this;
}
```

### 4. Документирование

```java
/**
 * Компонент для работы с полями выбора даты.
 *
 * <p>Поддерживает различные форматы дат и автоматическую валидацию.</p>
 *
 * <h2>Примеры использования:</h2>
 * <pre>{@code
 * // Установка текущей даты
 * dateComponent.setCurrentDate();
 *
 * // Установка конкретной даты
 * dateComponent.setDate("15-05-1990");
 *
 * // Установка даты относительно текущей
 * dateComponent.setDateRelative(-30); // месяц назад
 * }</pre>
 *
 * @author EVS Testing Framework Team
 * @since 2.0.0
 */
public class DateComponent extends BaseComponent {
    // ...
}
```

## 🔄 Рефакторинг существующего кода

### Идентификация проблем

```java
// ❌ Код с проблемами
public class OldPage {
    public void doSomething() {
        $x("//button[@id='btn']").click(); // hardcoded селектор
        // Используем умное ожидание вместо Thread.sleep
        StandardWaits.waitForPageLoad();
        // нет обработки ошибок
        // нет логирования
    }
}

// ✅ Рефакторинг
public class NewPage extends BasePage<NewPage> {
    private final ButtonComponent actionButton = ButtonComponent.createIdButton("btn", "Действие");

    public NewPage performAction() {
        logger.info("Выполнение действия");
        try {
            actionButton.click();
            waitForResult();
            return this;
        } catch (Exception e) {
            logger.error("Ошибка выполнения действия: {}", e.getMessage());
            throw new PageException("Не удалось выполнить действие", e);
        }
    }

    private void waitForResult() {
        // умное ожидание вместо sleep
    }
}
```

### Миграция шаг за шагом

1. **Анализ**: Найти все использования старого кода
2. **Создание**: Написать новый компонент/стратегию
3. **Тестирование**: Убедиться что новый код работает
4. **Замена**: Заменить старый код на новый
5. **Валидация**: Проверить что ничего не сломалось
6. **Удаление**: Удалить старый код

## 🎯 Заключение

Разработка в EVS Testing Framework должна следовать принципам:

- **Качество кода**: SOLID, DRY, чистая архитектура
- **Тестируемость**: Unit, Integration, E2E тесты
- **Поддерживаемость**: Документация, логирование, обработка ошибок
- **Производительность**: Оптимизация, кеширование, асинхронность
- **Расширяемость**: Паттерны, интерфейсы, композиция

Следуя этим практикам, вы сможете создавать надежный, поддерживаемый и эффективный автоматизированный код для тестирования! 🚀</contents>
</xai:function_call">Создал подробное руководство для разработчиков