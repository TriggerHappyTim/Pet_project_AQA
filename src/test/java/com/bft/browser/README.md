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
- `CRYPTOPRO_BASE64` - расширение CryptoPro в формате base64 (для удаленного запуска в CI)
- `CHROME_DRIVER_PATH` - путь к chromedriver
- `YANDEX_DRIVER_PATH` - путь к yandexdriver
- `GECKO_DRIVER_PATH` - путь к geckodriver
- `HEADLESS` - запуск в headless режиме (true/false)

## Установка расширения КриптоПРО для CI/CD

### Для удаленного запуска в GitLab CI

Расширение КриптоПРО автоматически устанавливается при запуске тестов в удаленном режиме (CI/CD).
Поддерживаются два способа установки:

#### Способ 1: Переменная окружения CRYPTOPRO_BASE64 (рекомендуется)

1. Закодируйте файл расширения в base64:
   ```bash
   # Для Chrome/Yandex (.crx файл)
   base64 -w 0 "CryptoPro Chrome 1.2.13.0.crx" > cryptopro_base64.txt
   
   # Для Firefox (.xpi файл)
   base64 -w 0 "cryptopro.ru.xpi" > cryptopro_base64.txt
   ```

2. Добавьте переменную окружения в GitLab CI:
   - Откройте `Settings → CI/CD → Variables`
   - Добавьте переменную `CRYPTOPRO_BASE64` со значением из файла `cryptopro_base64.txt`
   - Установите флаг **Masked** для безопасности

#### Способ 2: Путь к файлу расширения (CRYPTOPRO_PATH)

Если файл расширения доступен в CI окружении, укажите путь через переменную `CRYPTOPRO_PATH`:
```bash
export CRYPTOPRO_PATH=src/test/resources/CryptoPro\ Chrome\ 1.2.13.0.crx
export CRYPTOPRO_XPI_PATH=src/test/resources/cryptopro.ru.xpi
```

Расширение будет автоматически закодировано в base64 и установлено в браузер.

#### Способ 3: Предустановка в Docker-образе (для Firefox)

Для Firefox рекомендуется предустановить расширение в Docker-образе Selenium для стабильной работы:

```dockerfile
FROM selenium/standalone-firefox:latest
COPY cryptopro.ru.xpi /tmp/cryptopro.ru.xpi
# Добавьте логику установки расширения в образ
```

### Автоматическая установка

Код автоматически определяет способ установки расширения:
1. Если установлена переменная `CRYPTOPRO_BASE64` - используется она
2. Если указан путь в `CRYPTOPRO_PATH` - файл кодируется в base64 автоматически
3. Если ничего не найдено - выводится предупреждение, тесты продолжают работу без расширения

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