@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 exit /b 1

net stop %APP_SERVICE_NAME%
