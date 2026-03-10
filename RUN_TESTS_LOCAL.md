# Инструкция по запуску тестов локально

**Дата создания:** 2026-02-03  
**Статус:** ✅ Готово к использованию

---

## 🚀 Быстрый запуск

### Вариант 1: Через IntelliJ IDEA (рекомендуется)

1. Откройте проект в IntelliJ IDEA
2. Найдите тестовый класс (например, `CryptoProCertificateTest.java`)
3. Кликните правой кнопкой на класс или метод → `Run 'testMethodName()'`
4. Maven встроен в IDEA, дополнительная настройка не требуется

### Вариант 2: Через командную строку (Maven)

```cmd
cd evs-testing-framework
mvn clean test -Dgroups=smoke
```

### Вариант 3: Через скрипт run-tests.bat

```cmd
cd evs-testing-framework
scripts\run-tests.bat --local --smoke -b chrome
```

---

## 📋 Доступные команды Maven

### Запуск по группам тестов

```cmd
# Все smoke тесты
mvn clean test -Dgroups=smoke

# Все web тесты
mvn clean test -Dgroups=web

# Все негативные тесты
mvn clean test -Dgroups=negative

# Все crypto тесты
mvn clean test -Dgroups=crypto

# Комбинация групп
mvn clean test -Dgroups="web,smoke"
```

### Запуск конкретного теста

```cmd
# Весь тестовый класс
mvn clean test -Dtest=CryptoProCertificateTest

# Конкретный метод
mvn clean test -Dtest=CryptoProCertificateTest#verifyCryptoProPluginLoaded

# Несколько классов
mvn clean test -Dtest=NegativeTestCases,ApiNegativeTestCases
```

### Запуск с профилем браузера

```cmd
# Chrome (локально)
mvn clean test -Pbrowser_chrome_local -Dgroups=smoke

# Firefox (локально)
mvn clean test -Pbrowser_firefox_local -Dgroups=smoke
```

---

## 🌐 Playwright MCP Browser (визуальное тестирование)

### Текущий статус

- ✅ Браузер открыт через Playwright MCP
- ✅ Страница авторизации загружена: `https://ecp-test.sfr.gov.ru/insurer/#/`
- ✅ Страница перенаправлена на форму ЕПГУ: `https://extsso.test.ecp/oauth2/authorize`

### Использование Playwright MCP для проверок

Playwright MCP можно использовать для:
- Визуальной проверки элементов на странице
- Быстрой проверки UI без запуска полного теста
- Отладки селекторов элементов

**Примечание:** Playwright MCP используется для визуального тестирования. Для полного запуска Java/TestNG тестов используйте Maven или IntelliJ IDEA.

---

## 📊 Доступные тесты

### UI Тесты

#### CryptoProCertificateTest
- `verifyCryptoProPluginLoaded()` - проверка загрузки плагина КриптоПРО
- `testCertificateSelectionAndSigning()` - выбор и подписание сертификата
- `testPageDiagnostics()` - диагностика страницы
- `quickPluginCheck()` - быстрая проверка плагина

#### NegativeTestCases
- `testLoginWithInvalidUsername()` - авторизация с невалидным логином
- `testLoginWithInvalidPassword()` - авторизация с невалидным паролем
- `testLoginWithEmptyFields()` - авторизация с пустыми полями
- `testMissingElement()` - проверка отсутствующего элемента
- `testElementTimeout()` - проверка таймаута элемента
- `testInvalidEmailFormat()` - проверка невалидного email
- Параметризованные тесты с DataProvider

#### ApiNegativeTestCases
- `testNonExistentResource()` - запрос несуществующего ресурса (404)
- `testUnauthorizedRequest()` - неавторизованный запрос (401)
- `testInvalidRequestData()` - запрос с невалидными данными (400)
- `testServerError()` - ошибка сервера (500)
- `testRequestTimeout()` - превышение таймаута

---

## 🔧 Требования

### Системные требования

- Java 11+
- Maven 3.6+ (или Maven Wrapper)
- Chrome или Firefox браузер
- Доступ к тестовым окружениям (EVS_UAT_LKS, EVS_TEST_LKS)

### Переменные окружения

Для авторизации в тестах необходимо настроить:

```bash
# Windows
set evs.username=your_username@example.com
set evs.password=your_password

# Linux/Mac
export evs.username=your_username@example.com
export evs.password=your_password
```

---

## 📈 Генерация Allure отчета

После запуска тестов:

```cmd
# Генерация и открытие отчета
mvn allure:serve

# Только генерация (без открытия)
mvn allure:report
```

Отчет будет доступен по адресу: `http://localhost:port`

---

## 🐛 Troubleshooting

### Проблема: Maven не найден

**Решение:**
1. Используйте IntelliJ IDEA (Maven встроен)
2. Или добавьте Maven в PATH
3. Или используйте Maven Wrapper (`mvnw.cmd`)

### Проблема: Тесты не запускаются

**Решение:**
1. Проверьте, что вы в директории `evs-testing-framework`
2. Проверьте наличие `pom.xml`
3. Проверьте Java версию: `java -version` (должна быть 11+)

### Проблема: Браузер не запускается

**Решение:**
1. Убедитесь, что Chrome/Firefox установлен
2. Проверьте переменные окружения для браузера
3. Попробуйте запустить с явным указанием браузера: `-Pbrowser_chrome_local`

---

## ✅ Статус запуска

**Браузер Playwright MCP:**
- ✅ Открыт и заблокирован
- ✅ Страница авторизации загружена
- ✅ Готов к выполнению проверок

**Java тесты:**
- ⏳ Готовы к запуску через Maven или IntelliJ IDEA
- ✅ Все тесты скомпилированы без ошибок
- ✅ Helper классы созданы и готовы к использованию

---

**Дата:** 2026-02-03  
**Автор:** QA Automation Team
