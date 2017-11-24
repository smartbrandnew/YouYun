On Error Resume Next
Set objWbemLocator = CreateObject ("WbemScripting.SWbemLocator")
If Err.Number Then
	WScript.Echo "Error # 创建WbemScripting.SWbemLocator对象失败：" & Err.Description
End If
On Error GoTo 0	

If WScript.Arguments.Count < 3 then
	WScript.Echo "Error # 传入参数错误，登录参数配置"
	WScript.Quit(0)
End If

'获取主机地址、是否为本地IP参数
strHostAddr = Wscript.Arguments(0)
isLocalHost = Wscript.Arguments(1)
filParamIdx = 0

'连接主机
If isLocalHost="true" and WScript.Arguments.Count > 2 Then 
	On Error Resume Next
	Set wbemServices = objWbemLocator.ConnectServer(strHostAddr,"Root\CIMV2")
  wbemServices.Security_.ImpersonationLevel = 3
	filParamIdx = 2
	If Err.Number Then
		WScript.Echo "Error # 连接主机" & strHostAddr &"失败："& Err.Description
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
		WScript.Echo "Error # 连接主机" & strHostAddr &"失败：" &Err.Description &"。请确认用户名及密码是否正确。"
		WScript.Quit(0)
	End If
	On Error GoTo 0
Else 
		WScript.Echo "Error # 请检查登录用户名、密码配置"
		WScript.Quit(0)
End If
End If

strFolderName = Wscript.Arguments(filParamIdx)
'chkFolderExist strFolderName'取消对目录是否存在的判断，否则传入目录，无法获取数据。

'生成文件路径
arrFolderPath = Split(strFolderName, "\")
strDrive = ""&arrFolderPath(0)

On Error Resume Next
Set colItems = wbemServices.ExecQuery( "Select * from Win32_LogicalDisk where DeviceID='"&strDrive&"'")

For Each objItem in colItems
  Wscript.Echo  Int(objItem.Size /(1024 * 1024)) & "||" &Int(objItem.FreeSpace /(1024 * 1024))
Next

If Err.Number Then
	WScript.Echo "Error # 获取磁盘信息失败：" &Err.Description
End If

'返回
WScript.Quit(0)

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
