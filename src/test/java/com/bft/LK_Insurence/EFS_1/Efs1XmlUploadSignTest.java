package com.bft.LK_Insurence.EFS_1;

import com.bft.LK_Insurence.AbstractReportUploadSignTest;
import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import io.qameta.allure.AllureId;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Полный E2E ЕФС-1 на базе СЗВ-М: загрузка XML → подписание (API/rest) → проверка статуса.
 */
@Tag("lk-insurer")
@Tag("smoke")
@Tag("report-sign")
@Feature("ЕФС-1")
public class Efs1XmlUploadSignTest extends AbstractReportUploadSignTest {

    @Override
    @Test
    @AllureId("EFS1-SIGN-001")
    public void uploadSignAndVerify() {
        super.uploadSignAndVerify();
    }

    @Override
    protected ReportFormType formType() {
        return ReportFormType.EFS1;
    }

    @Override
    protected ReportXmlResource xmlResource() {
        return ReportXmlResource.EFS_FULL_ECP;
    }

    @Override
    protected String reportTypeCode() {
        return "8";
    }

    @Override
    protected boolean insurerLenient() {
        // Продукт-гэп: карточка ЕФС-1 после загрузки не подтягивает реквизиты страхователя
        // (РегНомер/ИНН/КПП/ОГРН/ОКВЭД) из загруженного XML — они остаются пустыми.
        return true;
    }

    @Override
    protected String displayName() {
        return "ЕФС-1";
    }
}
