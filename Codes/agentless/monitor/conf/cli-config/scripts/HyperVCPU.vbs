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
if isLocalHost="true" and WScript.Arguments.Count = 2 then
		Set wbemServices = objWbemLocator.ConnectServer _ 
		      (strHostAddr,"Root\virtualization")
else if isLocalHost="false" and WScript.Arguments.Count >= 3 then
		strUsername = Wscript.Arguments(2)
		strPasswd = ""
		if WScript.Arguments.Count = 4 then
		strPasswd = Wscript.Arguments(3)
		end if
		
		Set wbemServices = objWbemLocator.ConnectServer _ 
      			(strHostAddr,"Root\virtualization",strUsername,strPasswd)
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

	         
On Error Resume Next

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
             " " & Err.Description
End If
On Error GoTo 0

Const WQL = "Select * From Msvm_Processor"
   
Set colItems = wbemServices.ExecQuery(WQL,,48)
For Each objItem in colItems
	Name = objItem.Name
	LoadPercentage = objItem.LoadPercentage       
	Caption=objItem.Caption
	WQL1=" Select ElementName from Msvm_ComputerSystem where Name='"+Name+"'"		
	Set colItemsValue = wbemServices.ExecQuery(WQL1,,48)		   
	For Each objItem1 in colItemsValue
		Name=objItem1.ElementName
	Next
	Wscript.Echo LoadPercentage & "|| " & Name & " || " & Caption
       
Next

WScript.Quit(0)

