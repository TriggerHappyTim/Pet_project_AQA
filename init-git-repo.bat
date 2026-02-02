@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Initialize Git Repository
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

echo Current directory: %CD%
echo.

REM Check if git repository already exists
if exist .git (
    echo [INFO] Git repository already exists in this directory.
    echo.
    echo Current git status:
    git status --short
    echo.
    pause
    exit /b 0
)

echo [1/3] Initializing git repository...
git init
if %errorlevel% neq 0 (
    echo [ERROR] Failed to initialize git repository!
    echo.
    pause
    exit /b 1
)
echo [OK] Git repository initialized.
echo.

echo [2/3] Creating initial commit...
git add .
if %errorlevel% neq 0 (
    echo [WARNING] Some files could not be added.
)

git commit -m "Initial commit"
if %errorlevel% neq 0 (
    echo [WARNING] Initial commit failed or no changes to commit.
) else (
    echo [OK] Initial commit created.
)
echo.

echo [3/3] Repository setup complete!
echo.
echo Next steps:
echo   1. Add remote repository (optional):
echo      git remote add origin ^<your-repo-url^>
echo.
echo   2. Push to remote (if added):
echo      git push -u origin main
echo      or
echo      git push -u origin master
echo.

pause
cmd /k
