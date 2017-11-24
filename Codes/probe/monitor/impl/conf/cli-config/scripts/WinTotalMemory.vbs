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

'get system tiem
Set colItems = wbemServices.ExecQuery( "Select * from Win32_ComputerSystem")
For Each objItem in colItems
totalMemory = objItem.TotalPhysicalMemory/1024/1024
Next

'print
Wscript.Echo totalMemory
'end

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	             " " & Err.Description
End If
'Done
WScript.Quit(0)
