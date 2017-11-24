On Error Resume Next
Set objWbemLocator = CreateObject("WbemScripting.SWbemLocator")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

if WScript.Arguments.Count < 2 then
	WScript.Echo "Error # 传入参数错误，请检查登录用户名、密码配置"
	WScript.Quit(0)
end if

strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
if isLocalHost="true" and WScript.Arguments.Count = 2 then
	Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2")
elseif isLocalHost="false" and WScript.Arguments.Count >= 3 then
	strUsername = Wscript.Arguments(2)
	strPasswd = ""
	if WScript.Arguments.Count = 4 then
		strPasswd = Wscript.Arguments(3)
	end if

	Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2",strUsername,strPasswd)
else
	WScript.Echo "Error # 传入参数错误，请检查登录用户名、密码配置"
	WScript.Quit(0)
end if

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

Set colItems = wbemServices.ExecQuery("select name, ActiveRPCthreads, LDAPSearchcalls, LDAPResults from Win32_PerfFormattedData_MSExchangeAL_MSExchangeAL")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

On Error Resume Next
If -2147217392 = Err.Number Then '无效类别
	WScript.Echo vbCrLf & "Error # 获取Exchange WMI相关信息时发生错误，系统不支持此WMI服务，请使用性能管理器确定与修正Exchange WMI。"
	WScript.Quit(0)
elseif Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
end if

For Each row in colItems
	WScript.Echo row.name _
		& "|" & row.ActiveRPCthreads _
		& "|" & row.LDAPSearchcalls _
		& "|" & row.LDAPResults
Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # 获取Exchange信息时发生错误:" &" " & Err.Description
End If

WScript.Quit(0)
