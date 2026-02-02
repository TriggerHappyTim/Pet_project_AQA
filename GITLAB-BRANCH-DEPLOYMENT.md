# Инструкция по созданию ветки и деплою обновленного проекта в GitLab

## 🎯 Цель
Безопасно создать новую ветку в существующем GitLab репозитории и выложить туда обновленный проект, не потеряв старую версию.

---

## ⚠️ ВАЖНО: Подготовка

### 1. Создайте резервную копию текущего проекта
```bash
# Скопируйте всю папку проекта в безопасное место
xcopy "c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework" "c:\Backup\evs-testing-framework-backup" /E /I /H
```

Или просто скопируйте папку вручную в безопасное место (например, на другой диск или в облако).

---

## 📋 Пошаговая инструкция

### Шаг 1: Клонируйте существующий GitLab репозиторий во временную папку

```bash
# Перейдите в родительскую директорию
cd c:\Users\тим\IdeaProjects

# Клонируйте репозиторий во временную папку
git clone <URL_вашего_GitLab_репозитория> evs-testing-framework-gitlab-temp

# Пример:
# git clone https://gitlab.com/username/evs-testing-framework.git evs-testing-framework-gitlab-temp
```

**Результат:** У вас появится папка `evs-testing-framework-gitlab-temp` со старой версией проекта из GitLab.

---

### Шаг 2: Создайте новую ветку для обновленной версии

```bash
# Перейдите в клонированный репозиторий
cd evs-testing-framework-gitlab-temp

# Проверьте текущую ветку (обычно master или main)
git branch

# Создайте новую ветку для обновлений
git checkout -b feature/lks-testing-krivonosov

# Или используйте другое имя ветки:
# git checkout -b feature/ui-tests-update
# git checkout -b feature/playwright-tests
# git checkout -b dev/test-cases-2026
```

**Результат:** Вы создали и переключились на новую ветку. Старая ветка (master/main) осталась нетронутой.

---

### Шаг 3: Скопируйте обновленные файлы в клонированный репозиторий

#### Вариант A: Копирование только новых файлов (рекомендуется)

```bash
# Скопируйте только новые тест-кейсы и баг-репорт
xcopy "c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework\src\test\resources\docs\test-cases-lks-krivonosov.md" "c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp\src\test\resources\docs\" /Y

xcopy "c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework\src\test\resources\docs\bug-report-lks.md" "c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp\src\test\resources\docs\" /Y
```

#### Вариант B: Копирование всего обновленного проекта

**⚠️ ВНИМАНИЕ:** Этот вариант заменит ВСЕ файлы. Используйте только если уверены!

```bash
# Удалите все файлы из временного репозитория (кроме .git)
# НЕ УДАЛЯЙТЕ ПАПКУ .git !!!

# В PowerShell:
Get-ChildItem -Path "c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp" -Exclude ".git" | Remove-Item -Recurse -Force

# Скопируйте все файлы из обновленного проекта
xcopy "c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework\*" "c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp\" /E /I /H /Y /EXCLUDE:c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp\.git
```

---

### Шаг 4: Проверьте изменения

```bash
cd c:\Users\тим\IdeaProjects\evs-testing-framework-gitlab-temp

# Посмотрите статус
git status

# Посмотрите детали изменений
git diff

# Посмотрите список измененных файлов
git diff --name-only
```

**Что вы увидите:**
- Красным: удаленные файлы
- Зеленым: новые файлы
- Модифицированные файлы

---

### Шаг 5: Добавьте изменения в Git

#### Если копировали только новые файлы:
```bash
git add src/test/resources/docs/test-cases-lks-krivonosov.md
git add src/test/resources/docs/bug-report-lks.md
```

#### Если копировали весь проект:
```bash
# Добавьте все изменения
git add .

# Или добавьте выборочно по папкам
git add src/
git add pom.xml
git add README.md
# и т.д.
```

---

### Шаг 6: Создайте коммит

```bash
git commit -m "Добавлены тест-кейсы и отчет о багах для ЛК Страхователя (Кривоносов)

Изменения:
- Добавлено 30 детальных тест-кейсов для ЛК Страхователя
- Протестировано 10 тест-кейсов через Playwright MCP
- Найдено и задокументировано 8 багов (1 критичный, 1 high, 3 medium, 3 low)
- Создан детальный баг-репорт с скриншотами и рекомендациями

Покрытие:
- Авторизация через ЕПГУ
- Работа с отчетами ЕФС-1
- Фильтрация и поиск отчетов
- Управление черновиками
- Валидация форм
- Негативные сценарии

Статистика:
- Протестировано: 10/30 тест-кейсов (33%)
- Найдено критичных багов: 1
- Найдено багов высокого приоритета: 1
- Общее количество багов: 8"
```

---

### Шаг 7: Отправьте ветку в GitLab

```bash
# Отправьте новую ветку в remote репозиторий
git push -u origin feature/lks-testing-krivonosov

# Или если используете другое имя ветки:
# git push -u origin <имя_вашей_ветки>
```

**Результат:** Ваша новая ветка появится в GitLab, старая ветка (master/main) останется нетронутой.

---

### Шаг 8: Создайте Merge Request в GitLab (опционально)

1. Откройте ваш проект в GitLab
2. Перейдите в **Merge Requests** → **New Merge Request**
3. Выберите:
   - **Source branch**: `feature/lks-testing-krivonosov`
   - **Target branch**: `master` (или `main`)
4. Заполните описание:

```markdown
## 🎯 Цель
Добавление тест-кейсов и баг-репорта для тестирования ЛК Страхователя (учетная запись Кривоносова)

## 📝 Что добавлено
- ✅ 30 детальных тест-кейсов для UI тестирования ЛК Страхователя
- ✅ Баг-репорт с 8 найденными дефектами
- ✅ Скриншоты и детальные шаги воспроизведения

## 🧪 Тестирование
- Протестировано 10 из 30 тест-кейсов через Playwright MCP
- Покрытие: авторизация, отчеты, фильтры, черновики, валидация

## 🐛 Найденные баги
- 1 критичный (logout не работает)
- 1 high (некорректный счетчик отчетов)
- 3 medium (JS ошибки, WebSocket проблемы)
- 3 low (404 ошибки, CORS)

## 📊 Файлы
- `src/test/resources/docs/test-cases-lks-krivonosov.md` (1254 строки)
- `src/test/resources/docs/bug-report-lks.md` (656 строк)

## ✅ Checklist
- [x] Тест-кейсы соответствуют проектным стандартам
- [x] Баг-репорт содержит детальные шаги воспроизведения
- [x] Добавлены скриншоты
- [ ] Требуется review от QA Lead
- [ ] Требуется review от Dev Team
```

5. Нажмите **Create Merge Request**
6. Дождитесь review и апрува
7. После апрува нажмите **Merge**

---

## 🔄 Альтернативный подход: Использование IntelliJ IDEA

### Вариант 1: Через IntelliJ IDEA (проще всего)

1. **Откройте проект в IntelliJ IDEA**
   - File → Open → выберите `c:\Users\тим\IdeaProjects\evs-testing-framework_ai_2.0\evs-testing-framework`

2. **Подключите к существующему GitLab репозиторию**
   - VCS → Enable Version Control Integration → Git
   - VCS → Git → Remotes
   - Нажмите "+" и добавьте:
     - Name: `origin`
     - URL: `<URL_вашего_GitLab_репозитория>`

3. **Получите последние изменения из GitLab**
   - VCS → Git → Fetch
   - VCS → Git → Pull (выберите master/main)

4. **Создайте новую ветку**
   - Git → New Branch
   - Введите имя: `feature/lks-testing-krivonosov`
   - Оставьте галочку "Checkout branch"

5. **Добавьте файлы в коммит**
   - VCS → Commit (Ctrl+K)
   - Выберите файлы:
     - `test-cases-lks-krivonosov.md`
     - `bug-report-lks.md`
   - Напишите commit message (можно использовать текст из Шага 6 выше)

6. **Отправьте в GitLab**
   - VCS → Git → Push (Ctrl+Shift+K)
   - Убедитесь, что выбрана ветка `feature/lks-testing-krivonosov`
   - Нажмите **Push**

7. **Создайте Merge Request**
   - В IntelliJ IDEA появится уведомление "Create Merge Request"
   - Или создайте вручную в GitLab (см. Шаг 8)

---

## 🛡️ Проверка безопасности

### Перед push проверьте:

```bash
# Убедитесь, что вы в правильной ветке
git branch

# Должно показать:
# * feature/lks-testing-krivonosov
#   master (или main)

# Убедитесь, что master/main не изменен
git checkout master
git status
# Должно показать: "nothing to commit, working tree clean"

# Вернитесь в вашу ветку
git checkout feature/lks-testing-krivonosov
```

### После push проверьте в GitLab:

1. Откройте GitLab → ваш проект
2. Перейдите в **Repository** → **Branches**
3. Убедитесь, что есть две ветки:
   - `master` (или `main`) - старая версия (нетронутая)
   - `feature/lks-testing-krivonosov` - новая версия

---

## 📊 Что будет в результате

### В GitLab будет:

```
master (или main)
├── [старая версия проекта] ← НЕ ИЗМЕНЕНА
│
└── feature/lks-testing-krivonosov
    ├── [старая версия проекта]
    └── [+ новые файлы]
        ├── test-cases-lks-krivonosov.md
        └── bug-report-lks.md
```

### Преимущества такого подхода:

✅ Старая версия в `master` остается нетронутой  
✅ Новые изменения в отдельной ветке  
✅ Можно легко откатиться  
✅ Можно сделать code review через Merge Request  
✅ Можно протестировать новую версию отдельно  
✅ Можно мержить постепенно  

---

## 🚨 Что делать если что-то пошло не так

### Если случайно запушили в master:

```bash
# НЕ ПАНИКУЙТЕ! Можно откатить

# 1. Посмотрите историю
git log --oneline

# 2. Найдите хеш коммита ДО ваших изменений
# Например: abc1234

# 3. Откатите master к этому коммиту
git reset --hard abc1234

# 4. Принудительно запушьте (ОСТОРОЖНО!)
git push --force origin master

# 5. Создайте ветку с вашими изменениями
git checkout -b feature/lks-testing-krivonosov
git cherry-pick <хеш_вашего_коммита>
git push -u origin feature/lks-testing-krivonosov
```

### Если потеряли изменения:

```bash
# Посмотрите reflog (история всех действий)
git reflog

# Найдите нужный коммит и восстановите
git checkout <хеш_коммита>
git checkout -b feature/recovery
```

### Если не уверены:

**НЕ ДЕЛАЙТЕ `git push --force` БЕЗ КОНСУЛЬТАЦИИ С КОМАНДОЙ!**

Лучше:
1. Создайте issue в GitLab
2. Попросите помощи у коллег
3. Используйте резервную копию

---

## 📞 Контрольный чеклист перед началом

- [ ] Создана резервная копия проекта
- [ ] Известен URL GitLab репозитория
- [ ] Есть доступ к GitLab (логин/пароль или SSH ключ)
- [ ] Установлен Git на компьютере
- [ ] Выбрано имя для новой ветки
- [ ] Понятно, какие файлы нужно добавить
- [ ] Есть время на выполнение (30-60 минут)

---

## 🎓 Рекомендации

### Для новичков в Git:
👉 **Используйте IntelliJ IDEA** - это самый безопасный и простой способ

### Для опытных пользователей:
👉 **Используйте командную строку** - это быстрее и гибче

### Для команды:
👉 **Обязательно создавайте Merge Request** - это позволит сделать code review

---

## 📚 Полезные команды Git

```bash
# Посмотреть все ветки (локальные и remote)
git branch -a

# Посмотреть историю коммитов
git log --oneline --graph --all

# Посмотреть изменения в конкретном файле
git diff <файл>

# Отменить изменения в файле (до коммита)
git checkout -- <файл>

# Удалить последний коммит (но сохранить изменения)
git reset --soft HEAD~1

# Посмотреть remote репозитории
git remote -v

# Обновить информацию о remote ветках
git fetch --all

# Переключиться на другую ветку
git checkout <имя_ветки>

# Удалить локальную ветку
git branch -d <имя_ветки>

# Удалить remote ветку
git push origin --delete <имя_ветки>
```

---

## ✅ Итоговый чеклист выполнения

- [ ] Создана резервная копия
- [ ] Клонирован GitLab репозиторий во временную папку
- [ ] Создана новая ветка `feature/lks-testing-krivonosov`
- [ ] Скопированы обновленные файлы
- [ ] Проверены изменения через `git status` и `git diff`
- [ ] Добавлены файлы через `git add`
- [ ] Создан коммит с описательным сообщением
- [ ] Отправлена ветка в GitLab через `git push`
- [ ] Проверено в GitLab, что ветка появилась
- [ ] Проверено, что master/main не изменен
- [ ] Создан Merge Request (опционально)
- [ ] Удалена временная папка `evs-testing-framework-gitlab-temp` (после успешного push)

---

**Удачи! Если возникнут вопросы - обращайтесь!** 🚀
