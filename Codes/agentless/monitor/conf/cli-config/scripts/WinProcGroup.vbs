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

'get data
Set old_colItems = wbemServices.ExecQuery("SELECT ProcessId,Name,WorkingSetSize FROM Win32_Process")
Set objRefresher = CreateObject("WbemScripting.SWbemRefresher")
Set new_colItems = objRefresher.AddEnum(wbemServices, "Win32_PerfFormattedData_PerfProc_Process").objectSet
objRefresher.Refresh
Wscript.Sleep 3000
objRefresher.Refresh
	
sum = 0
For Each new_objItem in new_colItems		
	if new_objItem.Name <> "_Total" then
		sum = sum + new_objItem.PercentProcessorTime
	end if	
next
	
cpusum = 0	
For Each old_objItem in old_colItems	
	procName = old_objItem.Name
	procMemory = old_objItem.WorkingSetSize/1024/1024
	cpuUsage = 0	
	if sum > 0 then
		For Each new_objItem in new_colItems
			if old_objItem.ProcessId = new_objItem.IDProcess then
				temp = new_objItem.PercentProcessorTime
				cpuUsage = round(temp / sum * 100, 0)
				exit for
			end if		
		next	
	end if
	
	cpusum = cpusum + cpuUsage
	
	Wscript.Echo procName & "|" & cpuUsage & "|" & procMemory & "|run"
	'if cpuUsage > 0 or temp > 0 then
	'	Wscript.Echo procName & "|                " & cpuUsage & "              " & temp
	'end if
Next

'Wscript.Echo "sum = " & sum
'Wscript.Echo "cpusum = " & cpusum

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
End If
'Done
WScript.Quit(0)