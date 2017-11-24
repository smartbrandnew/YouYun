On Error Resume Next
Set objWbemLocator = CreateObject ("WbemScripting.SWbemLocator")
if Err.Number Then
	WScript.Echo "Error # 创建WbemScripting.SWbemLocator对象失败：" & Err.Description
End If
On Error GoTo 0	

if WScript.Arguments.Count < 4 then
	WScript.Echo "Error # 传入参数错误，登录参数配置"
	WScript.Quit(0)
end If

'获取主机地址、是否为本地IP参数
strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
filParamIdx = 0

'连接主机
If isLocalHost="true" and WScript.Arguments.Count > 3 Then 
	On Error Resume Next
		Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2")
		filParamIdx = 2
	if Err.Number Then
		WScript.Echo "Error # 连接主机" & strHostAddr &"失败："& Err.Description
	End If
	On Error GoTo 0    
Else If isLocalHost="false" and WScript.Arguments.Count > 5 Then  
	username = Wscript.Arguments(2)
	password = Wscript.Arguments(3)
	
	On Error Resume Next
		Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2",username,password)
		filParamIdx = 4
	if Err.Number Then
		WScript.Echo "Error # 连接主机" & strHostAddr &"失败，请确认用户名"&strUsername&"及密码正确"
	End If
	On Error GoTo 0
Else 
	WScript.Echo "Error # 请检查登录用户名、密码配置"
	WScript.Quit(0)
End If
End If

'获取文件目录、监测目录层数参数
strFolderName = Wscript.Arguments(filParamIdx)
intLevel = CInt(Wscript.Arguments(filParamIdx+1))
chkFolderExist strFolderName

'生成文件路径
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
'返回
WScript.Quit(0)

Sub GetSubFolders(strFolderName,currLevel)
	If currLevel <= intLevel Then
	On Error Resume Next
    		Set colSubfolders2 = wbemServices.ExecQuery("Associators of {Win32_Directory.Name='" & strFolderName & "'} " _
            		& "Where AssocClass = Win32_Subdirectory " _
                	& "ResultRole = PartComponent")
	If Err.Number Then
		WScript.Echo "Error # 获取文件目录["&strFolderName&"]的子目录文件信息失败"& Err.Description
	End If
	On Error GoTo 0
	
    	For Each objFolder2 in colSubfolders2
    		'生成文件路径
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
'获取该目录下所有文件信息
	On Error Resume Next
		Set colFiles = wbemServices.ExecQuery("Select FileSize,CreationDate,LastModified,Name,FileType,Status from CIM_DataFile where Drive = '"&strDrive&"' and Path = '" & strPath & "'")
	If Err.Number Then
		WScript.Echo "Error # 获取文件目录["&strDrive&strPath&"]下的文件信息失败"& Err.Description
	End If
	On Error GoTo 0
 	'获取文件数目
	For Each objFile in colFiles
		'打印文件信息
		Wscript.Echo objFile.FileSize&"||"&FormatDateTime(GetVBDate(objFile.CreationDate),0)&"||"&FormatDateTime(GetVBDate(objFile.LastModified),0)&"||"&objFile.Name&"||"&objFile.FileType&"||"&objFile.Status
	Next
End Sub

Sub chkFolderExist(arrFolderPath)
	On Error Resume Next
	Set dirs = wbemServices.ExecQuery("Associators of {Win32_Directory.Name='" & arrFolderPath & "'}" )
	If dirs.Count>0 Then
	End If
	If Err.Number Then
		WScript.Echo "Error # 目录["&arrFolderPath&"]不存在"
	End If
	On Error GoTo 0
End Sub

Function GetVBDate(wd)
  GetVBDate = DateSerial(left(wd,4),mid(wd,5,2),mid(wd,7,2)) + TimeSerial(mid(wd,9,2),mid(wd,11,2),mid(wd,13,2))
End Function