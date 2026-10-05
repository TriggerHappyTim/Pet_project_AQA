# MEMORY — кросс-системное подписание отчётов (EVS / Архив / СКЛ / РПУ / ЛК Страхователя)

> Единый автотест-харнесс: `C:\Users\Tim\IdeaProjects\Pet_project_AQA` (Maven/Java, test scope).
> Цель: подписание отчётов ЭВС через **GraphQL-mesh :20266 + xmldsig (ГОСТ)**, без КриптоПРО.
> Обновлено: 27.08.2026. Состояние: E2E-прогон СЗВ-М доходит до подписания (UI+DA работают),
> но GraphQL mesh `:20266` из этой песочницы недостижим (см. раздел 8). Нужен port-forward mesh
> (kubeconfig у пользователя) либо запуск в сети кластера.

---

## 1. Инвентарь репозиториев

| Проект | Путь | Роль |
|--------|------|------|
| **EVS AQA (наш харнесс)** | `C:\Users\Tim\IdeaProjects\Pet_project_AQA` | Maven/Java, test scope |
| EVS front (ЛК Страхователя, отчёты) | `D:\evs-oi-ipu-usv-front` | n2o-объекты: `signDoc.object.xml` (СЗВ-М: `prepareDocCommon`/`processSignatureCommon`) |
| EVS archive front | `D:\evs-ov-archive-interaction-front` | `requestRegisters/downloadRequest.object.xml` |
| EVS archive service | (backend, не локально) | — |
| **ЛК СКЛ front** | `D:\evs-ov-skl-provider-interaction-front` | `signingDocument.object.xml`, `skl.object.xml` |
| ЛК СКЛ service | `D:\evs-ov-skl-provider-interaction-service` | Kafka + gRPC(MDM) + отдаётся через GraphQL mesh |
| ЛК СКЛ storage | `D:\evs-ov-skl-provider-interaction-storage` | Kafka |
| РПУ archive autotest | `D:\rpu-persacc-autotestsPP` | эталон подключения Kafka + gRPC-REST шлюз |
| proezd autotest | `D:\proezd-autotest` | ФЕЙК `OnSignTaskComplete` — НЕ использовать |

---

## 2. Общий механизм подписания (реализован в AQA, работает для всех систем)

Поток одинаков для EVS / Архива / СКЛ:
1. **prepare**: GraphQL mesh `:20266` → операция prepare (возвращает `requestId` и/или `xmlGuid` документа для подписи).
2. **DA load**: скачать XML по `xmlGuid` (`DaPortalClient.loadFile`).
3. **xmldsig**: `XmlDsigSigner.signEnveloped` — enveloped, **KeyInfo/X509Data/X509Certificate обязателен**;
   ГОСТ идёт ручным путём по RFC 6986 (Santuario canonicalizer `exc-c14n` + BouncyCastle
   `ECGOST3410-2012-256`/`GOST3411-2012-256`), RSA — через Santuario.
4. **DA upload**: `POST /da/sk/api/store` (поле `fileSig`, **без format**) → `signedXmlFileRef`.
5. **process**: GraphQL mesh → операция process (передать `signedXmlFileRef`) → результат.

### Ключевые файлы харнесса
- `src/test/java/com/bft/service/sign/SigningHelper.java` — общий `signXmlAndUpload(itemId, xmlGuid) → signedXmlFileRef`.
- `.../service/sign/XmlDsigSigner.java` — xmldsig ГОСТ/RSA auto-detect (`signEnvelopedGost` / `signEnvelopedSantuario`).
- `.../service/sign/DaPortalClient.java` — `uploadXmlSignature` (`/da/sk/api/store`, без format), `loadFile`.
- `.../service/graphql/SignRequest.java` — `buildPrepareDocCommon`, `buildProcessSignatureCommon`, `buildProcessSignatureArchive` (+ будет `buildPrepareDocumentSkl`/`buildProcessSignatureSkl`).
- `.../service/graphql/SignService.java` / `SignServiceImpl.java` — `prepareDocCommon`, `processSignatureCommon`, `processSignatureArchive` (+ `PROCESS_OP_ARCHIVE`); переключатель `-Devs.sign.variant` (`common`/`printing`).
- `.../steps/ReportSignSteps.java` — `signAndUpload` делегирует `SigningHelper`.
- `.../steps/ArchiveSteps.java` — `apiSign` реальный поток архива.
- `.../test/examples/Example02_ApiSigningTest.java` — `signArchiveResponseDirectly`.
- certs: `src/test/resources/certs/krivonosov.cer.pem` + `krivonosov.key.pem` (ГОСТ Р 34.10-2012/256, key `ECGOST3410-2012-256`).

### Запуск EVS (на сетевом окружении)
```
mvn test -Dtest=SzvMXmlUploadSignTest \
  -Dservice.graphql-mesh.url=http://localhost:20266 \
  -Devs.sign.mode=api -Devs.sign.transport=graphql \
  -Devs.sign.xmldsig=true -Devs.sign.variant=common \
  -Devs.sign.pem-dir=src/test/resources/certs -Dheadless=true
```
Сборка (offline): `mvn -o test-compile`. ГОСТ self-check: `XmlDsigSignerSelfCheck` (`sigValid=true, refDigestValid=true`).

> ГОСТ-блокер СНЯТ (реализовано + офлайн-проверено). Wire-формат следует RFC 6986; бэкенд: «подпись формирует только сервер VipNet, подглядеть негде» — при отклонении подписи на стенде сверить с бэкендом.

---

## 3. EVS (ЛК Страхователя) — статус

- **СЗВ-М**: реализован реальный GraphQL+xmldsig путь (`prepareDocCommon`+`processSignatureCommon`, сервис `CommonReportSignatureService`).
  **НЕ прогнан E2E** (в песочнице нет mesh). ОСТАЛОСЬ: прогнать на стенде, убедиться что `prepareDocCommon.documents`
  содержит XML при `mimeTypes=PDF`, уточнить имя поля DA-загрузки (Allure-вложение).
- **Остальные отчёты ЕВС (план)**: СЗВ-ТД, СЗВ-DSO, СЗВ-СТАЖ, СЗВ-К — тестов пока нет. Предположительно те же
  `prepareDocCommon`/`processSignatureCommon` (common-вариант) с другим `formType`/`formName`.
  Уточнить по `signDoc.object.xml` backend ЛК Страхователя, когда скачают.

> Расхождение версий `signDoc.object.xml`: GitLab (строка 133) — `prepareDocCommon`+`processSignatureCommon`
> (сервис `CommonReportSignatureService`, prepare синхронный, возвращает requestId+documents сразу);
> локальный диск — `initPrepareDoc`+`checkPrepareDoc`(poll)+`processSignatureDone` (сервис `PrintingReportSignatureService`).
> Код реализует ОБА и переключается `-Devs.sign.variant=common`(default)/`printing`.

---

## 4. EVS Архив — статус (реализовано полностью)

Контракт из `D:\evs-ov-archive-interaction-front\...\requestRegisters\downloadRequest.object.xml`:
- сервис `ruGovPfrEcpEvsDsArchiveInteractionService_ArchivalResponseSignatureService`.
- `initPrepareDoc` (GetPrepareDocRouter): вход `itemId`, `formType` `"1608"`, `mimeTypes [MIME_TYPE_PROTO_PDF]`;
  выход `requestId` (генерит сервер) + `documents` (xmlGuid где mimeType XML).
- `checkPrepareDoc` (poll) → documents.
- `processSignatureDone` (GetSignatureDoneRouter): вход `itemId`, `requestId`, `formType "1608"`, `signedXmlFileRef`.
- Реализовано: `SigningHelper`, `SignRequest.buildProcessSignatureArchive`, `SignServiceImpl.processSignatureArchive`+`PROCESS_OP_ARCHIVE`,
  `ArchiveSteps.apiSign` (через `PwSession.awaitSigningData()` перехват `/syncPrepare` → itemId/xmlGuid/requestId),
  удалён фейк `OnSignTaskComplete` (proezd) и старый `signDocument`. formType захардкожен `"1608"`.

---

## 5. ЛК СКЛ (provider) — контракт (НЕ реализовано, ждёт)

Источник: `D:\evs-ov-skl-provider-interaction-front\src\main\resources\META-INF\conf\objects\signingDocument.object.xml`.
- сервис `ruGovPfrEcpEvsDsSklProviderInteractionService_ProviderDocumentSignatureService`.
- `prepareDocument` (GetPrepareDocRouter): вход `itemId`, `requestId`(**МЫ генерим UUID**), `fileName`(=`commonNumber`, default uuid),
  `formType`(**параметр**); выход `requestId` (эхо) + `documents[0].id` (xmlGuid).
- `processSignedDocument` (GetSignatureDoneRouter): вход `itemId`, `requestId`, `formType`, `signedXmlFileRef`(=`signXmlGuid`);
  выход `result`(boolean), `statusMessage`.
- `skl.object.xml` — только бизнес-мутации (ReferralOffer/DirectionDraft/ActDoneWorkDraft), подписи нет.

**Отличия от архива:** `requestId` — входной (не серверный); `formType` — параметр (не `"1608"`); нет `mimeTypes`.

**Подключение:**
- Фронт → **GraphQL mesh** `:20266` (`graphql-mesh-java.ps.svc:20266/graphql`, `mesh-java.uat.ecp/graphql`) — тот же порт, что у EVS.
- Сервис — **Kafka** (`kafka-url`/`KAFKA_BOOTSTRAP_ADDRESS`, default `172.18.32.223:9092`; test `172.18.32.245:9092`, uat `172.18.32.248:9092`)
  + **gRPC** к MDM (`ps.mdm.mdm-grpc-url` test `172.18.32.178`/uat `172.18.32.64`, `grpc-metadata.ps.svc`).
  Для нашего харнесса важен только GraphQL mesh.

**Чтобы реализовать:** добавить `SignRequest.buildPrepareDocumentSkl`/`buildProcessSignatureSkl` + `SignServiceImpl.PROCESS_OP_SKL` +
`processSignatureSkl` (генерить UUID requestId → prepare → xmlGuid → `SigningHelper.signXmlAndUpload` → process). `formType` уточнить под целевой документ СКЛ.

---

## 6. РПУ (архив) — эталон подключения (НЕ наш харнесс)

- `D:\rpu-persacc-autotestsPP`: **Kafka** через свойство `ps.events.kafka.url` (default `172.18.32.245:9092`);
  Spring `KafkaConfig`(`@EnableKafka`) + JUnit `ReadTopicExtension` (KafkaConsumer, protobuf-десериализатор), DTO `core.kafka.dto.*` из `.proto`.
- Бэкенд НЕ через GraphQL mesh, а через **gRPC-REST шлюз** `grpc-rest.<env>.ecp` (`RsConstants` `env.environment` default `uat`),
  `RestTemplate` trust-all SSL (`RsHttpClient`/`RsService`).
- Контраст: EVS / Архив / СКЛ используют **GraphQL mesh**; РПУ — **REST+gRPC шлюз**. Два разных паттерна.

---

## 7. Следующий шаг (когда скачают ЛК Страхователя backend)

1. В backend ЛК Страхователя найти `signDoc.object.xml` (или аналог) → подтвердить `prepareDocCommon`/`processSignatureCommon`
   и нужный `formType`/поля для СЗВ-М и остальных отчётов (СЗВ-ТД / DSO / СТАЖ / К).
2. Добить E2E СЗВ-М: поднять стенд + `kubectl port-forward … 20266:20266`, прогнать `SzvMXmlUploadSignTest`,
   проверить `prepareDocCommon.documents`, уточнить поле DA-загрузки (Allure-вложение), при необходимости сверить ГОСТ-wire с бэкендом.
3. Сделать тесты на остальные отчёты ЕВС: параметризовать `formType`/`formName` в `prepareDocCommon`/`processSignatureCommon`
   (скорее всего общий common-путь), добавить фасады `SzvReportsSteps` по аналогии с СЗВ-М.
4. (опц.) СКЛ: реализовать `processSignatureSkl` по п.5.

---

## 10. ЛК Страхователя — точный контракт подписания (backend, уточнён 27.08)

Источник: `D:\evs-oi-ipu-usv-front\src\main\resources\META-INF\conf\objects\signDoc.object.xml`
(сервис бэкенда `ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService`; имплементация —
`CommonReportSignatureServiceImpl extends SignatureConfigImpl` из общей либы `ru.gov.pfr.ecp.common.sign`).

- **`prepareDocInsurer` → GetPrepareDocRouter**: вход `itemId`, `mimeTypes: [MIME_TYPE_PROTO_XML]`;
  выход `requestId`, `documents[0].id`(xmlGuid), `documents[0].mimeType`, `documents[0].fileName`.
- **`saveSignInsurer` → GetSignatureDoneRouter**: вход `itemId`, `signedXmlFileRef`, `mimeTypes: [$$mimeType]`, `requestId`;
  выход `result`, `statusMessage`.
- **ВАЖНО:** `formName`/`formType` в CommonReport-операциях НЕ передаются — тип отчёта определяется бэкендом по `itemId`.

Прочие сервисы в `signDoc.object.xml` (НЕ основной путь): `ReportSignatureService` (`formType:$$docTemplate`, PDF),
`SupplierSignatureService` (поставщик АПСО/ТСР), `PrintingReportSignatureService` (initPrepareDoc+checkPrepareDoc+processSignatureDone, печатный вариант).

**Исправлено в AQA (27.08):** `SignRequest.buildPrepareDocCommon` больше не шлёт лишний `formName`
(добавляется только если задан явно); `ReportSignSteps.mimeType()` по умолчанию `MIME_TYPE_PROTO_XML`;
`reportTypeCode()` вынесен в свойство `evs.sign.report-type-code` (default `1`=СЗВ-М).

### Типы отчётов ЕВС (`ReportTypeDto` / `ExternalReportType` / `DocumentType`)
| Код (reportTypeCode) | Тип     | ExternalReportType            | DocumentType   |
|----------------------|---------|-------------------------------|----------------|
| 1                    | СЗВ-М   | SZVM (szv_m, 404)             | UOS_SZVM       |
| 2                    | СЗВ-ТД  | SZVTD (szv_td, 403)           | UOS_SZVTD      |
| 3                    | СЗВ-СТАЖ| SZVSTAJ (szv_staj, 400)       | UOS_SZVSTAJ    |
| 4                    | СЗВ-ИСХ | SZVISH (szv_ish, 401)         | UOS_SZVISH     |
| 5                    | ОДВ-1   | ODV_1                         | —              |
| 6                    | СЗВ-КОРР| SZVKORR (szv_korr, 402)       | UOS_SZVKORR    |
| 7                    | СЗВ-К   | SZVK (szv_k, 408)             | UOS_SZVK       |
| 8                    | ЕФС-1   | EFS_1                         | —              |
| 9                    | СЗВ-DSO | SZVDSO (szv_dso, 409)         | UOS_SZVDSO     |
| 10                   | 4-ФСС   | F4FSS                         | F4_FSS         |

Подписание для ВСЕХ этих отчётов — единый CommonReport-поток (тип по `itemId`); различаются только
создание отчёта (UI/загрузка XML) и `reportTypeCode` для агрегата `pfrEcpStorReportAggreServ`.

### Подключение ЛК Страхователя (service)
Kafka `kafka.bootstrap-address` default `172.18.32.223:9092` (test `172.18.32.245:9092`, uat `172.18.32.248:9092`),
gRPC к MDM (`mdm-grpc-url` test `172.18.32.178`/uat `172.18.32.64`, port 6565), GraphQL mesh `:20266`
(закомментировано в `application-uat.yml:79`: `graphql=http://graphql-mesh.ps.svc:20266/graphql`).
Для AQA-харнесса нужен только mesh `:20266`.

## 11. Тесты подписания в AQA (статус 27.08)

- `LK_Insurence/SZV_M/SzvMXmlUploadSignTest.java` — СЗВ-М: загрузка XML → API-подписание → проверка UUID/статуса.
  **Готов к прогону на стенде** (контракт выровнен под `CommonReportSignatureService`).
- `LK_Insurence/AllReportsSignTest.java` — **параметризованный** тест на 9 типов отчётов
  (СЗВ-М, СЗВ-ТД, СЗВ-СТАЖ, СЗВ-ИСХ, ОДВ-1, СЗВ-КОРР, СЗВ-К, ЕФС-1, СЗВ-DSO). Единый поток:
  `selectReportType(formType)` + `sendXml(resource)` + `signAndSendReport()` (API/xmldsig) + проверка статуса.
  Код типа отчёта передаётся через свойство `evs.sign.report-type-code` (1..9).
  4-ФСС (код 10) — отключённый TODO-тест (нет XML-фикстуры в репозитории).
- Общий фасад: `SzvReportsSteps` (authorize/addReports/selectReportType/sendXml…); подписание —
  `ReportSignSteps.signAndSendReport()` (универсально: тип из `itemId` + свойство `reportTypeCode`).
- Фикстуры XML: `src/test/resources/application/{szv-m,szv-td,Szv_Stazh,Szv_Ish,Szv_Corr,szv_k,Szv_Dso,odv_1,EFS}/…`
  (перечислены в `ReportXmlResource`; варианты `*_AF2` — относительные пути в репозитории, готовы к прогону).

### Команды прогона
**PowerShell-фикс:** аргументы `-D` манглятся без `--%` (получается "Unknown lifecycle phase").
ОБЯЗАТЕЛЬНО писать `mvn -o test --% -Dtest=... -Dservice.graphql-mesh.url=...`.

СЗВ-М (mesh через `ui.mesh-java.uat.ecp` за OAuth2/Keycloak; браузерный fetch):
```
mvn -o test --% -Dtest=SzvMXmlUploadSignTest ^
  -Devs.sign.mode=api -Devs.sign.transport=graphql -Devs.sign.xmldsig=true ^
  -Devs.sign.variant=common -Devs.sign.pem-dir=src/test/resources/certs ^
  -Devs.sign.graphql-via-browser=true ^
  -Devs.sign.graphql-via-browser-url=https://ui.mesh-java.uat.ecp/graphql ^
  -Devs.sign.graphql-basic=reader-user:reader-user ^
  -Devs.sign.xmldsig.upload-field=file -Dheadless=true
```
Все типы: `-Dtest=AllReportsSignTest` + `-Devs.sign.report-type-code=1..9`.

Браузерный same-origin GraphQL (mesh доступен через `ui.mesh-java.uat.ecp` за OAuth2/Keycloak):
`-Devs.sign.graphql-via-browser=true -Devs.sign.graphql-via-browser-url=https://ui.mesh-java.uat.ecp/graphql
-Devs.sign.graphql-basic=reader-user:reader-user`. `DaPortalClient.executeGraphQL` шлёт `fetch('/graphql')`
same-origin из аутентифицированной сессии (cookie `_ui_mesh_oauth2_proxy_0/1`).

## 8. Блокеры / открытые вопросы

- **ДОСТУП К MESH РЕШЁН (27.08), РАБОЧИЙ ПУТЬ — port-forward:** mesh-бэкенд `graphql-mesh` (namespace `ps`,
  порт `20266`) доступен напрямую через `kubectl port-forward` (есть kubeconfig с SA-токеном в namespace `ecp`,
  RBAC port-forward в `ps` разрешён). Поднимаем:
  ```
  $env:KUBECONFIG = "C:\Users\Tim\mesh-kubeconfig.yaml"
  kubectl -n ps port-forward svc/graphql-mesh 20266:20266   # фоново
  ```
  Далее mesh GraphQL доступен на `http://localhost:20266/graphql` и **НЕ требует авторизации**
  (проверено: `Invoke-WebRequest -Method Post -Body '{"query":"{__typename}"}'` → 200 `{"data":{"__typename":"Query"}}`).
  Тест в прямом режиме: `-Dservice.graphql-mesh.url=http://localhost:20266` (БЕЗ `-Devs.sign.graphql-via-browser`).
  Это обходит сломанный OAuth2/Envoy-фронт `ui.mesh-java.uat.ecp` (который сейчас 503 upstream error).
- kubeconfig сохранён: `C:\Users\Tim\mesh-kubeconfig.yaml` (токен SA `kubernetes-user`, кластер `172.18.33.0:6443`,
  context `mesh`, namespace `ecp`). Сервисы mesh в `ps`: `graphql-mesh` (20266), `graphql-mesh-java` (20266),
  `ext-graphql-mesh-java` (20266), `kb-graphql-mesh-java` (60266). Используем `graphql-mesh`.
- Путь через браузер (`ui.mesh-java.uat.ecp`) оставлен как запасной: `DaPortalClient.authenticateToMesh` проходит
  Keycloak (`reader-user`/`reader-user`) и ставит `_ui_mesh_oauth2_proxy_0/1`; `executeGraphQL` шлёт `fetch('/graphql')`
  same-origin. НЕ путать csrf-куку (`_ui_mesh_oauth2_proxy_csrf`) с сессионной. Сейчас фронт лежит (503), поэтому
  основной путь — port-forward + прямой режим.
- Имя поля DA-загрузки подписи подтверждено: бэкенд ждёт часть **`file`** (не `fileSig`) →
  `-Devs.sign.xmldsig.upload-field=file`.
- **НОВЫЙ БЛОКЕР (27.08):** в прямом режиме (port-forward) E2E падает ДО подписи — на `authorizeEVS`
  (ЛК Страхователя, строка 65 `SzvMXmlUploadSignTest`). Страница уходит на `sso-test.sfr.gov.ru/esia/callback`
  (ESIA/Keycloak SSO приложения). Это НЕ mesh — это логин в само приложение EVS. В via-browser прогонах
  `authorizeEVS` проходил (вероятно SSO-сессия ставилась через keycloak при `authenticateToMesh`), в прямом —
  нет. Возможно, нужно при старте теста тоже пройти keycloak SSO (reader-user) для сидинга сессии приложения,
  либо `authorizeEVS` флакает от окружения ESIA. ТРЕБУЕТСЯ уточнение у пользователя/стабилизация app-логина.

### Аутентификация mesh при прямом (port-forward) вызове — РЕШЕНИЕ НЕ НАЙДЕНО (27.08)
- `prepareDocCommon` (query) к `localhost:20266` проходит БЕЗ токена. `processSignatureCommon` (`GetSignatureDoneRouter`,
  mutation) требует токен: `UNAUTHENTICATED: Не передан токен доступа (в заголовке)`. Бэкенд ждёт `Authorization: Bearer`.
- Координаты Keycloak из редиректа прокси: realm=`geop`, client_id=`ui-mesh-java-debug-client`
  (`https://keycloak.dev.ecp/realms/geop/protocol/openid-connect/auth?...&client_id=ui-mesh-java-debug-client&...`).
- **ПОПЫТКИ (все неудачны):**
  - ROPC (`grant_type=password`) для `ui-mesh-java-debug-client` → 401 `unauthorized_client` (direct-grants выключены
    и/или клиент конфиденциальный, секрет недоступен).
  - `KeycloakTokenProvider` перебирает client_id: `admin-cli`/`security-admin-console` выдают валидный JWT
    (iss=`https://keycloak.dev.ecp/realms/geop`, `aud=` пусто, `azp=admin-cli`) — НО mesh отвергает (вероятно проверка
    `aud`/`azp` на свой клиент). `token-exchange` в `ui-mesh-java-debug-client` → 401 (нужен секрет клиента).
  - HttpOnly cookie `_ui_mesh_oauth2_proxy_0/1` (получен через `authenticateToMesh` в браузере, 10KB) → mesh НЕ читает
    куку: полная пара даёт HTTP 413 (header too large), только `_0` (3.9KB) → UNAUTHENTICATED. **Вывод: бэкенд mesh
    ожидает Bearer-JWT, а не session-cookie прокси.** Ходить в mesh через UI за кукой — ТУПИК.
- app-токен из браузера (`extractAppToken`) НЕ находится: токен приложения EVS лежит в HttpOnly-cookie (`.ecp-test.sfr.gov.ru`),
  через `document.cookie`/`localStorage` недоступен; через `context().cookies()` видны только `_oauth2_proxy` EVS-приложения
  (отдельный прокси `.ecp-test.sfr.gov.ru`, НЕ mesh).
- **РЕШЕНО (27.08.2026):** mesh при port-forward `:20266` принимает **Bearer-JWT**, полученный у Keycloak через ROPC на
  client `admin-cli` (юзер из `-Devs.sign.graphql-basic`, по умолчанию `reader-user`). `KeycloakTokenProvider.getAccessToken()`
  отдаёт такой токен (`iss=.../realms/geop`, `aud=` пусто, `azp=admin-cli`) — mesh его принимает ДЛЯ МУТАЦИЙ
  (processSignatureCommon с ним возвращает 200, а не UNAUTHENTICATED).
  - ВАЖНО: gateway mesh НЕ извлекает токен из session-cookie. Если слать cookie ОДНОВРЕМЕННО с Bearer — gateway пытается взять
    токен из cookie, падает и игнорирует Bearer → UNAUTHENTICATED. Поэтому шлём **только Bearer** (cookie — только fallback).
  - `GraphQLClient.executeMutation` переписан на **raw `HttpURLConnection`** (RestAssured почему-то не доставлял `Authorization`
    mesh-бэкенду: query без токена шёл, мутация с Bearer падала на UNAUTHENTICATED; raw HttpURLConnection с тем же Bearer → 200).
- **НЕ РЕШЕНО / заблокировано:** `authorizeEVS` (ESIA/Keycloak SSO логин страхователя) — флаковый (~50% падает на
  `sso-test.sfr.gov.ru/esia/callback`, «поле не найдено»). Это отдельный блокер полного E2E; не мешает mesh-авторизации.
- ГОСТ-wire-формат — сверить с бэкендом, если подпись отклонится (сейчас по RFC 6986).
- `formType` для СКЛ и других отчётов ЕВС — уточнить по backend-объектам.

---

## 9. Свойства запуска (итог)

`-Devs.sign.mode=api` · `-Devs.sign.transport=graphql|rest` · `-Devs.sign.xmldsig=true|false`
· `-Devs.sign.variant=common|printing` · `-Devs.sign.pem-dir` · `-Devs.sign.xmldsig.upload-field=file` (бэкенд ждёт `file`, не `fileSig`)
· `-Dservice.graphql-mesh.url`.
Сборка: `mvn -o test-compile`. ГОСТ self-check: `XmlDsigSignerSelfCheck`.

---

## 10. СЭДО gateway REST (шлюз к DA, проект `D:\sedo-gateway-rest`)

> Пользователь дал проект «СЭДО gateway» как эталон того, как в их экосистеме делается авторизация/подпись
> перед DA. Это ОТДЕЛЬНЫЙ сервис (СЭДО/Sedo), но его модель авторизации — чистый REST+JWT, возможно применим
> как альтернатива/аналог mesh-GraphQL для E2E. Зафиксировано 27.08.2026.

### 10.1 Где и что
- Репозиторий: `D:\sedo-gateway-rest` (Spring Boot, `server.port=28602`, context `/` → ingress host шаблонизируется).
- Шлюз стоит ПЕРЕД DA: `da-client.webclient.url = http://172.18.32.31:28080/api` (см. `application.yml` / `values.yaml`).
  Также ходит в `sedo-core.dev.ecp` (SOAP) и `cert-manager-rest.dev.ecp` (проверка ЭП).
- Swagger UI включён (`springdoc.swagger-ui.enabled`).

### 10.2 Авторизация (ключевое)
- `POST auth` (`application/x-www-form-urlencoded`, `permitAll`) — см. `SedoController.auth` → `AuthService.auth`.
  - Вход (`AuthorizationRequestDto`): `client_id` (UUID оператора), `request_id`, `timestamp`, `secret`.
  - `secret` = **CMS (PKCS#7) подпись** строки `client_id:request_id:timestamp` сертификатом УКЭП оператора.
  - Проверка: `certManGrpcService.parseAndCheckSecret(request)` (через cert-manager), либо ПРОПУСКАЕТСЯ, если
    `security.check-secret-disable=true` (тогда оператор берётся из БД по `client_id`, подпись не проверяется).
  - Успех → `AuthorizationResponseDto.access_token` = **JWT (HS256)**, `subject = client_id`.
- **JWT** (`JwtTokenProvider`): HS256, ключ = `security.secret`. В `application.yml` СЕКРЕТ ЗАХАРДКОЖЕН:
  ```
  security.secret: Xs18DmMnO+9dTMePAIRchpBWbEB2dg9+YGMs6f8sdz/XdI09Ppa/uIsUO9Ao2sZ7QQSJSrfOqK9O88iXeS0fYg
  ```
  `security.token.expiry = 36000` (10ч). `security.superuser-clientid = f143baec-28f6-44ce-9206-abb9140b8f89`.
  ⇒ JWT можно СГЕНЕРИТЬ САМОСТОЯТЕЛЬНО (HS256 + этот secret, subject=любой client_id) БЕЗ вызова `/auth`.
- Все остальные эндпоинты требуют `Authorization: Bearer <jwt>` (`JwtTokenFilter` через `DefaultBearerTokenResolver`):
  валидирует HS256, достаёт `subject` (client_id), грузит оператора.

### 10.3 Эндпоинты (полезные для E2E/подписи)
- `POST auth` — получить JWT (см. 10.2).
- `POST push` (`multipart/form-data`, headers `Content-MD5` + `Document-Type`, Bearer) — загрузка пакета документов
  (`PackageService.pushPackage`). Это аналог DA-загрузки подписанного файла.
- `GET pckg`, `GET pckg/{package_id}`, `GET pckg/last`, `GET pckg/test/{processId}`, `GET news` — статусы пакетов.
- `POST debug/md5checksum`, `debug/encrypt`, `debug/encrypt-default`, `debug/decrypt`, `debug/verify-signature`,
  `debug/sign`, `debug/generate-secret` (`DebugUtilsController`) — УТИЛИТЫ КРИПТО:
  - `debug/sign` — подписать файл (CMS) на стороне шлюза;
  - `debug/generate-secret` — сформировать `secret` (CMS-подпись `client_id:request_id:timestamp`) для `/auth`;
  - `debug/verify-signature` — проверить XML-подпись. Удобно для offline-самопроверки ГОСТ/xmldsig в тестах.

### 10.4 Конфиг/деплой
- Env (helm `deployment.yaml`): `TOKEN_EXPIRY`, `CHECK_SECRET_DISABLE` (= `security.checkSecretDisable`, в values `false`),
  `DA_CLIENT_URL`, `SEDOC_CORE_URL`, `CERT_THUMBPRINT`, `CERT_MANAGER_REST_URL`, `SECURITY_SECRET`(?) — секрет реально
  приходит из **Vault**: `vault.enable=true`, `role=devk8s-ecp`, `path=devk8s/data/ecp/open-api/sedo-gateway-rest`
  (args: `source /vault/secrets/config && java ...`). Т.е. `security.secret` в проде — из Vault, но в `application.yml` (dev) он есть.
- `kafka.bootstrap-address`, `grpc` (cert-manager), БД Postgres `evs_api_rest_dev`.

### 10.5 Гипотеза применимости к нашей задаче (EVS СЗВ-М)
- СЭДО-gateway — это «правильный» REST-путь к DA (то же, что mesh-GraphQL, но проще). Если E2E нужно вести не через
  mesh-GraphQL, а через СЭДО-gateway: (a) получить JWT — либо `POST auth` с CMS-`secret` (для `secret` годится
  `debug/generate-secret`, а подписать можно нашим `XmlDsigSigner`/PFX), либо при `CHECK_SECRET_DISABLE=true` — просто
  `client_id+request_id+timestamp`+любой `secret`; либо сгенерить JWT самим по HS256-secret из 10.2; (б) слать `Bearer`
  на `push`/другие эндпоинты.
- Для mesh-GraphQL (текущий путь) СЭДО-секрет НЕ нужен — там свой Keycloak (`realm=geop`, `ui-mesh-java-debug-client`),
  и рабочий токен уже получен (см. раздел 8/выше).

### 10.6 РЕШЕНИЕ (27.08): подписание переведено на СЭДО — НО только шаг подписи+загрузки
- Пользователь выбрал: **оставить EVS `prepareDocCommon` + `processSignatureCommon` (GraphQL, admin-cli Bearer)**,
  а шаг «подписать XML → загрузить в DA → signXmlGuid» перевести на СЭДО-gateway.
- Реализовано (`-Devs.sign.transport=sedo`):
  - `service/sedo/SedoGatewayClient.java` — JWT (HS256, минтуем сами по `security.secret`), `signFile` (`/debug/sign`,
    серверная CMS cert-manager), `verifySignature` (`/debug/verify-signature`), `pushPackage` (`/push`), `getPackage`.
  - `SigningHelper.signXmlAndUploadViaSedo` → `DaPortalClient.uploadSedoSignature`: СЭДО подписывает CMS, сертификат/алгоритм
    извлекаются из CMS (BouncyCastle), загрузка в DA тем же контрактом `/store/signature` (format=cms), возвращается sigGuid.
  - `ReportSignSteps.signCurrentReportViaApi` маршрутизирует `transport=sedo` на GraphQL-prepare/process + СЭДО-sign/upload.
- **РИСК (не закрыт)**: EVS `processSignatureCommon` по контракту ждёт **xmldsig**, а СЭДО `/debug/sign` выдаёт **CMS**.
  На первом прогоне (`transport=sedo`) проверить, примет ли бэкенд CMS-подпись в `signedXmlFileRef`. Если нет — либо
  (а) СЭДО умеет xmldsig (добавить ветку), либо (б) вернуть локальный `XmlDsigSigner` для байтов, а СЭДО оставить только
  для auth/verify (флаг `evs.sign.xmldsig=true` откатывает на локальный xmldsig внутри sedo-транспорта — пока НЕ сделано).
- Запуск требует port-forward СЭДО-gateway (`kubectl -n … port-forward … 28602:28602`) и доступности DA из шлюза.

---

## 12. ПРОРЫВ (29.08): рабочий путь подписания — n2o `syncSign` через ЕСИА-сессию

### 12.1 Корень проблемы graphql-транспорта
- `processSignatureCommon` (mutation `GetSignatureDoneRouter`) на **реальном** отчёте падает
  `UNAUTHENTICATED: Недостаточно прав … (или нет доступа)` ДЛЯ reader-user (admin-cli Bearer).
- ДОКАЗАНО (29.08, `MeshBearerProbeTest`): та же мутация с **dummy itemId** (несуществующий) →
  `200` + ошибка ВАЛИДАЦИИ аргументов (а НЕ auth) ⇒ проверка доступа/владения проходится, падает
  только ресурсная проверка **владения отчётом**. Отчёт создаётся ESIA-пользователем, а reader-user
  им не владеет.
- token-exchange в `ui-mesh-java-debug-client` (нужная audience) → 401 (нет client_secret). Токен
  реального ESIA-пользователя лежит в encrypted oauth2-proxy cookie `_oauth2_proxy_0/_1`
  (domain `.ecp-test.sfr.gov.ru`) и HttpOnly → как raw JWT не извлекается; эта cookie НЕ валидируется
  gateway `localhost:20266` (другой proxy-инстанс). ⇒ прямой mesh-вызов от имени владельца невозможен.

### 12.2 Решение: n2o `syncSign` выполняется СЕРВЕРОМ от имени ЕСИА-владельца
- Страница подписи отчёта (`/reports/svzm/{id}/mainInfo/sign`) в n2o вызывает
  `syncPrepare`/`asyncPrepare` (подготовка doc) и `syncSign` (`n2o/data/.../mainInfo/sign/syncSign`,
  POST) — это фронтовая обёртка над тем же `processSignatureCommon`, но **сервер выполняет мутацию
  в своей ESIA-сессии** (cookie `_oauth2_proxy` валидна для прокси приложения `ecp-test.sfr.gov.ru`).
  ⇒ ресурсная проверка владения пройдена.
- Поэтому: вызываем `syncSign` ИЗ БРАУЗЕРНОЙ СЕССИИ (`DaPortalClient.postPortalJson`, cookie уходит
  автоматически) — владелец = залогиненный ESIA-юзер ⇒ мутация проходит.
- Контракт `syncSign` (из дампа n2o-конфига страницы подписи): модель `{mimeType, formName, itemId,
  requestId, xmlGuid, signXmlGuid, mime_types:[...]}`; `s3BaseUrl=https://ecp-test.sfr.gov.ru/da`;
  поля `xmlGuidFieldId="xmlGuid"`, `signXmlGuidFieldId="signXmlGuid"`. Т.е. сервер ждёт **готовый
  DA-guid подписанного документа** (`signXmlGuid`), а не сырую подпись — ровно то, что даёт
  `DaPortalClient.uploadSignature`/`uploadXmlSignature`.

### 12.3 Что реализовано в `ReportSignSteps.signCurrentReportViaRestApiLegacy` (29.08)
- **default transport = `rest`** (`evs.sign.transport`, было `graphql`). Гибрид:
  1) `prepareDocCommon` (GraphQL mesh, reader-user, **READ** — без проверки владения) → `xmlGuid`;
  2) `da.loadFile(xmlGuid)` → байты;
  3) подпись → `da.uploadSignature`/`uploadXmlSignature` → `signXmlGuid`
     (формат — свойство `evs.sign.n2o-signature-format`, default `xmldsig`, альт. `cms`);
  4) `da.postPortalJson("/insurer/n2o/data{reportPath}/sign/syncSign", body)` — **ESIA-сессия**.
- Исправлены баги, из-за которых `rest` был «неправильным»:
  - URL `syncSign`/`asyncPrepare` НЕ хватало сегмента **`/sign`** (было `.../mainInfo/syncSign`,
    реально `.../mainInfo/sign/syncSign`) → 404.
  - `JS_POST_JSON`/`JS_GET_JSON` в `DaPortalClient` переписаны на **XMLHttpRequest** (не `window.fetch`):
    фронт EVS монки-патчит `window.fetch` обёрткой, которая дёргает `.json()` и падает
    `Unexpected end of JSON input` на пустых телах (condition_4_polling). XHR обёрткой не перехватывается.
- `asyncPrepare` + поллинг `condition_4_polling` **НЕ применимы** (500 standalone): n2o async-поллинг
  требует async-task-контекст, который ставит только виджет digSign. Поэтому `xmlGuid` берём через
  GraphQL `prepareDocCommon` (READ), а финальную ЗАПИСЬ — через n2o `syncSign` (ESIA-сессия).

### 12.4 Открытые вопросы / риски (нужен прогон на стенде, mesh :20266 + логин подняты)
- **Формат подписи для `syncSign` не подтверждён**: default `xmldsig` (требование бэкенда
  `processSignatureCommon`), но digSign/КриптоПРО часто шлёт detached **CMS**. Если `syncSign` упадёт
  с ошибкой формата → переключить `evs.sign.n2o-signature-format=cms`.
- `prepareDocCommon` требует mesh `:20266` (port-forward). В песочнице 29.08 mesh был временно DOWN —
  проверить на стенде.
- `authorizeEVS` (ESIA SSO) — транзиентно флаковый (~50%) на `sso-test.sfr.gov.ru/esia/callback`;
  не硬 блокер, но при падении перезапустить тест.
- ГОСТ-wire для xmldsig — как и ранее, сверить с бэкендом при отклонении.

### 12.5 Команда прогона (рабочий путь, 29.08)
```
mvn -o test --% -Dtest=SzvMXmlUploadSignTest `
  -Devs.sign.mode=api -Devs.sign.transport=rest `
  -Devs.sign.pem-dir=src/test/resources/certs -Dheadless=true
# опц. смена формата подписи: -Devs.sign.n2o-signature-format=cms
# mesh :20266 должен быть доступен (port-forward), логин ESIA — пройден.
```
