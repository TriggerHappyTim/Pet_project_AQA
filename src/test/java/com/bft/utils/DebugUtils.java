package com.bft.utils;

import com.bft.pw.SelenideElement;
import com.bft.pw.PwDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$$x;
import static com.bft.pw.WebDriverRunner.getWebDriver;

/**
 * Утилитный класс для отладки UI тестов.
 * Предоставляет методы для сохранения состояния страницы (HTML, структура, скриншоты)
 * при возникновении ошибок и логирования проблем валидации.
 */
public class DebugUtils {

    private static final Logger log = LoggerFactory.getLogger(DebugUtils.class);
    private static final String DEBUG_OUTPUT_DIR = "target/debug/";

    /**
     * Сохраняет полное состояние страницы при ошибке.
     * Включает:
     * 1. Проверку и логирование ошибок валидации.
     * 2. Парсинг структуры всех открытых диалогов (или всей страницы).
     * 3. Сохранение полного HTML исходного кода.
     *
     * @param context Контекст ошибки (например, имя теста или шаг), используется в именах файлов.
     * @param cause   Исключение, вызвавшее ошибку (может быть null).
     */
    public static void savePageStateOnError(String context, Throwable cause) {
        log.warn("=== СОХРАНЕНИЕ ДАМПА СТРАНИЦЫ: {} ===", context);
        if (cause != null) {
            log.warn("Причина: {}", cause.getMessage());
        }

        try {
            // 1. Проверка ошибок валидации
            checkAndLogValidationErrors();

            // 2. Парсинг диалогов
            var dialogs = $$x("//*[@role='dialog'] | //*[contains(@class,'modal')] | //*[contains(@class,'MuiDialog')]");

            if (!dialogs.isEmpty()) {
                log.debug("Найдено диалогов: {}. Сохраняем структуру каждого...", dialogs.size());
                for (int i = 0; i < dialogs.size(); i++) {
                    try {
                        SelenideElement dialog = dialogs.get(i);
                        if (dialog.exists() && dialog.isDisplayed()) {
                            FormStructureParser.parseAndSaveForm(dialog, "dialog-" + (i + 1) + "-" + context);
                        }
                    } catch (Exception e) {
                        log.warn("Не удалось сохранить структуру диалога [{}]: {}", i + 1, e.getMessage());
                    }
                }
            } else {
                log.debug("Диалоги не найдены. Сохраняем структуру всей страницы (body)...");
                try {
                    FormStructureParser.parseAndSaveForm($("body"), "page-body-" + context);
                } catch (Exception e) {
                    log.warn("Не удалось сохранить структуру body: {}", e.getMessage());
                }
            }

            // 3. Сохранение полного HTML
            try {
                PwDriver driver = getWebDriver();
                String pageSource = driver.getPageSource();
                FormStructureParser.saveHtmlToFile(pageSource, "source-" + context);
                log.debug("HTML страницы сохранен успешно.");
            } catch (Exception e) {
                log.warn("Не удалось сохранить HTML страницы: {}", e.getMessage());
            }

            log.info("=== Все данные сохранены в директорию: {} ===", DEBUG_OUTPUT_DIR);

        } catch (Exception ex) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА при сохранении дампа страницы: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Проверяет наличие видимых сообщений об ошибках валидации на странице
     * и логирует их.
     */
    private static void checkAndLogValidationErrors() {
        String[] selectors = {
                "//*[contains(text(),'обязательно')]",
                "//*[contains(text(),'Поле обязательно')]",
                "//*[@role='alert']",
                "//*[contains(@class,'error') and contains(text(),'ошибка')]",
                "//*[contains(@class,'Mui-error')]"
        };

        boolean foundError = false;
        for (String selector : selectors) {
            try {
                var elements = $$x(selector);
                for (var el : elements) {
                    if (el.exists() && el.isDisplayed()) {
                        String text = el.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            log.warn(">>> НАЙДЕНА ОШИБКА ВАЛИДАЦИИ: {}", text);
                            foundError = true;
                        }
                    }
                }
            } catch (Exception ignored) {
                // Игнорируем ошибки поиска, пробуем следующий селектор
            }
        }

        if (!foundError) {
            log.debug("Ошибки валидации на странице не обнаружены.");
        }
    }

    /**
     * Принудительно открывает директорию с дампами в файловом менеджере (опционально).
     * Может быть полезно при локальной отладке.
     */
    public static void openDebugFolder() {
        try {
            java.io.File folder = new java.io.File(DEBUG_OUTPUT_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            log.info("Папка отладки: {}", folder.getAbsolutePath());
            // Для Windows можно раскомментировать:
            // java.awt.Desktop.getDesktop().open(folder);
        } catch (Exception e) {
            log.warn("Не удалось открыть папку отладки: {}", e.getMessage());
        }
    }
}