call update-plugins.bat

:loop
@echo off
echo [%date% %time%] Starting Shadowfang server...
"java\jdk-25.0.1+8\bin\java.exe" -Xms2G -Xmx2G -jar folia.jar nogui
echo [%date% %time%] Server stopped. Restarting in 5 seconds...
timeout /t 5 /nobreak >nul
goto loop
