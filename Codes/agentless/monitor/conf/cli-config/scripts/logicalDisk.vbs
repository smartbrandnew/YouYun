On Error Resume Next
Set objWbemLocator = CreateObject ("WbemScripting.SWbemLocator")
If Err.Number Then
	WScript.Echo "Error # ����WbemScripting.SWbemLocator����ʧ�ܣ�" & Err.Description
End If
On Error GoTo 0	

If WScript.Arguments.Count < 3 then
	WScript.Echo "Error # ����������󣬵�¼��������"
	WScript.Quit(0)
End If

'��ȡ������ַ���Ƿ�Ϊ����IP����
strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
filParamIdx = 0

'��������
If isLocalHost="true" and WScript.Arguments.Count > 2 Then 
	On Error Resume Next
	Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2")
  wbemServices.Security_.ImpersonationLevel = 3
	filParamIdx = 2
	If Err.Number Then
		WScript.Echo "Error # ��������" & strHostAddr &"ʧ�ܣ�"& Err.Description
	End If
	On Error GoTo 0    
Else If isLocalHost="false" and WScript.Arguments.Count > 4 Then  
	username = Wscript.Arguments(2)
	password = Wscript.Arguments(3)
  
	On Error Resume Next
	Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2",username,password)
	wbemServices.Security_.ImpersonationLevel = 3
	filParamIdx = 4
	If Err.Number Then
		WScript.Echo "Error # ��������" & strHostAddr &"ʧ�ܣ�" &Err.Description &"����ȷ���û����������Ƿ���ȷ��"
		WScript.Quit(0)
	End If
	On Error GoTo 0
Else 
		WScript.Echo "Error # �����¼�û�������������"
		WScript.Quit(0)
End If
End If

strFolderName = Wscript.Arguments(filParamIdx)
'chkFolderExist strFolderName'ȡ����Ŀ¼�Ƿ���ڵ��жϣ�������Ŀ¼���޷���ȡ���ݡ�

'�����ļ�·��
arrFolderPath = Split(strFolderName, "\")
strDrive = ""&arrFolderPath(0)

On Error Resume Next
Set colItems = wbemServices.ExecQuery( "Select * from Win32_LogicalDisk where DeviceID='"&strDrive&"'")

For Each objItem in colItems
  Wscript.Echo  Int(objItem.Size /(1024 * 1024)) & "||" &Int(objItem.FreeSpace /(1024 * 1024))
Next

If Err.Number Then
	WScript.Echo "Error # ��ȡ������Ϣʧ�ܣ�" &Err.Description
End If

'����
WScript.Quit(0)

Sub chkFolderExist(arrFolderPath)
	On Error Resume Next
	Set dirs = wbemServices.ExecQuery("Associators of {Win32_Directory.Name='" & arrFolderPath & "'}" )
	If dirs.Count>0 Then
	End If
	If Err.Number Then
		WScript.Echo "Error # Ŀ¼["&arrFolderPath&"]������"
	End If
	On Error GoTo 0
End Sub
