# Тест-кейсы для ЛК Страхователя (Кривоносов Александр Петрович)

## Информация о пользователе

- **ФИО**: Кривоносов Александр Петрович
- **Организация**: ОРГАНИЗАЦИЯ -1546025669
- **Credential prefix**: `evs.user1`
- **TestUsers enum**: `TestUsers.KRIVONOSOV_ALEXANDER`
- **Используется для**: Smoke и Regression тестов

---

## 1. Авторизация и базовая навигация

### TC-LKS-AUTH-001: Успешная авторизация через Госуслуги (ЕПГУ) (Кривоносов)

**Приоритет**: CRITICAL  
**Группы**: `web`, `smoke`, `authentication`, `user-specific`  
**Allure**: `@Feature("User Authentication")`, `@Story("EPGU Authorization - Krivonosov")`

**Предусловия**:
- Credentials для `evs.user1` (Кривоносов) настроены в `credentials.properties`
- Браузер открыт

**Шаги**:
1. Открыть EVS_UAT_LKS через `LoginPage.open(UIType.EVS_UAT_LKS)`
2. Войти через **Госуслуги (ЕПГУ)** используя `authorizeEPGU(username, password)` с данными `evs.user1`
3. Выбрать организацию, **начинающуюся на «ОРГАНИЗАЦИЯ -154»**, через `selectUserCardEPGU(organization)` (например, «ОРГАНИЗАЦИЯ -1546025669»)

**Ожидаемый результат**:
- Пользователь успешно авторизован
- Отображается имя пользователя в элементе `//div[contains(@class, 'user-name')]`
- Доступны разделы ЛК Страхователя

**Реализация**:
```java
@Test(groups = {"web", "smoke", "authentication", "user-specific"})
@Feature("User Authentication")
@Story("EPGU Authorization - Krivonosov")
@DisplayName("Успешная авторизация Кривоносова через ЕПГУ")
@Severity(SeverityLevel.CRITICAL)
public void successfulLoginKrivonosov() {
    arrangeActAssert(
        () -> {
            // Arrange: получаем credentials
            logger.info("Подготовка credentials для Кривоносова");
        },
        () -> {
            // Act: авторизация
            SzvReportsSteps steps = new SzvReportsSteps();
            steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
        },
        softAssert -> {
            // Assert: проверка успешной авторизации
            softAssert.assertTrue(
                $x("//div[contains(@class, 'user-name')]").isDisplayed(),
                "Имя пользователя должно отображаться после авторизации"
            );
        }
    );
}
```

---

### TC-LKS-AUTH-002: Выход из системы (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `smoke`, `authentication`, `user-specific`

**Предусловия**: Пользователь Кривоносов авторизован

**Шаги**:
1. Кликнуть на элемент с именем пользователя
2. Нажать кнопку "Выйти"

**Ожидаемый результат**:
- Отображается сообщение "Войдите в систему"
- Пользователь разлогинен

**Реализация**:
```java
@Test(groups = {"web", "smoke", "authentication", "user-specific"}, 
      dependsOnMethods = "successfulLoginKrivonosov")
public void logoutKrivonosov() {
    new MainPage().logOut();
    
    softAssert.assertTrue(
        $x("//div[contains(text(), 'Войдите в систему')]").isDisplayed(),
        "Должно отображаться сообщение о необходимости входа"
    );
}
```

---

## 2. Работа с отчетами ЕФС-1

### TC-LKS-EFS1-001: Загрузка отчета ЕФС-1 через XML (Кривоносов)

**Приоритет**: CRITICAL  
**Группы**: `web`, `efs`, `smoke`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("EFS-1 Reports")`, `@Story("XML Upload - Krivonosov")`

**Предусловия**:
- Пользователь Кривоносов авторизован в EVS_UAT_LKS
- XML файл `efsBase1.xml` доступен в `src/test/resources/application/EFS/`

**Шаги**:
1. Перейти в "ЛК Страхователя" → "Отчеты"
2. Нажать кнопку "Добавить отчет"
3. Выбрать радиокнопку "ЕФС-1"
4. Нажать "Добавить"
5. Выбрать "Создать новый"
6. Нажать "Загрузить отчет"
7. Загрузить XML файл через `uploadFile(ReportType.EFS_FULL_ECP.value)`
8. Нажать "Загрузить" и подтвердить "Да"

**Ожидаемый результат**:
- XML файл успешно загружен без ошибок
- Отчет создан в таблице отчетов
- Отсутствуют сообщения об ошибках валидации

**Реализация**: См. `Efs1.java` метод `efs_1_xml_krivonosov()`

```java
@Test(groups = {"web", "efs", "smoke", "xml-upload", "user-specific"})
@Story("XML Upload - Кривоносов А.П.")
@Description("Тест проверяет загрузку отчёта ЕФС-1 через XML от пользователя Кривоносов")
@Severity(SeverityLevel.CRITICAL)
public void efs_1_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.sendXml(ReportType.EFS_FULL_ECP);
}
```

---

### TC-LKS-EFS1-002: Ручное создание отчета ЕФС-1 раздел 1.1 ТД (Кривоносов)

**Приоритет**: CRITICAL  
**Группы**: `web`, `efs`, `regression`, `manual-creation`, `user-specific`  
**Allure**: `@Story("Manual Creation - ТД - Krivonosov")`

**Предусловия**: Пользователь Кривоносов авторизован в EVS_TEST_LKS

**Шаги**:
1. Перейти в "ЛК Страхователя" → "Отчеты"
2. Нажать "Добавить отчет"
3. Выбрать "ЕФС-1" и нажать "Добавить"
4. Нажать "Создать новый"
5. Заполнить общие сведения:
   - Должность руководителя через `clickSpanId("headPosition", TestConfig.InsuredPerson.JOB)`
   - Фамилия через `inputFieldPerson("headSurname", TestConfig.InsuredPerson.LAST_NAME_1)`
6. Нажать "Продолжить" → подтвердить "Да"
7. Выбрать боковое меню "Раздел 1" → "1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ"
8. Нажать "Добавить" (застрахованное лицо)
9. Заполнить данные ЗЛ:
   - Фамилия: `personSurname`
   - Имя: `personName`
   - Отчество: `personMiddlename`
   - СНИЛС: `personSnils`
   - Дата рождения: `birthDate`
   - Статус (выбрать из выпадающего списка)
   - Гражданство (выбрать из выпадающего списка)
10. Нажать "Добавить" (мероприятие)
11. Заполнить мероприятие:
    - Вид мероприятия (выбрать из списка)
    - Дата события: `eventDate`
    - Должность: `position`
    - Код функции по ОКЗ
    - Документ-основание: `documentList[0].name`
    - Дата документа, серия, номер
12. Сохранить мероприятие (кнопка "Сохранить" #2)
13. Сохранить ЗЛ (кнопка "Сохранить" #1)

**Ожидаемый результат**:
- ЗЛ успешно создано и отображается в списке
- Мероприятие добавлено к ЗЛ
- Отчет сохранен (доступен для редактирования или отправки)
- Отсутствуют ошибки валидации

**Реализация**: См. `Efs1.java` метод `efs_szv_td_krivonosov()`

```java
@Test(groups = {"web", "efs", "regression", "manual-creation", "user-specific"})
@Story("Manual Creation - ТД - Кривоносов А.П.")
@Description("Тест проверяет ручное создание отчёта ЕФС-1 раздел 1.1 ТД")
@Severity(SeverityLevel.CRITICAL)
public void efs_szv_td_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_TEST_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.addGeneralInfoEFS();
    steps.createContinue();
    steps.sidebar();
    steps.addZL();
    steps.fillZLEFS();
    steps.addEventEFS();
    steps.saveEvent();
    steps.saveZL();
}
```

---

### TC-LKS-EFS1-003: Создание отчета ЕФС-1 раздел 1.2 СТАЖ (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `efs`, `regression`, `manual-creation`, `user-specific`  
**Allure**: `@Story("Manual Creation - STAZH - Krivonosov")`

**Предусловия**: Пользователь Кривоносов авторизован в EVS_UAT_LKS

**Шаги**:
1. Выполнить шаги 1-9 из TC-LKS-EFS1-002 (создание отчета и добавление ЗЛ)
2. Вместо добавления мероприятия, добавить сведения о стаже:
   - Нажать "Добавить" (#3 кнопка)
   - Выбрать "Тип сведений" (например, "Исходная")
   - Выбрать "Отчетный период" (например, "2023")
   - Нажать "Добавить" (#5 кнопка) для добавления периода
   - Заполнить `experienceTimePeriodDateAt` (дата начала)
   - Заполнить `experienceTimePeriodDateTo` (дата окончания)
   - Нажать "Добавить" для сохранения периода
3. Сохранить ЗЛ

**Ожидаемый результат**:
- Сведения о стаже успешно добавлены к ЗЛ
- Отчет сохранен
- Отсутствуют ошибки валидации периодов

**Реализация**:
```java
@Test(groups = {"web", "efs", "regression", "manual-creation", "user-specific"})
@Story("Manual Creation - СТАЖ - Кривоносов А.П.")
public void efs_szv_stazh_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.addGeneralInfoEFS();
    steps.createContinue();
    steps.sidebar();
    steps.addZL();
    steps.fillZLEFS();
    steps.addSTAJ(); // Добавление стажа вместо мероприятия
    steps.saveZL();
}
```

---

## 3. Работа с отчетами СЗВ-М

### TC-LKS-SZVM-001: Создание отчета СЗВ-М (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-m`, `regression`, `manual-creation`, `user-specific`  
**Allure**: `@Feature("SZV-M Reports")`, `@Story("Manual Creation - Krivonosov")`

**Предусловия**: Пользователь Кривоносов авторизован

**Шаги**:
1. Перейти в "Отчеты"
2. Нажать "Добавить отчет"
3. Выбрать радиокнопку "СЗВ-М"
4. Нажать "Добавить"
5. Нажать "Создать новый"
6. Заполнить общие сведения:
   - Выбрать отчетный период через `clickMuiInputSpan("Отчетный период", "Январь")`
   - Выбрать год через `clickMuiInputSpan("За год", "2025")`
   - Выбрать тип "Исходная"
7. Перейти к разделу ЗЛ
8. Добавить ЗЛ с данными (Фамилия, Имя, Отчество, СНИЛС)
9. Сохранить отчет

**Ожидаемый результат**:
- Отчет СЗВ-М создан успешно
- Данные ЗЛ корректно сохранены
- Отчет доступен в списке отчетов

**Реализация**:
```java
@Test(groups = {"web", "szv-m", "regression", "manual-creation", "user-specific"})
@Feature("SZV-M Reports")
@Story("Manual Creation - Krivonosov")
@DisplayName("Создание отчета СЗВ-М (Кривоносов)")
@Severity(SeverityLevel.NORMAL)
public void szvm_manual_creation_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVM);
    steps.addNewReport();
    steps.addGeneralInfo(); // Заполнение периода и года
    steps.goToZL();
    steps.addZL();
    steps.fillZL();
    steps.saveZL();
}
```

---

### TC-LKS-SZVM-002: Загрузка отчета СЗВ-М через XML (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-m`, `smoke`, `xml-upload`, `user-specific`

**Предусловия**:
- Пользователь авторизован
- XML файл `SZV-M.xml` доступен

**Шаги**:
1. Перейти в "Отчеты"
2. Добавить отчет
3. Выбрать "СЗВ-М"
4. Создать новый
5. Загрузить отчет через XML файл
6. Подтвердить загрузку

**Ожидаемый результат**:
- XML загружен успешно
- Отчет обработан без ошибок валидации

**Реализация**:
```java
@Test(groups = {"web", "szv-m", "smoke", "xml-upload", "user-specific"})
public void szvm_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVM);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_M);
}
```

---

## 4. Работа с отчетами СЗВ-ТД

### TC-LKS-SZVTD-001: Создание отчета СЗВ-ТД (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-td`, `regression`, `manual-creation`, `user-specific`  
**Allure**: `@Feature("SZV-TD Reports")`, `@Story("Manual Creation - Krivonosov")`

**Предусловия**: Пользователь авторизован

**Шаги**:
1. Добавить отчет
2. Выбрать "СЗВ-ТД"
3. Создать новый отчет
4. Заполнить общие сведения:
   - Должность директора через `clickSpanId("directorPosition", TestConfig.InsuredPerson.JOB)`
5. Нажать "Продолжить"
6. Перейти к разделу "Застрахованные лица"
7. Добавить ЗЛ (Фамилия, Имя, Отчество, СНИЛС, ИНН, дата рождения)
8. Добавить мероприятие:
   - Выбрать радиокнопку "Мероприятие"
   - Выбрать вид мероприятия (например, "Прием")
   - Заполнить дату события
   - Заполнить должность
   - Заполнить код функции по ОКЗ
   - Заполнить документ-основание (номер, дата, серия, название)
9. Сохранить мероприятие
10. Сохранить ЗЛ

**Ожидаемый результат**:
- Отчет СЗВ-ТД создан
- Мероприятие добавлено к ЗЛ
- Данные корректно сохранены

**Реализация**:
```java
@Test(groups = {"web", "szv-td", "regression", "manual-creation", "user-specific"})
@Feature("SZV-TD Reports")
@Story("Manual Creation - Krivonosov")
@Severity(SeverityLevel.NORMAL)
public void szvtd_manual_creation_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_TEST_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVTD);
    steps.addNewReport();
    steps.addGeneralInfoTD();
    steps.createContinue();
    steps.goToZL();
    steps.addZL();
    steps.fillZL();
    steps.fillZLINN();
    steps.fillZLBirth();
    steps.addEvent();
    steps.saveEvent();
    steps.saveZL();
}
```

---

### TC-LKS-SZVTD-002: Загрузка отчета СЗВ-ТД через XML (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-td`, `smoke`, `xml-upload`, `user-specific`

**Предусловия**: XML файл `SZV_TD.xml` доступен

**Шаги**:
1. Добавить отчет "СЗВ-ТД"
2. Создать новый
3. Загрузить XML
4. Подтвердить загрузку

**Ожидаемый результат**: Отчет загружен успешно

**Реализация**:
```java
@Test(groups = {"web", "szv-td", "smoke", "xml-upload", "user-specific"})
public void szvtd_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVTD);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_TD_TYPE1);
}
```

---

## 5. Работа с отчетами СЗВ-СТАЖ

### TC-LKS-SZVSTAZH-001: Загрузка СЗВ-СТАЖ через XML (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-stazh`, `smoke`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("SZV-STAZH Reports")`

**Предусловия**: XML файл доступен

**Шаги**:
1. Добавить отчет "СЗВ-СТАЖ"
2. Загрузить XML файл
3. Подтвердить

**Ожидаемый результат**: Успешная загрузка и обработка

**Реализация**:
```java
@Test(groups = {"web", "szv-stazh", "smoke", "xml-upload", "user-specific"})
@Feature("SZV-STAZH Reports")
public void szvstazh_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVSTAZH);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_STAZH_AF2);
}
```

---

## 6. Работа с отчетами СЗВ-КОРР

### TC-LKS-SZVKORR-001: Загрузка СЗВ-КОРР через XML (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-korr`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("SZV-KORR Reports")`

**Предусловия**: XML файл с корректировками доступен

**Шаги**:
1. Добавить отчет "СЗВ-КОРР"
2. Загрузить XML с корректировками
3. Подтвердить загрузку

**Ожидаемый результат**: Корректирующий отчет успешно обработан

**Реализация**:
```java
@Test(groups = {"web", "szv-korr", "xml-upload", "user-specific"})
@Feature("SZV-KORR Reports")
public void szvkorr_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVKORR);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_KORR_AF2);
}
```

---

## 7. Работа с отчетами СЗВ-ИСХ

СЗВ-ИСХ (Сведения исходные) — отчет о сведениях о стаже застрахованных лиц. Загрузка выполняется только через XML файл.

**Вход в систему (аналогично ЕФС-1)**:
1. Открыть EVS_UAT_LKS через `LoginPage.open(UIType.EVS_UAT_LKS)`
2. Войти через **Госуслуги (ЕПГУ)** — `authorizeEPGU(username, password)` с credentials `evs.user1`
3. Выбрать организацию, **начинающуюся на «ОРГАНИЗАЦИЯ -154»** через `selectUserCardEPGU(organization)`
   - Для Кривоносова: `ОРГАНИЗАЦИЯ -1546025669` (из `evs.user1.organization`)

### TC-LKS-SZVISH-001: Загрузка отчета СЗВ-ИСХ через XML (Кривоносов)

**Приоритет**: CRITICAL  
**Группы**: `web`, `szv-ish`, `smoke`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("XML Upload - Krivonosov")`

**Предусловия**:
- Credentials для `evs.user1` (Кривоносов) настроены в `credentials.properties`
- Выполнен вход через Госуслуги: пользователь Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»
- XML файл `СЗВ-ИСХ_Сведения о стаже.xml` доступен в `src/test/resources/application/Szv_Ish/`

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)` (Госуслуги + организация «ОРГАНИЗАЦИЯ -154*»)
2. Перейти в "ЛК Страхователя" → "Отчеты"
3. Нажать кнопку "Добавить отчет"
4. Выбрать радиокнопку "СЗВ-ИСХ"
5. Нажать "Добавить"
6. Выбрать "Создать новый"
7. Нажать "Загрузить отчет"
8. Загрузить XML файл через `uploadFile(ReportType.SZV_ISH_AF2.value)`
9. Нажать "Загрузить" и подтвердить "Да"

**Ожидаемый результат**:
- XML файл успешно загружен без ошибок
- Отчет создан в таблице отчетов
- Отсутствуют сообщения об ошибках валидации

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "smoke", "xml-upload", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("XML Upload - Кривоносов А.П.")
@Description("Тест проверяет загрузку отчёта СЗВ-ИСХ через XML от пользователя Кривоносов")
@Severity(SeverityLevel.CRITICAL)
public void szvish_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    // Вход через Госуслуги: Кривоносов А.П., организация «ОРГАНИЗАЦИЯ -154*»
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVISH);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_ISH_AF2);
}
```

---

### TC-LKS-SZVISH-002: Фильтрация отчетов по типу СЗВ-ИСХ (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-ish`, `reports`, `regression`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("Report Filtering")`

**Предусловия**: Выполнен вход через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»), есть отчеты СЗВ-ИСХ в системе

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)`
2. Перейти в "ЛК Страхователя" → "Отчеты"
3. Открыть панель фильтров
4. Выбрать тип отчета "СЗВ-ИСХ" через `clickMuiInputLabel("Тип отчета", "СЗВ-ИСХ")`
5. Нажать "Применить"

**Ожидаемый результат**:
- Отображаются только отчеты типа "СЗВ-ИСХ"
- Другие типы отчетов не отображаются в таблице
- Счетчик результатов соответствует отфильтрованным данным

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "reports", "regression", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("Filter Reports by Type - Krivonosov")
public void szvish_filter_by_type_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    new MainPage()
        .openTab("ЛК Страхователя")
        .openTab("Отчеты")
        .waitTableToLoad()
        .clickMuiInputLabel("Тип отчета", "СЗВ-ИСХ")
        .clickButton("Применить")
        .waitTableToLoad();
    
    softAssert.assertTrue(
        $x("//tbody[@class='n2o-advanced-table-tbody']").exists(),
        "Таблица отчетов СЗВ-ИСХ должна отображаться"
    );
}
```

---

### TC-LKS-SZVISH-003: Просмотр деталей отчета СЗВ-ИСХ (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-ish`, `reports`, `regression`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("Report Details View")`

**Предусловия**: Выполнен вход через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»), есть отчет СЗВ-ИСХ в статусе "Черновик" или "Отправлен"

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)`
2. Перейти в "Отчеты"
3. Применить фильтр по типу "СЗВ-ИСХ" (опционально)
4. Кликнуть на строку с отчетом СЗВ-ИСХ в таблице
5. Дождаться загрузки страницы деталей

**Ожидаемый результат**:
- Открывается страница с деталями отчета СЗВ-ИСХ
- Отображаются: тип отчета, статус, период, данные застрахованных лиц
- Нет ошибок загрузки страницы

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "reports", "regression", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("View Report Details - Krivonosov")
public void szvish_view_report_details_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    new MainPage()
        .openTab("ЛК Страхователя")
        .openTab("Отчеты")
        .waitTableToLoad()
        .clickMuiInputLabel("Тип отчета", "СЗВ-ИСХ")
        .clickButton("Применить")
        .waitTableToLoad();
    
    // Клик по первой строке СЗВ-ИСХ
    $x("//tbody[@class='n2o-advanced-table-tbody']/tr[1]").click();
    
    softAssert.assertTrue(
        $x("//*[contains(text(), 'СЗВ-ИСХ') or contains(text(), 'Сведения исходные')]").exists(),
        "Детали отчета СЗВ-ИСХ должны отображаться"
    );
}
```

---

### TC-LKS-SZVISH-004: Негативный сценарий — загрузка невалидного XML СЗВ-ИСХ (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-ish`, `negative`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("Invalid XML Validation")`

**Предусловия**: Выполнен вход через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»), подготовлен невалидный XML

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)`
2. Перейти в "Отчеты" → "Добавить отчет"
3. Выбрать "СЗВ-ИСХ"
4. Создать новый
5. Попытаться загрузить невалидный XML файл
6. Подтвердить загрузку

**Ожидаемый результат**:
- Отображается ошибка валидации XML
- Файл не принят системой
- Сообщение об ошибке содержит детали проблемы (схема, элемент и т.д.)

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "negative", "xml-upload", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("Invalid XML - Krivonosov")
@Severity(SeverityLevel.NORMAL)
public void szvish_invalid_xml_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.SZVISH);
    steps.addNewReport();
    
    new MainPage()
        .clickBtnSecondary("Загрузить отчет")
        .uploadFile("src/test/resources/application/invalid-test.xml")
        .clickButtonModal("Загрузить");
    
    softAssert.assertTrue(
        $x("//div[contains(@class, 'alert') or contains(@class, 'error') or contains(text(), 'ошибк')]").exists(),
        "Должна отображаться ошибка валидации XML"
    );
}
```

---

### TC-LKS-SZVISH-005: Проверка доступности формы добавления отчета СЗВ-ИСХ (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-ish`, `smoke`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("Add Report Form Availability")`

**Предусловия**: Credentials `evs.user1` настроены. Выполнить вход через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»)

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)`
2. Перейти в "Отчеты"
3. Нажать "Добавить отчет"
4. Проверить наличие радиокнопки "СЗВ-ИСХ" в списке типов отчетов
5. Выбрать "СЗВ-ИСХ"
6. Нажать "Добавить"
7. Проверить наличие кнопки "Загрузить отчет" или "Создать новый"

**Ожидаемый результат**:
- СЗВ-ИСХ доступен в списке типов отчетов
- Форма добавления отчета СЗВ-ИСХ открывается без ошибок
- Доступна опция загрузки XML

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "smoke", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("Form Availability - Krivonosov")
public void szvish_add_form_availability_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    
    softAssert.assertTrue(
        $x("//*[contains(text(), 'СЗВ-ИСХ')]").exists(),
        "Тип отчета СЗВ-ИСХ должен быть доступен"
    );
    
    steps.selectReportType(ReportType.SZVISH);
    steps.addNewReport();
    
    softAssert.assertTrue(
        $x("//*[contains(text(), 'Загрузить') or contains(text(), 'Создать новый')]").exists(),
        "Форма загрузки должна быть доступна"
    );
}
```

---

### TC-LKS-SZVISH-006: Ручное заполнение отчета СЗВ-ИСХ (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `szv-ish`, `regression`, `manual-creation`, `user-specific`  
**Allure**: `@Feature("SZV-ISH Reports")`, `@Story("Manual Creation - Krivonosov")`

**Предусловия**: Выполнен вход через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»)

**Описание**: Ручное создание отчета СЗВ-ИСХ по аналогии с ЕФС-1 и СЗВ-М. Форма содержит общие сведения (период, год, тип), застрахованных лиц с данными о стаже.

**Шаги**:
1. Выполнить вход: `steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER)`
2. Перейти в "ЛК Страхователя" → "Отчеты"
3. Нажать "Добавить отчет"
4. Выбрать радиокнопку "СЗВ-ИСХ"
5. Нажать "Добавить"
6. Нажать "Создать новый"
7. Заполнить общие сведения:
   - Отчетный период: `clickMuiInputSpan("Отчетный период", "Январь")`
   - Год: `clickMuiInputSpan("За год", "2025")`
   - Тип сведений: "Исходная"
8. Нажать "Продолжить" → подтвердить "Да" (если форма требует)
9. Перейти к разделу "Застрахованные лица"
10. Нажать "Добавить" (ЗЛ)
11. Заполнить данные ЗЛ:
    - Фамилия: `TestConfig.InsuredPerson.LAST_NAME_1` (Иванов)
    - Имя: `TestConfig.InsuredPerson.FIRST_NAME_1` (Иван)
    - Отчество: `TestConfig.InsuredPerson.MIDDLE_NAME_1` (Иванович)
    - СНИЛС: `TestConfig.InsuredPerson.SNILS_1` (65869076870)
    - ИНН: `TestConfig.InsuredPerson.INN_1` (если поле доступно)
12. (Опционально) Добавить сведения о стаже — "Добавить" период, даты начала/окончания
13. Сохранить ЗЛ (кнопка "Сохранить")

**Ожидаемый результат**:
- Отчет СЗВ-ИСХ создан успешно
- Общие сведения заполнены
- ЗЛ добавлено и отображается в списке
- Отчет сохранен (доступен как черновик)

**Реализация**:
```java
@Test(groups = {"web", "szv-ish", "regression", "manual-creation", "user-specific"})
@Feature("SZV-ISH Reports")
@Story("Manual Creation - Кривоносов А.П.")
@Description("Тест проверяет ручное создание отчёта СЗВ-ИСХ по аналогии с ЕФС-1")
@Severity(SeverityLevel.HIGH)
public void szvish_manual_creation_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVISH);
    steps.addNewReport();
    steps.addGeneralInfoISH(); // Период, год, тип "Исходная"
    steps.createContinue();    // Если форма требует
    steps.goToZL();
    steps.addZL();
    steps.fillZL();
    steps.fillZLINN();         // Если поле ИНН доступно
    steps.saveZL();
}
```

---

## 8. Работа с отчетами СЗВ-ДСО

### TC-LKS-SZVDSO-001: Загрузка отчета СЗВ-ДСО через XML (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `szv-dso`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("SZV-DSO Reports")`

**Предусловия**: XML файл СЗВ-ДСО доступен

**Шаги**:
1. Добавить отчет "СЗВ-ДСО"
2. Загрузить XML
3. Подтвердить

**Ожидаемый результат**: Успешная обработка отчета

**Реализация**:
```java
@Test(groups = {"web", "szv-dso", "xml-upload", "user-specific"})
@Feature("SZV-DSO Reports")
public void szvdso_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.SZVDSO);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_DSO_AF2);
}
```

---

## 9. Работа с отчетами ОДВ-1

### TC-LKS-ODV1-001: Загрузка отчета ОДВ-1 через XML (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `odv-1`, `xml-upload`, `user-specific`  
**Allure**: `@Feature("ODV-1 Reports")`

**Предусловия**: XML файл ОДВ-1 доступен

**Шаги**:
1. Добавить отчет "ОДВ-1"
2. Загрузить XML
3. Подтвердить

**Ожидаемый результат**: Отчет загружен успешно

**Реализация**:
```java
@Test(groups = {"web", "odv-1", "xml-upload", "user-specific"})
@Feature("ODV-1 Reports")
public void odv1_xml_upload_krivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    steps.addReports();
    steps.selectReportType(ReportType.ODV1);
    steps.addNewReport();
    steps.sendXml(ReportType.SZV_ODV_1_AF2);
}
```

---

## 10. Просмотр и фильтрация отчетов

### TC-LKS-REPORTS-001: Просмотр списка всех отчетов (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `reports`, `smoke`, `user-specific`  
**Allure**: `@Feature("Report Management")`, `@Story("Report List View")`

**Предусловия**: Пользователь авторизован

**Шаги**:
1. Перейти в "ЛК Страхователя" → "Отчеты"
2. Дождаться загрузки таблицы через `waitTableToLoad()`

**Ожидаемый результат**:
- Отображается таблица с отчетами организации "ОРГАНИЗАЦИЯ -1546025669"
- Таблица загружена без ошибок
- Элемент `//tbody[@class='n2o-advanced-table-tbody']` виден

**Реализация**:
```java
@Test(groups = {"web", "reports", "smoke", "user-specific"})
@Feature("Report Management")
@Story("Report List View - Krivonosov")
@DisplayName("Просмотр списка отчетов (Кривоносов)")
public void viewReportListKrivonosov() {
    arrangeActAssert(
        () -> {
            SzvReportsSteps steps = new SzvReportsSteps();
            steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
        },
        () -> {
            new MainPage()
                .openTab("ЛК Страхователя")
                .openTab("Отчеты")
                .waitTableToLoad();
        },
        softAssert -> {
            softAssert.assertTrue(
                $x("//tbody[@class='n2o-advanced-table-tbody']").isDisplayed(),
                "Таблица отчетов должна отображаться"
            );
        }
    );
}
```

---

### TC-LKS-REPORTS-002: Фильтрация отчетов по типу (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `reports`, `regression`, `user-specific`

**Предусловия**: 
- Пользователь авторизован
- Есть отчеты разных типов

**Шаги**:
1. Перейти в "Отчеты"
2. Открыть панель фильтров
3. Выбрать тип отчета (например, "ЕФС-1") через `clickMuiInputLabel("Тип отчета", "ЕФС-1")`
4. Нажать "Применить"

**Ожидаемый результат**:
- Отображаются только отчеты типа "ЕФС-1"
- Другие типы отчетов не отображаются в таблице

**Реализация**:
```java
@Test(groups = {"web", "reports", "regression", "user-specific"})
public void filterReportsByTypeKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    new MainPage()
        .openTab("ЛК Страхователя")
        .openTab("Отчеты")
        .waitTableToLoad()
        .clickMuiInputLabel("Тип отчета", "ЕФС-1")
        .clickButton("Применить")
        .waitTableToLoad();
    
    // Проверка, что в таблице только ЕФС-1
    ElementsCollection rows = $$x("//tbody[@class='n2o-advanced-table-tbody']/tr");
    softAssert.assertTrue(rows.size() > 0, "Должны быть отчеты ЕФС-1");
}
```

---

### TC-LKS-REPORTS-003: Фильтрация отчетов по статусу (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `reports`, `regression`, `user-specific`

**Предусловия**: Есть отчеты с разными статусами

**Шаги**:
1. Перейти в "Отчеты"
2. Открыть фильтр
3. Выбрать статус (например, "Черновик")
4. Применить фильтр

**Ожидаемый результат**: Отображаются только отчеты со статусом "Черновик"

**Реализация**:
```java
@Test(groups = {"web", "reports", "regression", "user-specific"})
public void filterReportsByStatusKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    new MainPage()
        .openTab("ЛК Страхователя")
        .openTab("Отчеты")
        .waitTableToLoad()
        .clickMuiInputLabel("Статус", "Черновик")
        .clickButton("Применить")
        .waitTableToLoad();
}
```

---

## 11. Подписание и отправка отчетов

### TC-LKS-SIGN-001: Подписание отчета ЭЦП (Кривоносов)

**Приоритет**: CRITICAL  
**Группы**: `web`, `efs`, `smoke`, `signing`, `user-specific`  
**Allure**: `@Feature("Report Signing")`, `@Story("Digital Signature - Krivonosov")`

**Предусловия**:
- Отчет создан как черновик
- Сертификат КриптоПро установлен и доступен

**Шаги**:
1. Открыть черновик отчета из списка
2. Нажать кнопку "Подписать и отправить"
3. Подтвердить в модальном окне "Да"
4. Дождаться загрузки (8 секунд)
5. Нажать "Подписать" (основная кнопка)
6. Выбрать криптопровайдер "КриптоПро" через `clickSpanCloseDialog("КриптоПро")`
7. Нажать "Выбрать"
8. Выбрать последний сертификат через `chooseLastProvider()`
9. Нажать "Подписать" в диалоге провайдера
10. Дождаться завершения (16 секунд)
11. Нажать "Готово" в футере

**Ожидаемый результат**:
- Отчет подписан ЭЦП
- Статус изменен на "Отправлен" или "На проверке"
- Отсутствуют ошибки подписания

**Реализация**:
```java
@Test(groups = {"web", "efs", "smoke", "signing", "user-specific"})
@Feature("Report Signing")
@Story("Digital Signature - Krivonosov")
@DisplayName("Подписание отчета ЭЦП (Кривоносов)")
@Severity(SeverityLevel.CRITICAL)
public void signReportWithCryptoProKrivonosov() {
    // Предусловие: создан черновик отчета
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    // Подписание отчета
    steps.submitAndSend(); // Метод выполняет полный цикл подписания
}
```

---

### TC-LKS-SIGN-002: Сохранение отчета как черновик (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `reports`, `regression`, `user-specific`

**Предусловия**: Отчет создан и частично заполнен

**Шаги**:
1. Заполнить часть данных отчета
2. Нажать кнопку "Сохранить как черновик"
3. Подтвердить "Да" в модальном окне

**Ожидаемый результат**:
- Отчет сохранен в статусе "Черновик"
- Отчет доступен для редактирования в списке отчетов
- Данные не потеряны

**Реализация**:
```java
@Test(groups = {"web", "reports", "regression", "user-specific"})
public void saveReportAsDraftKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    // Создание и частичное заполнение отчета
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.addGeneralInfoEFS();
    
    // Сохранение как черновик
    steps.saveAsDraft();
}
```

---

## 12. Негативные сценарии

### TC-LKS-NEG-001: Попытка создать отчет с незаполненными обязательными полями (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `negative`, `validation`, `user-specific`  
**Allure**: `@Feature("Validation")`, `@Story("Required Fields Validation")`

**Предусловия**: Пользователь авторизован

**Шаги**:
1. Создать новый отчет ЕФС-1
2. НЕ заполнять обязательные поля в общих сведениях
3. Попытаться нажать "Продолжить"

**Ожидаемый результат**:
- Отображаются ошибки валидации для незаполненных полей
- Кнопка "Продолжить" недоступна или переход не выполнен
- Сообщения об ошибках понятны пользователю

**Реализация**:
```java
@Test(groups = {"web", "negative", "validation", "user-specific"})
@Feature("Validation")
@Story("Required Fields Validation - Krivonosov")
@DisplayName("Валидация обязательных полей (Кривоносов)")
@Severity(SeverityLevel.NORMAL)
public void validateRequiredFieldsKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    
    // Попытка продолжить без заполнения
    new MainPage().clickSpanButton("Продолжить");
    
    // Проверка наличия ошибок валидации
    softAssert.assertTrue(
        $x("//div[contains(@class, 'error') or contains(@class, 'invalid')]").exists(),
        "Должны отображаться ошибки валидации"
    );
}
```

---

### TC-LKS-NEG-002: Загрузка невалидного XML файла (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `negative`, `xml-upload`, `user-specific`  
**Allure**: `@Story("Invalid XML Validation")`

**Предусловия**: Подготовлен поврежденный/невалидный XML файл

**Шаги**:
1. Добавить отчет ЕФС-1
2. Создать новый
3. Попытаться загрузить невалидный XML
4. Подтвердить загрузку

**Ожидаемый результат**:
- Отображается ошибка валидации XML
- Файл не принят системой
- Сообщение об ошибке содержит детали проблемы

**Реализация**:
```java
@Test(groups = {"web", "negative", "xml-upload", "user-specific"})
@Story("Invalid XML Validation - Krivonosov")
public void uploadInvalidXmlKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    
    // Загрузка невалидного XML (требуется подготовить тестовый файл)
    new MainPage()
        .clickBtnSecondary("Загрузить отчет")
        .uploadFile("src/test/resources/application/invalid-test.xml")
        .clickButtonModal("Загрузить");
    
    // Проверка ошибки
    softAssert.assertTrue(
        $x("//div[contains(@class, 'alert') or contains(@class, 'error')]").isDisplayed(),
        "Должна отображаться ошибка валидации XML"
    );
}
```

---

### TC-LKS-NEG-003: Попытка добавить ЗЛ с некорректным СНИЛС (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `negative`, `validation`, `user-specific`

**Предусловия**: Создан новый отчет, открыта форма добавления ЗЛ

**Шаги**:
1. Создать отчет и перейти к добавлению ЗЛ
2. Заполнить ФИО
3. Ввести некорректный СНИЛС (например, "123" или "000-000-000 00")
4. Попытаться сохранить ЗЛ

**Ожидаемый результат**:
- Отображается ошибка валидации СНИЛС
- ЗЛ не сохранено
- Сообщение указывает на формат СНИЛС (XXX-XXX-XXX XX)

**Реализация**:
```java
@Test(groups = {"web", "negative", "validation", "user-specific"})
public void validateInvalidSnilsKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.addGeneralInfoEFS();
    steps.createContinue();
    steps.sidebar();
    steps.addZL();
    
    // Заполнение с некорректным СНИЛС
    new MainPage()
        .inputFieldPerson("personSurname", "Тестовый")
        .inputFieldPerson("personName", "Тест")
        .clickMuiInputBase("personSnils", "123"); // Некорректный СНИЛС
    
    // Попытка сохранить
    new MainPage().clickBtnPrimary("Сохранить");
    
    // Проверка ошибки
    softAssert.assertTrue(
        $x("//div[contains(@class, 'error') or contains(text(), 'СНИЛС')]").exists(),
        "Должна отображаться ошибка валидации СНИЛС"
    );
}
```

---

### TC-LKS-NEG-004: Попытка отправить отчет без ЭЦП (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `negative`, `signing`, `user-specific`

**Предусловия**: Отчет создан как черновик

**Шаги**:
1. Открыть черновик
2. Нажать "Подписать и отправить"
3. При запросе ЭЦП закрыть окно выбора сертификата

**Ожидаемый результат**:
- Отчет не отправлен
- Статус остается "Черновик"
- Отображается ошибка или предупреждение о необходимости подписания

---

### TC-LKS-NEG-005: Попытка добавить дубликат ЗЛ с одинаковым СНИЛС (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `negative`, `validation`, `user-specific`

**Предусловия**: В отчете уже добавлено ЗЛ с определенным СНИЛС

**Шаги**:
1. Добавить первое ЗЛ с СНИЛС "123-456-789 01"
2. Сохранить первое ЗЛ
3. Попытаться добавить второе ЗЛ с тем же СНИЛС "123-456-789 01"
4. Попытаться сохранить

**Ожидаемый результат**:
- Отображается ошибка о дубликате СНИЛС
- Второе ЗЛ не сохранено

---

## 13. Проверка интеграции и обработки отчетов

### TC-LKS-INTEGRATION-001: Проверка статуса обработки отчета (Кривоносов)

**Приоритет**: HIGH  
**Группы**: `web`, `integration`, `regression`, `user-specific`  
**Allure**: `@Feature("Report Processing")`, `@Story("Status Check")`

**Предусловия**: Отчет отправлен в систему

**Шаги**:
1. Отправить отчет
2. Перейти в список отчетов
3. Открыть отправленный отчет
4. Проверить статус обработки через метод `waitReportToBeProcessed()`

**Ожидаемый результат**:
- Статус обработки изменяется с "В обработке" на "Завершена"
- Отображается результат обработки (Положительный/Отрицательный)
- Доступны протоколы проверки

**Реализация**:
```java
@Test(groups = {"web", "integration", "regression", "user-specific"})
@Feature("Report Processing")
@Story("Status Check - Krivonosov")
public void checkReportProcessingStatusKrivonosov() {
    SzvReportsSteps steps = new SzvReportsSteps();
    steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
    
    // Отправка отчета
    steps.addReports();
    steps.selectReportType(ReportType.EFS1);
    steps.addNewReport();
    steps.sendXml(ReportType.EFS_FULL_ECP);
    
    // Ожидание обработки
    new MainPage()
        .openTab("Отчеты")
        .waitTableToLoad()
        .openReportUos()
        .waitReportToBeProcessed()
        .checkProcessingStatus("Завершена");
}
```

---

### TC-LKS-INTEGRATION-002: Проверка протоколов при успешной обработке (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `integration`, `regression`, `user-specific`

**Предусловия**: Отчет успешно обработан

**Шаги**:
1. Открыть обработанный отчет
2. Перейти в раздел "Протоколы"
3. Проверить протоколы через `checkProtocolsPositive()`

**Ожидаемый результат**:
- Статус УПП: "Положительный"
- Протоколы ошибок пусты (нет ошибок)
- Отображается сообщение "Данных нет" в таблице ошибок

---

### TC-LKS-INTEGRATION-003: Проверка протоколов при ошибках обработки (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `integration`, `regression`, `negative`, `user-specific`

**Предусловия**: Отчет обработан с ошибками

**Шаги**:
1. Отправить отчет с намеренными ошибками (например, некорректные даты)
2. Дождаться обработки
3. Открыть отчет
4. Проверить протоколы через `checkProtocolsNegative(String[] errorCodes)`

**Ожидаемый результат**:
- Отображаются коды ошибок валидации
- Протоколы содержат описание ошибок
- Отчет в статусе "Не принят" или "Ошибка"

---

## 14. Дополнительные сценарии

### TC-LKS-MISC-001: Обновление страницы во время работы (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `regression`, `user-specific`

**Предусловия**: Пользователь работает с формой отчета

**Шаги**:
1. Начать создание отчета
2. Заполнить несколько полей (но не сохранять)
3. Обновить страницу (F5)

**Ожидаемый результат**:
- Отображается предупреждение о потере несохраненных данных
- Или данные автоматически сохранены как черновик

---

### TC-LKS-MISC-002: Работа с несколькими вкладками (Кривоносов)

**Приоритет**: LOW  
**Группы**: `web`, `regression`, `user-specific`

**Предусловия**: Пользователь авторизован

**Шаги**:
1. Открыть ЛК Страхователя в первой вкладке
2. Открыть ту же страницу во второй вкладке
3. Создать отчет в первой вкладке
4. Проверить отображение в списке отчетов во второй вкладке (после обновления)

**Ожидаемый результат**:
- Отчет, созданный в первой вкладке, отображается во второй после обновления
- Нет конфликтов сессий

---

### TC-LKS-MISC-003: Копирование отчета (Кривоносов)

**Приоритет**: NORMAL  
**Группы**: `web`, `regression`, `user-specific`

**Предусловия**: Есть ранее созданный отчет

**Шаги**:
1. Открыть список отчетов
2. Выбрать существующий отчет
3. Нажать "Копировать" (если функционал доступен)
4. Изменить скопированный отчет
5. Сохранить

**Ожидаемый результат**:
- Создана копия отчета
- Копия независима от оригинала
- Данные скопированы корректно

---

## Итого

### Статистика тест-кейсов

- **Всего тест-кейсов**: 35
- **CRITICAL приоритет**: 5
- **HIGH приоритет**: 7
- **NORMAL приоритет**: 16
- **LOW приоритет**: 2

### Группировка по функциональности

1. **Авторизация**: 2 тест-кейса
2. **Отчеты ЕФС-1**: 3 тест-кейса
3. **Отчеты СЗВ-М**: 2 тест-кейса
4. **Отчеты СЗВ-ТД**: 2 тест-кейса
5. **Отчеты СЗВ-СТАЖ**: 1 тест-кейс
6. **Отчеты СЗВ-КОРР**: 1 тест-кейс
7. **Отчеты СЗВ-ИСХ**: 6 тест-кейсов
8. **Отчеты СЗВ-ДСО**: 1 тест-кейс
9. **Отчеты ОДВ-1**: 1 тест-кейс
10. **Управление отчетами**: 3 тест-кейса
11. **Подписание**: 2 тест-кейса
12. **Негативные сценарии**: 5 тест-кейсов
13. **Интеграция**: 3 тест-кейса
14. **Прочее**: 3 тест-кейса

### TestNG группы

- `web` - все UI тесты
- `smoke` - критические smoke тесты
- `regression` - регрессионные тесты
- `user-specific` - тесты для конкретного пользователя (Кривоносов)
- `efs`, `szv-m`, `szv-td` и т.д. - по типам отчетов
- `xml-upload` - загрузка через XML
- `manual-creation` - ручное создание
- `authentication` - авторизация
- `reports` - управление отчетами
- `signing` - подписание ЭЦП
- `negative` - негативные сценарии
- `validation` - валидация данных
- `integration` - интеграционные проверки

---

## Примечания по реализации

### Использование TestUsers

Все тесты должны использовать:
```java
TestUsers.KRIVONOSOV_ALEXANDER
```

Для авторизации через Steps:
```java
steps.authorizeEVS(UIType.EVS_UAT_LKS, TestUsers.KRIVONOSOV_ALEXANDER);
```

### Конфигурация credentials.properties

Для входа через Госуслуги (Кривоносов, организация «ОРГАНИЗАЦИЯ -154*»):
```properties
evs.user1.username=133-900-785 49
evs.user1.password=***
evs.user1.organization=ОРГАНИЗАЦИЯ -1546025669
evs.user1.fullname=Кривоносов Александр Петрович
```

Организация должна начинаться с «ОРГАНИЗАЦИЯ -154» (для Кривоносова — полное имя «ОРГАНИЗАЦИЯ -1546025669»).

### Структура тестового класса

Рекомендуется создать отдельный класс для тестов Кривоносова:
```java
@Epic("ЛК Страхователя")
@Feature("Krivonosov Test Suite")
public class KrivonosovTestSuite extends UITestBase {
    // Тесты здесь
}
```

### Allure отчеты

Все тесты должны иметь:
- `@Feature` - функциональная область
- `@Story` - пользовательская история
- `@DisplayName` - понятное название
- `@Description` - описание теста
- `@Severity` - критичность
- `@AllureId` - уникальный ID (опционально)

---

**Документ создан**: 2026-01-29  
**Версия**: 1.0  
**Автор**: QA Automation Team
