set argus=wscript.arguments
Const ForReading = 1
if argus.count<>6 AND argus.count<>5 then
    Wscript.Echo "param count error"
    wscript.quit '参数数量不对，退出脚本
end If
domain = argus(0)
username = argus(1)
password = argus(2)
command = argus(3)
folder = argus(4)
if argus.count=6 then
    waitTime = argus(5)
end if
Dim resultTxt,wsh
resultTxt = domain&"-result.txt"
Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
if domain="127.0.0.1" then
Set objSWbemServices =objSWbemLocator.connectserver() 
else
Set objSWbemServices =objSWbemLocator.connectserver(domain,"root/cimv2",username,password) 
end if
Set Win_Process=objswbemservices.Get("Win32_ProcessStartup") 
Set Hide_Windows=Win_Process.SpawnInstance_ 
Hide_Windows.ShowWindow=12 
Set Rcmd=objswbemservices.Get("Win32_Process") 
Set colItems = objSWbemServices.ExecQuery("Select * From Win32_LogicalDisk where DriveType = 3")
Dim remoteDirectory
For Each objItem in colItems
remoteDirectory = objItem.Caption
Exit for
Next
remoteDirectory = remoteDirectory&"\cmdbTempFolder"
msg=Rcmd.create("cmd /c md "&remoteDirectory,Null,Hide_Windows,intProcessID)
msg=Rcmd.create("cmd /c "&command&" >"&remoteDirectory&"\"&resultTxt,Null,Hide_Windows,intProcessID)
WScript.Sleep waitTime
Set win32Share = objSWbemServices.Get("Win32_Share")
msg = win32Share.Create(remoteDirectory,"shareFolder",2147483648)
set wsh=createObject("wscript.shell")
wsh.run "cmd /c net use \\"&domain&"\shareFolder """&password&""" /user:"&username,0,1
if domain="127.0.0.1" then
wsh.run "cmd /c xcopy "&remoteDirectory&" "&folder&" /a",0,1
else
wsh.run "cmd /c xcopy \\"&domain&"\shareFolder "&folder&" /a",0,1
end if
wsh.run "cmd /c net use \\"&domain&"\shareFolder  /delete",0,1
set searcher = objSWbemServices.ExecQuery("Select * from Win32_Share Where Name = 'shareFolder'")
For Each shareFolder In searcher
    shareFolder.Delete()
Exit for
Next
msg=Rcmd.create("cmd /c rd/s/q "&remoteDirectory,Null,Hide_Windows,intProcessID)
Set objFSO = CreateObject("Scripting.FileSystemObject")
dim whileCount 
whileCount=0
while (Not objFSO.FileExists(folder&"\"&resultTxt)) and whileCount<100
    whileCount = whileCount+1
       WScript.Sleep 50
wend           
If objFSO.FileExists(folder&"\"&resultTxt) Then
   Set objFile = objFSO.OpenTextFile(folder&"\"&resultTxt)
    txt = objFile.ReadAll
    Wscript.Echo "execute success:"&txt
    objFile.close
    set sfile=objFSO.getfile(folder&"\"&resultTxt)
    sfile.attributes=0
    sfile.delete
Else
    Wscript.Echo "execute failed"
End If
