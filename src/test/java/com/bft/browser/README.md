# Browser Factory Pattern

Этот модуль реализует паттерн Factory для конфигурации браузеров в автоматизированных тестах.

## Архитектура

### Основные компоненты:

1. **BrowserConfigFactory** - интерфейс фабрики браузеров
2. **BaseBrowserFactory** - абстрактный базовый класс с общей логикой
3. **Реализации фабрик**:
   - `ChromeBrowserFactory` - для Google Chrome
   - `YandexBrowserFactory` - для Яндекс.Браузера
   - `FirefoxBrowserFactory` - для Mozilla Firefox
4. **BrowserFactoryManager** - менеджер для создания фабрик

## Использование

### Автоматическая конфигурация (через BaseTest)

```java
public class MyTest extends BaseTest {
    // Фабрика автоматически инициализируется в driverConfig()
    // и доступна через поле browserFactoryManager
}
```

### Ручное использование

```java
// Создание менеджера фабрик
BrowserFactoryManager factoryManager = new BrowserFactoryManager(
    "path/to/cryptopro.crx",
    "path/to/cryptopro.xpi",
    false // isRemote
);

// Создание фабрики для конкретного браузера
BrowserConfigFactory chromeFactory = factoryManager.createFactory("chrome");

// Получение конфигурации
Capabilities capabilities = chromeFactory.getCapabilities();
```

## Поддерживаемые браузеры

- **chrome** - Google Chrome
- **yandex** - Яндекс.Браузер
- **firefox** / **mozilla** - Mozilla Firefox

## Переменные окружения

- `BROWSER` - тип браузера (chrome, yandex, firefox)
- `BROWSER_VERSION` - версия браузера
- `CRYPTOPRO_PATH` - путь к расширению CryptoPro (.crx для Chrome/Yandex, .xpi для Firefox)
- `CHROME_DRIVER_PATH` - путь к chromedriver
- `YANDEX_DRIVER_PATH` - путь к yandexdriver
- `GECKO_DRIVER_PATH` - путь к geckodriver
- `HEADLESS` - запуск в headless режиме (true/false)

## Примеры запуска

```bash
# Chrome с расширением
mvn test -Dselenide.browser=chrome

# Firefox удаленно
mvn test -Dselenide.browser=firefox -Dselenide.remote=http://localhost:4444/wd/hub

# Yandex в headless режиме
mvn test -Dselenide.browser=yandex -Dselenide.headless=true
```

## Расширение функциональности

Для добавления нового браузера:

1. Создайте класс, наследующий `BaseBrowserFactory`
2. Реализуйте абстрактные методы
3. Добавьте тип браузера в `BrowserFactoryManager.createFactory()`

```java
public class CustomBrowserFactory extends BaseBrowserFactory {
    @Override
    public String getBrowserName() {
        return "custom";
    }

    @Override
    public boolean checkLocalBrowserAvailability() {
        // Логика проверки доступности браузера
        return true;
    }
}
```