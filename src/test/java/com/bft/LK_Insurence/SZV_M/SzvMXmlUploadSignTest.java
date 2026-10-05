package com.bft.LK_Insurence.SZV_M;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E-сценарий СЗВ-М: загрузка XML → подписание → проверка статуса по UUID.
 *
 * <p>Наследует базовый флоу {@link AbstractReportUploadSignTest} (загрузка XML → API-подписание
 * → проверка статуса), а также тесты ручного заполнения (минимального и максимального).
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("xml-upload")
@Tag("web")
@Tag("signing")
@Epic("Формы отчетности")
@Feature("СЗВ-М")
public class SzvMXmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("SZVM-SIGN-001")
    @Story("XML Upload + Подписание")
    @Description("Полный цикл СЗВ-М: загрузка XML, подписание, проверка статуса по ИД процесса")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.SZVM;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_M;
    }

    @Override
    protected String reportTypeCode() {
        return "1";
    }

    @Override
    protected String displayName() {
        return "СЗВ-М";
    }
}
