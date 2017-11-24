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
strLastTime =""

if isLocalHost="true" then
    Set wbemServices = objWbemLocator.ConnectServer _ 
          (strHostAddr,"Root\CIMV2")

strLastTime = Wscript.Arguments(2)

else if isLocalHost="false" and WScript.Arguments.Count >= 3 then
    strUsername = Wscript.Arguments(2)
    strPasswd = ""
    if WScript.Arguments.Count >= 4 then
    strPasswd = Wscript.Arguments(3)
    end If
    strLastTime = Wscript.Arguments(4)
    
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

'strLogFile = Wscript.Arguments(4) '暂时不用
'strEventType = "" '暂时不用

'if WScript.Arguments.Count = 7 then
'  strEventType = Wscript.Arguments(6)
'end if

Set dateTime = CreateObject("WbemScripting.SWbemDateTime")
dateTime.SetVarDate (CDate(strLastTime))

Set colLoggedEvents = wbemServices.ExecQuery _
    ("Select * from Win32_NTLogEvent " _
        & "Where (Logfile = 'System' or Logfile = 'Security' or Logfile = 'Application' or Logfile = 'Internet') and (EventType = '1' or  EventType = '2' or EventType = '5') and TimeWritten > '" & dateTime & "'")


Const adVarChar = 200
Const MaxCharacters = 255
Set DataList = CreateObject("ADOR.Recordset") 
DataList.Fields.Append "EventCode", adVarChar, MaxCharacters
DataList.Fields.Append "RecordNumber", adVarChar, MaxCharacters
DataList.Fields.Append "SourceName", adVarChar, MaxCharacters
DataList.Fields.Append "TimeWritten", adVarChar, MaxCharacters
DataList.Fields.Append "Type", adVarChar, MaxCharacters
DataList.Fields.Append "Logfile", adVarChar, MaxCharacters
DataList.Fields.Append "Message", adVarChar, MaxCharacters
DataList.Open

allCount = 1
For Each objEvent in colLoggedEvents
	DataList.AddNew
	DataList("EventCode") = objEvent.EventCode
	DataList("RecordNumber") = objEvent.RecordNumber
	DataList("SourceName") = objEvent.SourceName
	DataList("TimeWritten") = GetVBDate(objEvent.TimeWritten)
	DataList("Type") = objEvent.Type
	DataList("Logfile") = objEvent.Logfile
	
	If IsNull(objEvent.Message) then
		DataList("Message") = ""
	Else
		tempStr = Left(objEvent.Message, 12)
		If Len(tempStr) = 12 then
			tempStr = tempStr & "..."
		End If
		DataList("Message") = tempStr
	End If
 	DataList.Update
	allCount = allCount + 1
Next

If allCount <= 1 then
	WScript.Quit(0)
End If

count = 0
DataList.Sort = "TimeWritten"
DataList.MoveLast
Do Until DataList.BOF
 Wscript.Echo (count + 1)  & "||" _
		& DataList("EventCode") & "||" _
	    & DataList("RecordNumber") & "||" _
	    & DataList("SourceName") & "||" _
	    & DataList("TimeWritten") & "||" _
	    & DataList("Type") & "||" _
	    & DataList("Logfile") & "||" _
	    & DataList("Message")
 count = count + 1 
 If count >= 10 and allCount > 0 then 
	If count >= 11 then 
		Exit Do
	Else 
		DataList.MoveFirst
		count = allCount - 1
	End If
 Else
	DataList.MovePrevious
 End If
Loop

Function GetVBDate(wd)
    'yyyy-MM-dd HH:mm:ss
    GetVBDate = left(wd,4)&"-"&mid(wd,5,2)&"-"&mid(wd,7,2)&" " _
    &mid(wd,9,2)&":"&mid(wd,11,2)&":"&mid(wd,13,2)
End Function

if Err.Number Then
  WScript.Echo vbCrLf & "Error # " & _
               " " & Err.Description
End If
'Done
WScript.Quit(0)