
Set objWbemLocator = CreateObject ("WbemScripting.SWbemLocator")
'��ȡ������ַ���ļ�·������
strHostAddr = Wscript.Arguments(0)
strFolderName = Wscript.Arguments(1)
intLevel = CInt(Wscript.Arguments(2))
	Wscript.Echo "strHostAddr="&strHostAddr&"strFolderName="&strFolderName

'��������
Set wbemServices = objWbemLocator.ConnectServer (strHostAddr,"Root\CIMV2")

'�����ļ�·��
arrFolderPath = Split(strFolderName, "\")
strNewPath = ""
strDrive = ""
For i = 0 to Ubound(arrFolderPath)
	if i > 0 then
    	strNewPath = strNewPath & "\\" & arrFolderPath(i)
	else 
		strDrive = ""&arrFolderPath(i)
    end if
Next
strPath = strNewPath & "\\"

	GetFileInfo strDrive,strPath
	if intLevel > 1 then
   		GetSubFolders strFolderName,1
	end if
'����
WScript.Quit(0)

Sub GetSubFolders(strFolderName,currLevel)
	if currLevel <= intLevel then
    	Set colSubfolders2 = wbemServices.ExecQuery("Associators of {Win32_Directory.Name='" & strFolderName & "'} " _
            & "Where AssocClass = Win32_Subdirectory " _
                & "ResultRole = PartComponent")

    	For Each objFolder2 in colSubfolders2
    		'�����ļ�·��
        	strFolderName = objFolder2.Name                
        	arrFolderPath = Split(strFolderName, "\")
       	 	strNewPath = ""
			strDrive = ""
        	For i = 0 to Ubound(arrFolderPath)
				if i > 0 then
    				strNewPath = strNewPath & "\\" & arrFolderPath(i)
				else 
					strDrive = ""&arrFolderPath(i)
    			end if
        	Next
        	strPath = strNewPath & "\\"
			GetFileInfo strDrive,strPath
        	GetSubFolders strFolderName,(currLevel+1)
    	Next
    end if
End Sub

Sub GetFileInfo(strDrive,strPath)
'��ȡ��Ŀ¼�������ļ���Ϣ
	Set colFiles = wbemServices.ExecQuery("Select FileSize,CreationDate,LastModified,Name from CIM_DataFile where Drive = '"&strDrive&"' and Path = '" & strPath & "'")
 	'��ȡ�ļ���Ŀ
	For Each objFile in colFiles
		'��ӡ�ļ���Ϣ
		Wscript.Echo "FileSize="&objFile.FileSize&" CreationDate="&objFile.CreationDate&" LastModified="&objFile.LastModified&" Name="&objFile.Name
	Next
End Sub

