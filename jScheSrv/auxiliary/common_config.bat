@echo off

set APP_HOME=%~dp0..
for %%F in ("%APP_HOME%") do set APP_HOME=%%~fF

if not exist "%JAVA_HOME%" (
	echo No JAVA_HOME, using registry method
	call :REGISTRY_METHOD
	if not errorlevel 1 call :JAVA_HOME_METHOD
) else (
	echo JAVA_HOME found, using its location
	call :JAVA_HOME_METHOD
)

if errorlevel 1 (
	echo Configuration error
	exit /b 1
)

set APP_SERVICE_NAME=jSche

set PRUNSRV_EXE=%APP_SERVICE_NAME%.exe

set SRV_EXE=%APP_HOME%\bin\%PRUNSRV_EXE%
set APP_LOGS_FOLDER=%APP_HOME%\logs
set APP_CONSOLE_LOG=%APP_LOGS_FOLDER%\console.log

goto :EOF

:JAVA_HOME_METHOD
if not exist "%JAVA_HOME%" (
	echo Error: no Java installed and/or JAVA_HOME set. Install JRE or JDK and set the JAVA_HOME system variable pointing to that folder!
	exit /b 1
)
if exist "%JAVA_HOME%\jre\bin\server\jvm.dll" (
	set JVM_DLL=%JAVA_HOME%\jre\bin\server\jvm.dll
) else if exist "%JAVA_HOME%\bin\server\jvm.dll" (
	set JVM_DLL=%JAVA_HOME%\bin\server\jvm.dll
) else if exist "%JAVA_HOME%\bin\client\jvm.dll" (
	set JVM_DLL=%JAVA_HOME%\bin\client\jvm.dll
) else (
	echo Error: JAVA_HOME variable set but no jvm.dll found. Make sure you have properly installed JRE or JDK and set JAVA_HOME system variable pointing to that folder!
	exit /b 1
)
goto :EOF

:REGISTRY_METHOD
call jre_home.bat
if errorlevel 1 call jdk_home.bat
goto :EOF
