' Check command line parameters

On Error Resume Next
Set objWbemLocator = CreateObject _
	("WbemScripting.SWbemLocator")
if Err.Number Then
	WScript.Echo vbCrLf & "Error # "  & _
	             " " & Err.Description
End If
On Error GoTo 0	

On Error Resume Next
if WScript.Arguments.Count < 2 then
	WScript.Echo "Error # 传入参数错误，请检查登录用户名、密码配置"
	WScript.Quit(0)
end if

strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
if isLocalHost="true" and WScript.Arguments.Count = 2 then
		Set wbemServices = objWbemLocator.ConnectServer _ 
		      (strHostAddr,"Root\CIMV2")
else if isLocalHost="false" and WScript.Arguments.Count >= 3 then
		strUsername = Wscript.Arguments(2)
		strPasswd = ""
		if WScript.Arguments.Count = 4 then
		strPasswd = Wscript.Arguments(3)
		end if
		
		Set wbemServices = objWbemLocator.ConnectServer _ 
      			(strHostAddr,"Root\CIMV2",strUsername,strPasswd)
else
		WScript.Echo "Error # 传入参数错误，请检查登录用户名、密码配置"
		WScript.Quit(0)
end if
end if

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
	WScript.Quit(0)
End If

set objRefresher = CreateObject("WbemScripting.SWbemRefresher")
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
	WScript.Quit(0)
End If
Set colDisks = objRefresher.AddEnum _
    (wbemServices, "Win32_PerfFormattedData_PerfDisk_PhysicalDisk"). _
        objectSet
if Err.Number Then
  WScript.Echo vbCrLf & "Error # " & _
               " " & Err.Description
  WScript.Quit(0)
End If

objRefresher.Refresh
if Err.Number Then
  WScript.Echo vbCrLf & "Error # " & _
               " " & Err.Description
  WScript.Quit(0)
End If
Wscript.Sleep 1000
objRefresher.Refresh
if Err.Number Then
  WScript.Echo vbCrLf & "Error # " & _
               " " & Err.Description
  WScript.Quit(0)
End If

'Wscript.Echo "name"& & vbTab &"CurrentDiskQueueLength"   _
'		& vbTab &"DiskReadBytesPerSec" & vbTab &"DiskReadsPerSec"  & vbTab &"DiskWriteBytesPerSec" _
'	  & vbTab &"DiskWritesPerSec"& vbTab &"PercentDiskReadTime"& vbTab &"PercentDiskTime"& vbTab  _
'		&"PercentDiskWriteTime"& vbTab &"PercentIdleTime"
For Each objDisk in colDisks
	if Err.Number Then
		WScript.Echo vbCrLf & "Error # " & _
		             " " & Err.Description
		WScript.Quit(0)
	End If
		
        Wscript.Echo objDisk.Name &"|"& objDisk.CurrentDiskQueueLength  _
			 &"|"& objDisk.DiskReadBytesPerSec &"|"& objDisk.DiskReadsPerSec  &"|"& objDisk.DiskWriteBytesPerSec _
		  &"|"& objDisk.DiskWritesPerSec &"|"& objDisk.PercentDiskReadTime &"|"& objDisk.PercentDiskTime  _
			&"|"& objDisk.PercentDiskWriteTime &"|"& objDisk.PercentIdleTime
	if Err.Number Then
		WScript.Echo vbCrLf & "Error # " & _
		             " " & Err.Description
		WScript.Quit(0)
	End If
Next
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
End If
'Done
WScript.Quit(0)

