package com.bft.LK_Insurence.SZV_M;

import com.bft.enums.ReportFormType;
import com.bft.enums.ReportXmlResource;
import com.bft.enums.UITypeSelector;
import com.bft.pw.PwSession;
import com.bft.steps.SzvReportsSteps;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bft.pw.Selenide.$x;
import org.junit.jupiter.api.Tag;

/**
 * ПРОБА: открыть модалку «Подписание» (dig-sign-modal) на отчёте СЗВ-М, нажать
 * «Подписать» и слить DOM всех фреймов (в т.ч. iframe плагина CAdES) — чтобы найти
 * селекторы окна «Выбор сертификата» и кнопки завершения.
 */
@Tag("lk-insurer")
public class CryptoproSignModalProbe extends com.bft.test.base.UITestBase {

    @Test
    public void dumpSignModalFrames() throws Exception {
        SzvReportsSteps steps = new SzvReportsSteps();
        steps.authorizeEVS(UITypeSelector.getSelectedUIType());
        steps.addReports();
        steps.selectReportType(ReportFormType.SZVM);
        steps.addNewReport();
        steps.sendXml(ReportXmlResource.SZV_M);

        dumpMain("target/debug/probe-1-after-upload.html", PwSession.page());

        new com.bft.ui.pages.MainPage().clickSignAndSend();
        $x("//button[normalize-space() = 'Да']")
                .shouldBe(com.bft.pw.Condition.visible, java.time.Duration.ofSeconds(10))
                .click();
        System.out.println("PROBE: кликнут «Подписать и отправить» + «Да»");

        PwSession.sleep(2500);
        dumpMain("target/debug/probe-2-modal-open.html", PwSession.page());

        // Пробуем нажать «Подписать» в модалке подписания (первичная кнопка).
        try {
            $x("//button[text() = 'Подписать']")
                    .shouldBe(com.bft.pw.Condition.visible, java.time.Duration.ofSeconds(10))
                    .click();
            System.out.println("PROBE: нажат «Подписать» в модалке");
        } catch (Exception e) {
            System.out.println("PROBE: НЕ найден button 'Подписать': " + e.getMessage());
        }

        PwSession.sleep(4000); // даём плагину открыть окно выбора сертификата
        dumpAllFrames();
        System.out.println("PROBE: фреймы слиты");
        Allure.addAttachment("probe: main-frame list", "text/plain", framesSummary(), ".txt");
    }

    private void dumpAllFrames() throws Exception {
        StringBuilder summary = new StringBuilder();
        int i = 0;
        for (Frame frame : PwSession.page().frames()) {
            summary.append(i).append(" | ").append(frame.name()).append(" | ")
                    .append(frame.url()).append("\n");
            i++;
        }
        System.out.println("PROBE frames:\n" + summary);

        i = 0;
        for (Frame frame : PwSession.page().frames()) {
            try {
                String html = frame.content();
                String file = "target/debug/probe-frame-" + i + ".html";
                Files.write(Paths.get(file), html.getBytes(StandardCharsets.UTF_8));
                System.out.println("PROBE: frame " + i + " -> " + file + " (" + html.length() + " chars)");
            } catch (Exception e) {
                System.out.println("PROBE: frame " + i + " content error: " + e.getMessage());
            }
            i++;
        }
    }

    private String framesSummary() {
        StringBuilder sb = new StringBuilder();
        for (Frame frame : PwSession.page().frames()) {
            sb.append(frame.name()).append(" | ").append(frame.url()).append("\n");
        }
        return sb.toString();
    }

    private void dumpMain(String file, Page page) throws Exception {
        Files.write(Paths.get(file), page.content().getBytes(StandardCharsets.UTF_8));
    }
}