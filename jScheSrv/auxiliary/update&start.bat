@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 exit /b 1

call "%~dp0update_config.bat"
if errorlevel 1 exit /b 1

net start %APP_SERVICE_NAME%
