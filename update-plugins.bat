@echo off
set PLUGIN_URL=https://github.com/FreakyHydra/ShadowfangReclaimed/releases/latest/download/ShadowfangReclaimed.jar
set PLUGIN_DIR=%~dp0plugins
set JAR_NAME=ShadowfangReclaimed.jar

echo [%date% %time%] Checking for plugin updates...

if not exist "%PLUGIN_DIR%" (
    echo Creating plugins directory...
    mkdir "%PLUGIN_DIR%"
)

curl -s -L -o "%TEMP%\ShadowfangReclaimed.jar" "%PLUGIN_URL%"
if %errorlevel% equ 0 (
    move /Y "%TEMP%\ShadowfangReclaimed.jar" "%PLUGIN_DIR%\%JAR_NAME%" >nul
    echo [%date% %time%] Plugin updated successfully from GitHub.
) else (
    echo [%date% %time%] Failed to download update. Using existing plugin.
)

exit /b 0
