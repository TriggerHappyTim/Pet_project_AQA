package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.steps.ManualReportFiller;
import com.bft.steps.ReportFillers;
import com.bft.steps.ReportSignSteps;
import com.bft.steps.ReportValidationSteps;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.bft.pw.Selenide.$x;
import static com.bft.pw.Selenide.refresh;

/**
 * Базовый E2E-сценарий подписания отчёта ЕВС (ЛК Страхователя): загрузка XML →
 * подписание → проверка статуса по UUID. Единый для всех типов отчётов
 * (CommonReportSignatureService определяет тип по itemId); различаются только тип отчёта
 * в UI, XML-фикстура и код типа для агрегата ({@code evs.sign.report-type-code}).
 *
 * <p>Полный цикл повторяет {@code SzvMXmlUploadSignTest} (СЗВ-М). Конкретные тесты
 * (СЗВ-ТД, СЗВ-СТАЖ, СЗВ-ИСХ, ОДВ-1, СЗВ-КОРР, СЗВ-К, ЕФС-1, СЗВ-DSO) переопределяют
 * {@link #formType()}, {@link #xmlResource()} и {@link #reportTypeCode()}.
 *
 * <p>Подписание идёт через {@code -Devs.sign.mode=api} (по умолчанию) и
 * {@code -Devs.sign.transport=rest} (по умолчанию): prepareDocCommon (GraphQL mesh,
 * reader-user, READ) + n2o syncSign в браузерной сессии ЕСИА (владелец отчёта →
 * ресурсная проверка пройдена). КриптоПРО не требуется.
 */
@Tag("lk-insurer")
@Tag("xml-upload")
@Tag("web")
@Tag("signing")
@Epic("Формы отчетности")
public abstract class AbstractReportUploadSignTest extends UITestBase {

    protected static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private final ReportValidationSteps validationSteps = new ReportValidationSteps();
    private final ReportSignSteps signSteps = new ReportSignSteps();

    /** Тип отчёта для выбора в UI. */
    protected abstract ReportFormType formType();

    /** XML-фикстура для загрузки. */
    protected abstract ReportXmlResource xmlResource();

    /** Код типа отчёта для агрегата {@code pfrEcpStorReportAggreServ} (1=СЗВ-М … 9=СЗВ-DSO, 10=4-ФСС). */
    protected abstract String reportTypeCode();

    /** Отображаемое имя формы (для сообщений и вложений Allure). */
    protected abstract String displayName();

    /**
     * Смягчать ли проверку блока «Страхователь» на карточке (для типов с продукт-гэпом:
     * карточка не подтягивает реквизиты из загруженного XML). По умолчанию строго.
     */
    protected boolean insurerLenient() {
        return false;
    }

    @Test
    @Story("XML Upload + Подписание")
    @Description("Полный цикл: загрузка XML, подписание (API/rest), проверка статуса по ИД процесса")
    @Severity(SeverityLevel.BLOCKER)
    public void uploadSignAndVerify() {
        SzvReportsSteps steps = new SzvReportsSteps();
        String processId;

        // ---------- Arrange ----------
        steps.authorizeEVS(UITypeSelector.getSelectedUIType());

        // ---------- Act 1. Загрузка XML ----------
        steps.addReports();
        steps.selectReportType(formType());
        steps.addNewReport();                 // диалог «Создать новый», если были черновики
        steps.sendXml(xmlResource());

        // Код типа отчёта для агрегата (ReportSignSteps.resolveItemId)
        System.setProperty("evs.sign.report-type-code", reportTypeCode());

        // ---------- Assert 1. Нет бизнес-ошибок после загрузки ----------
        // После загрузки XML приложение открывает карточку отчёта /reports/{type}/{id}/...
        // (а не таблицу списка), поэтому проверяем переход на карточку, а не наличие строк списка.
        validationSteps.verifyUrlContains("/reports");
        assertions.assertFalse(
                $x("//*[contains(text(), 'Ошибка проверки по XSD')]").exists(),
                "Не должно быть ошибки проверки по XSD схеме для " + displayName());

        // Блок «Страхователь»: реквизиты (РегНомер/ИНН/КПП) с карточки должны совпадать с фикстурой.
        validationSteps.verifyInsurerRequisitesOnCard(xmlResource(), insurerLenient());

        // Закрепляем soft-проверки ReportValidationSteps (BaseTest применяет только свой assertions)
        validationSteps.getTestAssertions().assertAll();

        // ---------- Act 2. Подписание ----------
        signSteps.signAndSendReport();

        // Обновление страницы: статус и кнопки актуализируются только после refresh
        refresh();

        // ---------- Assert 2. Отчёт подписан ----------
        processId = new com.bft.ui.pages.MainPage().getProcessIdValue();
        assertions.assertNotNull(processId,
                "Отчёту " + displayName() + " должен быть присвоен Идентификатор процесса");
        if (processId != null) {
            logger.info("ИД процесса подписанного отчёта {}: {}", displayName(), processId);
            Allure.addAttachment("ИД процесса", "text/plain", displayName() + " : " + processId, ".txt");
            assertions.assertTrue(processId.matches(UUID_REGEX),
                    "Идентификатор должен быть в формате UUID. Фактическое значение: '" + processId + "'");
        }

        com.bft.ui.pages.MainPage mainPage = new com.bft.ui.pages.MainPage();
        assertions.assertTrue(
                mainPage.isSignAndSendDisabled(),
                "После подписания кнопка «Подписать и отправить» должна исчезнуть");

        // ---------- Assert 3. Read-only режим ----------
        // После подписания (syncSign) модалка закрывается автоматически и страница
        // переходит в режим «Чтение отчёта» — кнопка «Скачать отчёт» видна,
        // что и подтверждает успешное завершение операции подписания.
        mainPage.verifySignedReadOnlyMode(displayName());

        // ---------- Act 3. Закрыть отчёт и отфильтровать в списке по ИД процесса ----------
        // Возвращаемся из карточки в список отчётов («Закрыть» → «Да»), затем в таблице
        // фильтруем по ИД процесса (UUID) и проверяем статус подписанного отчёта.
        if (processId != null) {
            mainPage.closeOpenedReport();
            mainPage.filterReportsByProcessId(processId);

            // ---------- Assert 4. Статус «Доставлен» в отфильтрованной таблице ----------
            mainPage.waitTableToLoad();
            String status = mainPage.getReportStatusByProcessId(processId);
            logger.info("Статус подписанного отчёта {} по ИД процесса {}: '{}'",
                    displayName(), processId, status);
            assertions.assertTrue(
                    "Доставлен".equalsIgnoreCase(status),
                    "Отчёт " + displayName() + " должен иметь статус «Доставлен» в таблице. "
                            + "Фактический статус: '" + status + "' (ИД процесса: " + processId + ")");
        }
    }

    // ---------- Ручное заполнение (минимальное / максимальное) ----------

    /**
     * Ручное создание отчёта с минимальным заполнением (только обязательные поля).
     * Последовательность шагов берётся из {@link ReportFillers} по типу отчёта.
     */
    @Test
    @Tag("manual")
    @Story("Manual Creation (минимальное заполнение)")
    @Description("Ручное создание отчёта с минимально необходимым набором данных")
    @Severity(SeverityLevel.CRITICAL)
    public void manualCreateMinimal() {
        manualCreate(displayName() + " (минимальное заполнение)", false);
    }

    /**
     * Ручное создание отчёта с максимальным заполнением (обязательные + необязательные поля/строки).
     * Последовательность шагов берётся из {@link ReportFillers} по типу отчёта.
     */
    @Test
    @Tag("manual")
    @Story("Manual Creation (максимальное заполнение)")
    @Description("Ручное создание отчёта с максимально полным набором данных")
    @Severity(SeverityLevel.CRITICAL)
    public void manualCreateMaximal() {
        manualCreate(displayName() + " (максимальное заполнение)", true);
    }

    private void manualCreate(String label, boolean maximal) {
        SzvReportsSteps steps = new SzvReportsSteps();
        ManualReportFiller filler = ReportFillers.get(formType());
        assertions.assertNotNull(filler,
                "Для типа отчёта " + displayName() + " не зарегистрирована стратегия ручного заполнения (ReportFillers)");

        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(formType());
        steps.addNewReport();

        if (maximal) {
            filler.fillMaximal(steps);
        } else {
            filler.fillMinimal(steps);
        }

        // 1) нет ошибок валидации обязательных полей
        validationSteps.verifyNoValidationErrors();
        // 2) заполненные данные реально сохранены (ЗЛ/мероприятие/период видны на странице)
        filler.verifyPersisted(validationSteps);

        // Правило проекта: BaseTest применяет только свой `assertions`, поэтому soft-проверки
        // ReportValidationSteps закрепляем вручную, иначе они никогда не упадёт.
        validationSteps.getTestAssertions().assertAll();
        logger.info("Ручное создание {} завершено без ошибок валидации", label);
    }
}
