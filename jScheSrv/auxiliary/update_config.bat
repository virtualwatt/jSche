@echo off

setlocal

call "%~dp0common_config.bat"
if errorlevel 1 exit /b 1

::==========================
rem # JVM memory allocation pool parameters
set JVM_X_MINMEMSIZE=4M
set JVM_X_MAXMEMSIZE=256M

set USER_OPTIONS=-DconfigDir=%APP_HOME%\jScheConfigs#-DlogsDir=%APP_HOME%\logs
rem # For full HTTP response logging
rem set USER_OPTIONS=%USER_OPTIONS%#-DdetailedHttpResponse

::--------------------------
rem # Applying memory settings
set JAVA_OPTS=-Xms%JVM_X_MINMEMSIZE%#-Xmx%JVM_X_MAXMEMSIZE%

rem # Debug options (should be disabled in production)
rem set JAVA_OPTS=%JAVA_OPTS%#-Xdebug#-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n

if not "%USER_OPTIONS%" == "" set JAVA_OPTS=%JAVA_OPTS%#%USER_OPTIONS%

echo Updating service using configured options:
echo =========================================
echo JAVA_HOME:     [%JAVA_HOME%]
echo JVM_DLL:       [%JVM_DLL%]
echo APP_HOME:      [%APP_HOME%]
echo JAVA_OPTS:     [%JAVA_OPTS%]
echo =========================================

if not exist "%APP_LOGS_FOLDER%" md "%APP_LOGS_FOLDER%"

set APP_CLASSPATH=%APP_HOME%\lib\;%APP_HOME%\lib\*
set START_CLASS=vvat.jsche.srv.JScheSrv
set STOP_CLASS=%START_CLASS%
set START_METHOD=start
set STOP_METHOD=stop

"%SRV_EXE%" //US//%APP_SERVICE_NAME% --Classpath "%APP_CLASSPATH%" --StartClass %START_CLASS%^
 --StopClass %STOP_CLASS% --StartMethod %START_METHOD% --StopMethod %STOP_METHOD% --StartMode jvm --StopMode jvm^
 --Jvm "%JVM_DLL%" --JvmOptions "%JAVA_OPTS%"^
 --StdOutput "%APP_CONSOLE_LOG%" --StdError "%APP_CONSOLE_LOG%"
if errorlevel 1 goto ERROR

echo Update has been performed successfully, (re)start the service for the settings to take effect.
goto END

:ERROR
echo Error updating service!

:END
