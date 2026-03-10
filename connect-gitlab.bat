@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Connect to GitLab Repository
echo ========================================
echo.
echo Repository: https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework
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
)

REM Check existing remotes
echo [1/4] Checking existing remotes...
git remote -v
echo.

REM Check if origin already exists
git remote get-url origin >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Remote 'origin' already exists:
    git remote get-url origin
    echo.
    set /p replace_origin="Do you want to replace it with GitLab URL? (y/n): "
    if /i "!replace_origin!"=="y" (
        git remote remove origin
        echo [OK] Old remote 'origin' removed.
    ) else (
        echo Remote 'origin' not changed.
        echo.
        echo Current remote configuration:
        git remote -v
        echo.
        pause
        exit /b 0
    )
)

REM Add GitLab remote
echo.
echo [2/4] Adding GitLab remote...
set GITLAB_URL=https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework.git
git remote add origin "!GITLAB_URL!"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to add remote!
    echo.
    pause
    exit /b 1
)
echo [OK] Remote 'origin' added: !GITLAB_URL!
echo.

REM Verify remote
echo [3/4] Verifying remote configuration...
git remote -v
echo.

REM Check if there are commits
echo [4/4] Checking repository status...
git log --oneline >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] No commits found. You may want to create initial commit.
    echo.
    set /p create_commit="Create initial commit? (y/n): "
    if /i "!create_commit!"=="y" (
        echo.
        echo Creating initial commit...
        git add .
        git commit -m "Initial commit - EVS Testing Framework"
        if %errorlevel% equ 0 (
            echo [OK] Initial commit created.
        ) else (
            echo [WARNING] Initial commit failed or no changes to commit.
        )
        echo.
    )
)

REM Show status
git status --short
echo.

REM Ask about push
echo ========================================
echo   Repository Connected Successfully!
echo ========================================
echo.
echo Next steps:
echo   1. Verify connection: git remote -v
echo   2. Fetch from GitLab: git fetch origin
echo   3. Push commits: git push -u origin master
echo      or: git push -u origin main
echo.
set /p push_choice="Do you want to push to GitLab now? (y/n): "
if /i "!push_choice!"=="y" (
    echo.
    echo Fetching from GitLab first...
    git fetch origin
    echo.
    
    REM Detect default branch name
    git branch --show-current >nul 2>&1
    if %errorlevel% equ 0 (
        for /f "tokens=*" %%b in ('git branch --show-current') do set current_branch=%%b
    ) else (
        REM Try to detect from remote
        git ls-remote --heads origin master >nul 2>&1
        if %errorlevel% equ 0 (
            set current_branch=master
        ) else (
            git ls-remote --heads origin main >nul 2>&1
            if %errorlevel% equ 0 (
                set current_branch=main
            ) else (
                set current_branch=master
            )
        )
    )
    
    echo Using branch: !current_branch!
    echo.
    echo Pushing to GitLab...
    git push -u origin !current_branch!
    if %errorlevel% equ 0 (
        echo.
        echo [OK] Successfully pushed to GitLab!
    ) else (
        echo.
        REM Check if it's a non-fast-forward error
        git push -u origin !current_branch! 2>&1 | findstr /C:"non-fast-forward" >nul
        if %errorlevel% equ 0 (
            echo [INFO] Remote branch has commits that you don't have locally.
            echo.
            echo This is normal if the repository already exists on GitLab.
            echo.
            echo Options:
            echo   1. Create a new branch for your changes (RECOMMENDED)
            echo   2. Sync with remote first (use sync-with-gitlab.bat)
            echo.
            echo To create a new branch:
            echo   git checkout -b feature/your-changes
            echo   git push -u origin feature/your-changes
            echo.
            echo Or run: sync-with-gitlab.bat
        ) else (
            echo [WARNING] Push failed. Possible reasons:
            echo   1. Authentication required (login/password or SSH key)
            echo   2. Branch doesn't exist on remote
            echo   3. Permission denied
            echo.
            echo Try manually:
            echo   git push -u origin !current_branch!
            echo.
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
echo Repository URL: !GITLAB_URL!
echo.
pause
cmd /k
