# 🚀 Быстрый запуск тестов с Allure отчётом

## ⚡ Самый простой способ

### Вариант 1: Через двойной клик (РЕКОМЕНДУЕТСЯ)

1. Откройте папку в проводнике:
   ```
   <путь_к_проекту>\evs-testing-framework
   ```

2. **Двойной клик** на файл:
   ```
   run-tests-with-allure.bat
   ```

3. Дождитесь окончания (3-5 минут)

4. Браузер откроется автоматически с Allure отчётом

---

## Вариант 2: Через командную строку (CMD)

Откройте **Командную строку** (не PowerShell!):

```cmd
cd /d <путь_к_проекту>\evs-testing-framework

run-tests-with-allure.bat
```

---

## Вариант 3: Через IntelliJ IDEA Terminal

В IDEA откройте Terminal и выполните:

```bash
./run-tests-with-allure.bat
```

Или выберите **CMD** в выпадающем списке Terminal (справа вверху) вместо PowerShell.

---

## 📋 Что делает скрипт?

1. ✅ Очищает старые результаты
2. ✅ Запускает тест `Efs1#efs_1_xml` в **Firefox**
3. ✅ Генерирует Allure отчёт
4. ✅ Открывает отчёт в браузере

---

## ⏱️ Ожидаемое время выполнения

- Очистка: 1 сек
- Maven сборка: 30-60 сек
- Выполнение теста: 60-120 сек
- Генерация отчёта: 10-20 сек

**Итого:** 2-4 минуты

---

## 🎯 Альтернативные команды

### Только запуск тестов (без отчёта)

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml
```

### Только генерация отчёта (если тесты уже запущены)

```cmd
mvn allure:serve
```

### Запуск всех тестов EFS-1

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1
```

### Запуск с конкретным пользователем

```cmd
mvn clean test -Pbrowser_firefox_local -Dtest=Efs1#efs_1_xml_krivonosov
```

---

## 🐛 Troubleshooting

### Проблема: "Firefox не найден"

**Решение:**
1. Установите Firefox: https://www.mozilla.org/firefox/
2. Или используйте Chrome: замените в команде `browser_firefox_local` на `browser_chrome_local`

### Проблема: "Maven не найден"

**Решение:**
1. Откройте IntelliJ IDEA
2. Maven встроен в IDEA - используйте IDEA Terminal

### Проблема: "Allure отчёт не генерируется"

**Решение:**
```cmd
mvn io.qameta.allure:allure-maven:2.29.0:serve
```

---

## 📊 Просмотр отчёта

После запуска откроется браузер с Allure отчётом на адресе:
```
http://localhost:[случайный_порт]/
```

Отчёт покажет:
- ✅ Статус теста (passed/failed)
- 📸 Скриншоты при ошибках
- 📄 Page source
- 🔍 Подробные шаги теста
- ⏱️ Время выполнения
- 📊 Графики и статистику

---

## 🎉 Готово!

Теперь просто **дважды кликните** на `run-tests-with-allure.bat` и наслаждайтесь красивым отчётом! 🚀
