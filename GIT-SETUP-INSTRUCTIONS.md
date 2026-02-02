# Инструкция по настройке Git и созданию коммита

## Проблема
PowerShell в Cursor имеет проблемы с кириллицей в пути пользователя, поэтому git команды нужно выполнить вручную.

## Шаги для инициализации Git и создания коммита

### 1. Откройте Git Bash или CMD в папке проекта

Откройте терминал (Git Bash или CMD) в директории:
```
c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework
```

### 2. Инициализируйте Git репозиторий

```bash
git init
```

### 3. Настройте Git (если еще не настроено)

```bash
git config user.name "Ваше Имя"
git config user.email "your.email@example.com"
```

### 4. Добавьте созданные файлы

```bash
git add src/test/resources/docs/test-cases-lks-krivonosov.md
git add src/test/resources/docs/bug-report-lks.md
```

### 5. Проверьте статус

```bash
git status
```

Вы должны увидеть:
```
Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
        new file:   src/test/resources/docs/test-cases-lks-krivonosov.md
        new file:   src/test/resources/docs/bug-report-lks.md
```

### 6. Создайте коммит

```bash
git commit -m "Добавлены тест-кейсы и отчет о багах для ЛК Страхователя (Кривоносов)

- Добавлено 30 тест-кейсов для тестирования ЛК Страхователя
- Протестировано 8 тест-кейсов через Playwright MCP
- Найдено 8 багов (1 критичный, 1 high, 3 medium, 3 low)
- Создан детальный отчет о багах с скриншотами
- Покрытие: авторизация, работа с отчетами, фильтры, черновики, валидация"
```

### 7. Добавьте remote для GitLab (если нужно)

```bash
git remote add origin <URL вашего GitLab репозитория>
```

Например:
```bash
git remote add origin https://gitlab.com/username/evs-testing-framework.git
```

### 8. Отправьте в GitLab

```bash
git push -u origin master
```

Или если используется main:
```bash
git push -u origin main
```

---

## Что было создано

### Файлы для коммита:

1. **test-cases-lks-krivonosov.md** (1254 строки)
   - 30 детальных тест-кейсов
   - Покрытие: авторизация, отчеты ЕФС-1, СЗВ-М, фильтры, черновики, негативные сценарии
   - С примерами Java кода для автоматизации

2. **bug-report-lks.md** (656 строк)
   - 8 найденных багов
   - Детальные шаги воспроизведения
   - Скриншоты и рекомендации
   - Статистика тестирования

### Скриншоты (в .playwright-mcp/, можно не коммитить):
- 24 скриншота процесса тестирования
- Можно добавить в `.gitignore` если не нужны

---

## Альтернатива: Использовать Git GUI

Если командная строка не работает, можно использовать:
- **GitHub Desktop**
- **GitKraken**
- **SourceTree**
- **TortoiseGit**
- **IntelliJ IDEA встроенный Git**

В IntelliJ IDEA:
1. VCS → Enable Version Control Integration → Git
2. VCS → Commit (Ctrl+K)
3. Выберите файлы test-cases-lks-krivonosov.md и bug-report-lks.md
4. Напишите commit message
5. Commit and Push

---

## Проверка после коммита

```bash
git log --oneline -1
git show HEAD --stat
```

Должны увидеть ваш коммит с двумя добавленными файлами.
