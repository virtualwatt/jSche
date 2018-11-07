@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 exit /b 1

"%~dp0prunmgr.exe" //ES//%APP_SERVICE_NAME%
