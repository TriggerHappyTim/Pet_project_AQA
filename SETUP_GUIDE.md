# Настройка среды EVS Testing Framework

## 🔧 Системные требования

### Минимальные требования

| Компонент | Минимальная версия | Рекомендуемая версия |
|-----------|-------------------|---------------------|
| **Java** | 11.0 | 17.0+ |
| **Maven** | 3.6.0 | 3.8.0+ |
| **Chrome** | 90.0 | 120.0+ |
| **Firefox** | 88.0 | 115.0+ |
| **RAM** | 4 GB | 8 GB+ |
| **Disk** | 2 GB | 10 GB+ |
| **OS** | Windows 10/Linux/MacOS | Windows 11/Ubuntu 20.04+ |

### Поддерживаемые операционные системы

- ✅ **Windows 10/11**
- ✅ **Ubuntu 18.04+**
- ✅ **CentOS 7+**
- ✅ **macOS 10.15+**
- ✅ **Docker containers**

## 📦 Установка

### 1. Установка Java

#### Windows
```bash
# Скачать JDK с официального сайта
# https://adoptium.net/

# Проверить установку
java -version
# openjdk version "17.0.8" 2023-07-18

javac -version
# javac 17.0.8
```

#### Linux (Ubuntu/Debian)
```bash
# Обновить пакеты
sudo apt update

# Установить OpenJDK
sudo apt install openjdk-17-jdk

# Проверить установку
java -version
javac -version
```

#### macOS
```bash
# Используя Homebrew
brew install openjdk@17

# Добавить в PATH
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Проверить установку
java -version
```

### 2. Установка Maven

#### Windows
```bash
# Скачать с официального сайта
# https://maven.apache.org/download.cgi

# Распаковать в C:\apache-maven-3.9.9\
# Добавить в PATH: C:\apache-maven-3.9.9\bin

# Проверить установку
mvn -version
# Apache Maven 3.9.9
```

#### Linux/macOS
```bash
# Скачать и распаковать
wget https://downloads.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar xzf apache-maven-3.9.9-bin.tar.gz
sudo mv apache-maven-3.9.9 /opt/

# Добавить в PATH
export PATH=/opt/apache-maven-3.9.9/bin:$PATH

# Проверить установку
mvn -version
```

### 3. Установка браузеров

#### Chrome (рекомендуемый)

```bash
# Windows
# Скачать и установить Chrome с официального сайта

# Проверить версию
chrome://version
# Версия должна быть 120.0+

# Linux
wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" | sudo tee /etc/apt/sources.list.d/google-chrome.list
sudo apt update
sudo apt install google-chrome-stable

# macOS
brew install --cask google-chrome
```

#### Firefox

```bash
# Windows/Linux
# Скачать с официального сайта Mozilla

# macOS
brew install --cask firefox
```

### 4. Установка Git

```bash
# Windows
# Скачать Git for Windows
# https://gitforwindows.org/

# Linux
sudo apt install git

# macOS
brew install git

# Проверить установку
git --version
# git version 2.39.0
```

## 🚀 Настройка проекта

### 1. Клонирование репозитория

```bash
# HTTPS
git clone https://github.com/your-org/evs-testing-framework.git

# SSH
git clone git@github.com:your-org/evs-testing-framework.git

cd evs-testing-framework
```

### 2. Проверка структуры проекта

```bash
# Проверить наличие необходимых файлов
ls -la

# Должен содержать:
# - pom.xml
# - src/test/java/
# - src/test/resources/
# - README.md
# - .gitignore
```

### 3. Компиляция проекта

```bash
# Очистка и компиляция
mvn clean compile

# С тестами (не рекомендуется для первого запуска)
mvn clean test-compile
```

### 4. Проверка зависимостей

```bash
# Скачать все зависимости
mvn dependency:resolve

# Проверить дерево зависимостей
mvn dependency:tree
```

## ⚙️ Конфигурация

### 1. Основная конфигурация

#### `pom.xml` - Maven конфигурация

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bft</groupId>
    <artifactId>evs-testing-framework</artifactId>
    <version>2.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Версии зависимостей -->
        <selenide.version>7.0.2</selenide.version>
        <testng.version>7.8.0</testng.version>
        <allure.version>2.24.0</allure.version>
        <slf4j.version>2.0.9</slf4j.version>
        <logback.version>1.4.11</logback.version>
    </properties>

    <dependencies>
        <!-- Selenide -->
        <dependency>
            <groupId>com.codeborne</groupId>
            <artifactId>selenide</artifactId>
            <version>${selenide.version}</version>
        </dependency>

        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Allure -->
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-testng</artifactId>
            <version>${allure.version}</version>
        </dependency>

        <!-- Логирование -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>

        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>${logback.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>

            <plugin>
                <groupId>io.qameta.allure</groupId>
                <artifactId>allure-maven</artifactId>
                <version>2.12.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. Конфигурация TestNG

#### `testng.xml` - конфигурация тестов

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suite name="EVS Testing Suite" verbose="1" parallel="methods" thread-count="3">
    <listeners>
        <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
    </listeners>

    <test name="UI Tests" group-by-instances="true">
        <groups>
            <run>
                <include name="smoke"/>
                <include name="ui"/>
            </run>
        </groups>

        <classes>
            <class name="com.bft.LK_Insurence.CryptoProCertificateTest"/>
            <class name="com.bft.test.examples.ImprovedTestExamples"/>
        </classes>
    </test>

    <test name="API Tests" group-by-instances="true">
        <groups>
            <run>
                <include name="api"/>
                <include name="integration"/>
            </run>
        </groups>

        <classes>
            <!-- API тесты -->
        </classes>
    </test>
</suite>
```

### 3. Конфигурация логирования

#### `src/test/resources/logback-test.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="ALLURE" class="io.qameta.allure.logback.AllureLogbackAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.bft" level="DEBUG" additivity="false">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="ALLURE"/>
    </logger>

    <logger name="com.codeborne.selenide" level="INFO"/>
    <logger name="org.openqa.selenium" level="WARN"/>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### 4. Переменные окружения

#### `.env` файл (для локальной разработки)

```bash
# Браузер
TEST_BROWSER=chrome
TEST_BROWSER_HEADLESS=false

# URL приложения
TEST_BASE_URL=https://portal.test.ecp
TEST_API_URL=https://api.test.ecp

# Учетные данные (шифрованные)
TEST_USERNAME=encrypted_username
TEST_PASSWORD=encrypted_password
TEST_ADMIN_USERNAME=encrypted_admin_username
TEST_ADMIN_PASSWORD=encrypted_admin_password

# Таймауты (секунды)
TEST_IMPLICIT_WAIT=10
TEST_PAGE_LOAD_TIMEOUT=30
TEST_SCRIPT_TIMEOUT=10

# База данных (для интеграционных тестов)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=test_db
DB_USERNAME=test_user
DB_PASSWORD=test_password

# Allure конфигурация
ALLURE_RESULTS_DIR=allure-results
ALLURE_REPORT_DIR=allure-report

# Логирование
LOG_LEVEL=DEBUG
LOG_FILE=test.log

# Selenium Grid (для распределенного выполнения)
SELENIUM_HUB_URL=http://localhost:4444/wd/hub
SELENIUM_GRID_ENABLED=false

# Безопасность
ENCRYPTION_KEY=your-encryption-key-here
MASK_SENSITIVE_DATA=true
```

## 🐳 Docker настройка

### 1. Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

# Установка необходимых пакетов
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Установка Google Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Создание пользователя для тестов
RUN useradd -m -s /bin/bash testuser \
    && chown -R testuser:testuser /home/testuser

USER testuser
WORKDIR /home/testuser

# Копирование проекта
COPY --chown=testuser:testuser . /home/testuser/evs-testing-framework/

# Рабочая директория
WORKDIR /home/testuser/evs-testing-framework

# Компиляция проекта
RUN mvn clean compile -DskipTests

# Запуск тестов (переопределяется в docker-compose)
CMD ["mvn", "clean", "test"]
```

### 2. docker-compose.yml

```yaml
version: '3.8'

services:
  # Selenium Hub
  selenium-hub:
    image: selenium/hub:4.0
    container_name: evs-selenium-hub
    ports:
      - "4444:4444"
    environment:
      - GRID_MAX_SESSION=10
      - GRID_TIMEOUT=300
    networks:
      - evs-network

  # Chrome nodes
  chrome:
    image: selenium/node-chrome:4.0
    container_name: evs-chrome-node
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4443
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4442
      - SE_NODE_MAX_SESSIONS=3
      - SE_NODE_OVERRIDE_MAX_SESSIONS=true
    volumes:
      - /dev/shm:/dev/shm
    networks:
      - evs-network

  # Firefox nodes
  firefox:
    image: selenium/node-firefox:4.0
    container_name: evs-firefox-node
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4443
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4442
      - SE_NODE_MAX_SESSIONS=3
      - SE_NODE_OVERRIDE_MAX_SESSIONS=true
    volumes:
      - /dev/shm:/dev/shm
    networks:
      - evs-network

  # Allure сервер
  allure:
    image: frankescobar/allure-docker-service:2.24.0
    container_name: evs-allure-server
    ports:
      - "5050:5050"
    environment:
      - CHECK_RESULTS_EVERY_SECONDS=1
      - KEEP_HISTORY=1
      - KEEP_HISTORY_LATEST=25
    volumes:
      - ./allure-results:/app/allure-results
    networks:
      - evs-network

  # Тесты
  tests:
    build: .
    container_name: evs-tests
    depends_on:
      - selenium-hub
      - chrome
      - firefox
    environment:
      - REMOTE_URL=http://selenium-hub:4444/wd/hub
      - TEST_BROWSER=chrome
      - TEST_BROWSER_HEADLESS=true
      - ALLURE_RESULTS_DIR=allure-results
    volumes:
      - ./allure-results:/home/testuser/evs-testing-framework/allure-results
    networks:
      - evs-network

networks:
  evs-network:
    driver: bridge
```

### 3. Запуск в Docker

```bash
# Сборка образов
docker-compose build

# Запуск инфраструктуры
docker-compose up -d selenium-hub chrome firefox allure

# Запуск тестов
docker-compose up tests

# Просмотр отчетов
# http://localhost:5050
```

## 🔐 Настройка безопасности

### 1. Шифрование учетных данных

```java
// Генерация ключа шифрования
String encryptionKey = EncryptionUtils.generateKey();
System.setProperty("ENCRYPTION_KEY", encryptionKey);

// Шифрование пароля
String encryptedPassword = EncryptionUtils.encrypt("mySecretPassword", encryptionKey);

// Использование в коде
String decryptedPassword = EncryptionUtils.decrypt(encryptedPassword, encryptionKey);
```

### 2. Конфигурация маскировки данных

```java
// В test.properties
mask.sensitive.data=true
mask.patterns=email,phone,card,token,password

// Или программно
DataMaskerConfigurer configurer = new DataMaskerConfigurer()
    .enableMasking(true)
    .addPattern("email", Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"))
    .addPattern("phone", Pattern.compile("\\+7\\(\\d{3}\\)\\d{3}-\\d{2}-\\d{2}"))
    .configure();
```

### 3. Защищенное хранение секретов

```bash
# Использование HashiCorp Vault или AWS Secrets Manager
export VAULT_ADDR=https://vault.example.com
export VAULT_TOKEN=your-vault-token

# Или локально зашифрованные файлы
# credentials.enc
# encryption.key
```

## 📊 Мониторинг и логирование

### 1. Настройка ELK стека

```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  logstash:
    image: logstash:8.11.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5044:5044"

  kibana:
    image: kibana:8.11.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
```

### 2. Метрики производительности

```java
// В pom.xml добавить
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <version>1.11.0</version>
</dependency>

// В коде
MeterRegistry registry = new SimpleMeterRegistry();

// Метрики выполнения тестов
Timer.Sample sample = Timer.start(registry);
try {
    // тестовая логика
} finally {
    sample.stop(Timer.builder("test.execution")
        .tag("test.class", this.getClass().getSimpleName())
        .tag("test.method", testMethod.getName())
        .register(registry));
}
```

## 🚀 CI/CD интеграция

### 1. GitLab CI

```gitlab-ci
stages:
  - build
  - test
  - report
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  DOCKER_DRIVER: overlay2

build:
  stage: build
  image: maven:3.9.0-openjdk-17
  script:
    - mvn clean compile -DskipTests
  cache:
    paths:
      - .m2/repository
  artifacts:
    paths:
      - target/
    expire_in: 1 hour

test:ui:
  stage: test
  image: maven:3.9.0-openjdk-17
  services:
    - selenium/standalone-chrome:4.0
  variables:
    REMOTE_URL: http://selenium__standalone-chrome:4444/wd/hub
    TEST_BROWSER: chrome
  script:
    - mvn test -Dgroups=ui
  artifacts:
    reports:
      allure: allure-results/
    paths:
      - target/surefire-reports/
    expire_in: 1 hour

test:api:
  stage: test
  image: maven:3.9.0-openjdk-17
  script:
    - mvn test -Dgroups=api
  artifacts:
    reports:
      allure: allure-results/
    paths:
      - target/surefire-reports/
    expire_in: 1 hour

report:
  stage: report
  image: maven:3.9.0-openjdk-17
  script:
    - mvn allure:aggregate
  dependencies:
    - test:ui
    - test:api
  artifacts:
    paths:
      - allure-report/
    expire_in: 7 days

deploy:staging:
  stage: deploy
  script:
    - echo "Deploy to staging"
  environment:
    name: staging
    url: https://staging.example.com
  only:
    - develop

deploy:production:
  stage: deploy
  script:
    - echo "Deploy to production"
  environment:
    name: production
    url: https://example.com
  when: manual
  only:
    - main
```

### 2. Jenkins Pipeline

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven 3.9.0'
        jdk 'OpenJDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'git-credentials',
                    url: 'https://github.com/your-org/evs-testing-framework.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -Dgroups=unit'
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'mvn test -Dgroups=integration'
            }
        }

        stage('UI Tests') {
            steps {
                script {
                    docker.image('selenium/standalone-chrome:4.0').withRun('-p 4444:4444') { c ->
                        sh 'mvn test -Dgroups=ui -Dremote.url=http://localhost:4444/wd/hub'
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                sh 'mvn allure:aggregate'
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'allure-report',
                    reportFiles: 'index.html',
                    reportName: 'Allure Report'
                ])
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'allure-results/**', fingerprint: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

## 🔧 Устранение неполадок

### Распространенные проблемы

#### 1. Тесты не запускаются

```bash
# Проверить Java
java -version

# Проверить Maven
mvn -version

# Проверить зависимости
mvn dependency:resolve

# Очистить локальный репозиторий
rm -rf ~/.m2/repository/com/bft
mvn clean install
```

#### 2. Браузер не запускается

```bash
# Проверить WebDriver
mvn exec:java -Dexec.mainClass="io.github.bonigarcia.wdm.WebDriverManager"

# Проверить версию браузера
google-chrome --version

# Запустить в headless режиме
System.setProperty("selenide.headless", "true");
```

#### 3. Таймауты соединения

```java
// Увеличить таймауты
Configuration.timeout = 10000;        // 10 секунд
Configuration.pageLoadTimeout = 30000; // 30 секунд
Configuration.browserSize = "1920x1080";
```

#### 4. Проблемы с Allure

```bash
# Проверить версию
mvn allure:version

# Очистить результаты
rm -rf allure-results/
rm -rf allure-report/

# Перегенерировать отчет
mvn clean test
mvn allure:report
```

#### 5. Память JVM

```bash
# Увеличить heap size
export MAVEN_OPTS="-Xmx2048m -Xms1024m"
mvn test
```

### Debug режим

```bash
# Включить отладку Selenide
System.setProperty("selenide.reports", "target/screenshots");
Configuration.reportsFolder = "target/screenshots";
Configuration.savePageSource = false;
Configuration.screenshots = true;

# Логи WebDriver
System.setProperty("webdriver.chrome.verboseLogging", "true");
```

## 📞 Поддержка

### Ресурсы

- **Документация**: [docs.evs-testing-framework.com](https://docs.evs-testing-framework.com)
- **Примеры кода**: [examples/](examples/)
- **Вопросы**: [GitHub Discussions](https://github.com/evs-testing-framework/discussions)
- **Issues**: [GitHub Issues](https://github.com/evs-testing-framework/issues)

### Сообщество

- **Slack**: #evs-testing-framework
- **Telegram**: @evs_testing_chat
- **Forum**: [Community Forum](https://forum.evs-testing-framework.com)
- **Mailing list**: evs-testing-framework@googlegroups.com

---

После настройки среды вы готовы к запуску автоматизированных тестов! Убедитесь, что все зависимости установлены и конфигурация корректна перед запуском. 🚀</contents>
</xai:function_call">Создал подробное руководство по настройке среды