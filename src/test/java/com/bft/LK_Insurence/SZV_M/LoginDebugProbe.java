package com.bft.LK_Insurence.SZV_M;

import com.bft.pw.PwSession;
import com.bft.steps.AuthSteps;
import com.bft.steps.SzvReportsSteps;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Tag;

/**
 * ПРОБА: пройти authorizeEVS, поймать где застряли, сбросить HTML страницы в файл.
 */
@Tag("lk-insurer")
public class LoginDebugProbe extends com.bft.test.base.UITestBase {

    @Test
    public void debugLogin() throws Exception {
        AuthSteps auth = new AuthSteps();
        try {
            auth.authorizeEVS(com.bft.enums.UITypeSelector.getSelectedUIType());
            System.out.println("LOGIN OK, url=" + PwSession.page().url());
        } catch (Throwable t) {
            String url = PwSession.page().url();
            String html;
            try {
                html = String.valueOf(PwSession.page().content());
            } catch (Exception e) {
                html = "<cannot get content: " + e.getMessage() + ">";
            }
            System.out.println("LOGIN FAIL url=" + url);
            System.out.println("MSG=" + t.getMessage());
            Files.write(Paths.get("build/login_stuck.html"),
                    ("URL=" + url + "\n\n" + html).getBytes(StandardCharsets.UTF_8));
            Allure.addAttachment("login_stuck.html", "text/html", html, ".html");
            System.out.println("HTML_LEN=" + html.length());
        }
    }
}
