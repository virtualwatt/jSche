:: Simple run jScheSrv in command line logging to console, this is alternative utility usage
:: To stop the service press CTRL+C in console

@echo off
setlocal

echo %time%
echo .

rem -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y^
java -jar jsche-simpletest-1.1.0.jar %*

if errorlevel 1 echo An error occurred running jScheSrv

echo .
echo %time%
