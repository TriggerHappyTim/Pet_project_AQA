@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Git Commit and Push - EVS Testing Framework
echo ========================================
echo.

REM Check if git is available
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git not found in PATH!
    echo Please install Git or add it to PATH.
    echo.
    pause
    exit /b 1
)

REM Change to project directory
cd /d "%~dp0"

REM Show current directory for debugging
echo Current directory: %CD%
echo.

REM Check if this is a git repository
git status >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Current directory is not a git repository!
    echo.
    echo Directory: %CD%
    echo.
    echo Do you want to initialize a new git repository here? (y/n):
    set /p init_choice="Choice: "
    if /i "!init_choice!"=="y" (
        echo.
        echo Initializing git repository...
        git init
        if %errorlevel% equ 0 (
            echo [OK] Git repository initialized.
            echo.
            echo Note: You may want to add a remote repository:
            echo   git remote add origin ^<your-repo-url^>
            echo.
        ) else (
            echo [ERROR] Failed to initialize git repository!
            echo.
            pause
            exit /b 1
        )
    ) else (
        echo Git initialization cancelled.
        echo.
        pause
        exit /b 1
    )
)

echo [1/5] Checking repository status...
git status --short
echo.

REM Check if there are changes
git diff --quiet --exit-code
if %errorlevel% equ 0 (
    git diff --cached --quiet --exit-code
    if %errorlevel% equ 0 (
        echo [INFO] No changes to commit.
        echo.
        set /p push_only="Push existing commits? (y/n): "
        if /i "!push_only!"=="y" (
            goto :push_only
        ) else (
            echo.
            pause
            exit /b 0
        )
    )
)

echo [2/5] Adding all files to staging area...
git add .
if %errorlevel% neq 0 (
    echo [ERROR] Failed to add files to staging area!
    echo.
    pause
    exit /b 1
)
echo [OK] All files added.
echo.

REM Request commit message
echo [3/5] Enter commit message:
set /p commit_message="Message: "

if "!commit_message!"=="" (
    echo [ERROR] Commit message cannot be empty!
    echo.
    pause
    exit /b 1
)

echo.
echo [4/5] Creating commit...
git commit -m "!commit_message!"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to create commit!
    echo.
    pause
    exit /b 1
)

echo [OK] Commit created.
echo.

:push_only
echo [5/5] Pushing to remote repository...
git push
if %errorlevel% equ 0 (
    echo [OK] Push completed successfully!
) else (
    echo [ERROR] Failed to push!
    echo Check remote repository settings and connection.
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Commit and push completed successfully!
echo ========================================
echo.

REM Show last commit
echo Last commit:
git log -1 --pretty=format:"  Hash: %%h%%n  Author: %%an%%n  Date: %%ad%%n  Message: %%s" --date=format:"%%Y-%%m-%%d %%H:%%M:%%S"
echo.

echo Done!
echo.
pause
cmd /k
