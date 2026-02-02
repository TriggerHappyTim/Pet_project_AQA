package com.bft.ui.component;

import com.bft.ui.core.element.SmartElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Базовый абстрактный класс для всех UI компонентов фреймворка
 * 
 * Реализует паттерн Component для инкапсуляции взаимодействия с
 * повторяющимися элементами интерфейса (кнопки, поля ввода, чекбоксы и т.д.).
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Обертка над {@link SmartElement} с автоматическим логированием</li>
 *   <li>Общие методы проверки состояния (present, visible, enabled)</li>
 *   <li>Fluent API для цепочки вызовов</li>
 *   <li>Централизованная обработка ошибок</li>
 * </ul>
 * 
 * <p>Все компоненты наследуются от этого класса:
 * <ul>
 *   <li>{@link ButtonComponent} - кнопки</li>
 *   <li>{@link InputComponent} - поля ввода</li>
 *   <li>{@link CheckboxComponent} - чекбоксы</li>
 *   <li>{@link SelectComponent} - выпадающие списки</li>
 *   <li>и другие...</li>
 * </ul>
 * 
 * <p>Пример создания нового компонента:
 * <pre>{@code
 * public class MyComponent extends BaseComponent {
 *     
 *     public MyComponent(SmartElement element, String name) {
 *         super(element, name);
 *     }
 *     
 *     @Override
 *     public boolean isValid() {
 *         return isPresent() && isVisible();
 *     }
 *     
 *     public MyComponent doAction() {
 *         logger.info("Выполняем действие для: {}", componentName);
 *         rootElement.click();
 *         return this;
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see SmartElement для работы с элементами
 * @see ButtonComponent пример реализации компонента
 * @since 1.0
 */
public abstract class BaseComponent {

    /**
     * Logger для отладки и логирования действий компонента
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Корневой SmartElement компонента
     * Используется для всех взаимодействий с DOM элементом
     */
    protected final SmartElement rootElement;
    
    /**
     * Имя компонента для логирования и отладки
     */
    protected final String componentName;

    /**
     * Создает новый экземпляр компонента
     * 
     * Конструктор защищенный (protected), так как создание компонентов
     * должно происходить через статические factory методы в наследниках.
     * 
     * @param rootElement корневой {@link SmartElement} представляющий компонент в DOM
     * @param componentName читаемое имя компонента для логирования (например, "Кнопка Войти")
     * @throws IllegalArgumentException если rootElement или componentName равны null
     */
    protected BaseComponent(SmartElement rootElement, String componentName) {
        this.rootElement = rootElement;
        this.componentName = componentName;
        logger.debug("Создан компонент: {}", componentName);
    }

    /**
     * Проверяет присутствие компонента в DOM дереве
     * 
     * Элемент считается присутствующим, если он есть в DOM,
     * независимо от видимости или доступности.
     * 
     * @return true если элемент присутствует в DOM, false в противном случае
     */
    public boolean isPresent() {
        try {
            boolean present = rootElement.isPresent();
            logger.debug("Компонент '{}' {}", componentName, present ? "присутствует" : "отсутствует");
            return present;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке присутствия компонента '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет видимость компонента на странице
     * 
     * Элемент считается видимым, если:
     * - Присутствует в DOM
     * - Не скрыт через CSS (display: none, visibility: hidden)
     * - Имеет ненулевые размеры
     * 
     * @return true если элемент видим пользователю, false в противном случае
     */
    public boolean isVisible() {
        try {
            boolean visible = rootElement.isVisible();
            logger.debug("Компонент '{}' {}", componentName, visible ? "видим" : "невидим");
            return visible;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке видимости компонента '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет доступность компонента для взаимодействия
     * 
     * Элемент считается доступным (enabled), если:
     * - Присутствует в DOM и видим
     * - Не имеет атрибута disabled
     * - Не заблокирован через readonly (для полей ввода)
     * 
     * @return true если с элементом можно взаимодействовать, false в противном случае
     */
    public boolean isEnabled() {
        try {
            boolean enabled = rootElement.isEnabled();
            logger.debug("Компонент '{}' {}", componentName, enabled ? "доступен" : "недоступен");
            return enabled;
        } catch (Exception e) {
            logger.debug("Ошибка при проверке доступности компонента '{}': {}", componentName, e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет валидность состояния компонента
     * 
     * Абстрактный метод, который должен быть реализован в наследниках
     * для проверки специфичных условий валидности компонента.
     * 
     * <p>Типичные проверки:
     * <ul>
     *   <li>Кнопка: isPresent() && isVisible() && isEnabled()</li>
     *   <li>Поле ввода: isPresent() && isVisible() && isEnabled()</li>
     *   <li>Чекбокс: isPresent() && isVisible()</li>
     * </ul>
     * 
     * @return true если компонент в валидном состоянии, false в противном случае
     */
    public abstract boolean isValid();

    /**
     * Возвращает корневой SmartElement компонента
     * 
     * Используйте для прямого доступа к элементу в сложных сценариях,
     * когда методов компонента недостаточно.
     * 
     * @return корневой {@link SmartElement} компонента
     */
    public SmartElement getRootElement() {
        return rootElement;
    }

    /**
     * Возвращает имя компонента
     * 
     * Используется для логирования и отладки.
     * 
     * @return читаемое имя компонента
     */
    public String getComponentName() {
        return componentName;
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', present=%s, visible=%s, enabled=%s}",
                           getClass().getSimpleName(), componentName, isPresent(), isVisible(), isEnabled());
    }
}