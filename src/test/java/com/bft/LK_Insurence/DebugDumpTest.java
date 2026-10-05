package com.bft.LK_Insurence;

import com.bft.enums.ReportFormType;
import com.bft.enums.UITypeSelector;
import com.bft.pw.PwSession;
import com.bft.steps.ManualReportFiller;
import com.bft.steps.ReportFillers;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Tag;

@Tag("lk-insurer")
public class DebugDumpTest extends UITestBase {

    private static ReportFormType formType() {
        String r = System.getProperty("dump.report", "SZVKORR").toUpperCase();
        switch (r) {
            case "SZVM": return ReportFormType.SZVM;
            case "SZVTD": return ReportFormType.SZVTD;
            case "SZVSTAZH": return ReportFormType.SZVSTAZH;
            case "SZVISH": return ReportFormType.SZVISH;
            case "ODV1": return ReportFormType.ODV1;
            case "SZVKORR": return ReportFormType.SZVKORR;
            case "SZVK": return ReportFormType.SZVK;
            case "EFS1": return ReportFormType.EFS1;
            case "SZVDSO": return ReportFormType.SZVDSO;
            default: throw new IllegalArgumentException("unknown: " + r);
        }
    }

    @Test
    public void dump() throws Exception {
        SzvReportsSteps steps = new SzvReportsSteps();
        ReportFormType ft = formType();
        ManualReportFiller filler = ReportFillers.get(ft);
        try {
            steps.authorizeEVS(UITypeSelector.getSelectedUIType());
            steps.addReports();
            steps.selectReportType(ft);
            steps.addNewReport();
            filler.fillMinimal(steps);
        } catch (Throwable t) {
            String html = PwSession.driver().getPageSource();
            File out = new File("target/dom-" + ft.name() + ".html");
            Files.write(out.toPath(), html.getBytes(StandardCharsets.UTF_8));
            System.out.println(">>> DOM dumped for " + ft.name() + " to " + out.getAbsolutePath());
            throw new RuntimeException("dumped DOM for " + ft.name(), t);
        }
    }
}
