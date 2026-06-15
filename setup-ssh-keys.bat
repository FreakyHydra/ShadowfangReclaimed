@echo off
title Shadowfang - Setup SSH Keys (run once)
echo ============================================
echo  Setting up SSH key authentication
echo  to 10.0.0.12
echo ============================================
echo.
echo You will be asked for the password once: Deploy2024!
echo.

set KEYFILE="%USERPROFILE%\.ssh\id_rsa_shadowfang"

if not exist "%USERPROFILE%\.ssh" mkdir "%USERPROFILE%\.ssh"

if not exist %KEYFILE% (
    echo Generating SSH key...
    ssh-keygen -t rsa -b 4096 -f %KEYFILE% -N "" -q
    echo       Done.
)

echo Copying public key to main PC...
type %KEYFILE%.pub | ssh shadowfang-deploy@10.0.0.12 "if not exist .ssh mkdir .ssh && cat >> .ssh/authorized_keys"

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo  SSH keys installed successfully!
    echo  Future deploys will not need a password.
    echo ============================================
) else (
    echo.
    echo ERROR: Failed to copy SSH key.
    echo Check that:
    echo   - Main PC is online (10.0.0.12)
    echo   - setup-main-pc.bat was run on it
    echo   - Password was entered correctly
)

pause
