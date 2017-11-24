@rem dashboard windows startup script

@echo off

@title bat favourite

rem check conditions
if "%JAVA_HOME%" == "" goto error_no_java_home

:begin

set APP_HOME=%~dp0..
set PRD_HOME=%~dp0..\..

set RUN_OPTS=-Dapp.name=bee-lm
set RUN_OPTS=%RUN_OPTS% -Xmx200m
set RUN_OPTS=%RUN_OPTS% -Duser.dir=%APP_HOME%
set RUN_OPTS=%RUN_OPTS% -Djava.io.tmpdir=%APP_HOME%\temp
set RUN_OPTS=%RUN_OPTS% -Dlogback.configurationFile=file:%APP_HOME%\conf\logback.xml
set RUN_OPTS=%RUN_OPTS% -Dlogs.dir=%PRD_HOME%\logs

rem set RUN_OPTS=%RUN_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,address=6000,server=y,suspend=n

rem java class path
set JAVA_CLASSPATH=%APP_HOME%\lib\*;%PRD_HOME%\lib\*

rem java main class
set JAVA_MAINCLASS=uyun.bat.favourite.impl.Startup

rem run app
%JAVA_HOME%\bin\java %RUN_OPTS% %JAVA_OPTS% -cp %JAVA_CLASSPATH% %JAVA_MAINCLASS% %*

goto end

:error_no_java_home
echo ERROR: must set environment variable: JAVA_HOME

:end