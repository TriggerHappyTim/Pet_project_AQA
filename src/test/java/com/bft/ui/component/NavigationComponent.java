package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import org.openqa.selenium.By;

/**
 * Компонент для работы с навигацией и вкладками
 * 
 * Предоставляет унифицированный интерфейс для навигации по интерфейсу:
 * - Переход по вкладкам
 * - Выбор секций
 * - Навигация по sidebar (боковому меню)
 * - Открытие таблиц
 * - Работа с URL и историей браузера
 * 
 * <p>Все методы возвращают текущий экземпляр для поддержки fluent API.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Переход на вкладку
 * NavigationComponent navigation = new NavigationComponent("sidebar", "Навигация");
 * navigation.openTab("Отчеты");
 * 
 * // Выбор секции
 * navigation.selectSection("Раздел 1");
 * 
 * // Навигация по sidebar
 * navigation.navigateSidebar("sidebar__dropdown-title", "Раздел 1",
 *                            "sidebar__item inner", "1.1 ТД, 1.2 СТАЖ");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @since 1.0
 */
public class NavigationComponent extends BaseComponent {

    /**
     * Конструктор компонента навигации
     * @param navigationSelector селектор контейнера навигации
     * @param navigationName имя компонента для логирования
     */
    public NavigationComponent(String navigationSelector, String navigationName) {
        super(ElementFactory.css(navigationSelector).named(navigationName).waitVisible().build(), navigationName);
    }

    /**
     * Конструктор компонента навигации с By селектором
     * @param navigationLocator By селектор контейнера навигации
     * @param navigationName имя компонента для логирования
     */
    public NavigationComponent(By navigationLocator, String navigationName) {
        super(ElementFactory.by(navigationLocator).named(navigationName).waitVisible().build(), navigationName);
    }

    @Override
    public boolean isValid() {
        return isPresent() && isVisible();
    }

    /**
     * Переходит на вкладку по имени
     */
    public NavigationComponent openTab(String tabName) {
        logger.info("Переходим на вкладку '{}' в компоненте: {}", tabName, componentName);
        SmartElement tab = ElementFactory.xpath("//*[starts-with(@class,'sidebar__item') and text() = '" + tabName + "']")
                .named("Вкладка '" + tabName + "'")
                .waitClickable()
                .build();
        tab.click();
        return this;
    }

    /**
     * Открывает таблицу (кликает по ней)
     */
    public NavigationComponent openTable() {
        logger.info("Открываем таблицу в компоненте: {}", componentName);
        SmartElement table = ElementFactory.xpath("//table[@class='']")
                .named("Таблица")
                .waitClickable()
                .build();
        table.click();
        return this;
    }

    /**
     * Выбирает секцию по имени
     */
    public NavigationComponent selectSection(String sectionName) {
        logger.info("Выбираем секцию '{}' в компоненте: {}", sectionName, componentName);
        SmartElement section = ElementFactory.xpath("//section//a[text() = '" + sectionName + "']")
                .named("Секция '" + sectionName + "'")
                .waitClickable()
                .build();
        section.click();
        return this;
    }

    /**
     * Переходит по sidebar ссылке
     */
    public NavigationComponent navigateSidebar(String parentClass, String parentText, String childClass, String childText) {
        logger.info("Навигация по sidebar: {} -> {} в компоненте: {}", parentText, childText, componentName);

        // Кликаем по родительскому элементу
        SmartElement parent = ElementFactory.xpath("//div[contains(@class, '" + parentClass + "')][text() = '" + parentText + "']")
                .named("Родительский элемент '" + parentText + "'")
                .waitClickable()
                .build();
        parent.click();

        // Кликаем по дочернему элементу
        SmartElement child = ElementFactory.xpath("//a[contains(@class, '" + childClass + "')][text() = '" + childText + "']")
                .named("Дочерний элемент '" + childText + "'")
                .waitClickable()
                .build();
        child.click();

        return this;
    }

    /**
     * Получает текущий URL
     */
    public String getCurrentUrl() {
        return rootElement.getSelenideElement().getWrappedDriver().getCurrentUrl();
    }

    /**
     * Обновляет страницу
     */
    public NavigationComponent refreshPage() {
        logger.info("Обновляем страницу в компоненте: {}", componentName);
        rootElement.getSelenideElement().getWrappedDriver().navigate().refresh();
        return this;
    }

    /**
     * Возвращается назад
     */
    public NavigationComponent goBack() {
        logger.info("Возвращаемся назад в компоненте: {}", componentName);
        rootElement.getSelenideElement().getWrappedDriver().navigate().back();
        return this;
    }

    // ===== СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАЗЛИЧНЫХ ТИПОВ НАВИГАЦИИ =====

    /**
     * Создает компонент для основной навигации
     */
    public static NavigationComponent createMainNavigation(String navigationName) {
        return new NavigationComponent(
            By.xpath("//div[contains(@class, 'sidebar')]"),
            "Главная навигация '" + navigationName + "'"
        );
    }

    /**
     * Создает компонент для вкладок
     */
    public static NavigationComponent createTabNavigation(String navigationName) {
        return new NavigationComponent(
            By.xpath("//div[contains(@class, 'tabs')]"),
            "Навигация вкладок '" + navigationName + "'"
        );
    }

    /**
     * Создает компонент для меню
     */
    public static NavigationComponent createMenuNavigation(String navigationName) {
        return new NavigationComponent(
            By.xpath("//nav"),
            "Навигация меню '" + navigationName + "'"
        );
    }

    /**
     * Создает компонент для кастомной навигации
     */
    public static NavigationComponent createCustomNavigation(String xpath, String navigationName) {
        return new NavigationComponent(By.xpath(xpath), "Навигация '" + navigationName + "'");
    }

    // ===== СПЕЦИАЛИЗИРОВАННЫЕ МЕТОДЫ =====

    /**
     * Переходит в раздел "Застрахованные лица"
     */
    public NavigationComponent goToInsuredPersons() {
        return selectSection("Застрахованные лица");
    }

    /**
     * Переходит в раздел "Отчеты"
     */
    public NavigationComponent goToReports() {
        return openTab("Отчеты");
    }

    /**
     * Переходит в раздел "ЛК Страхователя"
     */
    public NavigationComponent goToInsurerAccount() {
        return openTab("ЛК Страхователя");
    }

    /**
     * Переходит в раздел "Реестр получателей услуг"
     */
    public NavigationComponent goToServiceRecipientsRegistry() {
        return openTab("Реестр получателей услуг");
    }

    /**
     * Переходит в раздел "Реестр запросов в архивы"
     */
    public NavigationComponent goToArchiveRequestsRegistry() {
        return openTab("Реестр запросов в архивы");
    }

    /**
     * Переходит в раздел "Управление запросами"
     */
    public NavigationComponent goToRequestManagement() {
        return openTab("Управление запросами");
    }

    /**
     * Переходит в раздел "ЛК Архивной Организации"
     */
    public NavigationComponent goToArchiveOrganizationAccount() {
        return openTab("ЛК Архивной Организации");
    }

    /**
     * Переходит в раздел "Реестр исполнителей"
     */
    public NavigationComponent goToPerformersRegistry() {
        return openTab("Реестр исполнителей");
    }

    /**
     * Выбирает вкладку "ЗЛ" (Застрахованные лица)
     */
    public NavigationComponent selectInsuredPersonsTab() {
        logger.info("Выбираем вкладку 'Застрахованные лица'");
        return selectSection("Застрахованные лица");
    }

    /**
     * Выбирает раздел "Раздел 1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ"
     */
    public NavigationComponent selectSection1() {
        return navigateSidebar("sidebar__dropdown-title", "Раздел 1",
                              "sidebar__item inner", "1.1 ТД, 1.2 СТАЖ, 1.3 БЮДЖ");
    }

    /**
     * Проверяет, что находимся на нужной вкладке
     */
    public boolean isOnTab(String tabName) {
        try {
            SmartElement activeTab = ElementFactory.xpath("//*[starts-with(@class,'sidebar__item') and contains(@class, 'active') and text() = '" + tabName + "']")
                    .named("Активная вкладка '" + tabName + "'")
                    .build();
            return activeTab.isVisible();
        } catch (Exception e) {
            logger.debug("Не удалось проверить активную вкладку '{}': {}", tabName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что находимся в нужном разделе
     */
    public boolean isInSection(String sectionName) {
        try {
            SmartElement activeSection = ElementFactory.xpath("//section//a[contains(@class, 'active') and text() = '" + sectionName + "']")
                    .named("Активная секция '" + sectionName + "'")
                    .build();
            return activeSection.isVisible();
        } catch (Exception e) {
            logger.debug("Не удалось проверить активную секцию '{}': {}", sectionName, e.getMessage());
            return false;
        }
    }
}