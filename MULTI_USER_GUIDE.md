# Быстрый старт: Авторизация с несколькими пользователями

## 🚀 Использование в тестах

### Авторизация от конкретного пользователя:

```java
import com.bft.security.TestUsers;

@Test
public void efs_1_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    // Авторизация от пользователя Кривоносов Александр Петрович
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    // ... остальные шаги
}
```

### Доступные пользователи:

| Enum | ФИО | Организация | Prefix |
|------|-----|-------------|--------|
| `TestUsers.KRIVONOSOV_ALEXANDER` | Кривоносов Александр Петрович | ОРГАНИЗАЦИЯ -1546025669 | `evs.user1` |
| `TestUsers.BEZDOMNIY_IVAN` | Бездомный Иван Николаевич | ОРГАНИЗАЦИЯ -2036470831 | `evs.user2` |
| `TestUsers.DEFAULT_USER` | Пользователь по умолчанию | ОРГАНИЗАЦИЯ -1563384004 | `evs` |

---

## ⚙️ Настройка локально (один раз)

### 1. Скопируйте шаблон:

```bash
cp src/test/resources/credentials.properties.example src/test/resources/credentials.properties
```

### 2. Заполните реальные данные в `credentials.properties`:

```properties
# Пользователь 1 - Кривоносов
evs.user1.username=133-900-785 49
evs.user1.password=Egisso13?
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669

# Пользователь 2 - Бездомный
evs.user2.username=bedomniy.in@mail.ru
evs.user2.password=gq3%!UyVI_7
evs.user2.organization=ОРГАНИЗАЦИЯ -2036470831
```

**⚠️ Важно:** Файл `credentials.properties` уже в `.gitignore` и НЕ попадёт в Git!

---

## 🔐 Настройка в GitLab CI/CD

В GitLab: **Settings → CI/CD → Variables** добавьте:

```
EVS_USER1_USERNAME = 133-900-785 49
EVS_USER1_PASSWORD = Egisso13?
EVS_USER1_ORGANIZATION = ОРГАНИЗАЦИЯ -1546025669

EVS_USER2_USERNAME = bedomniy.in@mail.ru
EVS_USER2_PASSWORD = gq3%!UyVI_7
EVS_USER2_ORGANIZATION = ОРГАНИЗАЦИЯ -2036470831
```

Установите флаги: ✅ **Protected** и ✅ **Masked**

---

## 📚 Полная документация

См. [README_TEST_USERS.md](src/test/java/com/bft/security/README_TEST_USERS.md)

---

## 💡 Примеры тестов

### Пример 1: Загрузка XML от разных пользователей

```java
@Test
public void efs_1_xml_krivonosov() {
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.sendXml(ReportType.EFS_FULL_ECP);
}

@Test
public void efs_1_xml_bezdomniy() {
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.BEZDOMNIY_IVAN);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.sendXml(ReportType.EFS_FULL_ECP);
}
```

### Пример 2: Параметризованный тест с разными пользователями

```java
@DataProvider(name = "testUsers")
public Object[][] usersProvider() {
    return new Object[][] {
        {TestUsers.KRIVONOSOV_ALEXANDER},
        {TestUsers.BEZDOMNIY_IVAN}
    };
}

@Test(dataProvider = "testUsers")
public void efs_1_xml_multiuser(TestUsers user) {
    steps.authorizeEVS(UIType.EVS_UAT_LKS, user);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.sendXml(ReportType.EFS_FULL_ECP);
}
```

### Пример 3: Обратная совместимость (без указания пользователя)

```java
@Test
public void efs_1_xml_default() {
    // Использует evs.username/password из credentials.properties
    steps.authorizeEVS(UIType.EVS_UAT_LKS);
    
    steps.addReports();
    // ...
}
```

---

## 🆕 Добавление нового пользователя

1. Добавьте в enum `TestUsers.java`:
   ```java
   NEW_USER("evs.user3", "Новый Пользователь");
   ```

2. Добавьте в `credentials.properties`:
   ```properties
   evs.user3.username=логин
   evs.user3.password=пароль
   evs.user3.organization=ОРГАНИЗАЦИЯ -XXXXXXX
   ```

3. Добавьте переменные в GitLab CI/CD (если нужно)

4. Используйте в тестах:
   ```java
   steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.NEW_USER);
   ```

---

## ✅ Преимущества

- ✅ Пароли **НЕ в коде**, только префиксы
- ✅ CI/CD переменные **перекрывают** локальный файл
- ✅ Читаемый код: `TestUsers.KRIVONOSOV_ALEXANDER`
- ✅ Allure отчеты: ФИО пользователя в степах
- ✅ Обратная совместимость со старыми тестами

---

## 🔍 Troubleshooting

**Ошибка:** "Credentials для пользователя не найдены"

**Решение:**
1. Проверьте `credentials.properties` - есть ли ключи `evs.user1.username`, `evs.user1.password`, `evs.user1.organization`
2. В CI/CD проверьте переменные окружения в GitLab

**Ошибка:** "Element not found: selectUserCardName"

**Решение:**
1. Проверьте правильность названия организации (с пробелами)
2. Убедитесь, что пользователь имеет доступ к этой организации
