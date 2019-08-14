:: Try Orcale JDK
set KEY="HKLM\SOFTWARE\JavaSoft\Java Development Kit"
set VALUE=CurrentVersion
reg query %KEY% /v %VALUE% >nul 2>nul || (
	echo Oracle JDK not found, trying Zulu...
	goto zulu
)
echo Oracle JDK detected
set JDK_VERSION=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
	set JDK_VERSION=%%b
)
::echo JRE VERSION: %JRE_VERSION%
set KEY="HKLM\SOFTWARE\JavaSoft\Java Development Kit\%JDK_VERSION%"
set VALUE=JavaHome
reg query %KEY% /v %VALUE% >nul 2>nul || (
	echo JavaHome not installed
	exit /b 1
)
set JAVA_HOME=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
	set JAVA_HOME=%%b
)
::echo JavaHome: %JAVA_HOME%
set KEY=
set VALUE=
set PATH=%JAVA_HOME%\bin;%PATH%
goto :EOF

:zulu
:: Try Zulu JDK
set KEY="HKLM\SOFTWARE\Azul Systems\Zulu\zulu-8"
set VALUE=InstallationPath
reg query %KEY% /v %VALUE% >nul 2>nul || (
	echo Zulu JDK not installed
	exit /b 1
)
echo Zulu JDK detected
set JAVA_HOME=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
	set JAVA_HOME=%%b
)
::echo JavaHome: %JAVA_HOME%
set KEY=
set VALUE=
set PATH=%JAVA_HOME%\bin;%PATH%
