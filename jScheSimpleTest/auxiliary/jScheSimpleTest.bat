:: Simple run jScheSrv in command line logging to console, this is alternative utility usage
:: To stop the service press CTRL+C in console

@echo off
setlocal

if "%JAVA_HOME%"=="" (
	echo Install JRE 7 or JDK 7 and set JAVA_HOME
	exit /b 1
)
path=%JAVA_HOME%\bin

echo JAVA_HOME=%JAVA_HOME%
cd
echo %time%
echo .

rem -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y^
java -jar jsche-simpletest-1.1.0.jar %*

if errorlevel 1 echo An error occurred running jScheSrv

echo .
echo %time%
