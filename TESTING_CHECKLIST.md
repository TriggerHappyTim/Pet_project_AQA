# Чеклист тестирования - EVS Testing Framework

**Дата создания:** 2026-02-03  
**Статус:** Активный

---

## ✅ Выполнено через Playwright MCP Browser

- [x] Открытие браузера
- [x] Навигация на страницу авторизации EVS UAT LKS
- [x] Проверка перенаправления на форму ЕПГУ
- [x] Проверка URL и параметров OAuth2
- [x] Документирование текущего состояния

---

## ⏳ Требует выполнения через Java тесты

### Базовые проверки авторизации

#### Стандартная авторизация
- [ ] Открытие страницы авторизации
- [ ] Наличие поля логина
- [ ] Наличие поля пароля
- [ ] Наличие кнопки входа
- [ ] Ввод валидных данных
- [ ] Успешная авторизация
- [ ] Появление имени пользователя после входа

#### Авторизация через ЕПГУ
- [ ] Клик по ссылке "Войти через ЕПГУ"
- [ ] Загрузка формы ЕПГУ
- [ ] Наличие полей логина и пароля
- [ ] Ввод данных и авторизация
- [ ] Выбор карточки пользователя
- [ ] Успешный вход в систему

### Негативные сценарии (уже реализованы в NegativeTestCases)

- [x] `testLoginWithInvalidUsername()` - невалидный логин
- [x] `testLoginWithInvalidPassword()` - невалидный пароль
- [x] `testLoginWithEmptyFields()` - пустые поля
- [x] `testMissingElement()` - отсутствующий элемент
- [x] `testElementTimeout()` - таймаут элемента
- [x] `testInvalidEmailFormat()` - невалидный email формат
- [x] Параметризованные тесты с DataProvider

### Проверки КриптоПРО (CryptoProCertificateTest)

- [x] `verifyCryptoProPluginLoaded()` - загрузка плагина
- [x] `testCertificateSelectionAndSigning()` - выбор и подписание
- [x] `testPageDiagnostics()` - диагностика страницы
- [x] `quickPluginCheck()` - быстрая проверка

---

## 🎯 Команды для запуска проверок

### Запуск всех негативных тестов авторизации

```cmd
mvn clean test -Dtest=NegativeTestCases
```

### Запуск конкретного негативного теста

```cmd
# Невалидный логин
mvn clean test -Dtest=NegativeTestCases#testLoginWithInvalidUsername

# Пустые поля
mvn clean test -Dtest=NegativeTestCases#testLoginWithEmptyFields

# Невалидный email
mvn clean test -Dtest=NegativeTestCases#testInvalidEmailFormat
```

### Запуск тестов КриптоПРО

```cmd
# Все тесты КриптоПРО
mvn clean test -Dtest=CryptoProCertificateTest

# Конкретный тест
mvn clean test -Dtest=CryptoProCertificateTest#verifyCryptoProPluginLoaded
```

### Запуск по группам

```cmd
# Все smoke тесты
mvn clean test -Dgroups=smoke

# Все web тесты
mvn clean test -Dgroups=web

# Все негативные тесты
mvn clean test -Dgroups=negative

# Комбинация групп
mvn clean test -Dgroups="web,smoke"
```

---

## 📋 Следующие шаги

1. **Запустить негативные тесты авторизации:**
   ```cmd
   mvn clean test -Dtest=NegativeTestCases
   ```

2. **Проверить результаты в Allure:**
   ```cmd
   mvn allure:serve
   ```

3. **Запустить smoke тесты:**
   ```cmd
   mvn clean test -Dgroups=smoke
   ```

4. **Проверить стабильность тестов:**
   - Запустить несколько раз
   - Проверить flaky тесты
   - Использовать retry механизм

---

## 🔍 Проверка результатов

### Что проверить после запуска:

- ✅ Все тесты запускаются без ошибок компиляции
- ✅ Тесты выполняются без NullPointerException
- ✅ Скриншоты создаются при ошибках
- ✅ Allure отчеты генерируются корректно
- ✅ Сообщения об ошибках информативны
- ✅ Retry механизм работает для flaky тестов

---

**Дата обновления:** 2026-02-03  
**Статус:** Готово к выполнению
