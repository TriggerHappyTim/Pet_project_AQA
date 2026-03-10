@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   GitLab Repository Setup
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

REM Check if this is a git repository
git status >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Git repository not initialized.
    echo.
    echo Do you want to initialize git repository? (y/n):
    set /p init_choice="Choice: "
    if /i "!init_choice!"=="y" (
        echo.
        echo Initializing git repository...
        git init
        if %errorlevel% neq 0 (
            echo [ERROR] Failed to initialize git repository!
            echo.
            pause
            exit /b 1
        )
        echo [OK] Git repository initialized.
        echo.
    ) else (
        echo Git initialization cancelled.
        echo.
        pause
        exit /b 1
    )
)

REM Check existing remotes
echo [1/4] Checking existing remotes...
git remote -v
echo.

REM Get GitLab repository URL
echo [2/4] GitLab Repository Configuration
echo.
echo Enter your GitLab repository URL:
echo Examples:
echo   HTTPS: https://gitlab.com/username/project-name.git
echo   HTTPS: https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework.git
echo   SSH:   git@gitlab.com:username/project-name.git
echo.
echo Default (press Enter to use): https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework.git
set /p gitlab_url="GitLab URL: "

if "!gitlab_url!"=="" (
    set gitlab_url=https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework.git
    echo Using default URL: !gitlab_url!
)

if "!gitlab_url!"=="" (
    echo [ERROR] GitLab URL cannot be empty!
    echo.
    pause
    exit /b 1
)

REM Check if origin already exists
git remote get-url origin >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo [WARNING] Remote 'origin' already exists:
    git remote get-url origin
    echo.
    set /p replace_origin="Do you want to replace it? (y/n): "
    if /i "!replace_origin!"=="y" (
        git remote remove origin
        echo [OK] Old remote 'origin' removed.
    ) else (
        echo Remote 'origin' not changed.
        echo.
        pause
        exit /b 0
    )
)

REM Add GitLab remote
echo.
echo [3/4] Adding GitLab remote...
git remote add origin "!gitlab_url!"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to add remote!
    echo.
    pause
    exit /b 1
)
echo [OK] Remote 'origin' added: !gitlab_url!
echo.

REM Verify remote
echo [4/4] Verifying remote configuration...
git remote -v
echo.

REM Ask about initial push
echo ========================================
echo   Repository Setup Complete!
echo ========================================
echo.
set /p push_choice="Do you want to push existing commits to GitLab? (y/n): "
if /i "!push_choice!"=="y" (
    echo.
    echo Pushing to GitLab...
    echo.
    
    REM Detect default branch name
    git branch --show-current >nul 2>&1
    if %errorlevel% equ 0 (
        for /f "tokens=*" %%b in ('git branch --show-current') do set current_branch=%%b
    ) else (
        set current_branch=master
    )
    
    echo Using branch: !current_branch!
    echo.
    
    REM Check if there are commits to push
    git log --oneline >nul 2>&1
    if %errorlevel% equ 0 (
        echo Pushing commits...
        git push -u origin !current_branch!
        if %errorlevel% equ 0 (
            echo.
            echo [OK] Successfully pushed to GitLab!
        ) else (
            echo.
            echo [WARNING] Push failed. You may need to:
            echo   1. Set up authentication (SSH key or credentials)
            echo   2. Check repository permissions
            echo   3. Try: git push -u origin !current_branch!
        )
    ) else (
        echo [INFO] No commits to push. Creating initial commit...
        git add .
        git commit -m "Initial commit"
        if %errorlevel% equ 0 (
            echo [OK] Initial commit created.
            echo.
            echo Pushing to GitLab...
            git push -u origin !current_branch!
            if %errorlevel% equ 0 (
                echo [OK] Successfully pushed to GitLab!
            ) else (
                echo [WARNING] Push failed. Check authentication and permissions.
            )
        )
    )
) else (
    echo.
    echo To push later, use:
    echo   git push -u origin ^<branch-name^>
    echo.
)

echo.
echo ========================================
echo   Setup Complete!
echo ========================================
echo.
echo Next steps:
echo   1. Verify connection: git remote -v
echo   2. Push commits: git push -u origin ^<branch-name^>
echo   3. Check GitLab repository in browser
echo.
pause
cmd /k
