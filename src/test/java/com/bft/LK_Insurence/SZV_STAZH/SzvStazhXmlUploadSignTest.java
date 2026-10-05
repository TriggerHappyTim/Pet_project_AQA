package com.bft.LK_Insurence.SZV_STAZH;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E СЗВ-СТАЖ на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("report-sign")
@Feature("СЗВ-СТАЖ")
public class SzvStazhXmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("SZVSTAZH-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.SZVSTAZH;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_STAZH_AF2;
    }

    @Override
    protected String reportTypeCode() {
        return "3";
    }

    @Override
    protected String displayName() {
        return "СЗВ-СТАЖ";
    }
}
