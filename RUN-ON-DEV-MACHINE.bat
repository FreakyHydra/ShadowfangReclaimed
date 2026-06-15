@echo off
title Shadowfang Server - Deploy to Main PC
echo ============================================
echo  Deploying Shadowfang Server to 10.0.0.12
echo ============================================
echo.

set MAIN_USER=shadowfang-deploy
set MAIN_HOST=10.0.0.12

echo [1/6] Checking OpenSSH Client...
ssh 2>nul
if %errorlevel% equ 9009 (
    echo       OpenSSH Client not found. Installing...
    powershell -Command "Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0" >nul 2>&1
    if %errorlevel% neq 0 (
        echo       ERROR: Failed to install OpenSSH Client.
        pause
        exit /b 1
    )
    echo       OpenSSH Client installed.
) else (
    echo       OpenSSH Client available.
)

echo [2/6] Building Shadowfang Core plugin...
cd /d "F:\Shadowfang-Folia-Project\Shadowfang-Core-Folia"
call gradlew.bat build -q
if %errorlevel% neq 0 (
    echo       ERROR: Build failed!
    pause
    exit /b 1
)
echo       Plugin built successfully.

echo [3/6] Staging server package...
set PKG=%TEMP%\shadowfang-deploy
set PKG_ZIP=%TEMP%\shadowfang-deploy.zip
rd /s /q "%PKG%" 2>nul
del "%PKG_ZIP%" 2>nul
mkdir "%PKG%"
mkdir "%PKG%\plugins\ShadowfangReclaimed"
mkdir "%PKG%\config\shadowfang-core"

copy /y "F:\Shadowfang-Folia-Project\foliaserver\folia.jar" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\start.bat" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\eula.txt" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\server.properties" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\bukkit.yml" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\spigot.yml" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\commands.yml" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\help.yml" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\permissions.yml" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\ops.json" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\whitelist.json" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\banned-players.json" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\banned-ips.json" "%PKG%\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\usercache.json" "%PKG%\" >nul

copy /y "F:\Shadowfang-Folia-Project\foliaserver\config\paper-global.yml" "%PKG%\config\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\config\paper-world-defaults.yml" "%PKG%\config\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\config\shadowfang-core\factions.json" "%PKG%\config\shadowfang-core\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\config\shadowfang-core\faction_chunks.json" "%PKG%\config\shadowfang-core\" >nul

xcopy /s/e/i/y "F:\Shadowfang-Folia-Project\foliaserver\plugins\AxiomPaper" "%PKG%\plugins\AxiomPaper\" >nul 2>&1
copy /y "F:\Shadowfang-Folia-Project\foliaserver\plugins\AxiomPaperPlugin.jar" "%PKG%\plugins\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\plugins\RosettaStone-1.0.0.jar" "%PKG%\plugins\" >nul
copy /y "F:\Shadowfang-Folia-Project\foliaserver\plugins\worlds-4.2.2-all.jar" "%PKG%\plugins\" >nul
xcopy /s/e/i/y "F:\Shadowfang-Folia-Project\foliaserver\plugins\Worlds" "%PKG%\plugins\Worlds\" >nul 2>&1

copy /y "F:\Shadowfang-Folia-Project\Shadowfang-Core-Folia\build\libs\Shadowfang-Core-Folia-1.0.0.jar" "%PKG%\plugins\ShadowfangReclaimed.jar" >nul

if exist "F:\Shadowfang-Folia-Project\foliaserver\plugins\ShadowfangReclaimed\worlds.yml" (
    copy /y "F:\Shadowfang-Folia-Project\foliaserver\plugins\ShadowfangReclaimed\worlds.yml" "%PKG%\plugins\ShadowfangReclaimed\worlds.yml" >nul
)

if exist "F:\Shadowfang-Folia-Project\resourcepack" (
    xcopy /s/e/i/y "F:\Shadowfang-Folia-Project\resourcepack" "%PKG%\resourcepack\" >nul 2>&1
)

robocopy "F:\Shadowfang-Folia-Project\foliaserver\java" "%PKG%\java\" /e /njh /njs /ndl >nul 2>&1
robocopy "F:\Shadowfang-Folia-Project\foliaserver\libraries" "%PKG%\libraries\" /e /njh /njs /ndl >nul 2>&1
robocopy "F:\Shadowfang-Folia-Project\foliaserver\versions" "%PKG%\versions\" /e /njh /njs /ndl >nul 2>&1
robocopy "F:\Shadowfang-Folia-Project\foliaserver\cache" "%PKG%\cache\" /e /njh /njs /ndl >nul 2>&1
if exist "F:\Shadowfang-Folia-Project\foliaserver\net" (
    robocopy "F:\Shadowfang-Folia-Project\foliaserver\net" "%PKG%\net\" /e /njh /njs /ndl >nul 2>&1
)

echo       Creating ZIP archive...
powershell -Command "Compress-Archive -Path '%PKG%\*' -DestinationPath '%PKG_ZIP%' -Force"
if %errorlevel% neq 0 (
    echo       ERROR: Failed to create ZIP.
    pause
    exit /b 1
)
rd /s /q "%PKG%" 2>nul
echo       Package zipped: %PKG_ZIP%

echo [4/6] Ensuring remote directory exists...
ssh %MAIN_USER%@%MAIN_HOST% "if not exist P:\Shadowfang-Server mkdir P:\Shadowfang-Server"

echo [5/6] Deploying ZIP via SCP...
scp -q "%PKG_ZIP%" "%MAIN_USER%@%MAIN_HOST%:/p:/Shadowfang-Server/shadowfang-server.zip"
if %errorlevel% neq 0 (
    echo       ERROR: SCP failed. Check SSH keys are set up.
    pause
    exit /b 1
)

echo [6/6] Extracting ZIP on main PC...
ssh %MAIN_USER%@%MAIN_HOST% "powershell -Command \"Expand-Archive -Path 'P:\Shadowfang-Server\shadowfang-server.zip' -DestinationPath 'P:\Shadowfang-Server' -Force\""
if %errorlevel% equ 0 (
    ssh %MAIN_USER%@%MAIN_HOST% "del P:\Shadowfang-Server\shadowfang-server.zip"
    echo       Extracted and cleaned up.
) else (
    echo       WARNING: Extraction may have failed. Check manually.
)

del "%PKG_ZIP%" 2>nul

echo.
echo ============================================
echo  DEPLOY COMPLETE
echo ============================================
echo.
echo  Files pushed to \\%MAIN_HOST%\P$\Shadowfang-Server
echo.
echo  Next steps on the main PC:
echo   1. Start the server: start.bat
echo   2. Add your world data separately
echo.
pause
