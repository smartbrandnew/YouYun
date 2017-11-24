' Check command line parameters

On Error Resume Next
Set objWbemLocator = CreateObject _
	("WbemScripting.SWbemLocator")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # "  & _
	             " " & Err.Description
	WScript.Quit(0)
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
On Error GoTo 0


On Error Resume Next
Set colItems = wbemServices.ExecQuery( "Select ServiceUptime,GetRequestsPersec,PostRequestsPersec,HeadRequestsPersec,OtherRequestMethodsPersec,Name from Win32_PerfRawData_W3SVC_WebService")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
	WScript.Quit(0)
End If
On Error GoTo 0

Dim count
On Error Resume Next
count = colItems.Count
If -2147217392=Err.Number Then '无效类别
	WScript.Echo vbCrLf & "Error # 获取IIS Web请求信息时发生错误，系统不支持此WMI服务，请确定IIS是否被正确安装。"
	WScript.Quit(0)
ElseIf Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
	WScript.Quit(0)
End If
On Error GoTo 0

On Error Resume Next
i=0
For Each objItem in colItems
	fileInfo="|"
	For Each param in objItem.Properties_
	fileInfo = fileInfo & " " & param.Value &"|"
	Next
	WScript.Echo fileInfo
Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # 获取IIS Web请求信息时发生错误:" &" " & Err.Description
             
End If
On Error GoTo 0

'Done
WScript.Quit(0)

