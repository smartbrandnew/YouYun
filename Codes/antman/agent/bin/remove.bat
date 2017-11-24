@ echo off
:: Define Variables
SET ANT_BIN_DIR=%~dp0
SET ANT_BIN_DIR=%ANT_BIN_DIR:~0,-1%
SET ANT_ROOT_DIR=%ANT_BIN_DIR%\..
SET ANT_STOP=%ANT_BIN_DIR%\stop.bat
SET NSSM=%ANT_BIN_DIR%\nssm32.exe
SET FORCE=%1

IF "%FORCE%"=="-f" (
    cd "%ANT_ROOT_DIR%"
    "%ANT_STOP%"
    "%NSSM%" stop ant-agent
    "%NSSM%" stop ant-upgrade
    "%NSSM%" remove ant-agent confirm
    "%NSSM%" remove ant-upgrade confirm
    timeout /T 2 /NOBREAK
    cd ..
    rd /s /q "%ANT_ROOT_DIR%"

) ELSE (
    SET /P INPUT=Are you sure to remove Ant Agent?(yes/no)
    IF "%INPUT%"=="yes" (
        cd "%ANT_ROOT_DIR%"
        "%ANT_STOP%"
        "%NSSM%" stop ant-agent
        "%NSSM%" stop ant-upgrade
        "%NSSM%" remove ant-agent confirm
        "%NSSM%" remove ant-upgrade confirm
        timeout /T 2 /NOBREAK
        cd ..
        rd /s /q "%ANT_ROOT_DIR%"
    ) ELSE (
        echo exit
    )
)
