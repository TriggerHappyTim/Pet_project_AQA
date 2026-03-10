@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Resolve Merge Conflicts
echo ========================================
echo.
echo This script will help resolve merge conflicts
echo by syncing your branch with master.
echo.

REM Check if git is available
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Git not found in PATH!
    pause
    exit /b 1
)

REM Change to project directory
cd /d "%~dp0"

REM Check if this is a git repository
git status >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Current directory is not a git repository!
    pause
    exit /b 1
)

echo [1/6] Checking current branch...
for /f "tokens=*" %%b in ('git branch --show-current') do set current_branch=%%b
echo Current branch: !current_branch!
echo.

if "!current_branch!"=="master" (
    echo [WARNING] You are on master branch!
    echo.
    set /p confirm="Do you want to continue? (y/n): "
    if /i not "!confirm!"=="y" (
        echo Cancelled.
        pause
        exit /b 0
    )
)

echo [2/6] Fetching latest changes from GitLab...
git fetch origin
if %errorlevel% neq 0 (
    echo [ERROR] Failed to fetch from GitLab!
    pause
    exit /b 1
)
echo [OK] Fetched successfully.
echo.

echo [3/6] Checking for uncommitted changes...
git diff --quiet --exit-code >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] You have uncommitted changes!
    echo.
    git status --short
    echo.
    set /p stash_choice="Do you want to stash them? (y/n): "
    if /i "!stash_choice!"=="y" (
        echo Stashing changes...
        git stash save "Stash before resolving conflicts - %date% %time%"
        echo [OK] Changes stashed.
        echo.
        set need_pop_stash=1
    ) else (
        echo [ERROR] Cannot proceed with uncommitted changes!
        echo Please commit or stash your changes first.
        pause
        exit /b 1
    )
) else (
    set need_pop_stash=0
)

echo [4/6] Merging master into !current_branch!...
git merge origin/master --no-edit
if %errorlevel% equ 0 (
    echo [OK] Merge completed successfully - no conflicts!
    echo.
    goto :push_changes
) else (
    echo.
    echo [INFO] Merge conflicts detected!
    echo.
    echo Conflicted files:
    git diff --name-only --diff-filter=U
    echo.
    echo ========================================
    echo   Conflict Resolution Required
    echo ========================================
    echo.
    echo You have merge conflicts that need to be resolved manually.
    echo.
    echo Options:
    echo   1. Resolve conflicts manually (RECOMMENDED)
    echo   2. Accept all changes from master (theirs)
    echo   3. Accept all changes from your branch (ours)
    echo   4. Abort merge and try rebase instead
    echo.
    set /p resolve_choice="Choose option (1-4): "
    
    if "!resolve_choice!"=="1" (
        echo.
        echo [5/6] Manual conflict resolution...
        echo.
        echo Conflicted files:
        git diff --name-only --diff-filter=U
        echo.
        echo To resolve conflicts:
        echo   1. Open conflicted files in your editor
        echo   2. Look for conflict markers: ^<^<^<^<^<^<^<, =======, ^>^>^>^>^>^>^>
        echo   3. Choose which changes to keep (or combine both)
        echo   4. Remove conflict markers
        echo   5. Save files
        echo   6. Run this script again or manually:
        echo      git add .
        echo      git commit
        echo.
        echo After resolving conflicts, run:
        echo   git add .
        echo   git commit
        echo   git push
        echo.
        pause
        exit /b 0
    ) else if "!resolve_choice!"=="2" (
        echo.
        echo [5/6] Accepting all changes from master...
        git checkout --theirs .
        git add .
        git commit -m "Merge master into !current_branch! - accept theirs"
        if %errorlevel% equ 0 (
            echo [OK] Conflicts resolved - accepted master changes.
            goto :push_changes
        ) else (
            echo [ERROR] Failed to commit!
            pause
            exit /b 1
        )
    ) else if "!resolve_choice!"=="3" (
        echo.
        echo [5/6] Accepting all changes from your branch...
        git checkout --ours .
        git add .
        git commit -m "Merge master into !current_branch! - accept ours"
        if %errorlevel% equ 0 (
            echo [OK] Conflicts resolved - accepted your changes.
            goto :push_changes
        ) else (
            echo [ERROR] Failed to commit!
            pause
            exit /b 1
        )
    ) else if "!resolve_choice!"=="4" (
        echo.
        echo [5/6] Aborting merge and switching to rebase...
        git merge --abort
        echo.
        echo Starting rebase instead...
        git rebase origin/master
        if %errorlevel% equ 0 (
            echo [OK] Rebase completed successfully!
            goto :push_changes_rebase
        ) else (
            echo [ERROR] Rebase failed! You have conflicts.
            echo.
            echo To resolve rebase conflicts:
            echo   1. Resolve conflicts in files
            echo   2. git add .
            echo   3. git rebase --continue
            echo   4. Or abort: git rebase --abort
            pause
            exit /b 1
        )
    ) else (
        echo Invalid choice. Cancelled.
        pause
        exit /b 0
    )
)

:push_changes
echo.
echo [6/6] Pushing resolved changes to GitLab...
git push origin !current_branch!
if %errorlevel% equ 0 (
    echo.
    echo [OK] Successfully pushed to GitLab!
    echo.
    echo Your Merge Request should now be ready to merge!
) else (
    echo.
    echo [ERROR] Failed to push!
    echo.
    echo Try manually:
    echo   git push origin !current_branch!
)
goto :end

:push_changes_rebase
echo.
echo [6/6] Pushing rebased changes to GitLab...
echo [WARNING] Rebase rewrites history - force push required!
git push origin !current_branch! --force-with-lease
if %errorlevel% equ 0 (
    echo.
    echo [OK] Successfully pushed to GitLab!
    echo.
    echo Your Merge Request should now be ready to merge!
) else (
    echo.
    echo [ERROR] Failed to push!
    echo.
    echo Try manually:
    echo   git push origin !current_branch! --force-with-lease
)

:end
if !need_pop_stash! equ 1 (
    echo.
    echo Restoring stashed changes...
    git stash pop
    if %errorlevel% equ 0 (
        echo [OK] Stashed changes restored.
    ) else (
        echo [WARNING] Could not restore stashed changes.
        echo Check: git stash list
    )
)

echo.
echo ========================================
echo   Conflict Resolution Complete!
echo ========================================
echo.
pause
cmd /k
