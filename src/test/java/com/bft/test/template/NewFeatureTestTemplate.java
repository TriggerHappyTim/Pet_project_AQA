package com.bft.test.template;

import com.bft.enums.ReportFormType;
import com.bft.enums.UITypeSelector;
import com.bft.security.TestUsers;
import com.bft.steps.AuthSteps;
import com.bft.steps.ReportNavigationSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;

/**
 * Шаблон нового UI-теста.
 *
 * <p>Как использовать:
 * <ol>
 *   <li>Скопируйте этот файл в пакет <code>com.bft.LK_Insurence.&lt;ИМЯ_МОДУЛЯ&gt;</code> (или
 *       <code>com.bft.LK_Archive</code>) и переименуйте класс в соответствии с проверяемым
 *       функционалом (например, {@code NewReportCreationTest}).</li>
 *   <li>Замените примеры {@code // TODO} реальными шагами из {@code com.bft.steps.*}.</li>
 *   <li>Добавьте {@code @Test}-метод с понятным именем и корректной группой (см. README_new_tests.md).</li>
 * </ol>
 *
 * <p>Класс находится в пакете {@code com.bft.test.template} — примеры ниже не выполняются
 * автоматически. Они приведены только для справки.
 *
 * @author QA Automation Team
 */
@Epic("Новая функциональность")
@Feature("Наименование фичи")
public class NewFeatureTestTemplate extends UITestBase {

    private final AuthSteps authSteps = new AuthSteps();
    private final ReportNavigationSteps reportNavSteps = new ReportNavigationSteps();

    /*
     * Пример готового теста. Раскомментируйте и адаптируйте под свой сценарий:
     *
     * @Test
     * @AllureId("MODULE-001")
     * @Story("Название истории")
     * @Description("Детальное описание того, что проверяет тест")
     * @Severity(SeverityLevel.CRITICAL)
     * public void myFeatureTest() {
     *     // Arrange: авторизация нужного пользователя на выбранном контуре
     *     authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), TestUsers.KRIVONOSOV_ALEXANDER);
     *
     *     // Act: шаги через steps-классы (а не напрямую через страницы в тесте)
     *     reportNavSteps.navigateToReports();
     *     reportNavSteps.createNewReportDraft(ReportFormType.EFS1);
     *
     *     // Assert: мягкие проверки через assertions (assertAll выполняется автоматически)
     *     assertions.assertTrue(true, "Условие должно выполниться");
     *     assertions.assertVisible($x("//div[contains(text(), 'Ожидаемый текст')]"),
     *             "Сообщение об успешном сохранении");
     * }
     */

    /**
     * Вспомогательный приватный метод для переиспользуемой подготовки состояния.
     * Размещается в этом классе, если подготовка используется несколькими тестами;
     * иначе - выносится в отдельный класс шагов.
     *
     * @param user пользователь, от имени которого выполняется авторизация
     */
    @SuppressWarnings("unused")
    private void authorizeAndOpenReports(TestUsers user) {
        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType(), user);
        reportNavSteps.navigateToReports();
    }
}