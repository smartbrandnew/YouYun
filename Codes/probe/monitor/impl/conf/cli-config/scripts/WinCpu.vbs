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
End If
On Error GoTo 0

	         
On Error Resume Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
End If
On Error GoTo 0

Const WQL = "Select * From Win32_PerfRawData_PerfOS_Processor Where Name = '_Total'"
Set colItems = wbemServices.ExecQuery(WQL)
For Each objItem in colItems
    sys1 = objItem.PercentPrivilegedTime
    user1 = objItem.PercentUserTime
    t1 = objItem.TimeStamp_Sys100NS
Next

For i = 0 to 0
    Wscript.Sleep(1000)
    Set colItems = wbemServices.ExecQuery(WQL)
    For Each objItem in colItems
        sys2 = objItem.PercentPrivilegedTime
        user2 = objItem.PercentUserTime
        t2 = objItem.TimeStamp_Sys100NS
        If t2 - t1 = 0 Then
            Wscript.Echo "0 0"
        Else
            sys = 100 * (sys2-sys1) / (t2-t1)
			user = 100 * (user2-user1) / (t2-t1)
			If sys > 100 Then
				sys = 99.99
			End If
			If user > 100 Then
				user = 99.99
			End If
            Wscript.Echo sys & " " & user
        End if
    Next
Next
'Done
WScript.Quit(0)

