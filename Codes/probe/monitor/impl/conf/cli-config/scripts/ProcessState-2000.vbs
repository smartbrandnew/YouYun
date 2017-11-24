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

'get system tiem
On Error Resume Next
t = now()
systimeStr = year(t) &"-"& month(t) &"-"& day(t) &" "& hour(t) &":"& minute(t) &":"& second(t)

WScript.Echo systimeStr

'get data
Set colItems = wbemServices.ExecQuery( _
    "SELECT * FROM Win32_Process",,48)

For Each objItem in colItems
	if objItem.CreationDate <> "" then
		Wscript.Echo objItem.Name & "|" & objItem.PageFileUsage / 1024 & "|" & objItem.CreationDate & "|" & systimeStr
	else
		Wscript.Echo objItem.Name & "|" & objItem.PageFileUsage / 1024 & "|" & "empty" & "|" & systimeStr
	end if
Next
'end

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
End If
'Done
WScript.Quit(0)