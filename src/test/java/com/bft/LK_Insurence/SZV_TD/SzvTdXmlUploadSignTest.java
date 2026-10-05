package com.bft.LK_Insurence.SZV_TD;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E СЗВ-ТД на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("report-sign")
@Feature("СЗВ-ТД")
public class SzvTdXmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("SZVTD-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.SZVTD;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_TD_TYPE1;
    }

    @Override
    protected String reportTypeCode() {
        return "2";
    }

    @Override
    protected String displayName() {
        return "СЗВ-ТД";
    }
}
