@echo off
setlocal enabledelayedexpansion

REM Quick commit with automatic message
REM Usage: commit-quick.bat [message]

REM Change to project directory
cd /d "%~dp0"

REM Check if git is available
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git not found!
    echo.
    pause
    exit /b 1
)

REM Check if this is a git repository
git status >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Current directory is not a git repository!
    echo Run init-git-repo.bat first to initialize git repository.
    echo.
    pause
    exit /b 1
)

REM Get commit message from argument or use date/time
if "%~1"=="" (
    for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value 2^>nul') do set datetime=%%I
    if not defined datetime (
        REM Fallback: use simple date format
        for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set mydate=%%c-%%a-%%b
        for /f "tokens=1-2 delims=: " %%a in ('time /t') do set mytime=%%a:%%b
        set commit_message=Auto commit: !mydate! !mytime!
    ) else (
        set commit_message=Auto commit: !datetime:~0,4!-!datetime:~4,2!-!datetime:~6,2! !datetime:~8,2!:!datetime:~10,2!
    )
) else (
    set commit_message=%~1
)

echo Adding files...
git add . >nul 2>&1

REM Check if there are any changes to commit
git diff --cached --quiet --exit-code
if %errorlevel% equ 0 (
    git diff --quiet --exit-code
    if %errorlevel% equ 0 (
        echo [INFO] No changes to commit. Working tree is clean.
        echo.
        timeout /t 2 >nul
        exit /b 0
    )
)

echo Creating commit: !commit_message!
git commit -m "!commit_message!" 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Git commit failed!
    echo.
    echo Possible reasons:
    echo   - Git repository not initialized
    echo   - Invalid commit message
    echo   - Git configuration issues
    echo.
    pause
    exit /b 1
)

echo [OK] Commit created successfully!
timeout /t 2 >nul
