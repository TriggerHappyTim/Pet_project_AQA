package com.bft.enums;

import java.time.Duration;

/**
 * Константы таймаутов для тестов EVS Testing Framework
 * 
 * Централизованное хранение всех временных интервалов ожидания.
 * Использование констант вместо магических чисел улучшает читаемость
 * и упрощает поддержку кода.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * import static com.bft.enums.TimeoutConstants.*;
 * 
 * // Вместо:
 * element.shouldBe(visible, Duration.ofSeconds(10));
 * 
 * // Используйте:
 * element.shouldBe(visible, DEFAULT_WAIT);
 * }</pre>
 * 
 * @author QA Automation Team
 * @since 2.0
 */
public final class TimeoutConstants {
    
    // ========== Стандартные UI таймауты ==========
    
    /**
     * Стандартный таймаут ожидания элементов (10 секунд)
     * Используется по умолчанию для большинства ожиданий
     */
    public static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);
    
    /**
     * Короткий таймаут для быстрых операций (3 секунды)
     * Используется для простых элементов, которые появляются быстро
     */
    public static final Duration SHORT_WAIT = Duration.ofSeconds(3);
    
    /**
     * Длинный таймаут для медленных операций (30 секунд)
     * Используется для тяжелых операций (загрузка данных, обработка)
     */
    public static final Duration LONG_WAIT = Duration.ofSeconds(30);
    
    /**
     * Таймаут загрузки страницы (60 секунд)
     * Используется для ожидания полной загрузки страницы
     */
    public static final Duration PAGE_LOAD_WAIT = Duration.ofSeconds(60);
    
    /**
     * Очень короткий таймаут для проверок присутствия (1 секунда)
     * Используется когда нужно быстро проверить наличие элемента
     */
    public static final Duration VERY_SHORT_WAIT = Duration.ofSeconds(1);
    
    // ========== Таймауты для специфичных UI элементов ==========
    
    /**
     * Таймаут для диалоговых окон (15 секунд)
     * Используется для alert, confirm, prompt и custom диалогов
     */
    public static final Duration DIALOG_WAIT = Duration.ofSeconds(15);
    
    /**
     * Таймаут для модальных окон (10 секунд)
     * Используется для появления и закрытия модальных окон
     */
    public static final Duration MODAL_WAIT = Duration.ofSeconds(10);
    
    /**
     * Таймаут для AJAX запросов (20 секунд)
     * Используется для ожидания завершения AJAX операций
     */
    public static final Duration AJAX_WAIT = Duration.ofSeconds(20);
    
    /**
     * Таймаут для dropdown меню (5 секунд)
     * Используется для ожидания раскрытия выпадающих списков
     */
    public static final Duration DROPDOWN_WAIT = Duration.ofSeconds(5);
    
    /**
     * Таймаут для tooltip и hints (3 секунды)
     * Используется для ожидания появления подсказок
     */
    public static final Duration TOOLTIP_WAIT = Duration.ofSeconds(3);
    
    // ========== Таймауты для API ==========
    
    /**
     * Стандартный таймаут ответа API (30 секунд)
     * Используется для большинства API запросов
     */
    public static final Duration API_RESPONSE_WAIT = Duration.ofSeconds(30);
    
    /**
     * Интервал между retry попытками API (5 секунд)
     * Используется при повторных попытках API запросов
     */
    public static final Duration API_RETRY_INTERVAL = Duration.ofSeconds(5);
    
    /**
     * Короткий таймаут для быстрых API (5 секунд)
     * Используется для простых GET запросов
     */
    public static final Duration API_SHORT_WAIT = Duration.ofSeconds(5);
    
    /**
     * Длинный таймаут для медленных API (60 секунд)
     * Используется для операций обработки больших данных
     */
    public static final Duration API_LONG_WAIT = Duration.ofSeconds(60);
    
    // ========== Таймауты для специальных операций ==========
    
    /**
     * Таймаут загрузки файлов (30 секунд)
     * Используется для ожидания завершения upload операций
     */
    public static final Duration FILE_UPLOAD_WAIT = Duration.ofSeconds(30);
    
    /**
     * Таймаут обработки отчетов (120 секунд = 2 минуты)
     * Используется для ожидания формирования и обработки отчетов
     */
    public static final Duration REPORT_PROCESSING_WAIT = Duration.ofSeconds(120);
    
    /**
     * Таймаут для авторизации через ЕПГУ (70 секунд)
     * Используется для ожидания загрузки страницы ЕПГУ и обработки авторизации
     */
    public static final Duration EPGU_AUTH_WAIT = Duration.ofSeconds(70);
    
    // ========== Таймауты для анимаций ==========
    
    /**
     * Таймаут для коротких анимаций (500 миллисекунд)
     * Используется для ожидания завершения CSS transitions
     */
    public static final Duration ANIMATION_SHORT = Duration.ofMillis(500);
    
    /**
     * Таймаут для стандартных анимаций (1 секунда)
     * Используется для ожидания завершения анимаций UI элементов
     */
    public static final Duration ANIMATION_NORMAL = Duration.ofSeconds(1);
    
    /**
     * Таймаут для сложных анимаций (2 секунды)
     * Используется для ожидания завершения сложных переходов
     */
    public static final Duration ANIMATION_LONG = Duration.ofSeconds(2);
    
    // ========== Polling intervals ==========
    
    /**
     * Интервал опроса для быстрых проверок (100 миллисекунд)
     * Используется в циклах ожидания с частой проверкой условия
     */
    public static final Duration POLL_FAST = Duration.ofMillis(100);
    
    /**
     * Интервал опроса для стандартных проверок (500 миллисекунд)
     * Используется в циклах ожидания со средней частотой проверки
     */
    public static final Duration POLL_NORMAL = Duration.ofMillis(500);
    
    /**
     * Интервал опроса для редких проверок (2 секунды)
     * Используется в циклах ожидания с редкой проверкой условия
     */
    public static final Duration POLL_SLOW = Duration.ofSeconds(2);
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров
     * Этот класс содержит только статические константы
     */
    private TimeoutConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
