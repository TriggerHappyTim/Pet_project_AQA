# Изменения: Поддержка нескольких пользователей для авторизации

## 📋 Краткое описание

Добавлена возможность авторизации от разных пользователей с их организациями в тестах EVS.

**Безопасность:** Пароли хранятся в `credentials.properties` (не в Git) или в GitLab CI/CD Variables.

---

## ✨ Что добавлено

### 1. Новые классы

#### `TestUsers.java` (enum)
Перечисление тестовых пользователей с prefixes для поиска credentials:

```java
TestUsers.KRIVONOSOV_ALEXANDER  // evs.user1.*
TestUsers.BEZDOMNIY_IVAN        // evs.user2.*
TestUsers.DEFAULT_USER          // evs.*
```

**Методы:**
- `getCredentials()` - получить UserCredentials
- `getUsername()` - получить логин
- `getPassword()` - получить пароль
- `getOrganization()` - получить название организации
- `hasCredentials()` - проверить доступность credentials

#### `OrganizationProfile.java`
Модель данных организации (название + владелец).

#### `TestUsersTest.java`
Unit-тесты для проверки работы `TestUsers`.

---

### 2. Обновлённые классы

#### `SzvReportsSteps.java`
Добавлены методы:

```java
// Новый метод с выбором пользователя
public void authorizeEVS(UIType uiType, TestUsers user)

// Существующий метод (обратная совместимость)
public void authorizeEVS(UIType uiType)
```

#### `Efs1.java`
Добавлены примеры тестов:

```java
efs_1_xml_krivonosov()      // XML от Кривоносова
efs_1_xml_bezdomniy()       // XML от Бездомного
efs_szv_td_krivonosov()     // Ручное создание от Кривоносова
```

---

### 3. Обновлённые конфигурационные файлы

#### `credentials.properties`
Добавлены секции:

```properties
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669

evs.user2.username=bedomniy.in@mail.ru
evs.user2.password=gq3%!UyVI_7
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831
```

#### `credentials.properties.example`
Обновлён шаблон для новых пользователей.

#### `.gitlab-ci.yml`
Добавлены новые тесты в список `SINGLE_TEST`:

```yaml
- "Efs1#efs_1_xml_krivonosov"
- "Efs1#efs_1_xml_bezdomniy"
- "Efs1#efs_szv_td_krivonosov"
```

---

### 4. Документация

| Файл | Описание |
|------|----------|
| `MULTI_USER_GUIDE.md` | Быстрый старт и примеры |
| `src/test/java/com/bft/security/README_TEST_USERS.md` | Полная документация |
| `docs/MULTI_USER_SETUP.md` | Инструкция по настройке для команды |
| `CHANGES_MULTI_USER.md` | Этот файл с изменениями |

---

## 🚀 Использование

### Вариант 1: С конкретным пользователем (новое)

```java
import com.bft.security.TestUsers;

@Test
public void efs_1_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Авторизация от Кривоносова
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.sendXml(ReportType.EFS_FULL_ECP);
}
```

### Вариант 2: Дефолтный пользователь (обратная совместимость)

```java
@Test
public void efs_1_xml() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Работает как раньше
    steps.authorizeEVS(UIType.EVS_UAT_LKS);
    
    steps.addReports();
    // ...
}
```

---

## ⚙️ Настройка

### Локально (один раз)

```bash
# 1. Скопируйте шаблон
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties

# 2. Откройте credentials.properties и заполните реальные данные
# (файл уже в .gitignore)
```

### GitLab CI/CD

Добавьте переменные в **Settings → CI/CD → Variables**:

```
EVS_USER1_USERNAME = 133-900-785 49
EVS_USER1_PASSWORD = Egisso13?
EVS_USER1_ORGANIZATION = ОРГАНИЗАЦИЯ -1546025669

EVS_USER2_USERNAME = bedomniy.in@mail.ru
EVS_USER2_PASSWORD = gq3%!UyVI_7
EVS_USER2_ORGANIZATION = ОРГАНИЗАЦИЯ -2036470831
```

Флаги: ✅ Protected, ✅ Masked

---

## 📊 Структура файлов

```
evs-testing-framework/
├── src/test/java/com/bft/
│   ├── security/
│   │   ├── TestUsers.java                    # ✨ Новый enum
│   │   ├── OrganizationProfile.java          # ✨ Новый класс
│   │   ├── TestUsersTest.java                # ✨ Новые тесты
│   │   ├── CredentialManager.java            # Без изменений
│   │   └── README_TEST_USERS.md              # ✨ Новая документация
│   ├── steps/
│   │   └── SzvReportsSteps.java              # ⚡ Обновлён
│   └── LK_Insurence/EFS_1/
│       └── Efs1.java                         # ⚡ Обновлён
├── src/test/resources/
│   ├── credentials.properties                # ⚡ Обновлён
│   └── credentials.properties.example        # ⚡ Обновлён
├── docs/
│   └── MULTI_USER_SETUP.md                   # ✨ Новая инструкция
├── .gitlab-ci.yml                            # ⚡ Обновлён
├── MULTI_USER_GUIDE.md                       # ✨ Быстрый старт
└── CHANGES_MULTI_USER.md                     # ✨ Этот файл
```

---

## ✅ Преимущества

| Преимущество | Описание |
|--------------|----------|
| 🔐 **Безопасность** | Пароли НЕ в коде, только префиксы |
| 🚀 **CI/CD Ready** | Переменные окружения перекрывают файл |
| 📖 **Читаемость** | `TestUsers.KRIVONOSOV` понятнее строк |
| 🔄 **Совместимость** | Старые тесты работают без изменений |
| 📈 **Расширяемость** | Легко добавить нового пользователя |
| 📊 **Allure** | ФИО пользователя автоматически в отчётах |

---

## 🔧 Добавление нового пользователя

### Шаг 1: Enum `TestUsers.java`

```java
NEW_USER("evs.user3", "Новый Пользователь Тестович");
```

### Шаг 2: `credentials.properties`

```properties
evs.user3.username=логин
evs.user3.password=пароль
evs.user3.organization=ОРГАНИЗАЦИЯ -XXXXXXX
```

### Шаг 3: GitLab Variables (если нужно)

```
EVS_USER3_USERNAME = логин
EVS_USER3_PASSWORD = пароль
EVS_USER3_ORGANIZATION = ОРГАНИЗАЦИЯ -XXXXXXX
```

### Шаг 4: Использование

```java
steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.NEW_USER);
```

---

## 🧪 Проверка работоспособности

### Запустить unit-тесты:

```bash
mvn test -Dtest=TestUsersTest
```

### Запустить E2E тест:

```bash
mvn test -Dtest=Efs1#efs_1_xml_krivonosov
```

---

## 📚 Дополнительная документация

- [MULTI_USER_GUIDE.md](MULTI_USER_GUIDE.md) - Быстрый старт с примерами
- [README_TEST_USERS.md](src/test/java/com/bft/security/README_TEST_USERS.md) - Полная документация
- [MULTI_USER_SETUP.md](docs/MULTI_USER_SETUP.md) - Инструкции для команды

---

## 🎯 Доступные пользователи

| Enum | ФИО | Логин | Организация | Prefix |
|------|-----|-------|-------------|--------|
| `KRIVONOSOV_ALEXANDER` | Кривоносов Александр Петрович | 133-900-785 49 | ОРГАНИЗАЦИЯ -1546025669 | `evs.user1` |
| `BEZDOMNIY_IVAN` | Бездомный Иван Николаевич | bedomniy.in@mail.ru | ОРГАНИЗАЦИЯ -2036470831 | `evs.user2` |
| `DEFAULT_USER` | Тестовый пользователь | testuser | ОРГАНИЗАЦИЯ -1563384004 | `evs` |

---

## 🔍 Troubleshooting

### Ошибка: "Credentials для пользователя не найдены"

**Причина:** Не заполнен файл `credentials.properties` или нет переменных в GitLab.

**Решение:**
1. Проверьте `credentials.properties` - есть ли ключи `evs.user1.*`
2. В CI/CD проверьте переменные в GitLab Variables

### Ошибка: "Element not found: selectUserCardName"

**Причина:** Неверное название организации или нет доступа.

**Решение:**
1. Проверьте правильность названия (с пробелами и дефисом)
2. Убедитесь, что пользователь имеет доступ к организации

---

## ✨ Итоги

✅ Добавлена поддержка нескольких пользователей  
✅ Безопасное хранение credentials (не в Git)  
✅ Готово для CI/CD (GitLab Variables)  
✅ Обратная совместимость со старыми тестами  
✅ Документация и примеры  
✅ Unit-тесты для проверки  

**Готово к использованию!** 🎉
