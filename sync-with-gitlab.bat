@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Sync with GitLab Repository
echo ========================================
echo.
echo Repository: https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework
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

echo Current branch:
git branch --show-current
echo.

echo [1/5] Fetching latest changes from GitLab...
git fetch origin
if %errorlevel% neq 0 (
    echo [ERROR] Failed to fetch from GitLab!
    pause
    exit /b 1
)
echo [OK] Fetched successfully.
echo.

echo [2/5] Available remote branches:
git branch -r
echo.

echo [3/5] Checking local changes...
git status --short
echo.

REM Check if there are uncommitted changes
git diff --quiet --exit-code >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] You have uncommitted changes!
    echo.
    set /p stash_choice="Do you want to stash them before sync? (y/n): "
    if /i "!stash_choice!"=="y" (
        echo Stashing changes...
        git stash save "Stash before sync with GitLab"
        echo [OK] Changes stashed.
        echo.
    )
)

REM Check if local master exists and has commits
git log --oneline master >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Local master branch has commits.
    echo.
    echo Options:
    echo   1. Create new branch for your changes (RECOMMENDED)
    echo   2. Merge remote master into local master
    echo   3. Rebase local master on remote master
    echo   4. Cancel
    echo.
    set /p sync_choice="Choose option (1-4): "
    
    if "!sync_choice!"=="1" (
        echo.
        echo [4/5] Creating new branch for your changes...
        set /p branch_name="Enter branch name (e.g., feature/updates-2026-02-02): "
        if "!branch_name!"=="" (
            for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value 2^>nul') do set datetime=%%I
            set branch_name=feature/updates-!datetime:~0,4!-!datetime:~4,2!-!datetime:~6,2!
        )
        git checkout -b !branch_name!
        if %errorlevel% equ 0 (
            echo [OK] Created and switched to branch: !branch_name!
            echo.
            echo [5/5] Pushing new branch to GitLab...
            git push -u origin !branch_name!
            if %errorlevel% equ 0 (
                echo.
            echo [OK] Successfully pushed branch to GitLab!
            echo.
            echo ========================================
            echo   Branch Created Successfully!
            echo ========================================
            echo.
            echo Branch: !branch_name!
            echo.
            echo Next steps:
            echo   1. Create Merge Request in GitLab:
            echo      https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework/-/merge_requests/new
            echo.
            echo   2. Or use direct link:
            echo      https://gitlab.bft.local/ecp/evs-oi/evs-testing-framework/-/merge_requests/new?merge_request%%5Bsource_branch%%5D=!branch_name!
            echo.
            echo   3. Review changes in GitLab
            echo   4. Merge to master after approval
            echo.
            ) else (
                echo [ERROR] Failed to push branch!
            )
        ) else (
            echo [ERROR] Failed to create branch!
        )
    ) else if "!sync_choice!"=="2" (
        echo.
        echo [4/5] Merging remote master into local master...
        git checkout master
        git merge origin/master --no-edit
        if %errorlevel% equ 0 (
            echo [OK] Merge completed successfully!
            echo.
            echo [5/5] Pushing merged changes...
            git push origin master
            if %errorlevel% equ 0 (
                echo [OK] Successfully pushed to GitLab!
            ) else (
                echo [ERROR] Failed to push!
            )
        ) else (
            echo [ERROR] Merge failed! You may have conflicts.
            echo.
            echo To resolve conflicts:
            echo   1. Check conflicted files: git status
            echo   2. Resolve conflicts manually
            echo   3. Add resolved files: git add .
            echo   4. Complete merge: git commit
        )
    ) else if "!sync_choice!"=="3" (
        echo.
        echo [WARNING] Rebase will rewrite history!
        echo.
        set /p confirm_rebase="Are you sure? (yes/no): "
        if /i "!confirm_rebase!"=="yes" (
            echo.
            echo [4/5] Rebasing local master on remote master...
            git checkout master
            git rebase origin/master
            if %errorlevel% equ 0 (
                echo [OK] Rebase completed successfully!
                echo.
                echo [5/5] Pushing rebased changes...
                git push origin master --force-with-lease
                if %errorlevel% equ 0 (
                    echo [OK] Successfully pushed to GitLab!
                ) else (
                    echo [ERROR] Failed to push!
                )
            ) else (
                echo [ERROR] Rebase failed! You may have conflicts.
                echo.
                echo To resolve conflicts:
                echo   1. Resolve conflicts in files
                echo   2. Add resolved files: git add .
                echo   3. Continue rebase: git rebase --continue
                echo   4. Or abort: git rebase --abort
            )
        ) else (
            echo Rebase cancelled.
        )
    ) else (
        echo Operation cancelled.
    )
) else (
    echo [INFO] Local master has no commits or doesn't exist.
    echo.
    REM Check if master branch already exists locally
    git show-ref --verify --quiet refs/heads/master
    if %errorlevel% equ 0 (
        echo [INFO] Master branch already exists locally.
        echo.
        echo To sync with remote master:
        echo   git checkout master
        echo   git merge origin/master
    ) else (
        echo [4/5] Checking out remote master...
        git checkout -b master origin/master
        if %errorlevel% equ 0 (
            echo [OK] Checked out remote master branch.
            echo.
            echo [5/5] Your local files are preserved as untracked.
            echo.
            echo To add your local files:
            echo   1. Review changes: git status
            echo   2. Add files: git add .
            echo   3. Commit: git commit -m "Your message"
            echo   4. Push: git push origin master
        ) else (
            echo [ERROR] Failed to checkout remote master!
        )
    )
)

echo.
echo ========================================
echo   Sync Complete!
echo ========================================
echo.
pause
cmd /k
