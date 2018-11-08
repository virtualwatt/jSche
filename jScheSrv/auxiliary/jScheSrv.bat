:: Simple run jScheSrv in command line logging to console, this is alternative utility usage
:: To stop the service press CTRL+C in console

@echo off
setlocal

cd %~dp0..\jScheConfigs

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
