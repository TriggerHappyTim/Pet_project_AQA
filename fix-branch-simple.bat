@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Simple Branch Fix - Easiest Way
echo ========================================
echo.
echo This will:
echo   1. Update master from GitLab
echo   2. Create new clean branch from master
echo   3. Add only your new files
echo   4. Push to GitLab
echo.

REM Check if git is available
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git not found!
    pause
    exit /b 1
)

cd /d "%~dp0"

echo [1/5] Switching to master and updating...
git checkout master
git pull origin master
if %errorlevel% neq 0 (
    echo [ERROR] Failed to update master!
    pause
    exit /b 1
)
echo [OK] Master updated.
echo.

echo [2/5] Creating new clean branch...
set /p branch_name="Enter branch name (press Enter for 'feature/setup-scripts'): "
if "!branch_name!"=="" (
    set branch_name=feature/setup-scripts
)

git checkout -b !branch_name!
if %errorlevel% neq 0 (
    echo [WARNING] Branch may already exist. Switching to it...
    git checkout !branch_name!
)
echo [OK] On branch: !branch_name!
echo.

echo [3/5] Adding your new files...
git add GITLAB-SETUP.md
git add GITLAB-SYNC-GUIDE.md
git add RESOLVE-CONFLICTS.md
git add create-merge-request.md
git add connect-gitlab.bat
git add setup-gitlab.bat
git add sync-with-gitlab.bat
git add resolve-merge-conflicts.bat
git add commit-all.bat
git add commit-and-push.bat
git add commit-quick.bat
git add init-git-repo.bat
git add fix-branch-simple.bat

echo.
echo Files to be committed:
git status --short
echo.

echo [4/5] Creating commit...
git commit -m "Add GitLab setup scripts and documentation

- GitLab connection scripts (connect-gitlab.bat, setup-gitlab.bat)
- Git commit scripts (commit-all.bat, commit-and-push.bat, commit-quick.bat)
- Sync and conflict resolution scripts (sync-with-gitlab.bat, resolve-merge-conflicts.bat)
- Documentation (GITLAB-SETUP.md, GITLAB-SYNC-GUIDE.md, RESOLVE-CONFLICTS.md)
- Repository initialization script (init-git-repo.bat)"

if %errorlevel% neq 0 (
    echo [WARNING] Commit failed - may be no changes or already committed.
    git status --short
    echo.
) else (
    echo [OK] Commit created.
    echo.
)

echo [5/5] Pushing to GitLab...
git push -u origin !branch_name!
if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   SUCCESS!
    echo ========================================
    echo.
    echo Branch: !branch_name!
    echo.
    echo Create Merge Request:
    echo https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework/-/merge_requests/new?merge_request%%5Bsource_branch%%5D=!branch_name!
    echo.
) else (
    echo.
    echo [ERROR] Push failed!
    echo Try manually: git push -u origin !branch_name!
)

pause
cmd /k
