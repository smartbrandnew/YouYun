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
	WScript.Echo "Error # ����������������¼�û�������������"
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
		WScript.Echo "Error # ����������������¼�û�������������"
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

totalPhysicalMemorySize = 0
freePhysicalMemory = 0
totalVirtualMemorySize = 0
freeVirtualMemory = 0

Const WQL2 = "Select * From Win32_OperatingSystem"
Set colItems = wbemServices.ExecQuery(WQL2)
For Each objItem in colItems
	totalPhysicalMemorySize = totalPhysicalMemorySize + objItem.TotalVisibleMemorySize/1024
	totalVirtualMemorySize = totalVirtualMemorySize + objItem.TotalVirtualMemorySize/1024
	freePhysicalMemory = freePhysicalMemory + objItem.FreePhysicalMemory/1024
	freeVirtualMemory = freeVirtualMemory + objItem.FreeVirtualMemory/1024
Next

'WScript.Echo usedPhysicalMemorySize & vbTab & physicalMemoryUsage  & vbTab & usedVirtualMemorySize & vbTab & virtualMemoryUsage
WScript.Echo (totalPhysicalMemorySize - freePhysicalMemory) & vbTab & 100 * (1 - freePhysicalMemory / totalPhysicalMemorySize)_
	& vbTab & (totalVirtualMemorySize - freeVirtualMemory)  & vbTab & 100 * (1 - freeVirtualMemory / totalVirtualMemorySize)

'Done
WScript.Quit(0)

