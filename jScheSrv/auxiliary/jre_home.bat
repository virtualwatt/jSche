set KEY="HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"
set VALUE=CurrentVersion
reg query %KEY% /v %VALUE% >nul 2>nul || (
	echo JRE not installed 
	exit /b 1
)
echo JRE detected
set JRE_VERSION=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
	set JRE_VERSION=%%b
)
::echo JRE VERSION: %JRE_VERSION%
set KEY="HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\%JRE_VERSION%"
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
set VALUE=RuntimeLib
reg query %KEY% /v %VALUE% >nul 2>nul || (
	echo RuntimeLib not installed
	exit /b 1
)
set JVM_DLL=
for /f "tokens=2,*" %%a in ('reg query %KEY% /v %VALUE% ^| findstr %VALUE%') do (
	set JVM_DLL=%%b
)
::echo RuntimeLib: %JVM_DLL%
set KEY=
set VALUE=
::set PATH=%JAVA_HOME%\bin;%PATH%
