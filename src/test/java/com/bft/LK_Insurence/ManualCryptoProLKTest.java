package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.TimeoutConstants;
import com.bft.enums.UITypeSelector;
import com.bft.steps.AuthSteps;
import com.bft.steps.ReportDataEntrySteps;
import com.bft.steps.ReportNavigationSteps;
import com.bft.test.base.UITestBase;
import com.bft.pw.Condition;
import com.bft.pw.Selenide;
import org.testng.annotations.Test;

import static com.bft.pw.Selenide.$x;

/**
 * Вспомогательный тест для интеграции КриптоПРО в ЛК Страхователя.
 * <p>
 * Проходит полный флоу СЗВ-М до шага «Подписать и отправить», жмёт «Подписать»
 * и оставляет окно открытым для ручного осмотра того, что появляется после клика
 * (нативное окно выбора сертификата или страничный диалог).
 * Время окна: {@code -Dmanual.window.min=5}.
 */
public class ManualCryptoProLKTest extends UITestBase {

    private static final int DEFAULT_WINDOW_MIN = 0;

    @Test(groups = {"manual", "crypto"}, testName = "[РУЧНОЙ] ЛК Страхователя: СЗВ-М до подписи")
    public void manualSignSzvM() throws InterruptedException {
        AuthSteps authSteps = new AuthSteps();
        ReportNavigationSteps navSteps = new ReportNavigationSteps();
        ReportDataEntrySteps dataSteps = new ReportDataEntrySteps();

        authSteps.authorizeEVS(UITypeSelector.getSelectedUIType());
        navSteps.addReports();
        navSteps.selectReportType(ReportFormType.SZVM);
        navSteps.addNewReport();
        dataSteps.addGeneralInfo();
        navSteps.createContinue();
        navSteps.goToZL();
        dataSteps.addZL();
        dataSteps.fillZL();
        dataSteps.fillZLINN();
        dataSteps.saveZL();

        logger.info("=== Переход во вкладку «Общие сведения» ===");
        new com.bft.ui.pages.MainPage().clickSectionA("Общие сведения");

        logger.info("=== Шаг «Подписать и отправить» ===");
        dataSteps.submitAndSend();
        logger.info("Клик по «Подписать» выполнен. Модалка «Подписание» открыта.");

        signAndFinish();

        int minutes = windowMinutes();
        logger.info("=== КОНТРОЛЬНОЕ ОКНО на {} мин (проверка статуса отчёта после подписания). ===", minutes);
        long endTime = System.currentTimeMillis() + minutes * 60_000L;
        while (System.currentTimeMillis() < endTime) {
            dumpDiagnostics();
            Thread.sleep(10_000);
        }
        dumpDiagnostics();
        logger.info("Итоговое состояние страницы после подписания.");
    }

    private void signAndFinish() {
        boolean muiVisible = $x("//div[contains(@class, 'MuiDialog-container')]").is(Condition.visible);
        if (!muiVisible) {
            logger.info("=== Шаг 1: клик «Подписать» в футере модалки «Подписание» → диалог «Выберите подпись» ===");
            $x("//div[contains(@class, 'modal-content')]//button[contains(@class, 'btn-primary')]//span[text() = 'Подписать']")
                    .shouldBe(Condition.visible, TimeoutConstants.CRYPTO_PLUGIN_WAIT)
                    .click();
        } else {
            logger.info("=== Шаг 1: MUI-диалог «Выберите подпись» уже открыт — пропускаем клик по футеру ===");
        }

        logger.info("=== Шаг 2: в диалоге «Выберите подпись» выбираем подпись и жмём «Подписать» ===");
        $x("//div[contains(@class, 'MuiDialog-container')]//label[contains(@class, 'MuiFormControlLabel-root')][.//input[@type = 'radio']]")
                .shouldBe(Condition.visible, TimeoutConstants.CRYPTO_PLUGIN_WAIT)
                .click();
        $x("//div[contains(@class, 'MuiDialog-container')]//button[contains(@class, 'btn-primary')]//span[text() = 'Подписать']")
                .shouldBe(Condition.visible, TimeoutConstants.CRYPTO_PLUGIN_WAIT)
                .click();

        logger.info("=== Шаг 3: ждём «Готово» в модалке «Подписание» и нажимаем ===");
        $x("//div[contains(@class, 'modal-content')]//button[contains(@class, 'btn-primary')]//span[text() = 'Готово']")
                .shouldBe(Condition.visible, TimeoutConstants.REPORT_PROCESSING_WAIT)
                .click();
        logger.info("«Готово» нажата — подписание завершено.");
    }

    private void dumpDiagnostics() {
        try {
            Object dialogs = Selenide.executeJavaScript(
                    "var out = {};"
                    + "out.title = document.title;"
                    + "out.providerDialog = !!document.querySelector('.provider-dialog');"
                    + "out.modalContent = !!document.querySelector('.modal-content');"
                    + "out.roleDialog = document.querySelectorAll('[role=dialog]').length;"
                    + "out.certSelects = (function(){var sels=document.querySelectorAll('select');var res=[];"
                    + "for(var i=0;i<sels.length;i++){res.push(sels[i].id||sels[i].name||'unnamed:'+sels[i].options.length);}return res;})();"
                    + "out.spinners = document.querySelectorAll('.spinner,.loading').length;"
                    + "out.visibleButtons = (function(){var bs=document.querySelectorAll('button');var res=[];"
                    + "for(var i=0;i<bs.length;i++){if(bs[i].offsetParent!==null||bs[i].getClientRects().length>0){"
                    + "var t=(bs[i].innerText||'').trim();if(t)res.push(t.substring(0,50));}}return res;})();"
                    + "out.headings = (function(){var hs=document.querySelectorAll('h1,h2,h3,.card-title,.title');var res=[];"
                    + "for(var i=0;i<hs.length;i++){if(hs[i].offsetParent!==null){var t=(hs[i].innerText||'').trim();if(t)res.push(t.substring(0,60));}}return res;})();"
                    + "out.modalText = (function(){var m=document.querySelector('[role=dialog]')||document.querySelector('.modal-content');"
                    + "return m ? (m.innerText||'').substring(0,800) : 'NO_MODAL';})();"
                    + "out.modalHtml = (function(){var m=document.querySelector('[role=dialog]')||document.querySelector('.modal-content');"
                    + "return m ? (m.outerHTML||'').substring(0,2500) : 'NO_MODAL';})();"
                    + "out.modalInputs = (function(){var m=document.querySelector('[role=dialog]')||document.querySelector('.modal-content');"
                    + "if(!m)return 'NO_MODAL';var els=m.querySelectorAll('button,label,input,select,radio,a');var res=[];"
                    + "for(var i=0;i<els.length;i++){var e=els[i];var t=(e.innerText||'').trim();"
                    + "res.push({tag:e.tagName,cls:(e.className||'').toString().substring(0,40),txt:t.substring(0,40),type:e.type||'',id:e.id||'',name:e.name||''});}return res;})();"
                    + "out.muiDialog = (function(){var m=document.querySelector('.MuiDialog-container');"
                    + "if(!m)return 'NO_MUI';"
                    + "var els=m.querySelectorAll('button,label,input,select,a');var res=[];"
                    + "for(var i=0;i<els.length;i++){var e=els[i];var t=(e.innerText||'').trim();"
                    + "res.push({tag:e.tagName,cls:(e.className||'').toString().substring(0,50),txt:t.substring(0,60),type:e.type||'',id:e.id||'',name:e.name||''});}"
                    + "return {html:(m.outerHTML||'').substring(0,2500),els:res};})();"
                    + "return out;");
            logger.info("Диагностика страницы ЛК: {}", dialogs);
        } catch (Exception e) {
            logger.warn("Не удалось выполнить диагностику страницы: {}", e.getMessage());
        }
        try {
            String bodySnippet = (String) Selenide.executeJavaScript(
                    "var b = document.body.innerText || ''; return b.substring(0, 2000);");
            logger.info("Текст страницы (фрагмент):\n{}", bodySnippet);
        } catch (Exception e) {
            logger.warn("Не удалось прочитать текст страницы: {}", e.getMessage());
        }
    }

    private int windowMinutes() {
        String prop = System.getProperty("manual.window.min");
        if (prop == null || prop.trim().isEmpty()) {
            return DEFAULT_WINDOW_MIN;
        }
        try {
            return Integer.parseInt(prop.trim());
        } catch (NumberFormatException e) {
            logger.warn("Некорректное значение -Dmanual.window.min={}, используется {}", prop, DEFAULT_WINDOW_MIN);
            return DEFAULT_WINDOW_MIN;
        }
    }
}