package com.bft.ui.core.element;

import com.bft.ui.core.wait.WaitStrategy;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Умный список элементов с расширенной функциональностью
 */
public class SmartElementList {

    private static final Logger logger = LoggerFactory.getLogger(SmartElementList.class);

    private final ElementsCollection elements;
    private final String name;
    private final String description;
    private final WaitStrategy waitStrategy;

    public SmartElementList(ElementsCollection elements, String name, String description, WaitStrategy waitStrategy) {
        this.elements = elements;
        this.name = name;
        this.description = description;
        this.waitStrategy = waitStrategy;
    }

    /**
     * Получение размера списка
     */
    public int size() {
        int size = elements.size();
        logger.debug("Размер списка '{}' : {}", name, size);
        return size;
    }

    /**
     * Получение элемента по индексу
     */
    public SmartElement get(int index) {
        logger.debug("Получаем элемент по индексу {} из списка '{}'", index, name);
        SelenideElement element = elements.get(index);
        return new SmartElement(element, name + "[" + index + "]", description, waitStrategy);
    }

    /**
     * Получение первого элемента
     */
    public SmartElement first() {
        logger.debug("Получаем первый элемент из списка '{}'", name);
        return get(0);
    }

    /**
     * Получение последнего элемента
     */
    public SmartElement last() {
        int lastIndex = size() - 1;
        logger.debug("Получаем последний элемент (индекс {}) из списка '{}'", lastIndex, name);
        return get(lastIndex);
    }

    /**
     * Поиск элемента по предикату
     */
    public Optional<SmartElement> find(Predicate<SelenideElement> predicate) {
        logger.debug("Ищем элемент в списке '{}' по предикату", name);

        for (int i = 0; i < elements.size(); i++) {
            SelenideElement element = elements.get(i);
            if (predicate.test(element)) {
                logger.debug("Найден элемент по индексу {} в списке '{}'", i, name);
                return Optional.of(new SmartElement(element, name + "[found:" + i + "]", description, waitStrategy));
            }
        }

        logger.debug("Элемент не найден в списке '{}'", name);
        return Optional.empty();
    }

    /**
     * Поиск элемента по тексту
     */
    public Optional<SmartElement> findByText(String text) {
        logger.debug("Ищем элемент с текстом '{}' в списке '{}'", text, name);
        return find(element -> {
            try {
                return text.equals(element.getText().trim());
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Поиск элемента по частичному тексту
     */
    public Optional<SmartElement> findByPartialText(String partialText) {
        logger.debug("Ищем элемент с частичным текстом '{}' в списке '{}'", partialText, name);
        return find(element -> {
            try {
                String text = element.getText();
                return text != null && text.contains(partialText);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Поиск элемента по атрибуту
     */
    public Optional<SmartElement> findByAttribute(String attribute, String value) {
        logger.debug("Ищем элемент с атрибутом '{}'='{}' в списке '{}'", attribute, value, name);
        return find(element -> {
            try {
                String attrValue = element.getAttribute(attribute);
                return value.equals(attrValue);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Поиск элемента по CSS классу
     */
    public Optional<SmartElement> findByClass(String cssClass) {
        logger.debug("Ищем элемент с классом '{}' в списке '{}'", cssClass, name);
        return find(element -> {
            try {
                String classes = element.getAttribute("class");
                return classes != null && classes.contains(cssClass);
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Фильтрация элементов по предикату
     */
    public List<SmartElement> filter(Predicate<SelenideElement> predicate) {
        logger.debug("Фильтруем элементы в списке '{}' по предикату", name);

        List<SmartElement> result = elements.stream()
            .filter(predicate)
            .map(element -> {
                int index = elements.indexOf(element);
                return new SmartElement(element, name + "[filtered:" + index + "]", description, waitStrategy);
            })
            .collect(Collectors.toList());

        logger.debug("Отфильтровано {} элементов из списка '{}'", result.size(), name);
        return result;
    }

    /**
     * Получение всех видимых элементов
     */
    public List<SmartElement> getVisible() {
        logger.debug("Получаем видимые элементы из списка '{}'", name);
        return filter(element -> {
            try {
                return element.isDisplayed();
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Получение всех доступных для взаимодействия элементов
     */
    public List<SmartElement> getEnabled() {
        logger.debug("Получаем доступные элементы из списка '{}'", name);
        return filter(element -> {
            try {
                return element.isEnabled();
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Проверка, что список не пустой
     */
    @Step("Проверяем, что список '{name}' не пустой")
    public SmartElementList shouldNotBeEmpty() {
        logger.debug("Проверяем, что список '{}' не пустой", name);
        if (elements.isEmpty()) {
            throw new AssertionError("Список '" + name + "' пустой, но должен содержать элементы");
        }
        logger.info("Список '{}' содержит {} элементов", name, elements.size());
        return this;
    }

    /**
     * Проверка размера списка
     */
    @Step("Проверяем размер списка '{name}': {expectedSize}")
    public SmartElementList shouldHaveSize(int expectedSize) {
        logger.debug("Проверяем размер списка '{}': ожидается {}", name, expectedSize);
        int actualSize = elements.size();
        if (actualSize != expectedSize) {
            throw new AssertionError(String.format(
                "Размер списка '%s' не соответствует ожиданию: ожидалось %d, фактически %d",
                name, expectedSize, actualSize));
        }
        logger.info("Размер списка '{}' корректный: {}", name, expectedSize);
        return this;
    }

    /**
     * Проверка, что список содержит хотя бы N элементов
     */
    @Step("Проверяем, что список '{name}' содержит минимум {minSize} элементов")
    public SmartElementList shouldHaveAtLeastSize(int minSize) {
        logger.debug("Проверяем, что список '{}' содержит минимум {} элементов", name, minSize);
        int actualSize = elements.size();
        if (actualSize < minSize) {
            throw new AssertionError(String.format(
                "Список '%s' содержит недостаточно элементов: ожидалось минимум %d, фактически %d",
                name, minSize, actualSize));
        }
        logger.info("Список '{}' содержит достаточно элементов: {} >= {}", name, actualSize, minSize);
        return this;
    }

    /**
     * Клик по элементу с указанным текстом
     */
    @Step("Кликаем по элементу с текстом '{text}' в списке: {name}")
    public SmartElementList clickByText(String text) {
        logger.debug("Кликаем по элементу с текстом '{}' в списке '{}'", text, name);
        Optional<SmartElement> element = findByText(text);
        if (element.isPresent()) {
            element.get().click();
            logger.info("Успешно кликнули по элементу с текстом '{}' в списке '{}'", text, name);
        } else {
            throw new AssertionError("Элемент с текстом '" + text + "' не найден в списке '" + name + "'");
        }
        return this;
    }

    /**
     * Получение текстов всех элементов
     */
    public List<String> getAllTexts() {
        logger.debug("Получаем тексты всех элементов в списке '{}'", name);
        return elements.stream()
            .map(element -> {
                try {
                    return element.getText();
                } catch (Exception e) {
                    logger.warn("Не удалось получить текст элемента в списке '{}': {}", name, e.getMessage());
                    return "";
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Получение базовой коллекции элементов
     */
    public ElementsCollection getElements() {
        return elements;
    }

    /**
     * Получение имени списка
     */
    public String getName() {
        return name;
    }

    /**
     * Получение описания списка
     */
    public String getDescription() {
        return description;
    }

    /**
     * Получение стратегии ожидания
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    @Override
    public String toString() {
        return String.format("SmartElementList{name='%s', description='%s', size=%d, strategy='%s'}",
                           name, description, elements.size(), waitStrategy.getClass().getSimpleName());
    }
}