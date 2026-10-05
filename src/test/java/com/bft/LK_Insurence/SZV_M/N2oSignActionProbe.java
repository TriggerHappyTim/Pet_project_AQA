package com.bft.LK_Insurence.SZV_M;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.pw.PwSession;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ПРОБА: залогиниться, создать отчёт СЗВ-М и слить n2o-конфиг страницы отчёта
 * (https://ecp-test.sfr.gov.ru/insurer/n2o/page/reports/svzm/{id}/mainInfo) и глобальный
 * /insurer/n2o/config — чтобы найти action «Подписать и отправить» и его backend-эндпоинт.
 */
public class N2oSignActionProbe extends com.bft.test.base.UITestBase {

    @Test
    public void dumpN2oConfig() throws Exception {
        SzvReportsSteps steps = new SzvReportsSteps();
        steps.authorizeEVS(com.bft.enums.UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(ReportFormType.SZVM);
        steps.addNewReport();
        steps.sendXml(ReportXmlResource.SZV_M);

        String pageUrl = PwSession.page().url();
        System.out.println("PAGE URL = " + pageUrl);
        String id = extractId(pageUrl);
        String n2oPageUrl = "https://ecp-test.sfr.gov.ru/insurer/n2o/page/reports/svzm/"
                + id + "/mainInfo";
        String n2oSignUrl = "https://ecp-test.sfr.gov.ru/insurer/n2o/page/reports/svzm/"
                + id + "/mainInfo/sign";
        String configUrl = "https://ecp-test.sfr.gov.ru/insurer/n2o/config?locale=ru";

        dumpViaBrowser(n2oPageUrl, "build/n2o_page_config.json");
        dumpViaBrowser(n2oSignUrl, "build/n2o_sign_config.json");
        dumpViaBrowser(configUrl, "build/n2o_config.json");
        System.out.println("DUMPED n2o configs");
    }

    private String extractId(String url) {
        // /insurer/page/reports/svzm/9075/mainInfo -> 9075
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("reports/svzm/(\\d+)/mainInfo").matcher(url);
        return m.find() ? m.group(1) : "UNKNOWN";
    }

    private void dumpViaBrowser(String url, String file) throws Exception {
        String js = "async (u) => {"
                + " try {"
                + "   const r = await fetch(u, {credentials:'include'});"
                + "   return await r.text();"
                + " } catch (e) { return 'FETCH_ERR ' + e.message; }"
                + "};";
        Object res = PwSession.page().evaluate(js, url);
        String body = String.valueOf(res);
        Files.write(Paths.get(file), body.getBytes(StandardCharsets.UTF_8));
        System.out.println("DUMP " + url + " -> " + file + " (" + body.length() + " chars)");
        Allure.addAttachment(file, "application/json", body, ".json");
    }
}
