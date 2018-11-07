:: Simple run jScheSrv in command line logging to console, this is alternative utility usage
:: To stop the service press CTRL+C in console

@echo off
setlocal

if "%JAVA_HOME%"=="" (
	echo Install JRE 7 or JDK 7 and set JAVA_HOME
	exit /b 1
)
path=%JAVA_HOME%\bin

cd %~dp0..\jScheConfigs

echo JAVA_HOME=%JAVA_HOME%
cd
echo %time%
echo .

rem -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y^
java^
 -Dlog4j.configuration=file:../lib/log4j_con.xml^
 -DconfigDir=.^
 -classpath ../lib/;../lib/*^
 vvat.jsche.srv.JScheSrv %*

echo .
echo %time%
