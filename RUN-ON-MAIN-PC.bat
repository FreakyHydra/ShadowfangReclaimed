@echo off
title Shadowfang Server - Main PC Setup
echo ============================================
echo  Shadowfang Server - Main PC Setup
echo ============================================
echo.

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: This script must be run as Administrator.
    echo Right-click ^> Run as Administrator.
    pause
    exit /b 1
)

echo [1/6] Installing OpenSSH Server...
powershell -Command "Add-WindowsCapability -Online -Name OpenSSH.Server~~~~0.0.1.0" >nul 2>&1
if %errorlevel% neq 0 (
    echo Installing via DISM...
    dism /online /Add-Capability /CapabilityName:OpenSSH.Server~~~~0.0.1.0 /quiet /norestart
)
echo       Done.

echo [2/6] Creating deploy user...
net user shadowfang-deploy Deploy2024! /add >nul 2>&1
net localgroup Administrators shadowfang-deploy /add >nul 2>&1
echo       User "shadowfang-deploy" created.

echo [3/6] Configuring SSH service...
sc config sshd start=auto >nul 2>&1
sc start sshd >nul 2>&1
echo       SSH service running (auto-start).

echo [4/6] Adding firewall rule for SSH port 22...
netsh advfirewall firewall add rule name="OpenSSH Server (SSH)" dir=in action=allow protocol=TCP localport=22 >nul 2>&1
echo       Firewall rule added.

echo [5/6] Creating server directory...
if not exist "P:\Shadowfang-Server" mkdir "P:\Shadowfang-Server"
icacls "P:\Shadowfang-Server" /grant "shadowfang-deploy:(OI)(CI)F" /T >nul 2>&1
echo       P:\Shadowfang-Server ready.

echo [6/6] Verifying...
sc query sshd | findstr RUNNING >nul 2>&1
if %errorlevel% equ 0 (
    echo       SSH server is RUNNING.
) else (
    echo       WARNING: SSH server is NOT running. Run: net start sshd
)

echo.
echo ============================================
echo  SETUP COMPLETE
echo ============================================
echo.
echo  Username:  shadowfang-deploy
echo  Password:  Deploy2024!
echo  Server:    P:\Shadowfang-Server
echo.
echo  Now run deploy-to-main.bat from your dev
echo  computer to push the server files.
echo.
pause
