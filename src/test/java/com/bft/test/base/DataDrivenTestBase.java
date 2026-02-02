package com.bft.test.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Базовый класс для data-driven тестов
 * 
 * Предоставляет инфраструктуру для параметризованного тестирования с использованием TestNG DataProvider.
 * Позволяет запускать один тест с различными наборами данных.
 * 
 * <p>Основные возможности:
 * <ul>
 *   <li>Предопределенные DataProvider для различных типов тестов</li>
 *   <li>Универсальный DataProvider с автоматическим определением типа данных</li>
 *   <li>Создание комбинаций тестовых данных</li>
 *   <li>Поддержка негативных сценариев и нагрузочного тестирования</li>
 *   <li>Кросс-браузерное тестирование</li>
 *   <li>Логирование параметризованных тестов</li>
 * </ul>
 * 
 * <p>Пример использования:
 * <pre>{@code
 * public class ParameterizedTest extends DataDrivenTestBase {
 *     
 *     @Test(dataProvider = "basicTestData", groups = {"web"})
 *     public void testWithData(String name, String data, boolean expected) {
 *         runParameterizedTest(new Object[]{name, data, expected}, () -> {
 *             // Логика теста с использованием параметров
 *             performAction("Действие с " + name, () -> {
 *                 // выполнение действий
 *             });
 *         });
 *     }
 *     
 *     @Test(dataProvider = "cryptoProTestData", groups = {"web"})
 *     public void testCryptoPro(String testName, boolean checkAll, boolean performSig, boolean expected) {
 *         // Тест с данными для CryptoPro
 *     }
 * }
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see UITestBase для базовой функциональности UI тестов
 * @see TestData для хранения тестовых данных с метаданными
 * @since 1.0
 */
public abstract class DataDrivenTestBase extends UITestBase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * DataProvider для базовых тестовых данных
     * 
     * Предоставляет простой набор тестовых данных для базовых сценариев.
     * Каждая строка содержит: имя теста, данные, ожидаемый результат (boolean).
     * 
     * @return массив массивов объектов с тестовыми данными
     */
    @DataProvider(name = "basicTestData")
    public Object[][] basicTestData() {
        return new Object[][] {
            {"test1", "data1", true},
            {"test2", "data2", false},
            {"test3", "data3", true}
        };
    }

    /**
     * DataProvider для тестов CryptoPro плагина
     * 
     * Предоставляет данные для тестирования функциональности CryptoPro:
     * - Имя теста
     * - Проверять ли все статусы плагина
     * - Выполнять ли подписание
     * - Ожидаемый результат (true - успех, false - ожидаемый провал)
     * 
     * @return массив массивов объектов с данными для CryptoPro тестов
     */
    @DataProvider(name = "cryptoProTestData")
    public Object[][] cryptoProTestData() {
        return new Object[][] {
            // testName, checkAllStatuses, performSignature, expectedResult
            {"Basic Status Check", true, false, true},
            {"Full Signature Test", true, true, true},
            {"Quick Status Check", false, false, true},
            {"Signature Only", false, true, false} // Ожидаем провал без проверки статусов
        };
    }

    /**
     * DataProvider для тестов UI элементов
     * 
     * Предоставляет данные для проверки UI элементов:
     * - ID элемента
     * - Ожидаемый текст (null если не требуется проверка текста)
     * - Должен ли элемент быть видимым
     * - Должен ли элемент быть доступным (enabled)
     * 
     * @return массив массивов объектов с данными для UI тестов
     */
    @DataProvider(name = "uiElementsTestData")
    public Object[][] uiElementsTestData() {
        return new Object[][] {
            // elementId, expectedText, shouldBeVisible, shouldBeEnabled
            {"ExtensionEnabledImg", null, true, false},
            {"ExtensionEnabledTxt", "Расширение загружено", true, false},
            {"PluginEnabledImg", null, true, false},
            {"CspEnabledImg", null, true, false},
            {"signButton", "Подписать", true, true}
        };
    }

    /**
     * Универсальный DataProvider с автоматическим определением типа данных
     * 
     * Автоматически определяет тип данных на основе имени тестового метода
     * и возвращает соответствующий DataProvider:
     * - Методы с "crypto" или "plugin" в имени → cryptoProTestData()
     * - Методы с "ui" или "element" в имени → uiElementsTestData()
     * - Остальные методы → basicTestData()
     * 
     * @param context контекст TestNG (не используется, но требуется для DataProvider)
     * @param method тестовый метод, для которого генерируются данные
     * @return массив массивов объектов с тестовыми данными
     */
    @DataProvider(name = "universalTestData")
    public Object[][] universalTestData(ITestContext context, Method method) {
        String testName = method.getName();
        logger.info("Генерация данных для теста: {}", testName);

        // Определяем тип данных на основе имени метода
        if (testName.contains("crypto") || testName.contains("plugin")) {
            return cryptoProTestData();
        } else if (testName.contains("ui") || testName.contains("element")) {
            return uiElementsTestData();
        } else {
            return basicTestData();
        }
    }

    /**
     * Создает поток тестовых данных для функционального программирования
     * 
     * Преобразует массив данных в поток объектов TestData для использования
     * в функциональном стиле программирования.
     * 
     * @param <T> тип тестовых данных
     * @param data массив тестовых данных
     * @param nameExtractor функция для извлечения имени из элемента данных
     * @return поток объектов TestData
     */
    protected <T> Stream<TestData<T>> createTestDataStream(T[] data, Function<T, String> nameExtractor) {
        return Arrays.stream(data)
                .map(item -> new TestData<>(item, nameExtractor.apply(item)));
    }

    /**
     * Класс для хранения тестовых данных с метаданными
     * 
     * Позволяет хранить тестовые данные вместе с именем и дополнительными метаданными.
     * Поддерживает добавление произвольных метаданных через метод withMetadata().
     * 
     * @param <T> тип тестовых данных
     */
    public static class TestData<T> {
        private final T data;
        private final String name;
        private final Map<String, Object> metadata;

        public TestData(T data, String name) {
            this.data = data;
            this.name = name;
            this.metadata = new HashMap<>();
        }

        /**
         * Получает тестовые данные
         * 
         * @return тестовые данные
         */
        public T getData() { return data; }
        
        /**
         * Получает имя тестовых данных
         * 
         * @return имя тестовых данных
         */
        public String getName() { return name; }

        /**
         * Добавляет метаданные к тестовым данным
         * 
         * Позволяет добавить произвольные метаданные для использования в тестах.
         * 
         * @param key ключ метаданных
         * @param value значение метаданных
         * @return текущий экземпляр TestData для цепочки вызовов
         */
        public TestData<T> withMetadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        /**
         * Получает метаданные по ключу
         * 
         * @param key ключ метаданных
         * @return значение метаданных или null, если ключ не найден
         */
        public Object getMetadata(String key) {
            return metadata.get(key);
        }

        @Override
        public String toString() {
            return String.format("TestData{name='%s', data=%s}", name, data);
        }
    }

    /**
     * Выполняет параметризованный тест с логированием
     * 
     * Обертка для выполнения параметризованного теста с автоматическим логированием
     * начала и завершения теста, а также обработкой ошибок.
     * 
     * @param testData массив тестовых данных для текущей итерации
     * @param testLogic логика теста для выполнения
     */
    protected void runParameterizedTest(Object[] testData, Runnable testLogic) {
        String testName = getTestNameFromData(testData);
        logger.info("=== ЗАПУСК ПАРАМЕТРИЗОВАННОГО ТЕСТА: {} ===", testName);
        logger.info("Тестовые данные: {}", Arrays.toString(testData));

        try {
            testLogic.run();
            logger.info("✅ ПАРАМЕТРИЗОВАННЫЙ ТЕСТ ПРОЙДЕН: {}", testName);
        } catch (Exception e) {
            logger.error("❌ ПАРАМЕТРИЗОВАННЫЙ ТЕСТ ПРОВАЛЕН: {} - {}", testName, e.getMessage());
            throw e;
        }
    }

    /**
     * Извлекает имя теста из тестовых данных
     * 
     * Если первый элемент массива - строка, использует её как имя теста.
     * В противном случае генерирует имя на основе хеш-кода данных.
     * 
     * @param testData массив тестовых данных
     * @return имя теста для логирования
     */
    private String getTestNameFromData(Object[] testData) {
        if (testData.length > 0 && testData[0] instanceof String) {
            return (String) testData[0];
        }
        return "ParameterizedTest_" + Arrays.hashCode(testData);
    }

    /**
     * Создает комбинации тестовых данных (декартово произведение)
     * 
     * Генерирует все возможные комбинации значений из переданных массивов.
     * Используется для pairwise testing или полного перебора комбинаций.
     * 
     * <p>Пример:
     * <pre>{@code
     * Object[] browsers = {"chrome", "firefox"};
     * Object[] os = {"windows", "linux"};
     * Object[][] combinations = createCombinations(browsers, os);
     * // Результат: [["chrome", "windows"], ["chrome", "linux"], ["firefox", "windows"], ["firefox", "linux"]]
     * }</pre>
     * 
     * @param arrays массивы значений для комбинирования
     * @return массив массивов со всеми возможными комбинациями
     */
    protected Object[][] createCombinations(Object[]... arrays) {
        if (arrays.length == 0) return new Object[0][0];

        List<Object[]> combinations = new ArrayList<>();
        generateCombinations(arrays, 0, new Object[arrays.length], combinations);

        return combinations.toArray(new Object[0][]);
    }

    private void generateCombinations(Object[][] arrays, int index, Object[] current, List<Object[]> result) {
        if (index == arrays.length) {
            result.add(Arrays.copyOf(current, current.length));
            return;
        }

        for (Object value : arrays[index]) {
            current[index] = value;
            generateCombinations(arrays, index + 1, current, result);
        }
    }

    /**
     * DataProvider для негативных сценариев
     * 
     * Предоставляет данные для тестирования обработки ошибок и валидации:
     * - Имя теста
     * - Невалидные данные
     * - Ожидаемое сообщение об ошибке
     * 
     * @return массив массивов объектов с данными для негативных тестов
     */
    @DataProvider(name = "negativeTestData")
    public Object[][] negativeTestData() {
        return new Object[][] {
            // testName, invalidData, expectedError
            {"Empty Username", "", "Username is required"},
            {"Invalid Email", "invalid-email", "Invalid email format"},
            {"Negative Number", -1, "Value must be positive"},
            {"Null Value", null, "Value cannot be null"}
        };
    }

    /**
     * DataProvider для нагрузочных тестов
     * 
     * Предоставляет данные для тестирования производительности:
     * - Количество одновременных пользователей
     * - Длительность теста в секундах
     * - Ожидаемый процент успешных запросов
     * 
     * @return массив массивов объектов с данными для нагрузочных тестов
     */
    @DataProvider(name = "loadTestData")
    public Object[][] loadTestData() {
        return new Object[][] {
            // users, duration, expectedSuccessRate
            {1, 30, 100},
            {5, 60, 95},
            {10, 120, 90},
            {50, 300, 80}
        };
    }

    /**
     * DataProvider для кросс-браузерного тестирования
     * 
     * Предоставляет данные для тестирования в различных браузерах:
     * - Название браузера
     * - Версия браузера
     * - Ожидаемый результат (true - тест должен пройти)
     * 
     * @return массив массивов объектов с данными для кросс-браузерных тестов
     */
    @DataProvider(name = "crossBrowserTestData")
    public Object[][] crossBrowserTestData() {
        return new Object[][] {
            // browser, version, expectedResult
            {"chrome", "latest", true},
            {"firefox", "latest", true},
            {"edge", "latest", true},
            {"safari", "latest", true}
        };
    }

    /**
     * Преобразует параметры теста в читаемую строку для отчетов
     * 
     * Форматирует массив параметров в строку вида "param1=value1, param2=value2".
     * Используется для улучшения читаемости отчетов TestNG.
     * 
     * @param parameters массив параметров теста
     * @return отформатированная строка с параметрами
     */
    protected String formatTestParameters(Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return "No parameters";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("param").append(i + 1).append("=").append(parameters[i]);
        }

        return sb.toString();
    }
}