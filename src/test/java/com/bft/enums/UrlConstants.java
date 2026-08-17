package com.bft.enums;

/**
 * Константы URL для тестов EVS Testing Framework.
 * <p>
 * Централизованное хранение всех URL, используемых в тестах.
 * Поддерживает как статические адреса (КриптоПРО), так и динамические,
 * зависящие от выбранного окружения (ЕВС, РПУ, УОС).
 *
 * <p>Пример использования:
 * <pre>{@code
 * import static com.bft.enums.UrlConstants.*;
 *
 * // Статический URL
 * Selenide.open(CRYPTOPRO_DEMO_PAGE_URL);
 *
 * // Динамический URL (автоматически подтянется из UITypeSelector)
 * Selenide.open(EVS_BASE_URL);
 * }</pre>
 *
 * @author QA Automation Team
 * @since 2.0
 * @see UITypeSelector
 */
public final class UrlConstants {

    // ========== КриптоПРО (Статические) ==========

    /**
     * URL демо-страницы КриптоПРО для проверки электронной подписи.
     */
    public static final String CRYPTOPRO_DEMO_PAGE_URL =
            "https://cryptopro.ru/sites/default/files/products/cades/demopage/cades_bes_sample.html";

    /**
     * Базовый URL сайта КриптоПРО.
     */
    public static final String CRYPTOPRO_BASE_URL = "https://cryptopro.ru";

    /**
     * URL страницы продуктов КриптоПРО.
     */
    public static final String CRYPTOPRO_PRODUCTS_URL = "https://cryptopro.ru/products";

    // ========== Основные системы (Динамические) ==========
    // Значения определяются на лету через UITypeSelector в зависимости от свойств запуска (-Denv...)

    /**
     * Базовый URL ЕВС (ЛК Страхователя).
     * Определяется через {@link UITypeSelector#getSelectedUIType()}.
     */
    public static final String EVS_BASE_URL = UITypeSelector.getSelectedUIType().value;

    /**
     * Базовый URL РПУ.
     * Определяется через {@link UITypeSelector#getSelectedRpuType()}.
     */
    public static final String RPU_BASE_URL = UITypeSelector.getSelectedRpuType().value;

    /**
     * Базовый URL УОС.
     * Определяется через {@link UITypeSelector#getSelectedUosType()}.
     */
    public static final String UOS_BASE_URL = UITypeSelector.getSelectedUosType().value;

    /**
     * Базовый URL Архива ЕВС (ЛКА).
     * Формируется автоматически на основе текущего типа ЕВС.
     * Если текущий тип содержит "_LKA", используется он, иначе происходит замена суффикса.
     */
    public static final String EVS_ARCHIVE_URL = getArchiveUrl();

    // ========== API (Заглушки/Статические) ==========

    /**
     * Базовый URL API по умолчанию.
     * TODO: Заменить на динамическое определение при реализации API тестов.
     */
    public static final String API_BASE_URL_DEFAULT = "https://api.example.com";

    /**
     * Вспомогательный метод для формирования URL архива.
     * Логика: если в текущем EVS уже есть суффикс _LKA, оставляем как есть.
     * Иначе пытаемся заменить _LKS на _LKA или добавить _LKA.
     */
    private static String getArchiveUrl() {
        UIType currentType = UITypeSelector.getSelectedUIType();
        String name = currentType.name();

        if (name.contains("_LKA")) {
            return currentType.value;
        }

        // Попытка найти соответствующий архивный контур
        // Простая эвристика: заменяем LKS на LKA или добавляем LKA
        if (name.contains("_LKS")) {
            String archiveName = name.replace("_LKS", "_LKA");
            try {
                return UIType.valueOf(archiveName).value;
            } catch (IllegalArgumentException e) {
                // Если точного соответствия нет, возвращаем дефолт или логируем ошибку
            }
        }

        // Fallback: возвращаем текущий URL, если не нашли явного архива
        return currentType.value;
    }

    private UrlConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}