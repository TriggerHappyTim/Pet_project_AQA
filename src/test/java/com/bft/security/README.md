# Система безопасности и управления учетными данными

## Обзор

Модуль безопасности предоставляет комплексное решение для безопасного хранения и использования учетных данных в автоматизированных тестах.

**Состав пакета:** 13 файлов (без учёта тестовых), 2 тестовых класса, 2 дополнительных README.

## Структура пакета

### Основные классы
- **CredentialProvider** — интерфейс для получения учётных данных
- **CredentialManager** — централизованный менеджер учётных данных
- **EnvironmentCredentialProvider** — провайдер из переменных окружения
- **MockCredentialProvider** — провайдер для тестовых данных
- **PropertiesCredentialProvider** (`providers/`) — провайдер из properties-файлов

### Тестовые пользователи
- **TestUsers** — перечисление тестовых пользователей
- **TestUserSelector** — выбор тестового пользователя по системным свойствам и переменным окружения (`test.user`, `TEST_USER` и т.п.)

### Организация
- **OrganizationProfile** — профиль организации для управления учётными данными

### Маскировка данных (`masking/`)
- **SecureLogger** — логирование с автоматической маскировкой
- **DataMasker** — интерфейс маскирования чувствительных данных
- **CompositeDataMasker** — композитный маскировщик (пароли, email, API-ключи, JWT, карты)

### Тестовые классы
- **SecuritySystemTest** — системные тесты модуля
- **TestUsersTest** — тесты TestUsers

### Дополнительная документация
- [README_TEST_USERS.md](README_TEST_USERS.md)
- [README_USER_ACCOUNT.md](README_USER_ACCOUNT.md)

## Настройка учетных данных

### Переменные окружения

Установите следующие переменные окружения для различных систем:

```bash
# EPGU (Единая система идентификации и аутентификации)
export epgu.username="ваш_логин_епгу"
export epgu.password="ваш_пароль_епгу"

# РПУ (Региональный портал услуг)
export rpu.username="ваш_логин_рпу"
export rpu.password="ваш_пароль_рпу"

# УОС (Универсальная обработка сведений)
export uos.username="ваш_логин_уос"
export uos.password="ваш_пароль_уос"

# EVS (Единая ведомственная система)
export evs.username="ваш_логин_evs"
export evs.password="ваш_пароль_evs"

# Тестовые данные (для локального тестирования)
export test.username="testuser"
export test.password="testpass123"

# API ключи
export api.key="ваш_api_ключ"
export api.secret="ваш_api_secret"
export api.token="ваш_jwt_token"

# База данных
export db.url="jdbc:postgresql://localhost:5432/testdb"
export db.username="testuser"
export db.password="testpass"
```

### Файлы конфигурации

Для продакшена используйте защищенные файлы конфигурации:

```properties
# application.properties или application.yml
epgu.username=ваш_логин_епгу
epgu.password=ваш_пароль_епгу
rpu.username=ваш_логин_рпу
rpu.password=ваш_пароль_рпу
```

## Использование в тестах

### Получение учетных данных

```java
import com.bft.security.CredentialManager;

// Получение менеджера учетных данных
CredentialManager credentialManager = CredentialManager.getInstance();

// Получение пользовательских credentials
var userCredentials = credentialManager.getUserCredentials("epgu");
if (userCredentials.isValid()) {
    loginPage.login(userCredentials.username, userCredentials.password);
}

// Получение API credentials
var apiCredentials = credentialManager.getApiCredentials("api");
if (apiCredentials.hasValidToken()) {
    request.header("Authorization", "Bearer " + apiCredentials.token);
}
```

### Безопасное логирование

```java
import com.bft.security.masking.SecureLogger;

public class MyTest {
    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    public void testLogin() {
        var credentials = credentialManager.getUserCredentials("test");

        // Это сообщение будет автоматически замаскировано
        logger.info("Логин пользователя: {} с паролем: {}", credentials.username, credentials.password);

        // Результат в логах:
        // "Логин пользователя: testuser с паролем: ***"
    }
}
```

## Маскировка данных

Система автоматически маскирует следующие типы данных:

- **Пароли**: `password: ***`
- **Email**: `user@domain.com` → `***@domain.com`
- **API ключи**: `sk-1234567890abcdef` → `sk-***`
- **JWT токены**: `eyJ...` → `eyJ***.***.***`
- **Номера карт**: `4111111111111111` → `**** **** **** 1111`

## Лучшие практики

### 1. Никогда не логируйте чувствительные данные

```java
// ❌ Плохо
logger.info("User password: " + password);

// ✅ Хорошо (автоматическая маскировка)
logger.info("User password: {}", password);

// ✅ Очень хорошо (явное избегание)
logger.info("User authentication successful");
```

### 2. Используйте переменные окружения для credentials

```bash
# В CI/CD пайплайнах
export EPГУ_USERNAME="prod_login"
export EPGU_PASSWORD="prod_password"
```

### 3. Валидируйте наличие credentials перед тестами

```java
@BeforeClass
public void validateCredentials() {
    var credentials = CredentialManager.getInstance().getUserCredentials("epgu");
    assumeTrue(credentials.isValid(), "EPGU credentials must be configured");
}
```

## Архитектура

```
CredentialManager
├── EnvironmentCredentialProvider (переменные окружения)
├── MockCredentialProvider (тестовые данные)
└── SecureLogger
    └── CompositeDataMasker
        ├── PasswordMasker
        ├── EmailMasker
        ├── ApiKeyMasker
        ├── JwtTokenMasker
        └── CreditCardMasker
```

## Безопасность

- Учетные данные никогда не хранятся в коде
- Чувствительные данные автоматически маскируются в логах
- Поддержка нескольких источников credentials
- Возможность использования защищенных хранилищ (HashiCorp Vault, AWS Secrets Manager)