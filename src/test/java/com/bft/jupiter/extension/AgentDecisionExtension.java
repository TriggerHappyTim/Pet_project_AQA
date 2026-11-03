package com.bft.jupiter.extension;

import com.bft.test.logging.AgentDecisionLog;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Пишет в Allure журнал хода теста: выбранный контур, имя теста и записанные шаги/решения.
 *
 * <p>Артефактов падения хватает (скриншот, trace, Playwright-лог), но по ним не видно,
 * что именно делал тест и почему. Расширение добавляет эту недостающую половину:
 * <ul>
 *   <li>метки {@code environment} (контур), {@code agent.contour}, {@code agent.test};</li>
 *   <li>вложение {@value AgentDecisionLog#ATTACHMENT_NAME} — шапка с контуром, тестом,
 *       тегами и списком решений/шагов в хронологическом порядке.</li>
 * </ul>
 *
 * <p>Контур берётся из системного свойства {@code environment} (его выставляет профиль
 * Maven через surefire), затем из переменной окружения {@code ENVIRONMENT}. Если не задан
 * ни один — в журнале будет «не задан», а не выдуманное значение.
 *
 * <p>Расширение подключено в {@code BaseTest}, поэтому покрывает все тесты, которые от
 * него наследуются (в том числе все UI-тесты). Регистрация в
 * {@code META-INF/junit-platform.properties} намеренно не используется: автодетект
 * расширений в проекте выключен.
 */
public class AgentDecisionExtension implements BeforeEachCallback, AfterTestExecutionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(AgentDecisionExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        AgentDecisionLog.startTest(
            context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName(),
            context.getDisplayName(),
            resolveContour(),
            context.getTags());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        try {
            AgentDecisionLog.finishTest(!failed, AgentDecisionLog.elapsed());
            AgentDecisionLog.attachToAllure();
        } catch (RuntimeException e) {
            // Журнал не должен ронять тест: если Allure недоступен, теряется только вложение.
            LOG.debug("Не удалось прикрепить журнал решений: {}", e.getMessage());
        } finally {
            AgentDecisionLog.clear();
        }
    }

    /**
     * Контур прогона: {@code environment} из surefire, затем переменная окружения.
     *
     * @return контур или {@code null}, если он не задан
     */
    private String resolveContour() {
        String contour = System.getProperty("environment");
        if (contour == null || contour.trim().isEmpty()) {
            contour = System.getenv("ENVIRONMENT");
        }
        return contour;
    }
}
