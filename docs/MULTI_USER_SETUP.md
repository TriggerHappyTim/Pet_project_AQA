# Настройка авторизации с несколькими пользователями

## Для разработчиков (локальная машина)

### Шаг 1: Скопируйте шаблон

```bash
cd evs-testing-framework
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties
```

### Шаг 2: Заполните учетные данные

Откройте `src/test/resources/credentials.properties` и добавьте:

```properties
# Пользователь 1 - Кривоносов Александр Петрович
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669

# Пользователь 2 - Бездомный Иван Николаевич
evs.user2.username=bedomniy.in@mail.ru
evs.user2.password=gq3%!UyVI_7
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831
```

### Шаг 3: Запустите тест

```bash
mvn test -Dtest=Efs1#efs_1_xml_krivonosov
```

---

## Для DevOps (GitLab CI/CD)

### Шаг 1: Откройте настройки проекта

```
GitLab → Ваш проект → Settings → CI/CD → Variables
```

### Шаг 2: Добавьте переменные

Нажмите **Add variable** и создайте:

#### Пользователь 1 (Кривоносов):
- **Key:** `EVS_USER1_USERNAME`  
  **Value:** `133-900-785 49`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ✅

- **Key:** `EVS_USER1_PASSWORD`  
  **Value:** `Egisso13?`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ✅

- **Key:** `EVS_USER1_ORGANIZATION`  
  **Value:** `ОРГАНИЗАЦИЯ -1546025669`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ❌

#### Пользователь 2 (Бездомный):
- **Key:** `EVS_USER2_USERNAME`  
  **Value:** `bedomniy.in@mail.ru`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ✅

- **Key:** `EVS_USER2_PASSWORD`  
  **Value:** `gq3%!UyVI_7`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ✅

- **Key:** `EVS_USER2_ORGANIZATION`  
  **Value:** `ОРГАНИЗАЦИЯ -2036470831`  
  **Type:** Variable  
  **Protected:** ✅  
  **Masked:** ❌

### Шаг 3: Запустите pipeline

Pipeline автоматически подхватит переменные окружения.

---

## Для тестировщиков (написание тестов)

### Пример 1: Простой тест с конкретным пользователем

```java
import com.bft.security.TestUsers;
import com.bft.enums.UIType;

@Test
public void testWithKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Авторизация от Кривоносова
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.sendXml(ReportType.EFS_FULL_ECP);
}
```

### Пример 2: Параметризованный тест

```java
@DataProvider(name = "users")
public Object[][] users() {
    return new Object[][] {
        {TestUsers.KRIVONOSOV_ALEXANDER, "Кривоносов"},
        {TestUsers.BEZDOMNIY_IVAN, "Бездомный"}
    };
}

@Test(dataProvider = "users")
public void testMultiUser(TestUsers user, String description) {
    steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
    // ... остальная логика
}
```

### Доступные пользователи:

```java
TestUsers.KRIVONOSOV_ALEXANDER  // Кривоносов Александр Петрович
TestUsers.BEZDOMNIY_IVAN        // Бездомный Иван Николаевич
TestUsers.DEFAULT_USER          // Пользователь по умолчанию
```

---

## FAQ

**Q: Где хранятся пароли?**  
A: Локально в `credentials.properties` (не в Git), в CI/CD - в GitLab Variables.

**Q: Как добавить нового пользователя?**  
A: Добавьте в enum `TestUsers.java` и в `credentials.properties`.

**Q: Старые тесты продолжат работать?**  
A: Да, метод `authorizeEVS(UIType)` без указания пользователя работает как раньше.

**Q: Как посмотреть доступных пользователей?**  
A: См. enum `com.bft.security.TestUsers`.

---

## Полная документация

- [README_TEST_USERS.md](../src/test/java/com/bft/security/README_TEST_USERS.md) - детальная документация
- [MULTI_USER_GUIDE.md](../MULTI_USER_GUIDE.md) - быстрый старт
