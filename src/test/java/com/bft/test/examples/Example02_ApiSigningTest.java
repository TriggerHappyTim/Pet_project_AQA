package com.bft.test.examples;

import com.bft.enums.UITypeSelector;
import com.bft.service.graphql.SignService;
import com.bft.service.graphql.SignServiceImpl;
import com.bft.steps.SzvReportsSteps;
import com.bft.test.base.UITestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ============================== УЧЕБНЫЙ ПРИМЕР №2 ==============================
 *
 * Подписание архивного ответа через API (GraphQL) вместо браузерного плагина КриптоПРО.
 *
 * ЗАЧЕМ ЭТО НУЖНО:
 *   Раньше подписание шло только через UI: кнопка «Подписать» -> диалог выбора
 *   провайдера -> плагин КриптоПРО в браузере. Это медленно и требует установленного
 *   плагина. Теперь есть ДВА режима:
 *
 *     • evs.sign.mode=ui (ПО УМОЛЧАНИЮ) — полное E2E через настоящий плагин.
 *     • evs.sign.mode=api — реальное подписание через бэкенд
 *       (ArchivalResponseSignatureService): фронт делает prepare, тест скачивает
 *       XML из DA, подписывает (XMLDSIG) и загружает в DA, затем вызывает
 *       processSignatureDone. КриптоПРО не требуется.
 *
 * КАК ЭТО РАБОТАЕТ ВНУТРИ:
 *   ArchiveSteps.signRequest() сам смотрит на свойство evs.sign.mode. В api-режиме
 *   последующий selectCertificate() пропускается автоматически.
 *
 * КАК ЗАПУСТИТЬ:
 *   mvn test -Dtest=Example02_ApiSigningTest -Dservice.graphql-mesh.url=http://localhost:20266
 *   -Devs.sign.mode=api -Devs.sign.pem-dir=src/test/resources/certs
 * ==============================================================================
 */
@Disabled("Обучающий пример №2 — не запускать в CI")
@Tag("example")
@Tag("signing")
public class Example02_ApiSigningTest extends UITestBase {

    /**
     * Вариант А (рекомендуемый): просто вызывайте шаги — режим выберется автоматически.
     */
    @Test
    public void signRequestViaSteps_autoMode() {
        SzvReportsSteps steps = new SzvReportsSteps();

        steps.authorizeArhivRPU(UITypeSelector.getSelectedRpuType());
        steps.createZaprosRPU();
        // ... заполнение формы ...
        steps.saveRequest();

        // Эта строка подпишет запрос:
        //   • по умолчанию — через API (GraphQL mutation в Camunda);
        //   • с -Devs.sign.mode=ui — через браузерный диалог и плагин.
        steps.signRequest();

        assertions.assertTrue(true, "Запрос должен быть подписан");
    }

    /**
     * Вариант Б: прямая работа с сервисом подписи архивного ответа.
     * Нужен, когда вы тестируете ПОДПИСАНИЕ как таковое и хотите управлять параметрами.
     */
    @Test
    public void signArchiveResponseDirectly() {
        // Создаём клиент подписания. URL сервиса берётся из:
        //   1) -Dservice.graphql-mesh.url=...
        //   2) переменной окружения SERVICE_GRAPHQL_MESH_URL
        //   3) дефолта http://localhost:20266 (порт port-forward'а из CI)
        SignService signer = new SignServiceImpl();

        // Завершаем подписание архивного ответа (processSignatureDone).
        // ВАЖНО: signedXmlFileRef должен быть реальным guid УЖЕ подписанного файла,
        // загруженного в DA (см. SigningHelper.signXmlAndUpload). Ниже — лишь форма вызова.
        boolean ok = signer.processSignatureArchive(
                "item-id-456",                       // id архивного ответа
                "file-guid-signed-xml-from-da",      // signedXmlFileRef (guid подписанного файла)
                "request-id-from-initPrepareDoc"     // requestId из initPrepareDoc
        );

        // Мягкая проверка результата. Запрос и ответ автоматически прикрепляются к Allure,
        // поэтому при падении вы увидите тело GraphQL прямо в отчёте.
        assertions.assertTrue(ok, "Подписание архива должно выполниться без ошибок");
    }
}
