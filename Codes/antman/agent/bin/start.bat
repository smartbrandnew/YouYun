@ echo off
:: Define Variables
Set ANT_BIN_DIR=%~dp0
Set ANT_BIN_DIR=%ANT_BIN_DIR:~0,-1%
Set ANT_ROOT_DIR=%ANT_BIN_DIR%\..
Set ANT_PYTHON=%ANT_ROOT_DIR%\embedded\python.exe

cd "%ANT_ROOT_DIR%"
"%ANT_PYTHON%" "%ANT_BIN_DIR%\circled" --config "%ANT_ROOT_DIR%\proc" --log-output "%ANT_ROOT_DIR%\logs\circle.log"
