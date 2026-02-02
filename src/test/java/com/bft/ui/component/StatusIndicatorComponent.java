package com.bft.ui.component;

import com.bft.ui.core.element.ElementFactory;
import com.bft.ui.core.element.SmartElement;
import io.qameta.allure.Step;

/**
 * Компонент для работы со статусными индикаторами
 * 
 * Предоставляет унифицированный интерфейс для работы со статусными индикаторами,
 * которые состоят из иконки и текста статуса.
 * 
 * <p>Поддерживает проверку различных состояний:
 * - Успешный статус (зеленая иконка)
 * - Статус ожидания
 * - Статус ошибки
 * - Получение текста и CSS класса иконки
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Проверка успешного статуса
 * StatusIndicatorComponent status = new StatusIndicatorComponent("status-icon", "status-text", "Статус обработки");
 * if (status.isSuccessStatus()) {
 *     System.out.println("Обработка завершена успешно");
 * }
 * 
 * // Получение информации о статусе
 * StatusInfo info = status.getStatusInfo();
 * System.out.println("Статус: " + info.text);
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see BaseComponent для базовой функциональности
 * @see StatusInfo для детальной информации о статусе
 * @since 1.0
 */
public class StatusIndicatorComponent extends BaseComponent {

    private final SmartElement iconElement;
    private final SmartElement textElement;

    /**
     * Создает компонент статусного индикатора
     * @param iconId ID элемента иконки
     * @param textId ID элемента текста
     * @param componentName имя компонента
     */
    public StatusIndicatorComponent(String iconId, String textId, String componentName) {
        super(ElementFactory.id(iconId)
                .named(componentName + " иконка")
                .waitVisible()
                .build(), componentName);

        this.iconElement = ElementFactory.id(iconId)
                .named(componentName + " иконка")
                .waitVisible()
                .build();

        this.textElement = ElementFactory.id(textId)
                .named(componentName + " текст")
                .waitVisible()
                .build();
    }

    /**
     * Создает компонент статусного индикатора с xpath локаторами
     * @param iconXPath XPath для иконки
     * @param textXPath XPath для текста
     * @param componentName имя компонента
     */
    public StatusIndicatorComponent(SmartElement iconElement, SmartElement textElement, String componentName) {
        super(iconElement, componentName);
        this.iconElement = iconElement;
        this.textElement = textElement;
    }

    /**
     * Проверяет, что статус успешный (зеленая иконка)
     */
    @Step("Проверяем, что статус '{componentName}' успешный")
    public boolean isSuccessStatus() {
        logger.debug("Проверяем успешный статус для '{}'", componentName);

        try {
            return iconElement.isVisible() &&
                   iconElement.getAttribute("class").contains("green");
        } catch (Exception e) {
            logger.debug("Ошибка при проверке успешного статуса '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что статус в состоянии ожидания
     */
    @Step("Проверяем, что статус '{componentName}' в ожидании")
    public boolean isWaitingStatus() {
        logger.debug("Проверяем статус ожидания для '{}'", componentName);

        try {
            String text = textElement.getText();
            return text != null && text.toLowerCase().contains("ожидание");
        } catch (Exception e) {
            logger.debug("Ошибка при проверке статуса ожидания '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что статус в состоянии ошибки
     */
    @Step("Проверяем, что статус '{componentName}' содержит ошибку")
    public boolean isErrorStatus() {
        logger.debug("Проверяем статус ошибки для '{}'", componentName);

        try {
            String text = textElement.getText().toLowerCase();
            return text.contains("ошибка") || text.contains("error") || text.contains("failed");
        } catch (Exception e) {
            logger.debug("Ошибка при проверке статуса ошибки '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Получает текст статуса
     */
    @Step("Получаем текст статуса '{componentName}'")
    public String getStatusText() {
        try {
            String text = textElement.getText();
            logger.debug("Текст статуса '{}' : '{}'", componentName, text);
            return text;
        } catch (Exception e) {
            logger.error("Не удалось получить текст статуса '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Получает CSS класс иконки
     */
    public String getIconClass() {
        try {
            String cssClass = iconElement.getAttribute("class");
            logger.debug("CSS класс иконки '{}' : '{}'", componentName, cssClass);
            return cssClass != null ? cssClass : "";
        } catch (Exception e) {
            logger.error("Не удалось получить CSS класс иконки '{}': {}", componentName, e.getMessage());
            return "";
        }
    }

    /**
     * Ожидает изменения статуса
     */
    @Step("Ожидаем изменения статуса '{componentName}'")
    public StatusIndicatorComponent waitForStatusChange(String initialText) {
        logger.debug("Ожидаем изменения статуса '{}' от '{}'", componentName, initialText);

        // Используем JavaScript для ожидания изменения
        String jsCondition = String.format(
            "document.getElementById('%s').textContent !== '%s'",
            textElement.getElement().getAttribute("id"),
            initialText.replace("'", "\\'")
        );

        // Здесь можно добавить логику ожидания изменения статуса
        // Пока просто возвращаем this
        return this;
    }

    /**
     * Проверяет корректность состояния компонента
     */
    @Override
    public boolean isValid() {
        return isPresent() && isVisible() &&
               iconElement.isPresent() && textElement.isPresent() &&
               !getStatusText().isEmpty();
    }

    /**
     * Получает детальную информацию о статусе
     */
    public StatusInfo getStatusInfo() {
        return new StatusInfo(
            getStatusText(),
            getIconClass(),
            isSuccessStatus(),
            isWaitingStatus(),
            isErrorStatus()
        );
    }

    /**
     * Класс для хранения информации о статусе
     */
    public static class StatusInfo {
        public final String text;
        public final String iconClass;
        public final boolean isSuccess;
        public final boolean isWaiting;
        public final boolean isError;

        public StatusInfo(String text, String iconClass, boolean isSuccess, boolean isWaiting, boolean isError) {
            this.text = text;
            this.iconClass = iconClass;
            this.isSuccess = isSuccess;
            this.isWaiting = isWaiting;
            this.isError = isError;
        }

        public boolean isReady() {
            return isSuccess && !isWaiting && !isError;
        }

        @Override
        public String toString() {
            return String.format("StatusInfo{text='%s', icon='%s', success=%s, waiting=%s, error=%s}",
                               text, iconClass, isSuccess, isWaiting, isError);
        }
    }

    @Override
    public String toString() {
        return String.format("StatusIndicatorComponent{name='%s', status='%s', success=%s}",
                           componentName, getStatusText(), isSuccessStatus());
    }
}