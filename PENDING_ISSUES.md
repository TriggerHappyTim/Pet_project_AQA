# Отложенные задачи (вернуться позже)

> **Обновлено 2026-08-26 18:30**: Бэкенд указал, что REST+CMS — неверный путь.
> Подписание отчётов через GraphQL/Camunda. См. `docs/plan-evs-signing.md`.

## 🔴 ПодписаниеSZV-M через REST+CMS: блокер — неверный путь (2026-08-26)

**Статус: REST+CMS-путь признан неверным. Переход на GraphQL/Camunda.**

Бэкенд-разработчик подтвердил:
- Подписание отчётов должно идти через **GraphQL/Camunda** (как для архивных запросов)
- Формат подписи — **xmldsig** (не CMS/PKCS#7)
- Ошибка `"Участники должны совпадать с документом"` — артефакт неправильного API-вызова

**Текущий REST+CMS flow (УСТАРЕЛ, оставить для отладки):**
```
syncPrepare → DA load → CMS sign (BouncyCastle) → DA upload → syncSign
```

**Целевой GraphQL flow (нужно реализовать):**
```
syncPrepare (перехват) → SignServiceImpl.signDocument(graphql/mutation)
→ Camunda decision → проверка статуса → UUID в UI
```

**Что нужно от бэкенда:**
1. GraphQL-запросы для подписанияSZV-M (mutation + параметры)
2. Схема xmldsig-генерации (кто создаёт подпись?)
3. Уточнение: `signedXmlFileRef` = `xmlGuid` из syncPrepare?

**Код для адаптации:** `ArchiveSteps.apiSign()` + `SignServiceImpl` — уже работают для архивных запросов.

---

## 🟢 Контракт подписания ЕВС — ВСКРЫТ ПОЛНОСТЬЮ (2026-08-26)

**Рабочая цепочка (подтверждено зондами на UAT):**
```
1. GET  /insurer/n2o/data/reports/{type}/{id}/mainInfo/pfrEcpStorReportAggreServ
        ?pfrEcpStorReportAggreServ_id={id}&pfrEcpStorReportAggreServ_reportType=1
   → list[0].itemId (62526), processId, eaFileId
2. POST /insurer/n2o/data/reports/{type}/{id}/mainInfo/syncPrepare
   тело: {"mimeType":"MIME_TYPE_PROTO_PDF","formName":"СЗВ-М","itemId":62526,
          "doSupportSigning":false}          ← itemId ЧИСЛОМ, пустое тело = 500!
   → data{xmlGuid, requestId, fileName, mimeType}
3. GET  /da/sk/api/store/load?fileGuid={xmlGuid}      ← nginx: /da/X → DA /api/X
   → байты XML (проверено: 200, ЭДПФР)
4. POST /da/sk/api/store/signature   multipart:
     fileSig = CMS (binary)
     metadata = JSON UploadSignatureRequest:
       {fileName, algorithm, certificate(base64 DER), format:"CMS",
        signDate(ISO), hashes:[{algorithm:"SHA-256", hash(base64)}]}
   → SignatureUploadOperationReply { sigGuid } = signXmlGuid
   (format="CMS" и "cades-bes" приняты; PKCS7/PKCS#7/... = INVALID_SIGNATURE_FORMAT)
5. POST /insurer/n2o/data/reports/{type}/{id}/mainInfo/syncSign
   тело: {mimeType, formName, itemId, requestId, xmlGuid, signXmlGuid,
          mime_types:[mimeType]}
   → отчёт подписан; далее refresh UI → статус в таблице
```
**Реализация в проекте:** `DaPortalClient` (все HTTP через браузерную сессию
Playwright — cookies автоматически), `PfxCmsSigner` (BC, ГОСТ/RSA автодетект),
`ReportSignSteps.signCurrentReportViaApi()` — чистый API без UI-кликов.
Блокер один: открывающийся PFX (см. выше).

**JS-мост Playwright:** выражение — ровно ОДНА функция `async arg => {...}`;
`(arg) => async ({url}) => {...}` сериализуется как null (грабли, наступили дважды).

## 🔴 РПУ: учётная запись для архивных тестов (2026-08-26)

**Разведка завершена, контракт вскрыт полностью.** Отчёт СЗВ-М = черновик 8982 на стенде
(ecp-test.sfr.gov.ru/insurer), ИД процесса f7d152c9... (8978) испорчен экспериментами.

**Полная цепочка подписания (фронтовая логика, извлечена из n2o-метаданных):**
```
1. POST /insurer/n2o/data/reports/{id}/mainInfo/syncPrepare
   Тело (модель reportSigning): {"mimeType":"MIME_TYPE_PROTO_PDF","formName":"СЗВ-М",
   "itemId":"62522","doSupportSigning":false}
   Ответ data: { itemId, formName, mimeType, xmlGuid, requestId, fileName, mimeTypeOut }
   ⚠️ ПУСТОЕ ТЕЛО = HTTP 500 «Ошибка запроса сервера»

2. Подпись файла xmlGuid через Document API:
   baseUrl https://ecp-test.sfr.gov.ru/da
   OpenAPI: GET /da/v3/api-docs (136 endpoints)
   Ключевые методы:
     - GET  /api/sk/api/store/presigned-url?fileGuid=...
     - POST /api/sk/api/store/signature              ← загрузка подписи
     - POST /api/sk/api/document/attach/add/signature
   Плагин КриптоПРО (webhost 127.0.0.1:61111) делает ровно это: создаёт
   attached-signature файла xmlGuid → новый guid signXmlGuid.

3. POST /insurer/n2o/data/reports/{id}/mainInfo/syncSign
   Protobuf: SignatureDoneCommandProto, ОБЯЗАТЕЛЬНО mime_types (repeated).
   Тело = модель prepare + signXmlGuid + mime_types:["MIME_TYPE_PROTO_PDF"].
   Без реального подписанного файла в DA → INTERNAL.
```

**Что уже работает в тестах (`SzvMXmlUploadSignTest`, `ReportSignSteps`):**
- вся UI-цепочка до подписания; перехват syncPrepare в `PwSession.awaitSigningData()`;
- режим `evs.sign.mode=ui` проходит целиком на машине с установленным КриптоПРО;
- режим `evs.sign.mode=api` упирается только в отсутствие mesh вне кластера.

**Варианты доведения до полного цикла БЕЗ плагина:**
1. **PFX+BouncyCastle (рекомендуется):** сертификат уже в проекте —
   `src/test/resources/certs/kotov-154.con.pfx`. Скачать файл по presigned-url,
   создать CMS-подпись (BC dependency), загрузить `store/signature`, вызвать syncSign.
2. Mesh/Camunda из CI (порт-форвард 20266 уже настроен в .gitlab-ci.yml).
3. Спросить бэкендеров про тестовый bypass в SignatureDoneCommand.

## 🔴 РПУ: учётная запись для архивных тестов (2026-08-26)

**Статус:** заблокировано на креденциалах. Вся кодовая часть ГОТОВА и ждёт только рабочей УЗ.

**Что случилось:**
- Старый URL `rpu-common-bo.uat.ecp` отдаёт HTTP 500 → заменён на рабочий
  `https://portal.test.ecp/rpu/#/?location=rpu` (`UIType.RPU_UAT`, см. `UIType.java`).
- Пароль УЗ `201OstrovskyDS` истёк, при входе потребована смена → сменён на `P@ssw0rd1232`,
  обновлён в `credentials.properties`, `application-test.yml`, `application-uat.yml`.
- Новая УЗ `201RogovaSV` с паролем `P@ssw0rd1232` **не проходит авторизацию** — ГИС ЕЦП
  отвечает «Ошибка авторизации» (неверный пароль ИЛИ login-метод отключён ИЛИ блокировка
  после серии попыток). Проверено в полностью чистой сессии (cookies/storage очищены).

**Что проверить при возврате:**
1. Ручной вход под `201RogovaSV` в обычном браузере: если появится форма «Смена пароля» —
   задать новый, обновить его во всех трёх файлах выше.
2. Если вручную та же ошибка — сброс пароля админом стенда либо другая парольная УЗ
   (УЗ должна быть именно логин/парольная, НЕ ЕСИА-only).
3. После успешного входа: раскрыть сайдбар («Реестр получателей услуг» → «Реестр запросов
   в архивы»), убедиться в правах УЗ на оба реестра.

**Код готов к прогону (ничего дописывать не нужно):**
- `ArchiveSteps.createZaprosRPU()` — авто-раскрытие сайдбара (`expandSidebarIfNeeded`);
- гибридное подписание: `-Devs.sign.mode=api` → UI доходит до подписания, `syncPrepare`
  перехватывается в `PwSession`, завершение через `SignServiceImpl` (GraphQL/Camunda);
- trace-запись шагов: `-Devs.trace=true`.

**Команда запуска после получения рабочих кредов:**
```bash
mvn test -Dtest="ArchivesTest" -Devs.sign.mode=api -Devs.trace=true
```
