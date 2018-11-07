@echo off
if "%1" == "/r" del "%~dp0%PRUNSRV_EXE%"
if errorlevel 1 goto error
if exist "%~dp0%PRUNSRV_EXE%" (
	echo Setup has been performed already, if you need to resetup again, specify /r argument
	exit /b
)
setlocal
if "%ProgramFiles(x86)%" == "" (
	set EXE=prunsrv32.exe
) else (
	set EXE=prunsrv64.exe
)
copy "%~dp0%EXE%" "%~dp0%PRUNSRV_EXE%"
if errorlevel 1 goto error
echo Setup: service executable file has been made, now you can install the service
exit /b
:error
echo Error (re)making service executable file
exit /b 1
