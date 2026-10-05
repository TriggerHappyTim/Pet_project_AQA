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
 * ПРОБА 2: залогиниться, создать отчёт, затем эмпирически прощупать n2o data-эндпоинты
 * подписи через авторизованный браузер (куки ESIA шлются автоматически):
 *  - POST .../sign/syncPrepare            -> ожидаем xmlGuid
 *  - POST .../sign/syncSign (cms)         -> смотрим контракт (поля/формат)
 *  - POST .../sign/syncVisualize          -> результат
 */
public class N2oSignProbe2 extends com.bft.test.base.UITestBase {

    @Test
    public void probeSignEndpoints() throws Exception {
        SzvReportsSteps steps = new SzvReportsSteps();
        steps.authorizeEVS(com.bft.enums.UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(ReportFormType.SZVM);
        steps.addNewReport();
        steps.sendXml(ReportXmlResource.SZV_M);

        String pageUrl = PwSession.page().url();
        String id = extractId(pageUrl);
        String base = "https://ecp-test.sfr.gov.ru/insurer/n2o/data/reports/svzm/" + id + "/mainInfo/sign";

        // 1) syncPrepare
        String prep = browserPost(base + "/syncPrepare", "{}");
        log("SYNC_PREPARE", prep);

        // 2) asyncPrepare + polling
        String aprep = browserPost(base + "/asyncPrepare", "{}");
        log("ASYNC_PREPARE", aprep);
        String poll = browserGet(base + "/condition_4_polling?page=1&size=1");
        log("POLLING", poll);

        // 3) syncSign with a dummy CMS-like payload, несколько вариантов полей
        String[] payloads = {
                "{\"signature\":\"DUMMY_CMS_BASE64\"}",
                "{\"cms\":\"DUMMY_CMS_BASE64\"}",
                "{\"xmlGuid\":\"x\",\"signature\":\"DUMMY_CMS_BASE64\"}",
                "signature=DUMMY_CMS_BASE64",
                "cms=DUMMY_CMS_BASE64",
        };
        for (String p : payloads) {
            String r = browserPost(base + "/syncSign", p);
            log("SYNC_SIGN[" + p.substring(0, Math.min(40, p.length())) + "]", r);
        }
        System.out.println("PROBE DONE");
    }

    private String extractId(String url) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("reports/svzm/(\\d+)/mainInfo").matcher(url);
        return m.find() ? m.group(1) : "UNKNOWN";
    }

    private String browserPost(String url, String body) {
        String js = "async (args) => { const u=args[0], b=args[1]; try {"
                + " const r = await fetch(u, {method:'POST', credentials:'include',"
                + " headers:{'Content-Type':'application/json'}, body:b});"
                + " return r.status + ' :: ' + await r.text();"
                + " } catch(e){ return 'ERR ' + e.message; } };";
        return String.valueOf(PwSession.page().evaluate(js, new Object[]{url, body}));
    }

    private String browserGet(String url) {
        String js = "async (args) => { const u=args[0]; try {"
                + " const r = await fetch(u, {credentials:'include'});"
                + " return r.status + ' :: ' + await r.text();"
                + " } catch(e){ return 'ERR ' + e.message; } };";
        return String.valueOf(PwSession.page().evaluate(js, new Object[]{url}));
    }

    private void log(String tag, String body) throws Exception {
        System.out.println(tag + " => " + body);
        Allure.addAttachment(tag, "text/plain", body, ".txt");
        Files.write(Paths.get("build/n2o_probe_" + tag + ".txt"),
                body.getBytes(StandardCharsets.UTF_8));
    }
}
