package com.bft.LK_Insurence;

import com.bft.config.TestConfig;
import com.bft.config.UITestStrategy;
import com.bft.test.base.UITestBase;
import com.bft.ui.pages.CryptoProDemoPage;
import com.bft.utils.CryptoProPluginVerifier;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.logging.LogType;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.bft.enums.UrlConstants.CRYPTOPRO_DEMO_PAGE_URL;
import static com.codeborne.selenide.Selenide.open;

/**
 * Вспомогательный тест для Фазы 0: ручной осмотр демо-страницы КриптоПРО.
 * <p>
 * Открывает Chrome с расширением КриптоПРО (через реальный стек проекта:
 * {@link UITestStrategy} + {@link com.bft.browser.factory.ChromeBrowserFactory})
 * и оставляет окно открытым для ручного изучения нативного окна выбора
 * сертификата (это окно ОС, его нельзя автоматизировать Selenium/Playwright).
 * <p>
 * Время окна задаётся системным свойством: {@code -Dmanual.window.min=5} (по умолчанию 5 минут).
 * После Фазы 0 тест можно удалить.
 */
public class ManualCryptoProInspectionTest extends UITestBase {

    private static final int DEFAULT_WINDOW_MIN = 5;

    private CryptoProDemoPage page;

    private void openDemoWithExtension() {
        new UITestStrategy(new TestConfig()).configureSelenide();
        open(CRYPTOPRO_DEMO_PAGE_URL);
        page = new CryptoProDemoPage();
        page.waitForPageLoad();
    }

    @Test(groups = {"manual", "crypto"}, testName = "[РУЧНОЙ] Открыть демо КриптоПРО")
    public void openDemoForManualInspection() throws InterruptedException {
        openDemoWithExtension();

        dumpBrowserConsole();

        logger.info("Статусы плагина: extension={} plugin={} csp={} objects={}",
                safeStatus(page::verifyExtensionLoaded), safeStatus(page::verifyPluginLoaded),
                safeStatus(page::verifyCspLoaded), safeStatus(page::verifyObjectsLoaded));
        logger.info("Плагин через JS: cadesplugin/CryptoPro = {}", CryptoProPluginVerifier.verifyViaJavaScript());

        int minutes = windowMinutes();
        logger.info("=== РУЧНОЙ РЕЖИМ: окно открыто на {} мин. ==="
                + " Нажми «Подписать», в нативном окне выбора сертификата зафиксируй:"
                + " заголовок окна, сколько сертификатов в списке, закрывается ли окно после подписи.", minutes);
        Thread.sleep(minutes * 60_000L);

        logger.info("Результат подписи на странице: {}", page.verifySignatureResult());
    }

    @Test(groups = {"manual", "crypto"}, testName = "[РУЧНОЙ] Демо КриптоПРО + клик «Подписать»")
    public void openDemoAndClickSignForManualInspection() throws InterruptedException {
        openDemoWithExtension();

        dumpBrowserConsole();

        logger.info("Статусы плагина: extension={} plugin={} csp={} objects={}",
                safeStatus(page::verifyExtensionLoaded), safeStatus(page::verifyPluginLoaded),
                safeStatus(page::verifyCspLoaded), safeStatus(page::verifyObjectsLoaded));

        Object certSelected = Selenide.executeJavaScript(
                "var sel = document.getElementById('CertListBox');"
                + "if (!sel || !sel.options.length) return 'NO_CERTS';"
                + "for (var i = 0; i < sel.options.length; i++) {"
                + "  if (sel.options[i].text.indexOf('1546025669') >= 0 || sel.options[i].text.indexOf('КОТОВ') >= 0) {"
                + "    sel.selectedIndex = i;"
                + "    sel.dispatchEvent(new Event('change', {bubbles: true}));"
                + "    return 'SELECTED: ' + sel.options[i].text;"
                + "  }"
                + "}"
                + "return 'CERT_NOT_FOUND';");
        logger.info("Выбор сертификата КОТОВ: {}", certSelected);

        try {
            page.clickSignButton();
            logger.info("Кнопка «Подписать» нажата. Ожидаем появления нативного окна выбора сертификата.");
        } catch (Exception e) {
            logger.warn("Клик через Selenide не удался, пробуем через JS: {}", e.getMessage());
            Object forced = Selenide.executeJavaScript(
                    "var btn = document.getElementById('SignBtn');"
                    + "if (!btn) return 'NO_SIGN_BTN';"
                    + "var wrap = btn.closest('.button-chkbox-wrapper');"
                    + "if (wrap) { wrap.style.display = 'block'; wrap.style.visibility = 'visible'; }"
                    + "btn.style.display = 'inline-block'; btn.style.visibility = 'visible';"
                    + "btn.disabled = false;"
                    + "btn.click();"
                    + "return 'JS_CLICKED';");
            logger.info("Кнопка «Подписать» нажата через JS: {}", forced);
        }

        int minutes = windowMinutes();
        logger.info("=== РУЧНОЙ РЕЖИМ: теперь вручную выбери сертификат в нативном окне и подпиши. Окно дано на {} мин. ===", minutes);
        Thread.sleep(minutes * 60_000L);

        logger.info("Результат подписи на странице: {}", page.verifySignatureResult());
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

    private boolean safeStatus(java.util.function.BooleanSupplier check) {
        try {
            return check.getAsBoolean();
        } catch (Throwable e) {
            logger.warn("Проверка статуса не выполнена: {}", e.getMessage());
            return false;
        }
    }

    private void dumpBrowserConsole() {
        try {
            Object cadesplugin = Selenide.executeJavaScript("return (typeof window.cadesplugin !== 'undefined') ? 'DEFINED' : 'UNDEFINED'");
            logger.info("JS диагностика: window.cadesplugin = {}", cadesplugin);
        } catch (Exception e) {
            logger.warn("Не удалось выполнить JS-диагностику: {}", e.getMessage());
        }
        try {
            String extCheck = (String) Selenide.executeJavaScript(
                    "var result = 'UNKNOWN';"
                    + "var s = document.createElement('script');"
                    + "s.src = 'chrome-extension://pfhgbfnnjiafkhfdkmpiflachepdcjod/nmcades_plugin_api.js';"
                    + "s.onload = function(){ window.__extLoaded = 'LOADED'; };"
                    + "s.onerror = function(){ window.__extLoaded = 'ERROR'; };"
                    + "document.head.appendChild(s);"
                    + "return 'запрос отправлен';");
            logger.info("Расширение (ресурс): запрос отправлен, ждём 3с...");
            Thread.sleep(3000);
            Object res = Selenide.executeJavaScript("return window.__extLoaded || 'NO_RESPONSE'");
            logger.info("Расширение (ресурс): {}", res);
        } catch (Exception e) {
            logger.warn("Не удалось проверить ресурс расширения: {}", e.getMessage());
        }
        for (int i = 1; i <= 5; i++) {
            try {
                Thread.sleep(2000);
                Object statuses = Selenide.executeJavaScript(
                        "return {ext: document.getElementById('ExtensionEnabledTxt')?.textContent,"
                        + " plugin: document.getElementById('PluginEnabledTxt')?.textContent,"
                        + " csp: document.getElementById('CspEnabledTxt')?.textContent,"
                        + " obj: document.getElementById('ObjectsLoadedTxt')?.textContent,"
                        + " extDot: document.getElementById('ExtensionEnabledImg')?.className,"
                        + " plgDot: document.getElementById('PluginEnabledImg')?.className,"
                        + " cspDot: document.getElementById('CspEnabledImg')?.className,"
                        + " objDot: document.getElementById('ObjectsLoadedImg')?.className};");
                logger.info("Статусы (итерация {}): {}", i, statuses);
                String json = String.valueOf(statuses);
                if (json.contains("\"green\"") && !json.contains("ожидание") && !json.contains("не загружено")) {
                    logger.info("Все статусы зелёные!");
                    break;
                }
            } catch (Exception e) {
                logger.warn("Ошибка опроса статусов: {}", e.getMessage());
            }
        }
        try {
            Object certs = Selenide.executeJavaScript(
                    "var sel = document.getElementById('CertListBox');"
                    + "if (!sel) return 'NO_CERT_LIST';"
                    + "var out = [];"
                    + "for (var i = 0; i < sel.options.length; i++) { out.push(sel.options[i].text); }"
                    + "return {count: sel.options.length, certs: out};");
            logger.info("Сертификаты в списке демо-страницы: {}", certs);
        } catch (Exception e) {
            logger.warn("Не удалось прочитать список сертификатов: {}", e.getMessage());
        }
        try {
            List<String> logs = WebDriverRunner.getWebDriver().manage().logs().get(LogType.BROWSER).getAll()
                    .stream()
                    .map(l -> l.getLevel() + ": " + l.getMessage())
                    .collect(Collectors.toList());
            logger.info("Консоль браузера ({} записей):\n{}", logs.size(), String.join("\n", logs));
        } catch (Exception e) {
            logger.warn("Не удалось прочитать консоль браузера: {}", e.getMessage());
        }
    }
}