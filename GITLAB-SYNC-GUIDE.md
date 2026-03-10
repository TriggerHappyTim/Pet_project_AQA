# Руководство по синхронизации с GitLab

## 🔄 Ситуация: "non-fast-forward" ошибка

Если вы получили ошибку:
```
! [rejected]        master -> master (non-fast-forward)
```

Это означает, что в удаленном репозитории GitLab уже есть коммиты, которых нет в вашем локальном репозитории.

## ✅ Решения

### Вариант 1: Создать новую ветку (РЕКОМЕНДУЕТСЯ)

Это самый безопасный способ - ваши изменения не будут конфликтовать с существующими.

```cmd
# Создайте новую ветку
git checkout -b feature/your-changes-2026-02-02

# Добавьте ваши файлы
git add .

# Создайте коммит
git commit -m "Add new files and updates"

# Отправьте новую ветку в GitLab
git push -u origin feature/your-changes-2026-02-02
```

После этого создайте Merge Request в GitLab для слияния с master.

### Вариант 2: Использовать скрипт синхронизации

Запустите автоматический скрипт:

```cmd
sync-with-gitlab.bat
```

Скрипт предложит несколько вариантов:
1. Создать новую ветку для ваших изменений
2. Объединить удаленный master с локальным (merge)
3. Переместить ваши коммиты поверх удаленных (rebase)

### Вариант 3: Объединить с удаленным master (merge)

Если вы хотите работать в ветке master:

```cmd
# Получите последние изменения
git fetch origin

# Переключитесь на master
git checkout master

# Объедините с удаленным master
git merge origin/master

# Если есть конфликты, разрешите их, затем:
git add .
git commit

# Отправьте изменения
git push origin master
```

### Вариант 4: Переместить коммиты поверх удаленных (rebase)

⚠️ **ВНИМАНИЕ:** Это переписывает историю. Используйте только если работаете один.

```cmd
# Получите последние изменения
git fetch origin

# Переключитесь на master
git checkout master

# Переместите ваши коммиты поверх удаленных
git rebase origin/master

# Если есть конфликты, разрешите их, затем:
git add .
git rebase --continue

# Отправьте изменения (требуется force)
git push origin master --force-with-lease
```

## 🎯 Рекомендуемый подход для вашей ситуации

Учитывая, что в GitLab уже есть ветка master с коммитами, лучше всего:

1. **Создать новую ветку** для ваших изменений:
   ```cmd
   git checkout -b feature/local-updates-2026-02-02
   git add .
   git commit -m "Add local updates: GITLAB-SETUP.md, connect-gitlab.bat, setup-gitlab.bat"
   git push -u origin feature/local-updates-2026-02-02
   ```

2. **Создать Merge Request** в GitLab:
   - Откройте GitLab в браузере
   - Перейдите в Merge Requests → New Merge Request
   - Выберите source: `feature/local-updates-2026-02-02`
   - Выберите target: `master`
   - Заполните описание и создайте MR

3. **После review** - слить изменения в master через GitLab UI

## 📋 Быстрые команды

### Проверить текущее состояние

```cmd
# Посмотреть все ветки
git branch -a

# Посмотреть статус
git status

# Посмотреть последние коммиты
git log --oneline -10

# Посмотреть удаленные ветки
git branch -r
```

### Создать ветку и отправить

```cmd
git checkout -b feature/my-changes
git add .
git commit -m "My changes"
git push -u origin feature/my-changes
```

### Объединить с удаленным master

```cmd
git fetch origin
git checkout master
git merge origin/master
git push origin master
```

## 🐛 Устранение проблем

### Конфликты при merge

```cmd
# Посмотреть конфликтующие файлы
git status

# Открыть файлы и разрешить конфликты вручную
# Затем:
git add .
git commit
```

### Отменить merge

```cmd
git merge --abort
```

### Отменить rebase

```cmd
git rebase --abort
```

### Вернуть изменения из stash

```cmd
git stash list
git stash pop
```

## ✅ Чеклист

- [ ] Проверил текущую ветку: `git branch`
- [ ] Получил последние изменения: `git fetch origin`
- [ ] Выбрал стратегию (новая ветка / merge / rebase)
- [ ] Создал коммит с изменениями
- [ ] Отправил изменения в GitLab
- [ ] Проверил результат в GitLab UI

---

**Рекомендация:** Используйте `sync-with-gitlab.bat` для автоматической синхронизации! 🚀
