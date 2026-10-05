package com.bft.LK_Insurence.ODV_1;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E ОДВ-1 на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("report-sign")
@Feature("ОДВ-1")
public class Odv1XmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("ODV1-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.ODV1;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.SZV_ODV_1_AF2;
    }

    @Override
    protected String reportTypeCode() {
        return "5";
    }

    @Override
    protected String displayName() {
        return "ОДВ-1";
    }
}
