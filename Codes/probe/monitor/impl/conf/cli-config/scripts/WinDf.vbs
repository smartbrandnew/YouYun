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
		if WScript.Arguments.Count >= 4 then
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



WQL = "Select * From Win32_LogicalDisk Where DriveType='3'"
If WScript.Arguments.Count = 5 Then
  WQL = WQL + " and DeviceID='"&Split(Wscript.Arguments(4), "\")(0)&"'"
End If

Set colItems = wbemServices.ExecQuery(WQL)
For Each objItem in colItems
	total = objItem.Size/1024/1024
	free = objItem.FreeSpace/1024/1024
	used = total - free
	percent = 100*used/total
	If percent > 100 Then
		percent = 99.99
	End If
	WScript.Echo objItem.Name & vbTab & total & vbTab & free & vbTab & percent
Next

'Done
WScript.Quit(0)

