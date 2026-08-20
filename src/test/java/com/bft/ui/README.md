# com.bft.ui — Page Object и UI-компоненты

Пакет содержит реализацию Page Object Pattern и переиспользуемые UI-компоненты.

**Всего файлов:** 23 (без учёта README)

## Структура

### pages/ (4 файла)
- **BasePage** — базовый класс страниц из `com.bft.ui.pages`
- **LoginPage** — страница авторизации
- **MainPage** — главная страница приложения
- **CryptoProDemoPage** — демо-страница КриптоПро (перенесена из удалённого пакета `gui/`)

### component/ (13 файлов)
- **BaseComponent** — базовый класс UI-компонентов
- **ButtonComponent** — кнопки
- **InputComponent** — текстовые поля
- **SelectComponent** — выпадающие списки
- **CheckboxComponent** — чекбоксы
- **RadioButtonComponent** — радиокнопки
- **DateComponent** — поля даты
- **FileComponent** — загрузка файлов
- **TextareaComponent** — многострочный текст
- **TableComponent** — таблицы
- **NavigationComponent** — навигация
- **StatusIndicatorComponent** — индикаторы статуса
- **GracePeriodDialogComponent** — диалог льготного периода стажа

### core/
- **BasePage** — generic-базовый класс страниц (умные элементы, стратегии ожидания)
- **element/** — SmartElement, SmartElementList, ElementFactory
- **wait/** — WaitStrategy, WaitStrategies

## История

> **Примечание:** Пакет `com.bft.gui` (legacy) **удалён** в рамках рефакторинга (шаг 4 `REFACTORING_PLAN.md`).
> `CryptoProDemoPage` перенесён в `ui/pages`, все импорты `com.bft.gui.*` заменены на `com.bft.ui.*`.
> Единственный источник правды по Page Objects — пакет `com.bft.ui`.