# План: Автоматизация ЭЦП и E2E-тесты ЛК Страхователя

## Текущее состояние (26.08.2026)

### Что сделано
- Изучена архитектура подписания в проекте (2 пути: GraphQL/Camunda и REST+CMS)
- Реализован API-путь подписанияSZV-M через REST (`ReportSignSteps.signCurrentReportViaApi()`)
- Настроен ГОСТ-криптопровайдер (BouncyCastle): загрузка PEM-сертификата + ключа, детерминация алгоритма
- Написан `PfxCmsSigner` — CMS/PKCS#7-подпись через BouncyCastle (GOST R 34.10-2012-256)
- Написан `DaPortalClient` — клиент Document API (через `page.evaluate` + `fetch` в браузерной сессии)
- Написан `SelfSignedCertGenerator` — генерация тестовых сертификатов с реквизитами ЕВС
- Интегрированы PEM-файлы Кривоносова (`krivonosov.cer.pem` / `krivonosov.key.pem`)
- Исправлен баг: обработка ошибок `syncSign` теперь ловит HTTP 400/500 и `severity:danger`

### Текущий блокер
- Бэкенд-разработчик сообщил: **текущий подход (REST+CMS) — неправильный**
- Правильный путь: **подписание через GraphQL/Camunda** с форматом **xmldsig**
- Ошибка `"Участники должны совпадать с документом"` — артефакт неправильного API-вызова
- Завтра получим вводные по GraphQL-запросам для подписания

### Ключевые файлы
| Файл | Назначение |
|------|-----------|
| `service/sign/PfxCmsSigner.java` | ГОСТ CMS-подпись (BouncyCastle) |
| `service/sign/DaPortalClient.java` | DA-клиент (браузерная сессия) |
| `service/sign/SelfSignedCertGenerator.java` | Генерация тестовых сертификатов |
| `service/graphql/SignServiceImpl.java` | GraphQL/Camunda-подписание (эталонный путь) |
| `service/graphql/SignRequest.java` | Построение GraphQL-мутации |
| `service/graphql/GraphQLClient.java` | HTTP-транспорт для GraphQL |
| `steps/ReportSignSteps.java` | Шаги подписания отчётов |
| `steps/SzvReportsSteps.java` | Фасад шаговSZV-M |
| `LK_Insurence/SZV_M/SzvMXmlUploadSignTest.java` | E2E-тестСЗВ-М |

---

## План: 27.08.2026

### Утро: Получение вводных

**Вводные от бэкенда (получено 27.08):**

> Источник: `evs-oi-ipu-usv-front` →
> `src/main/resources/META-INF/conf/objects/signDoc.object.xml#L133`
> (фронт ЛК Страхователя, модуль отчётов).

Подписание отчётов идёт через n2o-объект `signDoc`, операции:

**Без печати (основной путь СЗВ-М):**
- `prepareDocCommon` — подготовить документ (аналог syncPrepare; вернёт то, что нужно подписать)
- `processSignatureCommon` — обработка УЖЕ подписанного документа (аналог syncSign)

**С печатью (опционально):**
- `initPrepareDocPrinting` — запрос подготовки печатной формы
- `checkPrepareDocPrinting` — опрос результата (вызывать, пока не придут документы)
- `processSignaturePrinting` — обработка подписанного документа
- `initVisualizeDocPrinting` — запрос визуализации (после подписания); если PDF с подписью не нужен — НЕ вызывать
- `checkVisualizeDocPrinting` — опрос результата печати после подписания

**Чек-лист получения (статус):**
- [x] Методы подписания от бэкенда получены (имена + семантика)
- [x] Точный GraphQL-запрос `prepareDocCommon`/`processSignatureCommon` — получен из `signDoc.object.xml`
- [x] Структура `processSignatureCommon` — `signedXmlFileRef` = guid подписанного файла
- [x] Схема xmldsig: **xmldsig**, подпись генерирует **тест** (`XmlDsigSigner`, Santuario); порт mesh — **тот же** (`20266`)
- [x] Адрес GraphQL-эндпоинта: mesh `:20266` (как у архивов), подтверждено бэкендом
- [~] Контракт **загрузки** xmldsig в DA (`signedXmlFileRef`) — не подтверждён; заложен best-guess + вынесен в свойства

> ⚠️ Текущий `SignRequest.buildSignMutation` использует мутацию из ДРУГОГО модуля
> (`...ProezdClaimBoClientService_...OnSignTaskComplete`) — для отчётов НЕ подходит,
> заменён на операции `signDoc` (`prepareDocCommon`/`processSignatureCommon`).

> **Локальная проверка (27.08):** round-trip xmldsig проверен через Santuario —
> `refsValid=true, sigValid=true`. Генерация подписи корректна (RSA-путь; ГОСТ — те же
> константы). Остался только контракт загрузки подписанного XML в DA.

### День: Переделка подписания СЗВ-М на GraphQL
- [x] Получить из `signDoc.object.xml` точные GraphQL-операции
- [x] `SignRequest`: добавлены `buildPrepareDocCommon` / `buildProcessSignatureCommon`
- [x] `SignService`/`SignServiceImpl`: `prepareDocCommon` (→ requestId+xmlGuid) и `processSignatureCommon` (signedXmlFileRef)
- [x] `ReportSignSteps.signCurrentReportViaApi()`: переключено на GraphQL-путь; старый REST+CMS вынесен в `signCurrentReportViaRestApiLegacy()` (fallback через `-Devs.sign.transport=rest`)
- [x] **xmldsig**: добавлена зависимость `org.apache.santuario:xmlsec:3.0.4` и `XmlDsigSigner` (enveloped xmldsig, ГОСТ/RSA auto-detect); режим через `-Devs.sign.xmldsig=true` (по умолчанию), fallback на CMS при `false`
- [ ] **ОТКРЫТО: контракт загрузки xmldsig в DA** — см. ниже
- [ ] Прогон теста на стенде (нужен mesh/port-forward `:20266`)

> **Риск транспорта:** операции `ruGovPfrEcpEvsDaIpuService_..._GetPrepareDocRouter` —
> предполагается, что они на том же `graphql-mesh:20266`, что и архивный путь
> (согласно PENDING_ISSUES: «api-режим упирается только в отсутствие mesh»).
> Подтверждено бэкендом: «порт тот же». Если mesh их не отдаёт, нужно слать в
> n2o-graphql портала (в браузерной сессии, как `DaPortalClient`).

#### Статус контракта (подтверждён бэкендом 27.08)
Бэкенд ответил на открытые вопросы:
- **Формат** — **xmldsig**, **enveloped**, **KeyInfo/X509Data/X509Certificate** обязателен (#3,#4).
- **Загрузка в DA** (#5,#6): «просто загрузи через `api/sk/api/store`», поле `format` НЕ нужно.
  → `DaPortalClient.uploadXmlSignature` теперь шлёт `POST /da/sk/api/store` (свойство
  `-Devs.sign.xmldsig.upload-url`), multipart (поле `-Devs.sign.xmldsig.upload-field`,
  default `fileSig`), metadata **без format**. Точное имя поля уточнится на первом прогоне.
- **Оркестрация** (#8): для СЗВ-М — **только `prepareDocCommon` + `processSignatureCommon`**
  (common-вариант, наш default). printing-путь оставлен как fallback (`-Devs.sign.variant=printing`).
- **mimeType prepare** (#10): «просто ставь PDF» → `MIME_TYPE_PROTO_PDF`; другие значения не влияют.
- **Визуализация** (#11): не нужна (опциональна).
- **mesh** (#12): `graphql-mesh:20266`, POST.

**ГОСТ-блокер СНЯТ (реализовано + офлайн-проверено 27.08).**
Проблема: `xmlsec:3.0.4` не содержит ГОСТ-реализаций. Реализован **ручной enveloped-xmldsig
по RFC 6986** в `XmlDsigSigner.signEnvelopedGost`: canonicalizer `exc-c14n` из Santuario +
BouncyCastle `GOST3411-2012-256` (дайджест) и `ECGOST3410-2012-256` (подпись). Подпись
BC отдаёт ровно 64 байта raw r‖s (big-endian) — формат RFC 6986. Офлайн-самопроверка
(`XmlDsigSignerSelfCheck`, реальный cert `krivonosov`): `sigValid=true`, `refDigestValid=true`.
URI оставлены стандартные RFC 6986 (`gostr34102012-gostr34112012-256` / `gostr34112012-256` /
`exc-c14n#`). Идеально было бы сверить с эталоном бэкенда, но они ответили «подпись
формирует только сервер VipNet, подглядеть негде» — поэтому ориентир = стандарт RFC 6986.
RSA-путь (Santuario) тоже работает (round-trip `sigValid=true`).

**Что осталось для реального прогона E2E:**
1. Поднять стенд + `kubectl port-forward … 20266:20266` (mesh) — из этой среды недоступно.
2. Уточнить на первом прогоне точное имя поля загрузки в DA (Allure-вложение `DA upload XMLDSIG`)
   и что `prepareDocCommon.documents` действительно содержит XML для подписи при `mimeTypes=PDF`.
3. (Опц.) Сверить ГОСТ-wire-формат с бэкендом, если подпись отклонится — но реализация следует RFC 6986.

> **Расхождение версий `signDoc.object.xml` (найдено 27.08 в D:\evs-oi-ipu-usv-front):**
> - **GitLab (бэкенд, строка 133)** — `prepareDocCommon` + `processSignatureCommon`
>   (сервис `CommonReportSignatureService`), prepare возвращает `requestId`+`documents` сразу.
> - **Локальный диск** — `initPrepareDoc` + `checkPrepareDoc` (**поллинг!**) + `processSignatureDone`
>   (сервис `PrintingReportSignatureService`); именно они прописаны в `readSzvm.page.xml`.
> Код сейчас реализует ОБА варианта и переключается свойством `-Devs.sign.variant`:
> `common` (по умолчанию — GitLab, `prepareDocCommon`+`processSignatureCommon`, prepare синхронный)
> или `printing` (локальный диск — `initPrepareDoc`+`checkPrepareDoc` с поллингом + `processSignatureDone`).
> Свойство: `-Devs.sign.variant` (`common` по умолчанию).
> Для `printing` `formName` игнорируется, mimeType жёстко `MIME_TYPE_PROTO_PDF` (как в page.xml СЗВ-М).
> Если стенд поднят с локальной версией — достаточно `-Devs.sign.variant=printing`.

#### Источник: как фронт получает signedXmlFileRef
Подпись делает платформенный виджет n2o `dig-sign`/`dig-sign-modal` (конфиг в page.xml,
напр. `readSzvm.page.xml:218`). Сама выгрузка подписанного файла в DA и получение guid —
**внутри платформы**, в репозитории фронта только n2o-конфиги (JS-логики подписи нет).
Поэтому точный endpoint/формат загрузки отсюда не извлекается; используется best-guess
+ настраиваемые свойства, и контракт вскроется на первом прогоне на стенде (Allure-вложения
запроса/ответа DA).

### День: Переделка подписанияSZV-M на GraphQL
- [ ] Адаптировать `ReportSignSteps.signCurrentReportViaApi()` под GraphQL/Camunda
- [ ] Перехват ответа `/syncPrepare` (как в `ArchiveSteps.apiSign()`)
- [ ] Вызов `SignServiceImpl.signDocument()` или новой мутации
- [ ] Убрать/отключить REST+CMS путь (оставить как fallback для отладки)
- [ ] Прогон теста с новым flow

---

## План: Итерация 2 — Покрытие всех тестов ЭЦП

### Тесты, требующие подписания
| Тест | Описание | Статус |
|------|----------|--------|
| `SzvMXmlUploadSignTest` | СЗВ-М: загрузка + подписание + проверка UUID в таблице | ⏳ Переделка на GraphQL |
| Архивные запросы | Через `ArchiveSteps.apiSign()` | ✅ Уже через GraphQL |
| СЗВ-ТД | Отчёт о трудовой деятельности | ❌ Нет теста |
| СЗВ-DSO | Отчёт по досрочному выходу на пенсию | ❌ Нет теста |
| СЗВ-СТАЖ | Отчёт о стаже | ❌ Нет теста |
| СЗВ-К | Корректирующий отчёт | ❌ Нет теста |

### Задачи
- [ ] Переделать все тесты с ЭЦП на GraphQL/Camunda flow
- [ ] Создать базовый класс/фасад для подписания любых отчётов
- [ ] Параметризовать: тип отчёта, форма, mimeType

---

## План: Итерация 3 — E2E и полный бизнес-процесс

### E2E-цепочка для каждого типа отчёта
```
1. Авторизация (ЕСИА или прямой вход)
2. Навигация: Отчёты → Выбор типа → Новый отчёт
3. Заполнение / Загрузка XML
4. Валидация данных на форме (ИНН, СНИЛС, период, ЗЛ)
5. Сохранение черновика
6. Подписание (GraphQL/Camunda)
7. Проверка UUID процесса
8. Проверка статуса в таблице ("Подписан", "Отправлен")
9. Проверка вкладок: Данные страхователя, Список ЗЛ, Реквизиты
10. Проверка истории/журнала
```

### Задачи
- [ ] Разбить E2E на переиспользуемые шаги (`@Step`)
- [ ] Добавить проверки на вкладках:
  - [ ] **Данные страхователя**: ИНН, КПП, регномер, наименование, ОГРН
  - [ ] **Список ЗЛ**: ФИО, СНИЛС, количество лиц
  - [ ] **Реквизиты**: период, тип формы, дата заполнения
  - [ ] **Подписание**: наличие ЭЦП, алгоритм,.subject сертификата
  - [ ] **История/журнал**: trace-id, статусы обработки
- [ ] Проверка UUID после подписания:
  - [ ] UUID валиден (regex)
  - [ ] UUID присутствует в таблице отчётов
  - [ ] Статус строки = "Подписан и отправлен" / "Принят"
- [ ] Negative-тесты:
  - [ ] Попытка подписания без загрузки XML
  - [ ] Попытка двойного подписания
  - [ ] Невалидный XML (нарушение XSD)
  - [ ] Истёкший сертификат

---

## План: Итерация 4 — Интеграционные тесты

### Уровни тестирования
```
Level 1: Unit-тесты
  - PfxCmsSigner: генерация CMS-подписи (GOST, RSA)
  - SelfSignedCertGenerator: генерация сертификатов
  - SignRequest: построение GraphQL-мутации
  - DaPortalClient: парсинг ответов, извлечение sigGuid

Level 2: Интеграционные тесты (с壁дом)
  - GraphQL: мутация SignDocument через graphql-mesh
  - DA: upload/load через Document API
  - gRPC: connectivity checks (metadata, sequence, noble-registry, transact-manager)

Level 3: E2E тесты (браузер + стенд)
  - Полная цепочка: авторизация → отчёт → подписание → проверка
  - Разные типы отчётов (СЗВ-М, СЗВ-ТД, СЗВ-DSO, ...)
  - Разные режимы подписания (GraphQL API, UI/CryptoPro)
```

### Задачи
- [ ] Level 1: Unit-тесты крипто-модуля
  - [ ] Тест CMS-подписи: подписать → верифицировать
  - [ ] Тест PEM-загрузки: разные форматы ключей
  - [ ] Тест определения алгоритма (GOST vs RSA)
- [ ] Level 2: Интеграционные
  - [ ] GraphQL health check (все сервисы)
  - [ ] DA store operations (load, upload, signature)
  - [ ] Camunda decision flow
- [ ] Level 3: E2E
  - [ ] Параметризованные тесты: `@ParameterizedTest` с разными типами отчётов
  - [ ] Parallel execution (JUnit 5)

---

## План: Итерация 5 — Расширение покрытия

### Все вкладки ЛК Страхователя
| Вкладка | Тесты | Проверки |
|---------|-------|----------|
| Главная | Dashboard | Наличие виджетов, уведомления |
| Отчёты | Список отчётов | Фильтрация, пагинация, сортировка |
| Отчёты → Детали | Данные страхователя | ИНН, КПП, ОГРН, адрес |
| Отчёты → Детали | Список ЗЛ | ФИО, СНИЛС, дата рождения |
| Отчёты → Детали | Подписание | ЭЦП, статус, UUID |
| Отчёты → Детали | История | Журнал событий, trace-id |
| Загрузка отчётов | Upload XML | Валидация, XSD, дубли |
| Настройки | Профиль | Реквизиты организации |
| Обращения | Заявки | Статус, ответы |
| Календарь | Сроки | Ближайшие дедлайны |
| Уведомления | Сообщения | Прочитанные/непрочитанные |

### Метрики качества
- [ ] Покрытие всех критических бизнес-процессов
- [ ] Каждый тип отчёта: минимум 1 E2E-тест
- [ ] Каждая вкладка: минимум 2 проверки данных
- [ ] Negative: минимум 3 негативных сценария на модуль
- [ ] Allure-отчёты с шагами, скриншотами, вложениями

---

## Архитектурные решения

### Подписание: GraphQL/Camunda (целевой путь)
```
1. Загрузить XML → получить xmlGuid из syncPrepare
2. Подписать через GraphQL: SignServiceImpl.signDocument(...)
3. Проверить статус через syncSign/syncStatus
4. Проверить UUID в UI
```

### Подписание: REST+CMS (текущий, для отладки)
```
1. syncPrepare → xmlGuid, requestId
2. DA load → скачать XML
3. CMS sign → подпись (BouncyCastle/GOST)
4. DA upload → sigGuid
5. syncSign → результат
```

### Тест-структура
```
src/test/java/com/bft/
├── LK_Insurence/
│   ├── SZV_M/           # Тесты СЗВ-М
│   ├── SZV_TD/          # Тесты СЗВ-ТД (план)
│   ├── SZV_DSO/         # Тесты СЗВ-DSO (план)
│   └── Common/          # Общие E2E-сценарии (план)
├── steps/               # Переиспользуемые шаги
├── service/             # Сервисы (GraphQL, DA, gRPC, sign)
├── ui/pages/            # Page Objects
└── test/examples/       # Обучающие примеры
```

---

## Приоритеты на ближайшую неделю

| День | Задача |
|------|--------|
| 27.08 (ср) | Получить GraphQL-вводные, переделать СЗВ-М |
| 28.08 (чт) | E2E СЗВ-М: полный проход + все проверки |
| 29.08 (пт) | E2E СЗВ-ТД + СЗВ-DSO (если есть стенды) |
| 01.09 (пн) | Unit-тесты крипто-модуля, GraphQL health |
| 02.09 (вт) | Покрытие вкладок: страхователь, ЗЛ, реквизиты |
| 03.09 (ср) | Negative-тесты, edge cases |
| 04.09 (чт) | Allure-отчёты, метрики, документация |
