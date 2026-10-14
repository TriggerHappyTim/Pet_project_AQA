# PW_GUIDE — обёртка над Playwright (`com.bft.pw`)

`com.bft.pw` — **собственная** Selenide-подобная обёртка над Playwright.
Selenide как зависимость **не подключён** и подключать его не нужно: класс `Selenide`
внутри этого пакета — это наш адаптер, а не библиотека `com.codeborne.selenide`.

Зачем обёртка:

- единый стиль взаимодействия с UI (`$`, `$$`, `shouldBe`, `Condition`) вместо прямого Playwright API;
- умные ожидания с polling (никаких `Thread.sleep` в тестах);
- автогенерация понятных ошибок и скриншотов/трейсов при падении;
- изоляция сессии на уровень теста (`PwSession`).

> Источник истины по API — сам код: `src/test/java/com/bft/pw/`.

## Состав пакета

| Класс | Назначение |
|---|---|
| `Selenide` | Точка входа: `$(...)`, `$$(...)`, `$x(...)`, `open(...)`, `screenshot(...)`, `switchTo()`, `actions()`. |
| `SelenideElement` | Элемент: действия (`click`, `setValue`, …), проверки (`shouldBe`), чтение (`getText`, `getValue`). |
| `ElementsCollection` | Коллекция элементов: `first()`, `last()`, `texts()`, `filter(...)`, `shouldHave(...)`. |
| `PwElement` | Низкоуровневая обёртка над `Locator`; возвращает `SelenideElement` (обычно напрямую не нужен). |
| `PwWait` | Умное ожидание: `until(...)` с таймаутом и polling. |
| `Condition` / `CollectionCondition` | Условия для элементов и коллекций. |
| `PwSession` | Жизненный цикл сессии: page/context, трейсы, скриншоты, диалоги, данные подписания. |
| `By`, `Keys` | Локаторы и спецклавиши (Selenium-совместимые). |
| `Configuration` | Дефолтные таймауты (`timeout = 4s`, `pollingInterval = 100ms`). |

## Базовое использование

```java
import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$$;
import com.bft.pw.Condition;

// CSS-селектор
$("button[data-testid='save']").click();

// текст через Playwright-псевдоклассы (предпочтительно вместо XPath)
$("button:has-text('Сохранить')").click();
$("tr:has(td:has-text('СЗВ-М'))").shouldBe(Condition.visible);

// коллекция
$$("table tbody tr").shouldHave(CollectionCondition.sizeGreaterThan(0));
String firstRow = $$("table tbody tr").first().text();
```

### Локаторы: приоритет

1. `data-testid` → `$("[data-testid='save-btn']")`
2. роль/текст → `$("button:has-text('Сохранить')")`, `$("button:text-is('Да')")`
3. CSS → `$(".report-form .field")`
4. XPath — **крайний случай** (сложные оси, `following-sibling`, динамика без CSS-аналога).

`$x("...")` остаётся для сложных случаев, но новые локаторы на XPath лучше не добавлять.
`contains(text(), '...')` обычно заменяется на `:has-text('...')`, а точное совпадение —
на `:text-is('...')`.

## Действия (`SelenideElement`)

```java
el.click();  el.doubleClick();  el.hover();
el.setValue("текст");  el.append("+");  el.clear();
el.sendKeys(Keys.ENTER);  el.pressEnter();  el.pressTab();  el.pressEscape();  el.press(Keys.ESCAPE);
el.selectOption("Вариант");  el.selectOptionContainingText("Вари");  el.selectOptionByValue("1");
el.scrollTo();  el.scrollIntoView(true);
el.uploadFile(new File("..."));  el.uploadFromClasspath("application/file.xml");
el.setSelected(true);  el.focus();
```

## Проверки и ожидания

Проверки **сами ждут** (polling), поэтому это и есть «умные ожидания»:

```java
el.shouldBe(Condition.visible);
el.shouldBe(Condition.enabled, Duration.ofSeconds(10)); // свой таймаут
el.shouldHave(Condition.text("Сохранено"));
el.shouldNotBe(Condition.visible);
el.shouldNot(Condition.exist);
el.waitUntil(Condition.exist, Duration.ofSeconds(15));
el.waitWhile(Condition.visible, Duration.ofSeconds(15));
```

Готовые фабрики `Condition`:

| Условие | Смысл |
|---|---|
| `visible` / `hidden` | Видимость |
| `exist` / `disappear` | Наличие в DOM |
| `enabled` / `editable` | Доступность/редактируемость |
| `checked` | Чекбокс/радио выбран |
| `empty` / `notEmpty` | Пустой/непустой текст |
| `text("…")` / `exactText("…")` | Вхождение/точное совпадение текста |
| `value("…")` | Значение поля |
| `cssClass("…")` / `id("…")` / `attribute("name","value")` | Атрибуты |
| `and(…, …)` / `or(…, …)` / `not(…)` | Комбинации |

Коллекции:

```java
$$("table tbody tr").shouldHave(CollectionCondition.size(5));
$$("table tbody tr").shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1));
$$("li").filter(Condition.visible).shouldHave(CollectionCondition.empty());
```

## Чтение значений

```java
el.getText();  el.text();  el.innerText();
el.getValue();  el.val();
el.getAttribute("href");  el.getCssValue("color");
el.exists();  el.isDisplayed();  el.isEnabled();  el.isSelected();
$$(".row").count();
```

## `PwWait` и `Configuration`

`PwWait` — для ожиданий по произвольному состоянию:

```java
new PwWait(Duration.ofSeconds(20), Duration.ofMillis(200))
    .until(driver -> driver.getPage().url().contains("/reports"));
```

Дефолты (`Configuration`): `timeout = 4s`, `pollingInterval = 100ms`.
Таймаут можно задать на конкретную проверку (`shouldBe(cond, Duration)`), глобально — через `Configuration`.

Рекомендуемый таймаут для UI-шагов ЕВС (медленные формы) — **15–30 с**.

## `PwSession` (жизненный цикл и диагностика)

В тестах обычно не вызывается напрямую (этим занимается `UITestBase`), но полезно знать:

| Метод | Назначение |
|---|---|
| `PwSession.page()`, `context()`, `driver()` | Текущие Playwright-объекты. |
| `PwSession.url()` | Текущий URL. |
| `PwSession.screenshot(name)` | Скриншот (кладётся в Allure). |
| `PwSession.startTracing()` / `stopTracing(name)` | Playwright trace. |
| `PwSession.consoleLogs()`, `getAndClearMeshError()` | Консоль браузера и ошибки graphql-mesh. |
| `PwSession.awaitSigningData(timeout)` / `resetSigningData()` | Данные подписания, которые отдаёт фронт (requestId/itemId). |
| `PwSession.confirm()` / `dismiss()` / `lastDialog()` | Работа с нативными диалогами. |
| `PwSession.close()` | Закрытие сессии (вызывается после теста). |
| `PwSession.sleep(millis)` | ⚠️ Инфраструктурный `sleep`; **в тестах не использовать** (запрещено гейтом hard-waits). |

## Что не делать

- ❌ `Thread.sleep(...)` / `Selenide.sleep(...)` в тестах — только `shouldBe`/`PwWait`/`SmartWaits`.
- ❌ Прямой Playwright API (`page.locator(...)`) в тестах/pages — есть обёртка.
- ❌ Новые локаторы на сложном XPath, если есть CSS/`:has-text`.
- ❌ Логика ожидания «на глазок» — используй явное `Condition` с понятным именем.

## См. также

- [ARCHITECTURE.md](../ARCHITECTURE.md) — место `pw` в слоях фреймворка.
- [CONTRIBUTING.md](../CONTRIBUTING.md) — правила ожиданий, локаторов, DoD теста.
- [COMPONENTS_GUIDE.md](../COMPONENTS_GUIDE.md) — UI-компоненты поверх `pw`.
