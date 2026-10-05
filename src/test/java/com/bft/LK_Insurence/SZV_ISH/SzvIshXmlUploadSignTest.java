package com.bft.LK_Insurence.SZV_ISH;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E СЗВ-ИСХ на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("report-sign")
@Feature("СЗВ-ИСХ")
public class SzvIshXmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("SZVISH-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.SZVISH;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_ISH_AF2;
    }

    @Override
    protected String reportTypeCode() {
        return "4";
    }

    @Override
    protected String displayName() {
        return "СЗВ-ИСХ";
    }
}
