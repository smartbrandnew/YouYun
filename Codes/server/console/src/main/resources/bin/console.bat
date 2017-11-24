@rem dashboard windows startup script

@echo off

@title bat-console

rem check conditions
if "%JAVA_HOME%" == "" goto error_no_java_home

:begin

set APP_HOME=%~dp0..
set PRD_HOME=%~dp0..\..
set APP_NAME=bat-console
set RUN_OPTS=-Dapp.name=%APP_NAME%
set RUN_OPTS=%RUN_OPTS% -Xmx512m -XX:MaxMetaspaceSize=128m
set RUN_OPTS=%RUN_OPTS% -Dwork.dir="%APP_HOME%"
set RUN_OPTS=%RUN_OPTS% -Dbase.dir="%PRD_HOME%"
set RUN_OPTS=%RUN_OPTS% -Dlogs.dir="%PRD_HOME%\logs"
set RUN_OPTS=%RUN_OPTS% -Dlogback.configurationFile="file:%APP_HOME%\conf\logback.xml"

rem set RUN_OPTS=%RUN_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,address=6000,server=y,suspend=n

rem java class path
set JAVA_CLASSPATH="%APP_HOME%\lib\*";"%PRD_HOME%\lib\*";"%JAVA_HOME%\lib\*"

rem java main class
set JAVA_MAINCLASS=uyun.bat.console.env.Startup

rem run app
"%JAVA_HOME%\bin\java" %RUN_OPTS% %JAVA_OPTS% -cp %JAVA_CLASSPATH% %JAVA_MAINCLASS% %*

goto end

:error_no_java_home
echo ERROR: must set environment variable: JAVA_HOME

:end