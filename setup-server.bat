@echo off
title Shadowfang Server Setup
echo ============================================
echo  Shadowfang Server - Automated Setup
echo ============================================
echo.
echo This script sets up a Shadowfang Folia server.
echo Place it in an EMPTY folder and run as Admin.
echo.

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Run as Administrator (right-click ^> Run as Admin).
    pause
    exit /b 1
)

set SERVER_DIR=%~dp0
cd /d "%SERVER_DIR%"

echo [1/6] Checking Java 25...
where java >nul 2>&1
if %errorlevel% equ 0 (
    java -version 2>&1 | findstr "25." >nul
    if %errorlevel% equ 0 (
        echo       Java 25 found in PATH.
        goto :java_ok
    )
)

if exist "java\jdk-25.0.1+8\bin\java.exe" (
    echo       Java 25 found in bundled java\ folder.
    set "PATH=%SERVER_DIR%java\jdk-25.0.1+8\bin;%PATH%"
    goto :java_ok
)

echo       Java 25 not found.
echo       Downloading Adoptium JDK 25...
set JAVA_URL=https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.1+9/OpenJDK25U-jdk_x64_windows_hotspot_25.0.1_9.zip
set JAVA_ZIP=%TEMP%\jdk25.zip
set JAVA_DIR=%SERVER_DIR%java

powershell -Command "try { [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%JAVA_URL%' -OutFile '%JAVA_ZIP%' -UseBasicParsing } catch { exit 1 }"
if %errorlevel% neq 0 (
    echo       Download failed. Download manually from:
    echo       https://adoptium.net/temurin/releases/?version=25
    echo       Extract to: %JAVA_DIR%
    pause
    exit /b 1
)

echo       Extracting Java...
powershell -Command "Expand-Archive -Path '%JAVA_ZIP%' -DestinationPath '%JAVA_DIR%' -Force"
del "%JAVA_ZIP%" 2>nul

for /d %%d in ("%JAVA_DIR%\*") do (
    if exist "%%d\bin\java.exe" (
        move /y "%%d\*" "%JAVA_DIR%\" >nul 2>&1
        rd /s /q "%%d" 2>nul
    )
)
set "PATH=%SERVER_DIR%java\bin;%PATH%"
echo       Java 25 installed.

:java_ok

echo [2/6] Checking Folia server jar...
if not exist "folia.jar" (
    echo       Downloading Folia 1.21.4...
    set FOLIA_URL=https://api.papermc.io/v2/projects/folia/versions/1.21.4/builds/291/downloads/folia-1.21.4-291.jar
    powershell -Command "try { [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%FOLIA_URL%' -OutFile 'folia.jar' -UseBasicParsing } catch { exit 1 }"
    if %errorlevel% neq 0 (
        echo       Download failed. Place folia.jar manually in this folder.
        pause
        exit /b 1
    )
    echo       folia.jar downloaded.
) else (
    echo       folia.jar found.
)

echo [3/6] Configuring server...
if not exist "server.properties" (
    (
        echo motd=Shadowfang Reclaimed
        echo server-port=25565
        echo online-mode=true
        echo difficulty=easy
        echo gamemode=survival
        echo level-name=world
        echo enable-rcon=true
        echo rcon.password=shadowfang
        echo rcon.port=25575
        echo view-distance=10
        echo simulation-distance=10
        echo max-players=20
        echo enforce-secure-profile=true
        echo resource-pack=
        echo require-resource-pack=false
    ) > server.properties
    echo       server.properties created.
) else (
    echo       server.properties exists, keeping as-is.
)

if not exist "eula.txt" (
    echo eula=true > eula.txt
    echo       EULA accepted.
)

echo [4/6] Creating directory structure...
if not exist "plugins" mkdir plugins
if not exist "config" mkdir config
if not exist "config\shadowfang-core" mkdir config\shadowfang-core
if not exist "libraries" mkdir libraries
if not exist "versions" mkdir versions
if not exist "cache" mkdir cache
if not exist "resourcepack\assets\minecraft\models\item" mkdir resourcepack\assets\minecraft\models\item
if not exist "resourcepack\assets\minecraft\textures\items" mkdir resourcepack\assets\minecraft\textures\items

if not exist "config\factions.json" echo [] > config\shadowfang-core\factions.json 2>nul
if not exist "config\faction_chunks.json" echo {} > config\shadowfang-core\faction_chunks.json 2>nul

echo [5/6] Creating start.bat...
(
    echo @echo off
    echo echo [%%date%% %%time%%] Starting Shadowfang server...
    echo java -Xms2G -Xmx2G -jar folia.jar nogui
    echo echo Server stopped. Restarting in 5 seconds...
    echo timeout /t 5 /nobreak ^>nul
    echo goto :loop
) > start.bat
echo       start.bat created.

echo [6/6] Verifying...
java -version 2>&1 | findstr "version" >nul
if %errorlevel% equ 0 (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr "version"') do (
        echo       Java: %%g
    )
) else (
    echo       WARNING: Java not found in PATH.
)
if exist "folia.jar" (
    echo       Folia: folia.jar found
)
echo.
echo ============================================
echo  SETUP COMPLETE
echo ============================================
echo.
echo  Your server folder is ready at:
echo  %SERVER_DIR%
echo.
echo  Next steps:
echo   1. Drop your plugins into the plugins\ folder
echo   2. Drop your world folder (named "world") next to folia.jar
echo   3. Configure server.properties (motd, resource-pack, etc.)
echo   4. Run start.bat to launch
echo.
pause
