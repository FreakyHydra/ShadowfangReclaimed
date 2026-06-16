@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
echo =============================================
echo  Shadowfang Reclaimed - Main PC Setup
echo =============================================
echo.
echo This will install Git and clone the repo.
echo.
pause

:: --- Locate git (already installed or from ProgramFiles) ---
set "GIT="
where git >nul 2>&1 && set "GIT=git"
if not defined GIT (
    if exist "%ProgramFiles%\Git\bin\git.exe" set "GIT=%ProgramFiles%\Git\bin\git.exe"
    if exist "%ProgramFiles(x86)%\Git\bin\git.exe" set "GIT=%ProgramFiles(x86)%\Git\bin\git.exe"
)

:: --- Step 1: Install Git if missing ---
if not defined GIT (
    echo [1/3] Git not found. Downloading...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/git-for-windows/git/releases/download/v2.49.0.windows.1/Git-2.49.0-64-bit.exe' -OutFile '%TEMP%\Git-64-bit.exe'"
    if !ERRORLEVEL! neq 0 (
        echo.
        echo ERROR: Download failed. Check internet connection.
        pause
        exit /b 1
    )
    echo Installing Git (silent)...
    "%TEMP%\Git-64-bit.exe" /VERYSILENT /NORESTART /SUPPRESSMSGBOXES /CLOSEAPPLICATIONS
    if !ERRORLEVEL! neq 0 (
        echo.
        echo ERROR: Git installation failed.
        pause
        exit /b 1
    )
    set "GIT=%ProgramFiles%\Git\bin\git.exe"
    echo Git installed.
    echo.
    pause
) else (
    echo [1/3] Git found.
)

:: --- Step 2: Clone repo ---
echo.
echo [2/3] Setting up repository...

if exist "%USERPROFILE%\ShadowfangReclaimed" (
    echo Repo already exists. Updating...
    cd /d "%USERPROFILE%\ShadowfangReclaimed"
    "%GIT%" pull
    if !ERRORLEVEL! neq 0 (
        echo WARNING: git pull failed.
    )
) else (
    cd /d "%USERPROFILE%"
    "%GIT%" clone https://github.com/FreakyHydra/ShadowfangReclaimed.git
    if !ERRORLEVEL! neq 0 (
        echo.
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
"%GIT%" config user.name "FreakyHydra"
"%GIT%" config user.email "FreakyHydra@users.noreply.github.com"
"%GIT%" config credential.helper manager

echo.
echo If prompted, enter these for GitHub:
echo   Username: FreakyHydra
echo   Password: (paste your personal access token)
echo.
"%GIT%" fetch --quiet
if !ERRORLEVEL! equ 0 (
    echo Credentials verified.
) else (
    echo To set up credentials later, run:
    echo   git pull
    echo and enter your token when prompted.
)

:: --- Done ---
echo.
echo =============================================
echo  Setup complete!
echo =============================================
echo.
echo Repo: %USERPROFILE%\ShadowfangReclaimed
echo.
echo To update later, run:
echo   cd /d "%USERPROFILE%\ShadowfangReclaimed" ^&^& "%GIT%" pull
echo.
pause
