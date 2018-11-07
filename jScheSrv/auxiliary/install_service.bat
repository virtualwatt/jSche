@echo off
setlocal

call "%~dp0common_config.bat"
if errorlevel 1 (
	echo Error setting common configuration
	exit /b 1
)
if not exist "%PRUNSRV_EXE%" call setup.bat
if errorlevel 1 (
	echo Error creating service runner
	exit /b 1
)

"%SRV_EXE%" //IS//%APP_SERVICE_NAME% --DisplayName "jSche" --Description "Java Simple Scheduler" --LogPath "%APP_LOGS_FOLDER%" --Install "%SRV_EXE%" --Jvm "%JVM_DLL%" --StartPath "%APP_HOME%\jScheConfigs" --StopPath "%APP_HOME%\jScheConfigs"
if errorlevel 1 goto UPDATE
echo Service successfully installed.

:UPDATE
echo .
update_config.bat
