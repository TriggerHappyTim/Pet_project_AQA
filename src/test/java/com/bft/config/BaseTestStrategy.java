package com.bft.config;

import com.codeborne.selenide.Configuration;

/**
 * Базовый абстрактный класс для всех стратегий тестирования
 *
 * Содержит общую логику конфигурации, которая используется всеми стратегиями:
 * - Инициализация параметров из конфигурации
 * - Базовая настройка Selenide Configuration
 * - Реализация общих методов жизненного цикла тестов
 *
 * <p>Наследники должны реализовать абстрактные методы:
 * <ul>
 *   <li>{@link #setupEnvironmentVariables()} - настройка переменных окружения</li>
 *   <li>{@link #performPreTestActions()} - действия перед каждым тестом</li>
 *   <li>{@link #performPostTestActions()} - действия после каждого теста</li>
 *   <li>{@link #checkEnvironmentAvailability()} - проверка доступности окружения</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>{@code
 * public class CustomTestStrategy extends BaseTestStrategy {
 *     public CustomTestStrategy(TestConfig config) {
 *         super(config);
 *     }
 *
 *     @Override
 *     protected void setupEnvironmentVariables() {
 *         // специфичная настройка
 *     }
 *     // ... остальные абстрактные методы
 * }
 * }</pre>
 *
 * @author QA Automation Team
 * @version 2.0
 * @see TestStrategy для интерфейса стратегий
 * @see UITestStrategy для реализации UI стратегии
 * // @see ApiTestStrategy для реализации API стратегии - ЗАКОММЕНТИРОВАНО: API тесты не используются
 * @since 1.0
 */
public abstract class BaseTestStrategy implements TestStrategy {

    /**
     * Путь к расширению КриптоПРО для Chrome (.crx файл)
     */
    protected final String cryptoProPath;

    /**
     * Путь к расширению КриптоПРО для Firefox (.xpi файл)
     */
    protected final String cryptoProXpiPath;

    /**
     * Флаг использования удаленного Selenium сервера
     */
    protected final boolean isRemote;

    /**
     * Тип браузера для тестов (chrome, firefox, yandex и т.д.)
     */
    protected final String browser;

    /**
     * Версия браузера (может быть null)
     */
    protected final String browserVersion;

    /**
     * Флаг headless режима браузера
     */
    protected final boolean headless;

    /**
     * Создает базовую стратегию с параметрами из конфигурации
     *
     * Инициализирует все защищенные поля значениями из {@link TestConfig}.
     * Эти поля доступны наследникам для использования в их реализации.
     *
     * @param config конфигурация тестового окружения, не должна быть null
     * @throws NullPointerException если config равен null
     */
    protected BaseTestStrategy(TestConfig config) {
        this.cryptoProPath = config.getCryptoProPath();
        this.cryptoProXpiPath = config.getCryptoProXpiPath();
        this.isRemote = config.isRemote();
        this.browser = config.getBrowser();
        this.browserVersion = config.getBrowserVersion();
        this.headless = config.isHeadless();
    }

    /**
     * Настраивает окружение перед выполнением тестов
     *
     * Делегирует выполнение наследникам через абстрактный метод
     * {@link #setupEnvironmentVariables()}.
     *
     * @see TestStrategy#configureEnvironment()
     */
    @Override
    public void configureEnvironment() {
        // Настройка переменных окружения, если необходимо
        setupEnvironmentVariables();
    }

    /**
     * Настраивает базовую конфигурацию Selenide
     *
     * Устанавливает общие параметры Selenide, которые используются
     * всеми стратегиями. Наследники могут переопределить этот метод
     * для добавления специфичных настроек.
     *
     * <p>Базовые настройки:
     * <ul>
     *   <li>Размер окна: 1920x1080</li>
     *   <li>Таймаут: 10000 мс</li>
     *   <li>Интервал опроса: 500 мс</li>
     * </ul>
     *
     * @see TestStrategy#configureSelenide()
     */
    @Override
    public void configureSelenide() {
        // Базовая конфигурация Selenide
        Configuration.remote = isRemote ? System.getProperty("selenide.remote") : null;
        Configuration.browserVersion = browserVersion;
        Configuration.browserSize = "1920x1080";
        Configuration.headless = headless;
        Configuration.timeout = isRemote ? 20000 : 10000; // 20 сек для удаленного, 10 сек для локального
        Configuration.pollingInterval = 500;
    }

    /**
     * Выполняет действия перед каждым тестом
     *
     * Делегирует выполнение наследникам через абстрактный метод
     * {@link #performPreTestActions()}.
     *
     * @see TestStrategy#beforeTest()
     */
    @Override
    public void beforeTest() {
        // Действия перед каждым тестом
        performPreTestActions();
    }

    /**
     * Выполняет действия после каждого теста
     *
     * Делегирует выполнение наследникам через абстрактный метод
     * {@link #performPostTestActions()}.
     *
     * @see TestStrategy#afterTest()
     */
    @Override
    public void afterTest() {
        // Действия после каждого теста
        performPostTestActions();
    }

    /**
     * Возвращает базовый приоритет стратегии
     *
     * Наследники должны переопределить этот метод для установки
     * специфичного приоритета (например, UI: 10, API: 5).
     *
     * @return базовый приоритет (0 по умолчанию)
     * @see TestStrategy#getPriority()
     */
    @Override
    public int getPriority() {
        // Базовый приоритет
        return 0;
    }

    /**
     * Настраивает переменные окружения для стратегии
     *
     * Реализуется наследниками для установки специфичных переменных окружения,
     * путей к драйверам, системных свойств и других параметров, необходимых
     * для работы конкретной стратегии.
     *
     * <p>Примеры:
     * <ul>
     *   <li>UI: установка путей к драйверам браузеров</li>
     *   <li>API: установка базового URL и версии API</li>
     * </ul>
     */
    protected abstract void setupEnvironmentVariables();

    /**
     * Выполняет действия перед каждым тестом
     *
     * Реализуется наследниками для выполнения специфичных действий
     * перед запуском каждого тестового метода.
     *
     * <p>Примеры:
     * <ul>
     *   <li>UI: открытие браузера, максимизация окна, настройка КриптоПРО</li>
     *   <li>API: инициализация HTTP клиента, проверка доступности API</li>
     * </ul>
     */
    protected abstract void performPreTestActions();

    /**
     * Выполняет действия после каждого теста
     *
     * Реализуется наследниками для выполнения специфичных действий
     * после завершения каждого тестового метода.
     *
     * <p>Примеры:
     * <ul>
     *   <li>UI: закрытие браузера, очистка cookies и сессии</li>
     *   <li>API: очистка тестовых данных, сброс состояния HTTP клиента</li>
     * </ul>
     */
    protected abstract void performPostTestActions();

    /**
     * Проверяет, доступно ли окружение для данной стратегии
     *
     * Реализуется наследниками для проверки наличия всех необходимых
     * ресурсов и компонентов для работы стратегии.
     *
     * <p>Примеры проверок:
     * <ul>
     *   <li>UI: наличие браузера, доступность драйверов</li>
     *   <li>API: доступность API endpoints, валидность базового URL</li>
     * </ul>
     *
     * @return true если окружение доступно и стратегия может работать, false в противном случае
     * @see TestStrategy#isApplicable() для использования этого метода
     */
    protected abstract boolean checkEnvironmentAvailability();
}