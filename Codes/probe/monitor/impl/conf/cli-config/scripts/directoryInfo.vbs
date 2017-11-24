On Error Resume Next
Set objWbemLocator = CreateObject ("WbemScripting.SWbemLocator")
if Err.Number Then
	WScript.Echo "Error # ����WbemScripting.SWbemLocator����ʧ�ܣ�" & Err.Description
End If
On Error GoTo 0	

if WScript.Arguments.Count < 4 then
	WScript.Echo "Error # ����������󣬵�¼��������"
	WScript.Quit(0)
end If

'��ȡ������ַ���Ƿ�Ϊ����IP����
strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
filParamIdx = 0

'��������
If isLocalHost="true" and WScript.Arguments.Count > 3 Then 
	On Error Resume Next
		Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2")
		filParamIdx = 2
	if Err.Number Then
		WScript.Echo "Error # ��������" & strHostAddr &"ʧ�ܣ�"& Err.Description
	End If
	On Error GoTo 0    
Else If isLocalHost="false" and WScript.Arguments.Count > 5 Then  
	username = Wscript.Arguments(2)
	password = Wscript.Arguments(3)
	
	On Error Resume Next
		Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2",username,password)
		filParamIdx = 4
	if Err.Number Then
		WScript.Echo "Error # ��������" & strHostAddr &"ʧ�ܣ���ȷ���û���"&strUsername&"��������ȷ"
	End If
	On Error GoTo 0
Else 
	WScript.Echo "Error # �����¼�û�������������"
	WScript.Quit(0)
End If
End If

'��ȡ�ļ�Ŀ¼�����Ŀ¼��������
strFolderName = Wscript.Arguments(filParamIdx)
intLevel = CInt(Wscript.Arguments(filParamIdx+1))
chkFolderExist strFolderName

'�����ļ�·��
arrFolderPath = Split(strFolderName, "\")
strNewPath = ""
strDrive = ""
For i = 0 to Ubound(arrFolderPath)
	If i > 0 Then
    		strNewPath = strNewPath & "\\" & arrFolderPath(i)
	Else
		strDrive = ""&arrFolderPath(i)
    	End If
Next
strPath = strNewPath & "\\"

	GetFileInfo strDrive,strPath
	If intLevel > 1 Then
   		GetSubFolders strFolderName,1
	End If
'����
WScript.Quit(0)

Sub GetSubFolders(strFolderName,currLevel)
	If currLevel <= intLevel Then
	On Error Resume Next
    		Set colSubfolders2 = wbemServices.ExecQuery("Associators of {Win32_Directory.Name='" & strFolderName & "'} " _
            		& "Where AssocClass = Win32_Subdirectory " _
                	& "ResultRole = PartComponent")
	If Err.Number Then
		WScript.Echo "Error # ��ȡ�ļ�Ŀ¼["&strFolderName&"]����Ŀ¼�ļ���Ϣʧ��"& Err.Description
	End If
	On Error GoTo 0
	
    	For Each objFolder2 in colSubfolders2
    		'�����ļ�·��
        	strFolderName = objFolder2.Name                
        	arrFolderPath = Split(strFolderName, "\")
       	 	strNewPath = ""
		strDrive = ""
        	For i = 0 to Ubound(arrFolderPath)
			If i > 0 Then
    				strNewPath = strNewPath & "\\" & arrFolderPath(i)
			Else
				strDrive = ""&arrFolderPath(i)
    			End If
        	Next
        	strPath = strNewPath & "\\"
			GetFileInfo strDrive,strPath
        	GetSubFolders strFolderName,(currLevel+1)
    	Next
    End If
End Sub

Sub GetFileInfo(strDrive,strPath)
'��ȡ��Ŀ¼�������ļ���Ϣ
	On Error Resume Next
		Set colFiles = wbemServices.ExecQuery("Select FileSize,CreationDate,LastModified,Name,FileType,Status from CIM_DataFile where Drive = '"&strDrive&"' and Path = '" & strPath & "'")
	If Err.Number Then
		WScript.Echo "Error # ��ȡ�ļ�Ŀ¼["&strDrive&strPath&"]�µ��ļ���Ϣʧ��"& Err.Description
	End If
	On Error GoTo 0
 	'��ȡ�ļ���Ŀ
	For Each objFile in colFiles
		'��ӡ�ļ���Ϣ
		Wscript.Echo objFile.FileSize&"||"&FormatDateTime(GetVBDate(objFile.CreationDate),0)&"||"&FormatDateTime(GetVBDate(objFile.LastModified),0)&"||"&objFile.Name&"||"&objFile.FileType&"||"&objFile.Status
	Next
End Sub

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

Function GetVBDate(wd)
  GetVBDate = DateSerial(left(wd,4),mid(wd,5,2),mid(wd,7,2)) + TimeSerial(mid(wd,9,2),mid(wd,11,2),mid(wd,13,2))
End Function