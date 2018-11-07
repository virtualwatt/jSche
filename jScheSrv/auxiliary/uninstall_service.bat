@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 (
	echo Error setting common configuration
	exit /b 1
)

"%SRV_EXE%" //DS//%APP_SERVICE_NAME%
if errorlevel 1 (
	echo Error uninstalling service
	exit /b 1
)

echo Service successfully uninstalled.
