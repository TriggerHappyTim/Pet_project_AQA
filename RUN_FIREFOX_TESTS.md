# Запуск тестов в Firefox и генерация Allure отчёта

## 🚀 Быстрый запуск

### Вариант 1: Через Command Prompt (CMD)

```cmd
# Перейдите в корневую папку проекта (где находится pom.xml)
cd /d <путь_к_проекту>\evs-testing-framework

rem Запуск одного теста в Firefox
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml

rem Генерация Allure отчёта
mvn allure:serve
```

### Вариант 2: Через PowerShell (если есть проблемы с кодировкой)

```powershell
# Перейдите в корневую папку проекта (где находится pom.xml)
Set-Location "<путь_к_проекту>\evs-testing-framework"

# Запуск теста
& mvn clean test -Pbrowser_firefox_local "-Dtest=Efs1#efs_1_xml"

# Генерация отчёта
& mvn allure:serve
```

### Вариант 3: Через bat-скрипт

Запустите файл: `run-firefox-test.bat` (двойной клик)

---

## 📋 Доступные команды

### 1. Запуск всех тестов EFS-1 в Firefox

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1
```

### 2. Запуск тестов с конкретным пользователем

```cmd
rem Тест от пользователя Кривоносов
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_krivonosov

rem Тест от пользователя Бездомный
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_bezdomniy
```

### 3. Запуск группы тестов

```cmd
rem Все smoke тесты
mvn clean test -Pbrowser_firefox_local -Dgroups=smoke

rem Все EFS тесты
mvn clean test -Pbrowser_firefox_local -Dgroups=efs

rem XML upload тесты
mvn clean test -Pbrowser_firefox_local -Dgroups=xml-upload
```

### 4. Запуск всех UI тестов

```cmd
mvn clean test -Pbrowser_firefox_local -Dgroups=web
```

---

## 📊 Генерация Allure отчёта

### Способ 1: Автоматический запуск в браузере (рекомендуется)

```cmd
mvn allure:serve
```

Эта команда:
1. Генерирует отчёт из результатов
2. Запускает локальный веб-сервер
3. Автоматически открывает отчёт в браузере

**Остановка сервера:** Нажмите `Ctrl+C` в терминале

### Способ 2: Генерация статического отчёта

```cmd
rem Генерация отчёта в папку target/site/allure-maven-plugin
mvn allure:report

rem Открыть отчёт
start target\site\allure-maven-plugin\index.html
```

### Способ 3: Использование Allure CLI

```cmd
rem Если установлен Allure CLI
allure generate allure-results --clean -o allure-report
allure open allure-report
```

---

## 🔧 Настройка Firefox для тестов

### Проверка установки Firefox

```cmd
"C:\Program Files\Mozilla Firefox\firefox.exe" --version
```

Если Firefox не установлен, скачайте с [mozilla.org](https://www.mozilla.org/ru/firefox/)

### Настройка профиля браузера

По умолчанию используется профиль `browser_firefox_local` из `pom.xml`.

Конфигурация находится в: `config/browser/browser_firefox_local.xml`

---

## 📝 Примеры использования

### Пример 1: Полный цикл (тест + отчёт)

```cmd
# Перейдите в корневую папку проекта (где находится pom.xml)
cd /d <путь_к_проекту>\evs-testing-framework

rem 1. Запуск тестов
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1

rem 2. Генерация и открытие отчёта
mvn allure:serve
```

### Пример 2: Множественный запуск тестов

```cmd
rem Запуск 3 разных тестов подряд
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml
mvn test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_krivonosov
mvn test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_bezdomniy

rem Генерация общего отчёта по всем прогонам
mvn allure:serve
```

### Пример 3: Параллельный запуск (если нужно)

```cmd
rem В pom.xml можно настроить параллельность
mvn clean test -Pbrowser_firefox_local -Dgroups=smoke -DthreadCount=2
```

---

## 🐛 Troubleshooting

### Проблема: Firefox не запускается

**Решение 1:** Проверьте путь к Firefox

```cmd
where firefox
```

**Решение 2:** Установите переменную окружения

```cmd
set PATH=%PATH%;C:\Program Files\Mozilla Firefox
```

**Решение 3:** Укажите путь явно в `selenide.properties`

```properties
selenide.browser=firefox
webdriver.gecko.driver=C:/path/to/geckodriver.exe
```

### Проблема: "Could not start a new session"

**Решение:** Обновите GeckoDriver

```cmd
rem Скачайте с https://github.com/mozilla/geckodriver/releases
rem Распакуйте в C:\WebDriver\geckodriver.exe
rem Добавьте в PATH
```

### Проблема: Allure отчёт не генерируется

**Решение 1:** Проверьте наличие результатов

```cmd
dir allure-results
```

**Решение 2:** Установите Allure CLI

```cmd
rem Через Scoop
scoop install allure

rem Через Chocolatey  
choco install allure
```

**Решение 3:** Используйте Maven плагин

```cmd
mvn io.qameta.allure:allure-maven:2.29.0:serve
```

### Проблема: Тесты падают из-за отсутствия credentials

**Решение:** Проверьте файл `credentials.properties`

```cmd
type src\test\resources\credentials.properties
```

Убедитесь, что заполнены:
```properties
evs.user1.username=...
evs.user1.password=...
evs.user1.organization=...
```

---

## 📂 Структура результатов

```
evs-testing-framework/
├── allure-results/           # Сырые результаты тестов
│   ├── *-result.json         # Результаты тестов
│   ├── *-container.json      # Информация о сьютах
│   └── *-attachment.*        # Скриншоты, логи
├── target/
│   ├── site/
│   │   └── allure-maven-plugin/  # Статический HTML отчёт
│   └── surefire-reports/    # TestNG/JUnit отчёты
└── allure-report/            # (если используется CLI)
```

---

## 🎯 Полезные флаги Maven

```cmd
rem Пропустить тесты, если нужно только собрать
mvn clean install -DskipTests

rem Показать подробный вывод
mvn test -X -Pbrowser_firefox_local

rem Показать только ошибки
mvn test -e -Pbrowser_firefox_local

rem Запуск без очистки (быстрее)
mvn test -Pbrowser_firefox_local

rem С логированием в файл
mvn test -Pbrowser_firefox_local > test-log.txt 2>&1
```

---

## 📊 Просмотр отчётов

### Maven Site отчёт

```cmd
mvn site
start target\site\index.html
```

### TestNG отчёт

```cmd
start target\surefire-reports\index.html
```

### Allure отчёт (через веб-сервер)

```cmd
mvn allure:serve
rem Откроется в браузере на http://localhost:RANDOM_PORT
```

---

## 🔗 Полезные ссылки

- [Selenide Documentation](https://selenide.org/)
- [Allure Report](https://docs.qameta.io/allure/)
- [TestNG](https://testng.org/)
- [Firefox WebDriver (GeckoDriver)](https://github.com/mozilla/geckodriver)

---

## ✅ Чек-лист перед запуском

- [ ] Firefox установлен
- [ ] GeckoDriver установлен (или Maven скачает автоматически)
- [ ] Java 11+ установлена
- [ ] Maven установлен
- [ ] Файл `credentials.properties` заполнен
- [ ] Проект скомпилирован: `mvn compile`

---

**Готово!** Теперь можете запускать тесты в Firefox и генерировать красивые Allure отчёты. 🎉
