# Выбор учётной записи для тестирования

## Обзор

В проекте реализована возможность динамического выбора учётной записи для тестирования через:
- **Maven profiles** (для CI/CD)
- **TestUserSelector** (helper-класс)
- **GitLab CI/CD переменная** `USER_ACCOUNT`

---

## Доступные учётные записи

### 1. **Кривоносов Александр Петрович**
- **Enum**: `TestUsers.KRIVONOSOV_ALEXANDER`
- **Credential prefix**: `evs.user1`
- **Maven profile**: `user_krivonosov`
- **Организация**: ОРГАНИЗАЦИЯ -1546025669

### 2. **Бездомный Иван Николаевич**
- **Enum**: `TestUsers.BEZDOMNIY_IVAN`
- **Credential prefix**: `evs.user2`
- **Maven profile**: `user_bezdomniy`
- **Организация**: ОРГАНИЗАЦИЯ -2036470831

### 3. **Тестовый пользователь по умолчанию**
- **Enum**: `TestUsers.DEFAULT_USER`
- **Credential prefix**: `evs`
- **Maven profile**: `user_default`
- **Организация**: ОРГАНИЗАЦИЯ -1563384004

---

## Использование в CI/CD (GitLab)

### Запуск pipeline с выбором УЗ

1. Открыть GitLab → **CI/CD** → **Pipelines**
2. Нажать **Run Pipeline**
3. Выбрать переменные:
   - `TEST_SCRIPT`: `smoke_web`
   - `BROWSERS`: `browser_chrome_remote`
   - `ENVIRONMENT`: `test`
   - **`USER_ACCOUNT`**: `user_krivonosov` ← **выбор УЗ**
4. Запустить pipeline

### Как это работает

```bash
# GitLab CI/CD выполняет:
mvn test -P smoke_web,browser_chrome_remote,test,user_krivonosov

# Maven profile user_krivonosov устанавливает system property:
<systemPropertyVariables>
  <test.user>KRIVONOSOV_ALEXANDER</test.user>
</systemPropertyVariables>

# В тесте TestUserSelector получает пользователя:
TestUsers user = TestUserSelector.getSelectedUser();
// user = TestUsers.KRIVONOSOV_ALEXANDER
```

---

## Использование в локальных тестах

### Вариант 1: Явное указание пользователя

```java
@Test(groups = {"web", "smoke"})
public void testWithKrivonosov() {
    // Явно указываем пользователя
    TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
    
    // ... тест
}
```

### Вариант 2: Динамический выбор через TestUserSelector

```java
@Test(groups = {"web", "smoke"})
public void testWithDynamicUser() {
    // Получаем пользователя из system property (или default)
    TestUsers user = TestUserSelector.getSelectedUser();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
    
    // ... тест
}
```

### Вариант 3: Запуск с Maven profile локально

```bash
# Запуск с профилем user_bezdomniy
mvn test -P user_bezdomniy

# Или комбинация профилей
mvn test -P smoke_web,user_bezdomniy
```

### Вариант 4: Установка через system property

```java
@BeforeMethod
public void setup() {
    // Установить пользователя для всех тестов в классе
    TestUserSelector.setSelectedUser(TestUsers.BEZDOMNIY_IVAN);
}

@Test(groups = {"web"})
public void testWithSetUser() {
    TestUsers user = TestUserSelector.getSelectedUser();
    // user = TestUsers.BEZDOMNIY_IVAN
}

@AfterMethod
public void teardown() {
    // Очистить после тестов
    TestUserSelector.clearSelectedUser();
}
```

---

## Примеры тестов

### Пример 1: Тест с конкретной УЗ

```java
@Feature("EFS-1 Reports")
@Story("XML Upload - Krivonosov")
public class Efs1KrivonosovTest extends UITestBase {
    
    @Test(groups = {"web", "smoke"})
    @DisplayName("Upload EFS-1 XML with Krivonosov account")
    public void uploadEfs1XmlKrivonosov() {
        arrangeActAssert(
            () -> {
                // Arrange: явно указываем Кривоносова
                TestUsers user = TestUsers.KRIVONOSOV_ALEXANDER;
            },
            () -> {
                // Act
                steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
                steps.uploadEfs1Xml("reports/efs1_sample.xml");
            },
            softAssert -> {
                // Assert
                softAssert.assertTrue($(".success-message").isDisplayed());
            }
        );
    }
}
```

### Пример 2: Универсальный тест с динамической УЗ

```java
@Feature("Report Management")
@Story("Dynamic User Account")
public class ReportUniversalTest extends UITestBase {
    
    @Test(groups = {"web", "regression"})
    @DisplayName("View reports with any user account")
    public void viewReportsWithDynamicUser() {
        arrangeActAssert(
            () -> {
                // Arrange: получаем пользователя из CI/CD или default
                TestUsers user = TestUserSelector.getSelectedUser();
                logger.info("Testing with user: {}", user.getFullName());
            },
            () -> {
                // Act: авторизуемся с выбранным пользователем
                TestUsers user = TestUserSelector.getSelectedUser();
                steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
                
                new MainPage()
                    .openTab("Отчеты")
                    .waitForReportsLoad();
            },
            softAssert -> {
                // Assert
                softAssert.assertTrue($(".reports-table").isDisplayed());
            }
        );
    }
}
```

### Пример 3: Параметризованный тест для всех УЗ

```java
@Feature("User Authentication")
public class LoginAllUsersTest extends UITestBase {
    
    @DataProvider(name = "allUsers")
    public Object[][] allUsersProvider() {
        return new Object[][] {
            {TestUsers.KRIVONOSOV_ALEXANDER},
            {TestUsers.BEZDOMNIY_IVAN},
            {TestUsers.DEFAULT_USER}
        };
    }
    
    @Test(dataProvider = "allUsers", groups = {"web", "smoke"})
    @DisplayName("Login with all user accounts")
    public void loginWithAllUsers(TestUsers user) {
        arrangeActAssert(
            () -> {
                logger.info("Testing login for: {}", user.getFullName());
            },
            () -> {
                steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
            },
            softAssert -> {
                softAssert.assertTrue($(".user-profile").isDisplayed());
            }
        );
    }
}
```

---

## Настройка credentials

### В `credentials.properties`:

```properties
# Кривоносов Александр
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669

# Бездомный Иван
evs.user2.username=bedomniy.in@mail.ru
evs.user2.password=SecurePass123
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831

# Default user
evs.username=testuser@example.com
evs.password=DefaultPass123
evs.organization=ОРГАНИЗАЦИЯ -1563384004
```

### В переменных окружения (CI/CD):

```bash
# Кривоносов
EVS_USER1_USERNAME="133-900-785 49"
EVS_USER1_PASSWORD="Egisso13?"
EVS_USER1_ORGANIZATION="ОРГАНИЗАЦИЯ -1546025669"

# Бездомный
EVS_USER2_USERNAME="bedomniy.in@mail.ru"
EVS_USER2_PASSWORD="SecurePass123"
EVS_USER2_ORGANIZATION="ОРГАНИЗАЦИЯ -2036470831"

# Default
EVS_USERNAME="testuser@example.com"
EVS_PASSWORD="DefaultPass123"
EVS_ORGANIZATION="ОРГАНИЗАЦИЯ -1563384004"
```

---

## API TestUserSelector

### Основные методы:

```java
// Получить выбранного пользователя (или default)
TestUsers user = TestUserSelector.getSelectedUser();

// Получить с fallback на конкретного пользователя
TestUsers user = TestUserSelector.getSelectedUser(TestUsers.BEZDOMNIY_IVAN);

// Проверить, установлен ли пользователь через system property
boolean isSet = TestUserSelector.isUserSelected();

// Установить пользователя (для тестов)
TestUserSelector.setSelectedUser(TestUsers.KRIVONOSOV_ALEXANDER);

// Очистить system property
TestUserSelector.clearSelectedUser();
```

---

## Troubleshooting

### Проблема: Credentials не найдены

**Ошибка:**
```
IllegalStateException: Credentials для пользователя 'Кривоносов Александр Петрович' не найдены
```

**Решение:**
1. Проверить `credentials.properties`:
   ```properties
   evs.user1.username=...
   evs.user1.password=...
   evs.user1.organization=...
   ```
2. Или установить переменные окружения:
   ```bash
   EVS_USER1_USERNAME=...
   EVS_USER1_PASSWORD=...
   EVS_USER1_ORGANIZATION=...
   ```

### Проблема: Неизвестный пользователь в system property

**Ошибка:**
```
IllegalArgumentException: Неизвестный пользователь в system property 'test.user': 'UNKNOWN_USER'
```

**Решение:**
Использовать только допустимые значения:
- `KRIVONOSOV_ALEXANDER`
- `BEZDOMNIY_IVAN`
- `DEFAULT_USER`

---

## Преимущества подхода

✅ **Гибкость**: Выбор УЗ через CI/CD или в коде  
✅ **Безопасность**: Credentials не хардкодятся  
✅ **Универсальность**: Один тест для разных УЗ  
✅ **Читаемость**: `TestUsers.KRIVONOSOV_ALEXANDER` понятнее строк  
✅ **Централизация**: Все УЗ в одном enum  
✅ **Типобезопасность**: Compile-time проверка  

---

## См. также

- [TestUsers.java](TestUsers.java) - Enum с учётными записями
- [TestUserSelector.java](TestUserSelector.java) - Helper для выбора УЗ
- [README_TEST_USERS.md](README_TEST_USERS.md) - Общая документация по TestUsers
- [CredentialManager.java](CredentialManager.java) - Управление credentials
