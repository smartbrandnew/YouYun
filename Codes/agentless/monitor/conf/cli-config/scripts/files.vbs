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
filParamIdx = 0
isLocalHost = Wscript.Arguments(1)
if isLocalHost="true" and WScript.Arguments.Count > 2 then
		Set wbemServices = objWbemLocator.ConnectServer _ 
		      (strHostAddr,"Root\CIMV2")
		filParamIdx = 2
		
		if Err.Number Then
			WScript.Echo vbCrLf & "Error # ��������" & strHostAddr &"ʧ��"
		End If
		On Error GoTo 0
		
else if isLocalHost="false" and WScript.Arguments.Count > 4 then
		strUsername = Wscript.Arguments(2)
		strPasswd = Wscript.Arguments(3)
		
		Set wbemServices = objWbemLocator.ConnectServer _ 
      			(strHostAddr,"Root\CIMV2",strUsername,strPasswd)
    filParamIdx = 4
    
		if Err.Number Then
			WScript.Echo vbCrLf & "Error # ��������" & strHostAddr &"ʧ�ܣ���ȷ���û���"&strUsername&"��������ȷ"
		End If
		On Error GoTo 0
else
		WScript.Echo "Error # �����¼�û�������������"
		WScript.Quit(0)
end if
end if


strCondition = ""
do while filParamIdx < WScript.Arguments.Count
	strCondition = strCondition & " or Name='" & WScript.Arguments(filParamIdx) & "'"
	filParamIdx = filParamIdx + 1
loop
strCondition = Replace(strCondition,"\","\\")

strCondition = Right(strCondition,len(strCondition)-4)

On Error Resume Next
Set colItems = wbemServices.ExecQuery( "Select FileSize,LastModified,Name from CIM_DataFile where "&strCondition&"")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
End If
On Error GoTo 0
		
On Error Resume Next
For Each objItem in colItems
	WScript.Echo objItem.FileSize&"||"&Replace(FormatDateTime(GetVBDate(objItem.LastModified),0)," ","_")&"||"&objItem.Name
Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # ��ȡ�ļ���С������޸�ʱ���Լ��ļ�������Ϣʱ��������:" &" " & Err.Description
             
End If
On Error GoTo 0

Function GetVBDate(wd)
  GetVBDate = DateSerial(left(wd,4),mid(wd,5,2),mid(wd,7,2)) + TimeSerial(mid(wd,9,2),mid(wd,11,2),mid(wd,13,2))
End Function


'Done
WScript.Quit(0)

