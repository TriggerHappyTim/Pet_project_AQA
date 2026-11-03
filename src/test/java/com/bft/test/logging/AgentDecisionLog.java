package com.bft.test.logging;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Журнал хода теста: что было сделано и почему.
 *
 * <p>Падение видно по скриншоту и trace, но по ним не видно решений: на каком контуре
 * шёл прогон, какие шаги выполнил тест, что он считал ожидаемым. Журнал закрывает этот
 * пробел — он прикрепляется к Allure-отчёту отдельным вложением
 * {@value #ATTACHMENT_NAME} рядом со скриншотами и trace.
 *
 * <p>Записи двух видов:
 * <ul>
 *   <li>{@link #decision(String, String)} — решение и его причина (пишет тест или агент);</li>
 *   <li>{@link #step(String)} — выполненный шаг (пишут {@code BaseTest}, {@code TestLogger}
 *       и бизнес-шаги).</li>
 * </ul>
 *
 * <p>Контур, имя теста, отображаемое имя и теги добавляются автоматически расширением
 * {@link com.bft.jupiter.extension.AgentDecisionExtension}. Журнал хранится в
 * {@link ThreadLocal}: у каждого теста (и каждого потока при параллельном прогоне) свой.
 *
 * <p>Пример:
 * <pre>{@code
 * AgentDecisionLog.decision("подписываем через API", "КриптоПРО на стенде не установлен");
 * AgentDecisionLog.step("открыли реестр отчётов");
 * }</pre>
 */
public final class AgentDecisionLog {

    /** Имя вложения в Allure-отчёте. */
    public static final String ATTACHMENT_NAME = "agent-decisions";

    /** Предел записей на тест: журнал не должен превращаться во второй лог. */
    private static final int MAX_ENTRIES = 200;

    private static final Logger LOG = LoggerFactory.getLogger(AgentDecisionLog.class);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final ThreadLocal<Journal> JOURNAL = ThreadLocal.withInitial(Journal::new);

    private AgentDecisionLog() {
    }

    /**
     * Начинает журнал теста. Вызывается расширением перед каждым тестовым методом.
     *
     * @param testName    полное имя теста ({@code класс#метод})
     * @param displayName отображаемое имя ({@code @DisplayName} или имя метода)
     * @param contour     контур прогона (профиль Maven: {@code test}/{@code uat})
     * @param tags        теги JUnit текущего теста
     */
    public static void startTest(String testName, String displayName, String contour, Collection<String> tags) {
        Journal journal = JOURNAL.get();
        journal.reset();
        journal.testName = testName == null ? "неизвестен" : testName;
        journal.displayName = displayName == null ? journal.testName : displayName;
        journal.contour = contour == null || contour.trim().isEmpty() ? "не задан" : contour.trim();
        journal.tags = tags == null || tags.isEmpty() ? "нет" : String.join(", ", tags);
        journal.startedAt = LocalDateTime.now();
    }

    /**
     * Записывает решение и причину, по которой оно принято.
     *
     * @param decision что решили
     * @param reason   почему (может быть {@code null}, если причина очевидна)
     */
    public static void decision(String decision, String reason) {
        add("РЕШЕНИЕ", decision, reason);
    }

    /**
     * Записывает выполненный шаг.
     *
     * @param description что сделали
     */
    public static void step(String description) {
        add("ШАГ", description, null);
    }

    /**
     * Записывает заметку без оценки (например, наблюдение о стенде).
     *
     * @param text текст заметки
     */
    public static void note(String text) {
        add("ЗАМЕТКА", text, null);
    }

    /**
     * Завершает журнал: фиксирует статус, длительность и итог.
     *
     * @param success  прошёл ли тест
     * @param duration длительность теста
     */
    public static void finishTest(boolean success, Duration duration) {
        Journal journal = JOURNAL.get();
        journal.status = success ? "PASSED" : "FAILED";
        journal.duration = duration;
        add("ИТОГ", journal.status + " за " + duration.toMillis() + " мс", null);
    }

    /**
     * Сколько времени прошло с начала теста по журналу.
     *
     * @return длительность; {@link Duration#ZERO}, если журнал не начат
     */
    public static Duration elapsed() {
        Journal journal = JOURNAL.get();
        return journal.startedAt == null ? Duration.ZERO : Duration.between(journal.startedAt, LocalDateTime.now());
    }

    /**
     * Прикрепляет журнал к Allure-отчёту и выставляет метки контура и теста.
     *
     * <p>Метки нужны, чтобы отчёт можно было фильтровать: по контуру
     * ({@code environment}) и по имени теста ({@code agent.test}).
     */
    public static void attachToAllure() {
        Journal journal = JOURNAL.get();
        Allure.label("environment", journal.contour);
        Allure.label("agent.contour", journal.contour);
        Allure.label("agent.test", journal.testName);
        Allure.addAttachment(ATTACHMENT_NAME, "text/plain", render(), ".txt");
    }

    /**
     * Текущее содержимое журнала.
     *
     * @return текст журнала: шапка с контуром и тестом, затем записи по времени
     */
    public static String render() {
        Journal journal = JOURNAL.get();
        StringBuilder text = new StringBuilder();
        text.append("Тест: ").append(journal.testName).append('\n')
            .append("Имя: ").append(journal.displayName).append('\n')
            .append("Контур: ").append(journal.contour).append('\n')
            .append("Теги: ").append(journal.tags).append('\n')
            .append("Начало: ").append(journal.startedAt == null ? "неизвестно" : journal.startedAt).append('\n')
            .append("Итог: ").append(journal.status);
        if (journal.duration != null && journal.duration.toMillis() > 0) {
            text.append(" (").append(journal.duration.toMillis()).append(" мс)");
        }
        text.append('\n');
        if (journal.entries.isEmpty()) {
            text.append("\nЗаписей нет: тест не сообщал решений и не выполнял шагов, ")
                .append("известных фреймворку.\n");
        } else {
            text.append('\n');
            for (String entry : journal.entries) {
                text.append(entry).append('\n');
            }
        }
        return text.toString();
    }

    /**
     * Очищает журнал текущего потока.
     */
    public static void clear() {
        JOURNAL.remove();
    }

    private static void add(String kind, String what, String why) {
        String message = what == null || what.trim().isEmpty() ? "(без описания)" : what.trim();
        Journal journal = JOURNAL.get();
        if (journal.entries.size() >= MAX_ENTRIES) {
            if (journal.entries.size() == MAX_ENTRIES) {
                journal.entries.add("[" + LocalDateTime.now().format(TIME) + "] … журнал обрезан на "
                    + MAX_ENTRIES + " записях");
            }
            LOG.debug("{}: {} {}", kind, message, why == null ? "" : "— " + why);
            return;
        }
        String entry = "[" + LocalDateTime.now().format(TIME) + "] " + kind + ": " + message;
        if (why != null && !why.trim().isEmpty()) {
            entry += " — причина: " + why.trim();
        }
        journal.entries.add(entry);
        LOG.debug("{}: {}", kind, message);
    }

    /**
     * Состояние журнала одного теста.
     */
    private static final class Journal {
        private final List<String> entries = new ArrayList<>();
        private String testName = "неизвестен";
        private String displayName = "неизвестно";
        private String contour = "не задан";
        private String tags = "нет";
        private String status = "не завершён";
        private LocalDateTime startedAt;
        private Duration duration = Duration.ZERO;

        private void reset() {
            entries.clear();
            testName = "неизвестен";
            displayName = "неизвестно";
            contour = "не задан";
            tags = "нет";
            status = "не завершён";
            startedAt = null;
            duration = Duration.ZERO;
        }
    }
}
