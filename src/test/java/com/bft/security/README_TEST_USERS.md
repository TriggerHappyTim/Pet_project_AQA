# Управление тестовыми пользователями

Система управления несколькими тестовыми пользователями и организациями для авторизации в EVS.

## 📋 Содержание

- [Архитектура решения](#архитектура-решения)
- [Настройка локально](#настройка-локально)
- [Настройка в GitLab CI/CD](#настройка-в-gitlab-cicd)
- [Использование в тестах](#использование-в-тестах)
- [Добавление нового пользователя](#добавление-нового-пользователя)

---

## Архитектура решения

### Компоненты:

1. **`TestUsers` (enum)** - перечисление тестовых пользователей с префиксами для поиска credentials
2. **`OrganizationProfile`** - профиль организации (название + владелец)
3. **`CredentialManager`** - менеджер получения credentials с приоритетами
4. **`credentials.properties`** - файл с учетными данными (НЕ в Git)

### Приоритеты загрузки credentials:

```
1. Переменные окружения (CI/CD) ⬆️ Высший приоритет
2. credentials.properties (локально)
3. Mock данные (unit-тесты) ⬇️ Низший приоритет
```

---

## Настройка локально

### 1. Создайте файл `credentials.properties`

Скопируйте файл-шаблон:

```bash
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties
```

### 2. Заполните реальные данные

Откройте `credentials.properties` и добавьте учетные данные:

```properties
# Пользователь по умолчанию
evs.username=ваш_логин
evs.password=ваш_пароль
evs.organization=ОРГАНИЗАЦИЯ -XXXXXXX

# Пользователь 1 - Кривоносов Александр Петрович
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669
evs.user1.fullname=Кривоносов Александр Петрович

# Пользователь 2 - Бездомный Иван Николаевич
evs.user2.username=bedomniy.in@mail.ru
evs.user2.password=gq3%!UyVI_7
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831
evs.user2.fullname=Бездомный Иван Николаевич
```

### 3. Убедитесь, что файл в `.gitignore`

Файл `credentials.properties` **НЕ должен** попадать в Git:

```gitignore
# .gitignore
src/test/resources/credentials.properties
```

---

## Настройка в GitLab CI/CD

### 1. Откройте настройки проекта в GitLab

```
Settings → CI/CD → Variables
```

### 2. Добавьте переменные окружения

Для каждого пользователя создайте переменные:

#### Пользователь по умолчанию:
```
EVS_USERNAME = ваш_логин
EVS_PASSWORD = ваш_пароль
EVS_ORGANIZATION = ОРГАНИЗАЦИЯ -XXXXXXX
```

#### Пользователь 1 (Кривоносов):
```
EVS_USER1_USERNAME = 133-900-785 49
EVS_USER1_PASSWORD = Egisso13?
EVS_USER1_ORGANIZATION = ОРГАНИЗАЦИЯ -1546025669
```

#### Пользователь 2 (Бездомный):
```
EVS_USER2_USERNAME = bedomniy.in@mail.ru
EVS_USER2_PASSWORD = gq3%!UyVI_7
EVS_USER2_ORGANIZATION = ОРГАНИЗАЦИЯ -2036470831
```

### 3. Настройте защиту переменных

⚠️ **Важно**: Установите флаги:
- ✅ **Protected** - доступ только для защищенных веток
- ✅ **Masked** - скрывает значение в логах

---

## Использование в тестах

### Вариант 1: С конкретным пользователем (рекомендуется)

```java
import com.bft.security.TestUsers;
import com.bft.enums.UIType;

@Test
public void efs_1_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Авторизация от пользователя Кривоносов
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.sendXml(ReportType.EFS_FULL_ECP);
}

@Test
public void efs_1_xml_bezdomniy() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Авторизация от пользователя Бездомный
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.BEZDOMNIY_IVAN);
    
    steps.addReports();
    // ...
}
```

### Вариант 2: С дефолтным пользователем (обратная совместимость)

```java
@Test
public void efs_1_xml_default() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Использует evs.username / evs.password из credentials.properties
    steps.authorizeEVS(UIType.EVS_UAT_LKS);
    
    steps.addReports();
    // ...
}
```

### Доступные пользователи в enum:

```java
TestUsers.KRIVONOSOV_ALEXANDER  // evs.user1.*
TestUsers.BEZDOMNIY_IVAN        // evs.user2.*
TestUsers.DEFAULT_USER          // evs.*
```

---

## Добавление нового пользователя

### Шаг 1: Добавьте запись в enum `TestUsers.java`

```java
public enum TestUsers {
    // ... существующие пользователи ...
    
    /**
     * Новый пользователь
     * Credential prefix: evs.user3
     * Организация: ОРГАНИЗАЦИЯ -XXXXXXX
     */
    NEW_USER("evs.user3", "Новый Пользователь Тестович");
}
```

### Шаг 2: Обновите `credentials.properties.example`

```properties
# Пользователь 3 - Новый пользователь
evs.user3.username=ваш_логин_3
evs.user3.password=ваш_пароль_3
evs.user3.organization=ОРГАНИЗАЦИЯ -XXXXXXX
evs.user3.fullname=Новый Пользователь Тестович
```

### Шаг 3: Добавьте данные в `credentials.properties` (локально)

```properties
evs.user3.username=реальный_логин
evs.user3.password=реальный_пароль
evs.user3.organization=ОРГАНИЗАЦИЯ -123456789
evs.user3.fullname=Новый Пользователь Тестович
```

### Шаг 4: Добавьте переменные в GitLab CI/CD

```
EVS_USER3_USERNAME = реальный_логин
EVS_USER3_PASSWORD = реальный_пароль
EVS_USER3_ORGANIZATION = ОРГАНИЗАЦИЯ -123456789
```

### Шаг 5: Используйте в тестах

```java
@Test
public void test_with_new_user() {
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.NEW_USER);
    // ...
}
```

---

## Преимущества решения

✅ **Безопасность**: пароли НЕ в коде, только префиксы  
✅ **CI/CD Ready**: переменные окружения перекрывают файл  
✅ **Читаемость**: `TestUsers.KRIVONOSOV_ALEXANDER` понятнее строк  
✅ **Обратная совместимость**: старые тесты продолжают работать  
✅ **Расширяемость**: легко добавить нового пользователя  
✅ **Allure отчеты**: ФИО пользователя автоматически в степах  

---

## Структура файлов

```
evs-testing-framework/
├── src/test/java/com/bft/security/
│   ├── TestUsers.java                    # Enum с пользователями
│   ├── OrganizationProfile.java          # Профиль организации
│   ├── CredentialManager.java            # Менеджер credentials
│   └── README_TEST_USERS.md              # Эта документация
├── src/test/resources/
│   ├── credentials.properties            # Реальные данные (в .gitignore)
│   └── credentials.properties.example    # Шаблон (в Git)
└── .gitignore                            # credentials.properties
```

---

## Troubleshooting

### Ошибка: "Credentials для пользователя не найдены"

**Решение:**
1. Проверьте наличие файла `credentials.properties`
2. Убедитесь, что ключи имеют правильный формат:
   ```properties
   evs.user1.username=...
   evs.user1.password=...
   evs.user1.organization=...
   ```
3. В CI/CD проверьте переменные окружения в GitLab

### Ошибка: "Организация не найдена"

**Решение:**
1. Добавьте ключ `{prefix}.organization` в credentials.properties:
   ```properties
   evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669
   ```

### Тест падает с "Element not found: selectUserCardName"

**Решение:**
1. Проверьте правильность названия организации (с пробелами и дефисом)
2. Убедитесь, что у пользователя есть доступ к этой организации

---

## См. также

- [CredentialManager.java](./CredentialManager.java) - система управления credentials
- [EnvironmentCredentialProvider.java](./EnvironmentCredentialProvider.java) - провайдер из env
- [SzvReportsSteps.java](../steps/SzvReportsSteps.java) - шаги авторизации
