# com.bft.ui — Page Object и UI-компоненты

Пакет содержит реализацию Page Object Pattern и переиспользуемые UI-компоненты.

**Всего файлов:** 18

## Структура

### pages/ (3 файла)
- **BasePage** — базовый класс страниц из `com.bft.ui.pages`
- **LoginPage** — страница авторизации
- **MainPage** — главная страница приложения

### component/ (12 файлов)
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

### core/
- **BasePage** — generic-базовый класс страниц (умные элементы, стратегии ожидания)
- **element/** — SmartElement, SmartElementList, ElementFactory
- **wait/** — WaitStrategy, WaitStrategies

## Пакет gui/

> **Примечание:** Пакет `com.bft.gui` (legacy) — отдельный, но **активно используется**.  
> Содержит 4 файла: BasePage, LoginPage, CryptoProDemoPage и др.  
> Не входит в `com.bft.ui`, однако тесты продолжают его использовать.
