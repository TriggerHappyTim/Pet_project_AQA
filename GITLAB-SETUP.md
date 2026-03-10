# Настройка GitLab для EVS Testing Framework

## 🚀 Быстрая настройка

### Вариант 1: Быстрое подключение к BFT GitLab (РЕКОМЕНДУЕТСЯ)

Для подключения к репозиторию `https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework`:

1. Запустите скрипт:
   ```cmd
   connect-gitlab.bat
   ```

2. Скрипт автоматически:
   - Инициализирует git репозиторий (если нужно)
   - Добавит remote с правильным URL
   - Предложит создать начальный коммит
   - Предложит сделать push

### Вариант 2: Автоматическая настройка (любой GitLab)

1. Запустите скрипт:
   ```cmd
   setup-gitlab.bat
   ```

2. Следуйте инструкциям на экране:
   - Введите URL вашего GitLab репозитория (или нажмите Enter для использования BFT GitLab по умолчанию)
   - Выберите, хотите ли вы сделать push существующих коммитов

### Вариант 2: Ручная настройка

#### 1. Создайте репозиторий в GitLab

1. Войдите в GitLab
2. Создайте новый проект (New Project)
3. Выберите "Create blank project"
4. Скопируйте URL репозитория (HTTPS или SSH)

#### 2. Инициализируйте git репозиторий (если еще не сделано)

```cmd
cd evs-testing-framework
git init
```

#### 3. Добавьте GitLab remote

**Для BFT GitLab (внутренний сервер):**
```cmd
git remote add origin https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework.git
```

**Для публичного GitLab HTTPS:**
```cmd
git remote add origin https://gitlab.com/username/project-name.git
```

**Для SSH:**
```cmd
git remote add origin git@gitlab.com:username/project-name.git
```

#### 4. Проверьте remote

```cmd
git remote -v
```

Должно показать:
```
origin  https://gitlab.com/username/project-name.git (fetch)
origin  https://gitlab.com/username/project-name.git (push)
```

#### 5. Создайте начальный коммит (если нужно)

```cmd
git add .
git commit -m "Initial commit"
```

#### 6. Push в GitLab

```cmd
git push -u origin master
```

или для ветки `main`:

```cmd
git push -u origin main
```

## 🔐 Настройка аутентификации

### HTTPS аутентификация

GitLab будет запрашивать логин и пароль при push. Для удобства можно использовать:

1. **Personal Access Token** (рекомендуется):
   - Settings → Access Tokens
   - Создайте token с правами `write_repository`
   - Используйте token вместо пароля

2. **Git Credential Manager**:
   ```cmd
   git config --global credential.helper manager-core
   ```

### SSH аутентификация

1. **Проверьте наличие SSH ключа:**
   ```cmd
   type %USERPROFILE%\.ssh\id_rsa.pub
   ```

2. **Если ключа нет, создайте его:**
   ```cmd
   ssh-keygen -t ed25519 -C "your_email@example.com"
   ```

3. **Скопируйте публичный ключ:**
   ```cmd
   type %USERPROFILE%\.ssh\id_ed25519.pub
   ```

4. **Добавьте ключ в GitLab:**
   - Settings → SSH Keys
   - Вставьте содержимое публичного ключа

5. **Используйте SSH URL для remote:**
   ```cmd
   git remote set-url origin git@gitlab.com:username/project-name.git
   ```

## 📋 Проверка подключения

### Проверить remote

```cmd
git remote -v
```

### Проверить подключение (SSH)

```cmd
ssh -T git@gitlab.com
```

Должно показать:
```
Welcome to GitLab, @username!
```

### Проверить подключение (HTTPS)

```cmd
git ls-remote origin
```

Должно показать список веток и коммитов.

## 🔄 Работа с GitLab

### Push изменений

```cmd
git add .
git commit -m "Your commit message"
git push
```

### Pull изменений

```cmd
git pull
```

### Создать новую ветку

```cmd
git checkout -b feature/new-feature
git push -u origin feature/new-feature
```

### Синхронизация с GitLab

```cmd
git fetch origin
git merge origin/master
```

## 🐛 Устранение проблем

### Ошибка: "remote origin already exists"

```cmd
git remote remove origin
git remote add origin <your-gitlab-url>
```

### Ошибка: "Permission denied"

1. Проверьте права доступа к репозиторию в GitLab
2. Проверьте аутентификацию (SSH ключ или токен)
3. Убедитесь, что используете правильный URL

### Ошибка: "Repository not found"

1. Проверьте правильность URL репозитория
2. Убедитесь, что репозиторий существует в GitLab
3. Проверьте права доступа

### Изменить URL remote

```cmd
git remote set-url origin <new-url>
```

## 📚 Полезные команды

### Просмотр информации о remote

```cmd
git remote show origin
```

### Удалить remote

```cmd
git remote remove origin
```

### Добавить несколько remotes

```cmd
git remote add gitlab https://gitlab.com/username/project.git
git remote add github https://github.com/username/project.git
```

### Push в конкретный remote

```cmd
git push gitlab master
```

## 🔗 Полезные ссылки

- [GitLab Documentation](https://docs.gitlab.com/)
- [GitLab SSH Keys](https://docs.gitlab.com/ee/user/ssh.html)
- [GitLab Personal Access Tokens](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)

## ✅ Чеклист настройки

- [ ] GitLab репозиторий создан
- [ ] Git репозиторий инициализирован (`git init`)
- [ ] Remote добавлен (`git remote add origin`)
- [ ] Аутентификация настроена (SSH или HTTPS)
- [ ] Подключение проверено (`git remote -v`)
- [ ] Первый коммит создан
- [ ] Push выполнен успешно (`git push -u origin master`)

---

**Готово!** Ваш проект теперь подключен к GitLab. 🎉
