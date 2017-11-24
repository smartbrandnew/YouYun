On Error Resume Next
Set objWbemLocator = CreateObject("WbemScripting.SWbemLocator")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

if WScript.Arguments.Count < 2 then
	WScript.Echo "Error # ����������������¼�û�������������"
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
	WScript.Echo "Error # ����������������¼�û�������������"
	WScript.Quit(0)
end if

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

set objRefresher = CreateObject("WbemScripting.SWbemRefresher")
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

Set colItems = objRefresher.AddEnum(wbemServices, "Win32_PerfFormattedData_ESE_MSExchangeDatabase").objectSet
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

objRefresher.Refresh
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If
Wscript.Sleep 5000
objRefresher.Refresh
if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & " " & Err.Description
	WScript.Quit(0)
End If

For Each row in colItems
	WScript.Echo row.name _
		& "|" & row.DatabaseCacheSizeMB _
		& "|" & row.IODatabaseReadsPersec _
		& "|" & row.IODatabaseWritesPersec _
		& "|" & row.IOLogReadsPersec _
		& "|" & row.IOLogWritesPersec _
		& "|" & row.LogBytesWritePersec		 
Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # ��ȡExchange��Ϣʱ��������:" &" " & Err.Description
End If

WScript.Quit(0)
