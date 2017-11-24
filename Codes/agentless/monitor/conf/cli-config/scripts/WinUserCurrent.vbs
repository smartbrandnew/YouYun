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
Set colItems = wbemServices.ExecQuery("Select LogonId from Win32_LogonSession where LogonType=2")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
End If
On Error GoTo 0
		
On Error Resume Next
For Each objItem in colItems
	For Each param in objItem.Properties_
	Set cols = wbemServices.ExecQuery("Select Antecedent from Win32_LoggedOnUser where Dependent like '%"+param.Value+"%'");
	if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
    End If
    On Error GoTo 0
	On Error Resume Next
	For Each obj in cols
	AntecedentInfo=""
	For Each p in obj.Properties_
	AntecedentInfo = AntecedentInfo & " " & p.Value
	Next
	WScript.Echo AntecedentInfo
	Next
Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # 获取Windows注册用户信息时发生错误:" &" " & Err.Description
             
End If
On Error GoTo 0

'Done
WScript.Quit(0)

