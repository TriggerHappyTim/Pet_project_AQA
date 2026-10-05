package com.bft.LK_Insurence.SZV_K;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E СЗВ-К на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("smoke")
@Tag("report-sign")
@Feature("СЗВ-К")
public class SzvKXmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("SZVK-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.SZVK;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_K_AF2;
    }

    @Override
    protected String reportTypeCode() {
        return "7";
    }

    @Override
    protected String displayName() {
        return "СЗВ-К";
    }
}
