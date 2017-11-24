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

dim abc(18)

Const WQL_System = "Select * From Msvm_ComputerSystem"

Set colItems = wbemServices.ExecQuery(WQL_System,,48)
For Each objItem in colItems
    Name = objItem.Name
    Description = objItem.Description
    ElementName = objItem.ElementName
    HealthState = objItem.HealthState      
    InstallDate	= objItem.InstallDate	
	OnTimeInMilliseconds=objItem.OnTimeInMilliseconds
	EnabledState = objItem.EnabledState 	
	Caption=objItem.Caption
	TimeOfLastStateChange=objItem.TimeOfLastStateChange
	
	If EnabledState=0 then
    EnabledState="未知"
	ElseIf EnabledState=2 then
    EnabledState="正常运行"
	ElseIf EnabledState=3 then
	EnabledState="关机"
    ElseIf EnabledState=32768 then
	EnabledState="暂停"
    ElseIf EnabledState=32769 then
	EnabledState="已保存"
	ElseIf EnabledState=32770 then
	EnabledState="正在启动"
	ElseIf EnabledState=32771 then
	EnabledState="正在建立快照"
	ElseIf EnabledState=32773 then		 
	EnabledState="正在保存"
	ElseIf EnabledState=32774 then
	EnabledState="正在关机"
	ElseIf EnabledState=32776 then
	EnabledState="正在暂停"
	ElseIf EnabledState=32777 then
	EnabledState="正在恢复"
	Else
	EnabledState="未知"
	end IF			
	   
	WQL_Service="Select * from Msvm_VirtualSystemManagementService"		
	Set Management = wbemServices.ExecQuery(WQL_Service,,48)
	For Each objItem2 in Management
		abc(0) = 0
		abc(1) = 1
		abc(2) = 2
		abc(3) = 3
		abc(4) = 4
		abc(5) = 100
		abc(6) = 101
		abc(7) = 101
		abc(8) = 103
		abc(9) = 104
		abc(10) = 105
		abc(11) = 106
		abc(12) = 107
		abc(13) = 108
		abc(14) = 109
		abc(15) = 110
		abc(16) = 111
		abc(17) = 112
		abc(18) = 113

		dim var
		state = objItem2.GetSummaryInformation(,abc,var)
		
		if state=0 then
	
		For Each objItem1 in var
			ID = objItem1.Name
			
			if(ID = Name) then
				Notes = objItem1.Notes
				CreationTime = objItem1.CreationTime
				'不知道什么
				Heartbeat = objItem1.Heartbeat
				GuestOperatingSystem = objItem1.GuestOperatingSystem
				'分配给虚拟机的内存
				MemoryUsage = objItem1.MemoryUsage
				'不知道什么
				MemoryAvailable = objItem1.MemoryAvailable
				'不知道什么
				AvailableMemoryBuffer = objItem1.AvailableMemoryBuffer
				'分配的处理器数量
				NumberOfProcessors = objItem1.NumberOfProcessors
				'可能是CPU使用率
				ProcessorLoad = objItem1.ProcessorLoad
				UpTime = objItem1.UpTime
				
				Wscript.Echo Description & " || "& ElementName & " || " & EnabledState & " || " & CreationTime & " || " & OnTimeInMilliseconds  &  " || " & Caption  &  "|| " & HealthState &  " || " & TimeOfLastStateChange  & " || " & GuestOperatingSystem & " || " & Notes & " || " & MemoryUsage & " || " & NumberOfProcessors & " || " & ProcessorLoad & " || " & UpTime
			end if
			
			
		Next
		
		else 
			WScript.Echo "Error # 无法获取数据"
			WScript.Quit(0)
		end if
		
	Next
	   
Next	




'Done
WScript.Quit(0)

