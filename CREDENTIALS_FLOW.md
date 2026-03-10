# Откуда берутся данные авторизации

## 🔍 Цепочка получения данных

Когда тест вызывает `steps.authorizeEVS(UIType.EVS_UAT_LKS)`, данные проходят следующую цепочку:

```
Тест Efs1.java
    ↓
steps.authorizeEVS(UIType.EVS_UAT_LKS)
    ↓
SzvReportsSteps.authorizeEVS()
    ↓
CredentialManager.getUserCredentials("evs")
    ↓
[Провайдеры в порядке приоритета]
    ↓
1️⃣ EnvironmentCredentialProvider (переменные окружения)
    ↓
2️⃣ PropertiesCredentialProvider (credentials.properties)
    ↓
3️⃣ MockCredentialProvider (fallback для тестов)
    ↓
UserCredentials {username, password, email}
    ↓
LoginPage.authorizeEPGU(username, password)
    ↓
Ввод в форму ЕПГУ
```

## 📍 Где хранятся данные

### 1. **Переменные окружения** (высший приоритет) ⬆️

Для теста `efs_1_xml()` используется префикс `"evs"`, поэтому ищутся:

```bash
# Windows PowerShell
$env:evs.username = "133-900-785 49"
$env:evs.password = "Egisso13?"
$env:evs.organization = "ОРГАНИЗАЦИЯ -1563384004"

# Windows CMD
set evs.username=133-900-785 49
set evs.password=Egisso13?
set evs.organization=ОРГАНИЗАЦИЯ -1563384004

# Linux/Mac/GitLab CI
export evs.username="133-900-785 49"
export evs.password="Egisso13?"
export evs.organization="ОРГАНИЗАЦИЯ -1563384004"
```

**Где проверить:**
- Локально: `echo $env:evs.username` (PowerShell) или `echo %evs.username%` (CMD)
- GitLab CI: в секции `variables` файла `.gitlab-ci.yml`

### 2. **Файл credentials.properties** (средний приоритет)

Файл: `src/test/resources/credentials.properties` (НЕ в Git!)

```properties
# Пользователь по умолчанию (префикс "evs")
evs.username=133-900-785 49
evs.password=Egisso13?
evs.organization=ОРГАНИЗАЦИЯ -1563384004

# Конкретные пользователи
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669

evs.user2.username=другой_логин
evs.user2.password=другой_пароль
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831
```

**Важно:** Файл `credentials.properties` НЕ должен быть в Git! Используйте `.gitignore`.

### 3. **Mock данные** (низший приоритет, fallback) ⬇️

Если ни переменные окружения, ни файл не найдены, используются mock данные из `MockCredentialProvider`:

```java
// Для префикса "evs" или "test"
mockCredentials.put("evs.username", "133-900-785 49");
mockCredentials.put("evs.password", "Egisso13?");
```

## 🔄 Как работает CredentialManager

```java
// 1. Инициализация провайдеров (в порядке приоритета)
providers.add(new EnvironmentCredentialProvider());      // 1️⃣
providers.add(new PropertiesCredentialProvider());       // 2️⃣
providers.add(new MockCredentialProvider());              // 3️⃣

// 2. Поиск credentials
public UserCredentials getUserCredentials(String userPrefix) {
    // Ищет: {prefix}.username, {prefix}.password, {prefix}.email
    // Например, для "evs": evs.username, evs.password, evs.email
    
    for (CredentialProvider provider : providers) {
        UserCredentials creds = provider.getUserCredentials(userPrefix);
        if (creds != null && creds.isValid()) {
            return creds; // Возвращает первый найденный!
        }
    }
    return null;
}
```

## 📝 Примеры использования в тестах

### Пример 1: Дефолтный пользователь

```java
@Test
public void efs_1_xml() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Использует префикс "evs"
    steps.authorizeEVS(UIType.EVS_UAT_LKS);
    
    // Ищет: evs.username, evs.password, evs.organization
    // Из: переменных окружения → credentials.properties → mock данных
}
```

### Пример 2: Конкретный пользователь

```java
@Test
public void efs_1_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Использует TestUsers.KRIVONOSOV_ALEXANDER
    // Который имеет префикс "evs.user1"
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    // Ищет: evs.user1.username, evs.user1.password, evs.user1.organization
}
```

## 🔐 Безопасность

### ✅ Правильно (данные НЕ в коде):

```java
// ✅ Хорошо: данные из CredentialManager
var credentials = credentialManager.getUserCredentials("evs");
loginPage.authorizeEPGU(credentials.username, credentials.password);
```

### ❌ Неправильно (данные в коде):

```java
// ❌ Плохо: пароль в коде!
loginPage.authorizeEPGU("133-900-785 49", "Egisso13?");
```

## 🛠️ Как настроить локально

### Шаг 1: Создайте файл credentials.properties

```bash
# Скопируйте шаблон
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties
```

### Шаг 2: Заполните реальные данные

Откройте `src/test/resources/credentials.properties`:

```properties
evs.username=133-900-785 49
evs.password=Egisso13?
evs.organization=ОРГАНИЗАЦИЯ -1563384004
```

### Шаг 3: Убедитесь, что файл в .gitignore

```gitignore
# credentials.properties не должен быть в Git!
src/test/resources/credentials.properties
```

## 🐛 Отладка: где именно взялись данные?

Добавьте логирование в тест:

```java
@Test
public void efs_1_xml() {
    CredentialManager cm = CredentialManager.getInstance();
    
    // Проверяем, откуда берутся данные
    var credentials = cm.getUserCredentials("evs");
    
    logger.info("Username: {}", credentials.username);
    logger.info("Password: {}", credentials.password != null ? "***" : "null");
    
    // Проверяем источник
    if (System.getProperty("evs.username") != null) {
        logger.info("Источник: System Property");
    } else if (System.getenv("evs.username") != null) {
        logger.info("Источник: Environment Variable");
    } else {
        logger.info("Источник: Properties File или Mock");
    }
}
```

## 📊 Схема приоритетов

```
┌─────────────────────────────────────┐
│  CredentialManager.getUserCredentials("evs")  │
└─────────────────────────────────────┘
              │
              ▼
    ┌─────────────────────┐
    │  Провайдер 1: ENV   │ ← Высший приоритет
    │  System.getProperty │
    │  System.getenv()    │
    └─────────────────────┘
              │ (если не найдено)
              ▼
    ┌─────────────────────┐
    │  Провайдер 2: FILE  │
    │  credentials.properties │
    └─────────────────────┘
              │ (если не найдено)
              ▼
    ┌─────────────────────┐
    │  Провайдер 3: MOCK  │ ← Низший приоритет
    │  MockCredentialProvider │
    └─────────────────────┘
```

## 🎯 Итого

**Данные `133-900-785 49` и `Egisso13?` берутся из:**

1. **Переменных окружения** (если установлены) - `evs.username`, `evs.password`
2. **Файла `credentials.properties`** (если существует) - `evs.username=133-900-785 49`
3. **Mock данных** (fallback) - `MockCredentialProvider` содержит `133-900-785 49`/`Egisso13?`

**Проверьте:**
- `echo $env:evs.username` (PowerShell)
- Существует ли `src/test/resources/credentials.properties`
- Логи теста покажут, какой провайдер использовался
