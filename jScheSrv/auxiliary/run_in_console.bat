@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 (
	echo Error setting common configuration
	exit /b 1
)

rem You need to install the service to use this option, all options given to the application are the same options that were set using update_tomcat_config.bat
"%SRV_EXE%" //TS//%APP_SERVICE_NAME%
