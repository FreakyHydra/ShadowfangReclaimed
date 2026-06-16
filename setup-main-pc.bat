@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
echo =============================================
echo  Shadowfang Reclaimed — Main PC Setup
echo =============================================
echo.
echo This will install Git and clone the repo.
echo.
pause

:: --- Step 1: Install Git if missing ---
where git >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo [1/3] Git not found. Downloading...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/git-for-windows/git/releases/download/v2.49.0.windows.1/Git-2.49.0-64-bit.exe' -OutFile '%TEMP%\Git-64-bit.exe'}"
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Download failed. Check internet connection.
        pause
        exit /b 1
    )
    echo Installing Git (silent)...
    "%TEMP%\Git-64-bit.exe" /VERYSILENT /NORESTART /SUPPRESSMSGBOXES /CLOSEAPPLICATIONS
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Git installation failed. Try running manually:
        echo   "%TEMP%\Git-64-bit.exe"
        pause
        exit /b 1
    )
    echo Git installed.
) else (
    echo [1/3] Git already installed.
)

:: --- Refresh PATH from registry ---
for /f "skip=2 tokens=3*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "PATH=%%a%%b"
for /f "skip=2 tokens=2*" %%a in ('reg query "HKCU\Environment" /v Path 2^>nul') do set "PATH=%%a%%b;%PATH%"

:: --- Step 2: Clone repo ---
echo.
echo [2/3] Setting up repository...

if exist "%USERPROFILE%\ShadowfangReclaimed" (
    echo Repo already exists. Updating...
    cd /d "%USERPROFILE%\ShadowfangReclaimed"
    git pull
    if !ERRORLEVEL! neq 0 (
        echo WARNING: git pull failed. Will try to continue.
    )
) else (
    cd /d "%USERPROFILE%"
    git clone https://github.com/FreakyHydra/ShadowfangReclaimed.git
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Clone failed. Check internet connection.
        pause
        exit /b 1
    )
    cd /d "%USERPROFILE%\ShadowfangReclaimed"
    echo Repository cloned.
)

:: --- Step 3: Configure credentials ---
echo.
echo [3/3] Configuring Git...
cd /d "%USERPROFILE%\ShadowfangReclaimed"
git config user.name "FreakyHydra"
git config user.email "FreakyHydra@users.noreply.github.com"
git config credential.helper manager

:: Try a fetch to trigger credential prompt
echo.
echo If prompted, enter these credentials for GitHub:
echo   Username: FreakyHydra
echo   Password: (paste your personal access token)
echo.
git fetch --quiet
if !ERRORLEVEL! equ 0 (
    echo Credentials verified.
) else (
    echo Note: git fetch had an issue. You may need to run:
    echo   git pull
    echo manually and enter your token when prompted.
)

:: --- Done ---
echo.
echo =============================================
echo  Setup complete!
echo =============================================
echo.
echo Repo: %USERPROFILE%\ShadowfangReclaimed
echo.
echo To update the source later, run:
echo   cd /d "%USERPROFILE%\ShadowfangReclaimed" ^&^& git pull
echo.
pause
