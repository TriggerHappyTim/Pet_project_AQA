# Разрешение конфликтов слияния

## 🔴 Проблема

GitLab показывает:
```
Merge blocked: merge conflicts must be resolved.
The source branch is 30 commits behind the target branch.
```

Это означает, что ваша ветка `New` отстает от `master` на 30 коммитов, и есть конфликты, которые нужно разрешить.

## ✅ Решение

### Вариант 1: Автоматическое разрешение (РЕКОМЕНДУЕТСЯ)

Запустите скрипт:

```cmd
resolve-merge-conflicts.bat
```

Скрипт:
1. Получит последние изменения из GitLab
2. Объединит master с вашей веткой
3. Предложит варианты разрешения конфликтов
4. Отправит изменения обратно в GitLab

### Вариант 2: Ручное разрешение

#### Шаг 1: Переключитесь на вашу ветку

```cmd
git checkout New
```

#### Шаг 2: Получите последние изменения

```cmd
git fetch origin
```

#### Шаг 3: Объедините master с вашей веткой

```cmd
git merge origin/master
```

#### Шаг 4: Найдите конфликтующие файлы

```cmd
git status
```

Вы увидите файлы с пометкой `both modified` или `unmerged`.

#### Шаг 5: Откройте конфликтующие файлы

В файлах вы увидите маркеры конфликтов:

```
<<<<<<< HEAD
Ваши изменения
=======
Изменения из master
>>>>>>> origin/master
```

#### Шаг 6: Разрешите конфликты

Выберите один из вариантов:

**A. Оставить ваши изменения:**
```java
// Удалите маркеры и оставьте только ваши изменения
Ваши изменения
```

**B. Оставить изменения из master:**
```java
// Удалите маркеры и оставьте только изменения из master
Изменения из master
```

**C. Объединить оба изменения:**
```java
// Оставьте нужные части из обоих вариантов
Ваши изменения
Изменения из master (если нужны)
```

#### Шаг 7: Сохраните файлы и завершите merge

```cmd
# Добавьте разрешенные файлы
git add .

# Завершите merge
git commit -m "Merge master into New - resolve conflicts"

# Отправьте изменения
git push origin New
```

### Вариант 3: Принять все изменения из master

Если ваши изменения не критичны и можно принять все из master:

```cmd
git checkout New
git fetch origin
git merge origin/master
git checkout --theirs .
git add .
git commit -m "Merge master - accept all changes from master"
git push origin New
```

### Вариант 4: Принять все ваши изменения

Если изменения из master не нужны:

```cmd
git checkout New
git fetch origin
git merge origin/master
git checkout --ours .
git add .
git commit -m "Merge master - keep our changes"
git push origin New
```

### Вариант 5: Использовать rebase вместо merge

Rebase переместит ваши коммиты поверх master:

```cmd
git checkout New
git fetch origin
git rebase origin/master
```

Если есть конфликты:
```cmd
# Разрешите конфликты в файлах
# Затем:
git add .
git rebase --continue

# Или отмените:
git rebase --abort
```

После успешного rebase:
```cmd
git push origin New --force-with-lease
```

## 🔍 Проверка конфликтов

### Посмотреть список конфликтующих файлов

```cmd
git diff --name-only --diff-filter=U
```

### Посмотреть детали конфликта в файле

```cmd
git diff <filename>
```

### Посмотреть статус

```cmd
git status
```

## 📋 Пошаговая инструкция (рекомендуется)

1. **Запустите скрипт:**
   ```cmd
   resolve-merge-conflicts.bat
   ```

2. **Выберите вариант разрешения:**
   - Если конфликты простые → выберите вариант 2 или 3 (принять theirs/ours)
   - Если нужен контроль → выберите вариант 1 (ручное разрешение)

3. **После разрешения:**
   - Скрипт автоматически отправит изменения
   - Или выполните вручную: `git push origin New`

4. **Проверьте в GitLab:**
   - Обновите страницу Merge Request
   - Конфликты должны быть разрешены
   - Кнопка "Merge" должна стать активной

## ⚠️ Важные замечания

### Перед началом

- **Создайте резервную копию** или убедитесь, что ваши изменения закоммичены
- **Проверьте текущую ветку:** `git branch`
- **Сохраните незакоммиченные изменения:** `git stash`

### После разрешения

- **Проверьте, что все работает:** запустите тесты
- **Проверьте изменения в GitLab:** убедитесь, что все правильно
- **Создайте коммит с понятным сообщением**

## 🐛 Устранение проблем

### Конфликты слишком сложные

```cmd
# Отмените merge
git merge --abort

# Создайте новую ветку от актуального master
git checkout master
git pull origin master
git checkout -b New-v2
git cherry-pick <ваши-коммиты>
```

### Потерялись изменения

```cmd
# Посмотрите reflog
git reflog

# Восстановите коммит
git checkout <hash>
git checkout -b recovery-branch
```

### Нужно начать заново

```cmd
# Удалите локальную ветку
git checkout master
git branch -D New

# Создайте новую от актуального master
git pull origin master
git checkout -b New
# Скопируйте ваши файлы заново
git add .
git commit -m "Your changes"
git push -u origin New
```

## ✅ Чеклист

- [ ] Запущен `resolve-merge-conflicts.bat`
- [ ] Получены последние изменения: `git fetch origin`
- [ ] Выполнен merge: `git merge origin/master`
- [ ] Конфликты разрешены
- [ ] Изменения закоммичены: `git commit`
- [ ] Изменения отправлены: `git push origin New`
- [ ] Проверено в GitLab - конфликты разрешены
- [ ] Merge Request готов к слиянию

---

**Рекомендация:** Используйте `resolve-merge-conflicts.bat` для автоматического разрешения! 🚀
