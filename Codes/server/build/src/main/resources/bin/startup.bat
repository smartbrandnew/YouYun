@echo off
set procList=event datastore gateway
for %%i in (%procList%) do (
start ..\%%i\bin\%%i.bat
)