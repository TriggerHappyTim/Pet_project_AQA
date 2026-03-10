# Быстрый старт - Запуск тестов локально

## 🚀 Самый простой способ

### Через IntelliJ IDEA:

1. Откройте проект в IDEA
2. Найдите `CryptoProCertificateTest.java` или `NegativeTestCases.java`
3. Кликните правой кнопкой → `Run 'testMethodName()'`
4. Готово! ✅

---

## 📝 Через командную строку

### Windows (CMD):

```cmd
cd evs-testing-framework
mvn clean test -Dgroups=smoke
```

### Или используйте скрипт:

```cmd
cd evs-testing-framework
scripts\run-tests.bat --local --smoke -b chrome
```

---

## 🌐 Playwright MCP Browser

**Текущий статус:**
- ✅ Браузер открыт
- ✅ Страница авторизации загружена: `https://ecp-test.sfr.gov.ru/insurer/#/`
- ✅ Готов к выполнению проверок

**Использование:**
- Playwright MCP используется для визуального тестирования UI
- Для полного запуска Java тестов используйте Maven или IntelliJ IDEA

---

## 📋 Доступные команды

```cmd
# Все smoke тесты
mvn clean test -Dgroups=smoke

# Все web тесты  
mvn clean test -Dgroups=web

# Конкретный тест
mvn clean test -Dtest=CryptoProCertificateTest#verifyCryptoProPluginLoaded

# Негативные тесты
mvn clean test -Dtest=NegativeTestCases

# С Allure отчетом
mvn clean test -Dgroups=smoke && mvn allure:serve
```

---

**Подробная инструкция:** См. `RUN_TESTS_LOCAL.md`
