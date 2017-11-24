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

Const WQL1 = "Select * From Win32_ComputerSystem"
Const WQL2 = "Select * From Win32_OperatingSystem"
Const WQL3 = "Select * From Win32_NetworkAdapterConfiguration where MacAddress is not null"
Const WQL4 = "Select * From win32_Diskdrive"
Set colItems = wbemServices.ExecQuery(WQL1)
For Each objItem in colItems
	strComputerType = objItem.Manufacturer & "-" + objItem.Model & "(" + objItem.SystemType + ")"
	iCpuCount = objItem.NumberOfProcessors
Next

Set colItems = wbemServices.ExecQuery(WQL2)
For Each objItem in colItems
	strSystemName = objItem.Caption & "(" & objItem.CSDVersion & ")"
	strVersion = objItem.Version
	iProcessCount = objItem.NumberOfProcesses
	iMemorySize = objItem.TotalVisibleMemorySize/1024
	strHostName = objItem.CSName
Next

intfList = ""
Set colItems = wbemServices.ExecQuery(WQL3)
For Each objItem in colItems
	if IsArray(objItem.IPAddress) Then
		For Each ip in objItem.IPAddress
			If intfList <> "" Then
				intfList = intfList & ";"
			End If
			intfList = intfList & objItem.Description & "=" & ip & "/" & objItem.MacAddress
		Next
	Else
		If intfList <> "" Then
			intfList = intfList & ";"
		End If
		intfList = intfList & objItem.Description & "=" & objItem.IPAddress & "/" & objItem.MacAddress		
	End If		
Next

Set colItems = wbemServices.ExecQuery(WQL4)
For Each objItem in colItems
	If diskName = "" Then
		diskName = objItem.Name
	else
		diskName = diskName & "@" & objItem.Name
	End if
Next

'WScript.Echo totalVisibleMemorySize & vbTab & freePhysicalMemory & vbTab & totalVirtualMemorySize & vbTab & freeVirtualMemory
WScript.Echo iCpuCount & "||" & strComputerType _
	& "||" & strSystemName & "||" & strVersion _
	& "||" & iMemorySize & "||" & iProcessCount & "||" & intfList & "||" & diskName & "||" & strHostName

'Done
WScript.Quit(0)