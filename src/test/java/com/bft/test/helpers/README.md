# Test Helpers - Вспомогательные классы для тестов

Этот пакет содержит вспомогательные классы для улучшения качества и читаемости тестов.

## 📚 Доступные классы

### TestSetupHelper

Класс для общей логики инициализации тестов.

**Расположение:** `com.bft.test.helpers.TestSetupHelper`

#### Примеры использования

```java
import com.bft.test.helpers.TestSetupHelper;
import com.bft.config.TestStrategyType;

@BeforeEach
public void setup() {
    // Гарантирует инициализацию конфигурации
    TestSetupHelper.ensureConfigurationInitialized(TestStrategyType.UI);
}
```

// Проверка типа окружения
if (TestSetupHelper.isUIEnvironment()) {
    // UI тест
}

// Безопасное получение конфигурации
var config = TestSetupHelper.getCurrentConfigSafe();
```

---

### LoginHelper

Класс для упрощения авторизации в тестах.

**Расположение:** `com.bft.test.helpers.LoginHelper`

#### Примеры использования

```java
import com.bft.test.helpers.LoginHelper;
import com.bft.enums.UIType;

// Стандартная авторизация через CredentialManager
LoginHelper.loginAsEVSUser(UIType.EVS_UAT_LKS);

// Авторизация с кастомными данными
LoginHelper.loginWithCredentials(UIType.EVS_UAT_LKS, "user@example.com", "password");

// Авторизация через ЕПГУ
LoginHelper.loginViaEPGU(UIType.EVS_UAT_LKS, "user@example.com", "password");

// Проверка авторизации
if (LoginHelper.isUserLoggedIn()) {
    // Пользователь авторизован
}

// Выход из системы
LoginHelper.logout();
```

---

### PageObjectHelper

Класс для безопасной инициализации Page Objects.

**Расположение:** `com.bft.test.helpers.PageObjectHelper`

#### Примеры использования

```java
import com.bft.test.helpers.PageObjectHelper;

// Проверка инициализации перед использованием
PageObjectHelper.requirePageObjectInitialized(myPage, "MyPage");
```

---

### AssertionHelper

Класс для создания информативных сообщений об ошибках с контекстом выполнения теста.

**Расположение:** `com.bft.test.helpers.AssertionHelper`

#### Примеры использования

##### Форматирование ошибок элементов UI

```java
import com.bft.test.helpers.AssertionHelper;
import org.testng.asserts.SoftAssert;

// Простое использование
softAssert.assertTrue(
    element.isDisplayed(),
    AssertionHelper.formatElementError("Кнопка 'Войти'", "должна быть видимой")
);

// С указанием фактического состояния
softAssert.assertTrue(
    element.isEnabled(),
    AssertionHelper.formatElementError(
        "Поле 'Email'", 
        "должно быть доступно", 
        element.isEnabled() ? "доступно" : "недоступно",
        driver.getCurrentUrl()
    )
);
```

##### Форматирование ошибок значений

```java
String actualEmail = emailField.getValue();
String expectedEmail = "user@example.com";

softAssert.assertEquals(
    actualEmail,
    expectedEmail,
    AssertionHelper.formatValueError("Email", expectedEmail, actualEmail)
);
```

##### Форматирование ошибок API

```java
import io.restassured.response.Response;

Response response = given().when().get("/api/users/123");

softAssert.assertEquals(
    response.getStatusCode(),
    200,
    AssertionHelper.formatApiError(
        "/api/users/123",
        200,
        response.getStatusCode(),
        response.getBody().asString()
    )
);
```

##### Форматирование ошибок таймаутов

```java
try {
    element.shouldBe(Condition.visible, Duration.ofSeconds(10));
} catch (Exception e) {
    throw new AssertionError(
        AssertionHelper.formatTimeoutError("Кнопка 'Отправить'", 10, "видимость")
    );
}
```

#### Доступные методы

| Метод | Описание | Параметры |
|-------|----------|-----------|
| `formatElementError()` | Форматирует ошибку для элемента UI | `elementName`, `expectedState`, `actualState?`, `pageUrl?` |
| `formatValueError()` | Форматирует ошибку для значения поля | `fieldName`, `expectedValue`, `actualValue` |
| `formatPageStateError()` | Форматирует ошибку состояния страницы | `pageName`, `expectedState`, `actualState` |
| `formatApiError()` | Форматирует ошибку API запроса | `endpoint`, `expectedStatus`, `actualStatus`, `responseBody?` |
| `formatTestError()` | Форматирует ошибку с контекстом теста | `testName`, `errorMessage`, `additionalContext?` |
| `formatTimeoutError()` | Форматирует ошибку таймаута | `elementName`, `timeout`, `condition` |
| `formatValidationError()` | Форматирует ошибку валидации данных | `dataType`, `fieldName`, `reason` |

---

### SmartWaits

Класс для умных ожиданий элементов и страниц с использованием констант таймаутов.

**Расположение:** `com.bft.test.helpers.SmartWaits`

#### Примеры использования

##### Ожидание видимости элемента

```java


import static com.bft.pw.Selenide.$;

// С использованием стандартного таймаута (10 секунд)
SmartWaits.waitForElementVisible($(".submit-button"),"Кнопка 'Отправить'");

// С кастомным таймаутом

SmartWaits.waitForElementVisible(
        $(".submit-button"),
        "Кнопка 'Отправить'",
        TimeoutConstants.LONG_WAIT  // 30 секунд
        );
```

##### Ожидание исчезновения элемента

```java
// Ожидание исчезновения спиннера загрузки
SmartWaits.waitForElementDisappear($(".loading-spinner"), "Спиннер загрузки");
```

##### Ожидание кликабельности элемента

```java
SmartWaits.waitForElementClickable($(".submit-button"), "Кнопка 'Отправить'");
```

##### Ожидание загрузки страницы

```java
// Ожидание полной загрузки страницы через JavaScript readyState
SmartWaits.waitForPageLoad();

// С кастомным таймаутом
SmartWaits.waitForPageLoad(TimeoutConstants.PAGE_LOAD_WAIT);
```

##### Ожидание завершения AJAX запросов

```java
// Ожидание завершения всех AJAX запросов через jQuery
SmartWaits.waitForAjaxComplete();

// С кастомным таймаутом
SmartWaits.waitForAjaxComplete(TimeoutConstants.AJAX_WAIT);
```

##### Ожидание исчезновения спиннеров

```java
// С использованием стандартных селекторов
SmartWaits.waitForSpinnerDisappear();

// С указанием кастомных селекторов
SmartWaits.waitForSpinnerDisappear(
    TimeoutConstants.DEFAULT_WAIT,
    ".spinner",
    ".loading",
    "[class*='loader']"
);
```

##### Ожидание завершения анимации

```java
// Стандартный таймаут анимации (1 секунда)
SmartWaits.waitForAnimation();

// Кастомный таймаут
SmartWaits.waitForAnimation(TimeoutConstants.ANIMATION_LONG);  // 2 секунды
```

#### Доступные методы

| Метод | Описание | Параметры |
|-------|----------|-----------|
| `waitForElementVisible()` | Ожидает видимости элемента | `element`, `elementName`, `timeout?` |
| `waitForElementDisappear()` | Ожидает исчезновения элемента | `element`, `elementName`, `timeout?` |
| `waitForElementClickable()` | Ожидает кликабельности элемента | `element`, `elementName`, `timeout?` |
| `waitForPageLoad()` | Ожидает загрузки страницы | `timeout?` |
| `waitForAjaxComplete()` | Ожидает завершения AJAX запросов | `timeout?` |
| `waitForSpinnerDisappear()` | Ожидает исчезновения спиннеров | `timeout?`, `spinnerSelectors...` |
| `waitForAnimation()` | Ожидает завершения анимации | `timeout?` |

#### Использование констант таймаутов

Все методы используют константы из `com.bft.enums.TimeoutConstants`:

- `DEFAULT_WAIT` - 10 секунд (стандартный таймаут)
- `SHORT_WAIT` - 3 секунды (быстрые операции)
- `LONG_WAIT` - 30 секунд (медленные операции)
- `PAGE_LOAD_WAIT` - 60 секунд (загрузка страницы)
- `AJAX_WAIT` - 20 секунд (AJAX запросы)
- `DIALOG_WAIT` - 15 секунд (диалоговые окна)
- `MODAL_WAIT` - 10 секунд (модальные окна)
- `ANIMATION_NORMAL` - 1 секунда (стандартные анимации)
- `ANIMATION_LONG` - 2 секунды (сложные анимации)

---

## 🔗 Интеграция с другими компонентами

### Использование с SoftAssert

```java
import com.bft.test.helpers.AssertionHelper;
import org.testng.asserts.SoftAssert;

SoftAssert softAssert = new SoftAssert();

softAssert.assertTrue(
    condition,
    AssertionHelper.formatElementError("Элемент", "должен быть видимым")
);
```

### Использование с Selenide-совместимым API (адаптер com.bft.pw)

```java
import com.bft.test.helpers.SmartWaits;
import static com.bft.pw.Selenide.$;

// Вместо:
$(".button").shouldBe(Condition.visible, Duration.ofSeconds(50));

// Используйте:
SmartWaits.waitForElementVisible($(".button"), "Кнопка");
```

### Использование в Page Objects

```java
public class LoginPage {
    
    public LoginPage enterCredentials(String username, String password) {
        SmartWaits.waitForElementVisible($("#username"), "Поле 'Логин'");
        $("#username").setValue(username);
        
        SmartWaits.waitForElementVisible($("#password"), "Поле 'Пароль'");
        $("#password").setValue(password);
        
        return this;
    }
    
    public void clickSubmit() {
        SmartWaits.waitForElementClickable($(".submit-button"), "Кнопка 'Войти'");
        $(".submit-button").click();
        
        SmartWaits.waitForPageLoad();
    }
}
```

### Использование в тестах

```java
import com.bft.test.base.UITestBase;
import com.bft.test.helpers.AssertionHelper;
import com.bft.test.helpers.SmartWaits;
import org.testng.annotations.Test;

public class LoginTest extends UITestBase {
    
    @Test(groups = {"web", "smoke"})
    public void successfulLogin() {
        arrangeActAssert(
            () -> {
                // Arrange
                logger.info("Подготовка тестовых данных");
            },
            () -> {
                // Act
                new LoginPage()
                    .open(UIType.EVS_UAT_LKS)
                    .enterCredentials("user@test.com", "password")
                    .clickSubmit();
                
                SmartWaits.waitForPageLoad();
            },
            softAssert -> {
                // Assert
                softAssert.assertTrue(
                    $(".user-name").isDisplayed(),
                    AssertionHelper.formatElementError(
                        "Имя пользователя",
                        "должно отображаться после входа"
                    )
                );
            }
        );
    }
}
```

---

## 📝 Best Practices

### 1. Используйте информативные имена элементов

```java
// ❌ Плохо
SmartWaits.waitForElementVisible($(".btn"), "Button");

// ✅ Хорошо
SmartWaits.waitForElementVisible($(".submit-button"), "Кнопка 'Отправить форму'");
```

### 2. Используйте константы таймаутов вместо магических чисел

```java
// ❌ Плохо
SmartWaits.waitForElementVisible($(".button"), "Button", Duration.ofSeconds(50));

// ✅ Хорошо
SmartWaits.waitForElementVisible($(".button"), "Button", TimeoutConstants.LONG_WAIT);
```

### 3. Комбинируйте SmartWaits с AssertionHelper

```java
try {
    SmartWaits.waitForElementVisible($(".button"), "Кнопка");
} catch (AssertionError e) {
    throw new AssertionError(
        AssertionHelper.formatTimeoutError("Кнопка", 10, "видимость"),
        e
    );
}
```

### 4. Используйте SmartWaits для сложных ожиданий

```java
// Ожидание завершения всех операций перед проверкой
SmartWaits.waitForAjaxComplete();
SmartWaits.waitForSpinnerDisappear();
SmartWaits.waitForPageLoad();

// Теперь можно безопасно проверять результат
softAssert.assertTrue(condition, "Результат должен быть видимым");
```

---

## 🐛 Troubleshooting

### Проблема: Элемент не найден

**Решение:** Убедитесь, что используете правильный селектор и что элемент действительно существует на странице.

```java
// Проверьте селектор
$(".button").shouldBe(Condition.exist);  // Проверка существования

// Используйте SmartWaits с более длинным таймаутом
SmartWaits.waitForElementVisible($(".button"), "Кнопка", TimeoutConstants.LONG_WAIT);
```

### Проблема: AJAX запросы не завершаются

**Решение:** Убедитесь, что jQuery доступен на странице, или используйте альтернативный метод проверки.

```java
// Проверьте доступность jQuery
boolean jQueryAvailable = (Boolean) ((JavascriptExecutor) driver)
    .executeScript("return typeof jQuery !== 'undefined'");

if (jQueryAvailable) {
    SmartWaits.waitForAjaxComplete();
} else {
    // Альтернативный метод: ожидание конкретного элемента
    SmartWaits.waitForElementVisible($(".result"), "Результат");
}
```

---

## 📚 Дополнительные ресурсы

- [TimeoutConstants](../constants/TimeoutConstants.java) - Константы таймаутов
- [BaseTest](../../BaseTest.java) - Базовый класс для тестов
- [UITestBase](../base/UITestBase.java) - Базовый класс для UI тестов
- [ApiTestBase](../base/ApiTestBase.java) - Базовый класс для API тестов

---

**Версия:** 1.0  
**Дата обновления:** 2026-02-03  
**Автор:** QA Automation Team
