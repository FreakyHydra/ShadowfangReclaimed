@echo off
chcp 65001 >nul
title Shadowfang Reclaimed — Main PC Setup
echo =============================================
echo  Shadowfang Reclaimed — Main PC Setup
echo =============================================
echo.

:: --- Check if Git is installed ---
where git >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [1/4] Git not found. Downloading Git for Windows...
    curl -L -o "%TEMP%\Git-64-bit.exe" "https://github.com/git-for-windows/git/releases/download/v2.49.0.windows.1/Git-2.49.0-64-bit.exe"
    echo Installing Git (silent)...
    "%TEMP%\Git-64-bit.exe" /VERYSILENT /NORESTART /SUPPRESSMSGBOXES /CLOSEAPPLICATIONS
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Git installation failed.
        pause
        exit /b 1
    )
    echo Git installed.
) else (
    echo [1/4] Git already installed.
)

:: --- Refresh PATH so git is available ---
for /f "skip=2 tokens=3*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "PATH=%%a%%b"
for /f "skip=2 tokens=3*" %%a in ('reg query "HKCU\Environment" /v Path 2^>nul') do set "PATH=%%a%%b;%PATH%"

:: --- Clone repo ---
set REPO_DIR=%USERPROFILE%\ShadowfangReclaimed
if exist "%REPO_DIR%" (
    echo [2/4] Repo already exists at %REPO_DIR%
    cd /d "%REPO_DIR%"
    git pull
) else (
    echo [2/4] Cloning repository...
    cd /d "%USERPROFILE%"
    git clone https://github.com/FreakyHydra/ShadowfangReclaimed.git
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Clone failed. Check internet connection.
        pause
        exit /b 1
    )
    cd /d "%REPO_DIR%"
    echo Repository cloned.
)

:: --- Set up credentials ---
echo [3/4] Setting up Git credentials...
git config user.name "FreakyHydra"
git config user.email "FreakyHydra@users.noreply.github.com"
git config credential.helper wincred
echo.
echo Now we need a GitHub Personal Access Token to push/pull.
echo If you already have one saved, it may be reused.
echo.
git remote set-url origin https://github.com/FreakyHydra/ShadowfangReclaimed.git
echo.
echo When the login prompt appears below, enter:
echo   Username: FreakyHydra
echo   Password: (paste your GitHub personal access token)
echo.
echo If no prompt appears, credentials are already cached.
git fetch --quiet 2>nul
if %ERRORLEVEL% equ 0 (
    echo Credentials verified.
) else (
    echo.
    echo Please run the following commands manually:
    echo   git pull
    echo   ^(then enter your credentials when prompted^)
)

:: --- Done ---
echo.
echo [4/4] Setup complete!
echo.
echo Repo location: %REPO_DIR%
echo To update later, run:
echo   cd /d "%REPO_DIR%" ^&^& git pull
echo.
pause
