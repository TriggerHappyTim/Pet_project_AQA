@echo off
REM EVS Testing Framework - Test Runner Script for Windows
REM This script provides convenient commands for running tests

REM Устанавливаем кодировку UTF-8 для правильного отображения русского текста
chcp 65001 >nul 2>&1

REM Отключаем автоматическое закрытие окна при ошибках
setlocal enabledelayedexpansion
set "EXIT_CODE=0"

REM Default values (устанавливаем ДО перехода к main)
set "ENVIRONMENT=test"
set "BROWSER=chrome"
set "THREAD_COUNT=1"
set "TEST_TYPE=all"
set "DOCKER_COMPOSE_FILE=docker-compose.yml"

REM Переходим к основной логике, пропуская определения функций
goto :main

REM Colors for output (Windows CMD)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "RESET=[0m"

REM Default values
set "ENVIRONMENT=test"
set "BROWSER=chrome"
set "THREAD_COUNT=1"
set "TEST_TYPE=all"
set "DOCKER_COMPOSE_FILE=docker-compose.yml"

REM Function to print usage
:print_usage
echo Usage: %0 [OPTIONS]
echo.
echo Options:
echo   -e, --environment ENV    Test environment (test, dev, staging, prod) [default: test]
echo   -b, --browser BROWSER    Browser to use (chrome, firefox) [default: chrome]
echo   -t, --threads COUNT      Number of parallel threads [default: 1]
echo   -g, --groups GROUPS      Test groups to run (smoke, regression, security)  REM api - ЗАКОММЕНТИРОВАНО: API тесты не используются
echo   -d, --docker             Run tests in Docker containers
echo   --local                  Run tests locally
echo   --smoke                  Run only smoke tests
echo   --regression             Run regression tests
echo   --security               Run security tests
REM echo   --api                    Run API tests  REM ЗАКОММЕНТИРОВАНО: API тесты не используются
echo   --performance            Run performance tests
echo   --allure                 Generate and serve Allure report
echo   -h, --help               Show this help message
echo.
echo Examples:
echo   %0 --smoke --docker          # Run smoke tests in Docker
echo   %0 --regression -b firefox   # Run regression tests with Firefox
REM echo   %0 --api --local             # Run API tests locally  REM ЗАКОММЕНТИРОВАНО: API тесты не используются
echo   %0 --allure                  # Generate Allure report
goto :eof

REM Function to log messages
:log_info
echo %BLUE%[INFO]%RESET% %~1
goto :eof

:log_warn
echo %YELLOW%[WARN]%RESET% %~1
goto :eof

:log_error
echo %RED%[ERROR]%RESET% %~1
goto :eof

:log_success
echo %GREEN%[SUCCESS]%RESET% %~1
goto :eof

REM Parse command line arguments
:parse_args
REM Если аргументов нет, просто возвращаемся (не завершаем скрипт)
if "%~1"=="" goto :eof
if "%~1"=="-e" (
    set "ENVIRONMENT=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="--environment" (
    set "ENVIRONMENT=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="-b" (
    set "BROWSER=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="--browser" (
    set "BROWSER=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="-t" (
    set "THREAD_COUNT=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="--threads" (
    set "THREAD_COUNT=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="-g" (
    set "TEST_GROUPS=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="--groups" (
    set "TEST_GROUPS=%~2"
    shift & shift
    goto :parse_args
)
if "%~1"=="-d" (
    set "USE_DOCKER=true"
    shift
    goto :parse_args
)
if "%~1"=="--docker" (
    set "USE_DOCKER=true"
    shift
    goto :parse_args
)
if "%~1"=="--local" (
    set "USE_LOCAL=true"
    shift
    goto :parse_args
)
if "%~1"=="--smoke" (
    set "TEST_TYPE=smoke"
    set "TEST_GROUPS=smoke"
    shift
    goto :parse_args
)
if "%~1"=="--regression" (
    set "TEST_TYPE=regression"
    set "TEST_GROUPS=regression"
    shift
    goto :parse_args
)
if "%~1"=="--security" (
    set "TEST_TYPE=security"
    set "TEST_GROUPS=security"
    shift
    goto :parse_args
)
REM ЗАКОММЕНТИРОВАНО: API тесты не используются
REM if "%~1"=="--api" (
REM     set "TEST_TYPE=api"
REM     set "TEST_GROUPS=api"
REM     shift
REM     goto :parse_args
REM )
if "%~1"=="--performance" (
    set "TEST_TYPE=performance"
    set "TEST_GROUPS=performance"
    shift
    goto :parse_args
)
if "%~1"=="--allure" (
    set "GENERATE_ALLURE=true"
    shift
    goto :parse_args
)
if "%~1"=="-h" (
    call :print_usage
    set "EXIT_CODE=0"
    goto :end_script
)
if "%~1"=="--help" (
    call :print_usage
    set "EXIT_CODE=0"
    goto :end_script
)
echo Unknown option: %~1
call :print_usage
set "EXIT_CODE=1"
goto :end_script

REM Function to run tests locally
:run_tests_local
call :log_info "Running %TEST_TYPE% tests locally with %BROWSER% browser"

REM Переходим в директорию проекта (где находится pom.xml)
cd /d "%~dp0.." 2>nul

REM Проверяем, что Maven доступен (проверка уже была в :main, но на всякий случай)
if "%MAVEN_AVAILABLE%"=="0" (
    call :log_error "Maven is not available!"
    exit /b 1
)

REM Используем сохраненную команду Maven
set "TEST_CLASS="
set "MVN_BASE_CMD="
set "MVN_ARGS=clean test"

if "%MVN_CMD%"=="mvnw.cmd" (
    call :log_info "Using Maven Wrapper (mvnw.cmd)"
    set "MVN_BASE_CMD=mvnw.cmd"
) else if defined MAVEN_PATH (
    REM Используем полный путь к Maven, если он был найден
    REM Обязательно оборачиваем в кавычки, так как путь может содержать пробелы
    call :log_info "Using Maven from: %MAVEN_PATH%"
    set "MVN_BASE_CMD=%MAVEN_PATH%"
) else (
    call :log_info "Using system Maven"
    set "MVN_BASE_CMD=mvn"
)

if "%TEST_TYPE%"=="smoke" (
    set "TEST_CLASS=*SmokeTest*"
)
if "%TEST_TYPE%"=="regression" (
    set "TEST_CLASS=*RegressionTest*"
)
if "%TEST_TYPE%"=="security" (
    set "TEST_CLASS=*SecurityTest*"
)
REM ЗАКОММЕНТИРОВАНО: API тесты не используются
REM if "%TEST_TYPE%"=="api" (
REM     set "TEST_CLASS=*ApiTest*"
REM )
if "%TEST_TYPE%"=="performance" (
    set "TEST_CLASS=*PerformanceTest*"
)

if defined TEST_CLASS (
    set "MVN_ARGS=%MVN_ARGS% -Dtest=%TEST_CLASS%"
)

if defined TEST_GROUPS (
    set "MVN_ARGS=%MVN_ARGS% -Dgroups=%TEST_GROUPS%"
)

REM Формируем финальную команду с правильным экранированием
REM В batch файлах кавычки внутри set нужно экранировать по-особому
if defined MAVEN_PATH (
    REM Если путь содержит пробелы, используем отдельные переменные для команды и аргументов
    REM Это более надежный способ для путей с пробелами
    set "MVN_EXE=%MVN_BASE_CMD%"
    set "MVN_CMD=%MVN_EXE% %MVN_ARGS%"
) else (
    REM Для обычных команд без пробелов кавычки не нужны
    set "MVN_CMD=%MVN_BASE_CMD% %MVN_ARGS%"
)

REM Set environment variables
set "ENVIRONMENT=%ENVIRONMENT%"
set "BROWSER=%BROWSER%"
set "THREAD_COUNT=%THREAD_COUNT%"

call :log_info "Executing: %MVN_CMD%"
REM Убеждаемся, что мы в правильной директории (где находится pom.xml)
if not exist "pom.xml" (
    call :log_error "pom.xml not found in current directory: %CD%"
    exit /b 1
)
REM Выполняем команду Maven
REM Если путь содержит пробелы, используем кавычки при выполнении
if defined MAVEN_PATH (
    REM Путь с пробелами - оборачиваем только исполняемый файл в кавычки
    "%MVN_EXE%" %MVN_ARGS%
) else (
    REM Обычная команда без пробелов
    %MVN_CMD%
)
set "MVN_EXIT_CODE=%ERRORLEVEL%"

if %MVN_EXIT_CODE% equ 0 (
    call :log_success "Tests completed successfully"
    call :generate_allure_report
    exit /b 0
) else (
    call :log_error "Tests failed with exit code %MVN_EXIT_CODE%"
    if %MVN_EXIT_CODE% equ 9009 (
        call :log_error "Command not found. Maven may not be in PATH or accessible."
        call :log_info "Try running from IntelliJ IDEA terminal or add Maven to PATH"
    )
    exit /b %MVN_EXIT_CODE%
)
goto :eof

REM Function to run tests in Docker
:run_tests_docker
call :log_info "Running %TEST_TYPE% tests in Docker with %BROWSER% browser"

REM Check if Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :log_error "Docker is not running. Please start Docker first."
    call :log_info "Switching to local execution mode..."
    REM Переключаемся на локальный режим вместо завершения
    call :run_tests_local
    set "EXIT_CODE=%ERRORLEVEL%"
    goto :eof
)

REM Build the test image
call :log_info "Building Docker image..."
docker build -t evs-testing-framework .

REM Set environment variables for Docker
set "ENV_VARS=-e ENVIRONMENT=%ENVIRONMENT% -e BROWSER=%BROWSER% -e THREAD_COUNT=%THREAD_COUNT%"

REM Add credentials if available
if exist ".env" (
    set "ENV_VARS=%ENV_VARS% --env-file .env"
)

set "TEST_CMD=mvn clean test"

if "%TEST_TYPE%"=="smoke" (
    set "TEST_CMD=%TEST_CMD% -Dtest=*SmokeTest*"
)
if "%TEST_TYPE%"=="regression" (
    set "TEST_CMD=%TEST_CMD% -Dtest=*RegressionTest*"
)
if "%TEST_TYPE%"=="security" (
    set "TEST_CMD=%TEST_CMD% -Dtest=*SecurityTest*"
)
REM ЗАКОММЕНТИРОВАНО: API тесты не используются
REM if "%TEST_TYPE%"=="api" (
REM     set "TEST_CMD=%TEST_CMD% -Dtest=*ApiTest*"
REM )
if "%TEST_TYPE%"=="performance" (
    set "TEST_CMD=%TEST_CMD% -Dtest=*PerformanceTest*"
)

if defined TEST_GROUPS (
    set "TEST_CMD=%TEST_CMD% -Dgroups=%TEST_GROUPS%"
)

REM Run tests in Docker
call :log_info "Starting Docker containers..."
docker-compose up -d selenium-hub chrome-node firefox-node allure-server

call :log_info "Waiting for Selenium Grid to be ready..."
timeout /t 10 /nobreak >nul

call :log_info "Running tests..."
docker run --rm --network container:selenium-hub -v "%cd%":/app -v "%cd%/allure-results":/app/allure-results %ENV_VARS% evs-testing-framework bash -c "cd /app && %TEST_CMD%"

if %ERRORLEVEL% equ 0 (
    call :log_success "Tests completed successfully"
    call :generate_allure_report
) else (
    call :log_error "Tests failed"
)

REM Cleanup
call :log_info "Cleaning up Docker containers..."
docker-compose down

if %ERRORLEVEL% equ 0 (
    set "EXIT_CODE=0"
) else (
    set "EXIT_CODE=%ERRORLEVEL%"
)

goto :eof

REM Function to generate Allure report
:generate_allure_report
if "%GENERATE_ALLURE%"=="true" (
    call :log_info "Generating Allure report..."
    REM Check if allure command is available
    where allure >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        allure generate allure-results --clean -o allure-report
        REM Note: allure open doesn't work well in batch scripts
        call :log_success "Allure report generated"
    ) else (
        call :log_warn "Allure CLI not found. Install it to generate reports: https://docs.qameta.io/allure/"
    )
)
goto :eof

REM Main execution
:main
REM Переходим в директорию проекта (где находится pom.xml)
cd /d "%~dp0.." 2>nul

call :log_info "EVS Testing Framework Test Runner"

REM Parse arguments first
call :parse_args %*
if %EXIT_CODE% NEQ 0 goto :end_script

REM Выводим переменные ПОСЛЕ парсинга аргументов
call :log_info "Environment: %ENVIRONMENT%, Browser: %BROWSER%, Threads: %THREAD_COUNT%"

REM Проверяем наличие Maven или Maven Wrapper ПОСЛЕ парсинга аргументов
REM (чтобы можно было использовать --help без проверки Maven)
REM Сохраняем результат проверки в переменную для использования в функциях
set "MAVEN_AVAILABLE=0"
if exist "mvnw.cmd" (
    set "MAVEN_AVAILABLE=1"
    set "MVN_CMD=mvnw.cmd"
    call :log_info "Maven Wrapper found (mvnw.cmd)"
) else (
    REM Проверяем наличие Maven в PATH и его работоспособность
    REM Используем полный путь к Maven для надежности
    for /f "delims=" %%i in ('where mvn 2^>nul') do set "MAVEN_PATH=%%i"
    
    REM Если Maven не найден в PATH, пытаемся найти его в стандартных местах
    if not defined MAVEN_PATH (
        REM Проверяем переменные окружения
        if defined IDEA_MVN (
            if exist "%IDEA_MVN%" (
                set "MAVEN_PATH=%IDEA_MVN%"
                call :log_info "Found Maven via IDEA_MVN: %IDEA_MVN%"
            )
        )
        if not defined MAVEN_PATH (
            if defined MAVEN_HOME (
                if exist "%MAVEN_HOME%\bin\mvn.cmd" (
                    set "MAVEN_PATH=%MAVEN_HOME%\bin\mvn.cmd"
                    call :log_info "Found Maven via MAVEN_HOME: %MAVEN_PATH%"
                ) else if exist "%MAVEN_HOME%\bin\mvn.bat" (
                    set "MAVEN_PATH=%MAVEN_HOME%\bin\mvn.bat"
                    call :log_info "Found Maven via MAVEN_HOME: %MAVEN_PATH%"
                )
            )
        )
        
        REM Если все еще не найден, пытаемся найти в стандартных местах установки IntelliJ IDEA
        if not defined MAVEN_PATH (
            REM Проверяем встроенный Maven IntelliJ IDEA (обычно в plugins/maven/lib/maven3)
            if defined LOCALAPPDATA (
                call :log_info "Searching for IntelliJ IDEA Maven in: %LOCALAPPDATA%\JetBrains\"
                for /d %%d in ("%LOCALAPPDATA%\JetBrains\*") do (
                    if not defined MAVEN_PATH (
                        if exist "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" (
                            set "MAVEN_PATH=%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
                            call :log_info "Found IntelliJ IDEA Maven: %MAVEN_PATH%"
                        )
                    )
                )
            )
        )
        
        REM Также проверяем в Program Files (для установленных версий IntelliJ IDEA)
        if not defined MAVEN_PATH (
            if defined PROGRAMFILES (
                for /d %%d in ("%PROGRAMFILES%\JetBrains\*") do (
                    if not defined MAVEN_PATH (
                        if exist "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" (
                            set "MAVEN_PATH=%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
                            call :log_info "Found IntelliJ IDEA Maven in Program Files: %MAVEN_PATH%"
                        )
                    )
                )
            )
        )
    ) else (
        call :log_info "Found Maven in PATH: %MAVEN_PATH%"
    )
    
    if defined MAVEN_PATH (
        REM Дополнительная проверка: пытаемся выполнить mvn --version
        "%MAVEN_PATH%" --version >nul 2>&1
        if %ERRORLEVEL% equ 0 (
            set "MAVEN_AVAILABLE=1"
            REM Проверяем, содержит ли путь mvn.cmd или mvn.bat (используем findstr для надежности)
            echo "%MAVEN_PATH%" | findstr /i /c:"mvn.cmd" >nul 2>&1
            if %ERRORLEVEL% equ 0 (
                set "MVN_CMD=%MAVEN_PATH%"
            ) else (
                echo "%MAVEN_PATH%" | findstr /i /c:"mvn.bat" >nul 2>&1
                if %ERRORLEVEL% equ 0 (
                    set "MVN_CMD=%MAVEN_PATH%"
                ) else (
                    REM Если это просто "mvn" без расширения, используем как есть
                    set "MVN_CMD=mvn"
                )
            )
            call :log_info "Maven found and verified: %MAVEN_PATH%"
        ) else (
            call :log_error "Maven found but not working: %MAVEN_PATH%"
            call :log_info "Please check your Maven installation"
            set "EXIT_CODE=1"
            goto :end_script
        )
    ) else (
        call :log_error "Maven is not found in PATH!"
        call :log_info "Please ensure Maven is installed and added to PATH"
        call :log_info "Or run this script from IntelliJ IDEA terminal (Maven is built-in)"
        call :log_info "Or add mvnw.cmd (Maven Wrapper) to project root"
        set "EXIT_CODE=1"
        goto :end_script
    )
)

REM Determine execution mode
if "%USE_DOCKER%"=="true" (
    call :run_tests_docker
    set "EXIT_CODE=%ERRORLEVEL%"
) else if "%USE_LOCAL%"=="true" (
    call :run_tests_local
    set "EXIT_CODE=%ERRORLEVEL%"
) else (
    REM Auto-detect: prefer Docker if available
    docker info >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        call :log_info "Docker detected, running tests in containers"
        call :run_tests_docker
        set "EXIT_CODE=%ERRORLEVEL%"
    ) else (
        call :log_info "Docker not available, running tests locally"
        call :run_tests_local
        set "EXIT_CODE=%ERRORLEVEL%"
    )
)

REM Всегда переходим к паузе, даже при ошибках
goto :end_script

REM Пауза в конце, чтобы окно не закрывалось сразу
REM Выполняется всегда, даже при ошибках
:end_script
echo.
echo ========================================
echo Press any key to exit...
echo ========================================
REM Сохраняем код выхода перед endlocal (endlocal удаляет все локальные переменные)
set "FINAL_EXIT_CODE=%EXIT_CODE%"
REM Принудительная пауза - окно не закроется
pause >nul 2>&1
endlocal
REM Восстанавливаем код выхода после endlocal
if "%FINAL_EXIT_CODE%"=="" set "FINAL_EXIT_CODE=0"
if "%FINAL_EXIT_CODE%" NEQ "0" (
    exit /b %FINAL_EXIT_CODE%
) else (
    exit /b 0
)

REM ========================================
REM Скрипт для локального запуска в стиле GitLab CI/CD
REM См. файл: scripts/run-local-gitlab-style.bat
REM ========================================