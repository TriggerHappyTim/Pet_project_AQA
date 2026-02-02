@echo off
REM EVS Testing Framework - Test Runner Script for Windows
REM This script provides convenient commands for running tests

setlocal enabledelayedexpansion

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
echo   -g, --groups GROUPS      Test groups to run (smoke, regression, api, security)
echo   -d, --docker             Run tests in Docker containers
echo   --local                  Run tests locally
echo   --smoke                  Run only smoke tests
echo   --regression             Run regression tests
echo   --security               Run security tests
echo   --api                    Run API tests
echo   --performance            Run performance tests
echo   --allure                 Generate and serve Allure report
echo   -h, --help               Show this help message
echo.
echo Examples:
echo   %0 --smoke --docker          # Run smoke tests in Docker
echo   %0 --regression -b firefox   # Run regression tests with Firefox
echo   %0 --api --local             # Run API tests locally
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
if "%~1"=="" goto :main
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
if "%~1"=="--api" (
    set "TEST_TYPE=api"
    set "TEST_GROUPS=api"
    shift
    goto :parse_args
)
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
    exit /b 0
)
if "%~1"=="--help" (
    call :print_usage
    exit /b 0
)
echo Unknown option: %~1
call :print_usage
exit /b 1

REM Function to run tests locally
:run_tests_local
call :log_info "Running %TEST_TYPE% tests locally with %BROWSER% browser"

set "MVN_CMD=mvn clean test"
set "TEST_CLASS="

if "%TEST_TYPE%"=="smoke" (
    set "TEST_CLASS=*SmokeTest*"
)
if "%TEST_TYPE%"=="regression" (
    set "TEST_CLASS=*RegressionTest*"
)
if "%TEST_TYPE%"=="security" (
    set "TEST_CLASS=*SecurityTest*"
)
if "%TEST_TYPE%"=="api" (
    set "TEST_CLASS=*ApiTest*"
)
if "%TEST_TYPE%"=="performance" (
    set "TEST_CLASS=*PerformanceTest*"
)

if defined TEST_CLASS (
    set "MVN_CMD=%MVN_CMD% -Dtest=%TEST_CLASS%"
)

if defined TEST_GROUPS (
    set "MVN_CMD=%MVN_CMD% -Dgroups=%TEST_GROUPS%"
)

REM Set environment variables
set "ENVIRONMENT=%ENVIRONMENT%"
set "BROWSER=%BROWSER%"
set "THREAD_COUNT=%THREAD_COUNT%"

call :log_info "Executing: %MVN_CMD%"
%MVN_CMD%

if %ERRORLEVEL% equ 0 (
    call :log_success "Tests completed successfully"
    call :generate_allure_report
) else (
    call :log_error "Tests failed"
    exit /b 1
)
goto :eof

REM Function to run tests in Docker
:run_tests_docker
call :log_info "Running %TEST_TYPE% tests in Docker with %BROWSER% browser"

REM Check if Docker is running
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :log_error "Docker is not running. Please start Docker first."
    exit /b 1
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
if "%TEST_TYPE%"=="api" (
    set "TEST_CMD=%TEST_CMD% -Dtest=*ApiTest*"
)
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
call :log_info "EVS Testing Framework Test Runner"
call :log_info "Environment: %ENVIRONMENT%, Browser: %BROWSER%, Threads: %THREAD_COUNT%"

REM Parse arguments
call :parse_args %*

REM Determine execution mode
if "%USE_DOCKER%"=="true" (
    call :run_tests_docker
) else if "%USE_LOCAL%"=="true" (
    call :run_tests_local
) else (
    REM Auto-detect: prefer Docker if available
    docker info >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        call :log_info "Docker detected, running tests in containers"
        call :run_tests_docker
    ) else (
        call :log_info "Docker not available, running tests locally"
        call :run_tests_local
    )
)

goto :eof