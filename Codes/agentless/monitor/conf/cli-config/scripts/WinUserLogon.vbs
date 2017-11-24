' Check command line parameters

On Error Resume Next
Set objWbemLocator = CreateObject _
	("WbemScripting.SWbemLocator")

if Err.Number Then
	WScript.Echo vbCrLf & "Error # "  & _
	             " Source:" &Err.Source &",Description:" & Err.Description
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
		      (strHostAddr,"Root\CIMV2")
else if isLocalHost="false" and WScript.Arguments.Count >= 3 then
		strUsername = Wscript.Arguments(2)
		strPasswd = ""
		if WScript.Arguments.Count = 4 then
		strPasswd = Wscript.Arguments(3)
		end if
		
		Set wbemServices = objWbemLocator.ConnectServer _ 
      			(strHostAddr,"Root\CIMV2",strUsername,strPasswd)
else
		WScript.Echo "Error # 传入参数错误，请检查登录用户名、密码配置"
		WScript.Quit(0)
end if
end if

if Err.Number Then
	WScript.Echo vbCrLf & "Error # " & _
	               "Description: " & Err.Description 
End If
On Error GoTo 0

' 从原理上来说，session中的type=2为本地登录，type=10为mstsc登录
set sessions = wbemServices.ExecQuery("Select * from Win32_LogonSession")
set users = wbemServices.ExecQuery("Select * from Win32_LoggedOnUser")
set processes = wbemServices.ExecQuery("Select * from Win32_SessionProcess")

' 开始处理
dim accessed(100, 2)
curr = 0
consoleCount = 1
rdpCount = 1
for each session in sessions
	' 过滤没有任何进程的用户
	exists = false
	for each process in processes
		processLogonId = between(process.antecedent, "LogonId=""", """")
		if session.logonId = processLogonId then
			exists = true
			exit for
		end if
	next
		
	if exists and (session.logonType = 0 or session.logonType = 2 or session.logonType = 3 or session.logonType = 10) then
		for each user in users
			userLogonId = between(user.dependent, """", """")
			if session.logonId = userLogonId then			
				domain = between(user.antecedent, "Domain=""", """")
				name = between(user.antecedent, "Name=""", """")
				' 过滤win2012 DWM用户
				if len(name) > 3 and mid(name, 1, 3) = "DWM" then
					exit for
				elseif name = "SYSTEM" then
					exit for
				end if 				
			
				' 过滤win2012 重复用户
				name = domain & "\" & name
				loginTime = session.startTime
				exists = false
				for j = 0 to curr 
					if accessed(j, 0) = name and accessed(j, 1) = loginTime then
						exists = true
						exit for
					end if
				next
				if exists then
					exit for
				end if
				accessed(curr, 0) = name
				accessed(curr, 1) = loginTime
				curr = curr + 1
				
				computer = ""
				if session.logonType = 0 or session.logonType = 2 then
					computer = "console#" & consoleCount
					consoleCount = consoleCount + 1
				elseif session.logonType = 3 or session.logonType = 10 then
					computer = "rdp#" & rdpCount
					rdpCount = rdpCount + 1
				end if				
				
				loginTime = covertTime(loginTime)
				WScript.Echo "|" & computer & "|" & name & "|" & loginTime & "|"
				exit for
			end if
		next
	end if
next

' 转到时间
function covertTime(text)	
	covertTime = mid(text, 1, 4) & "-" & mid(text, 5, 2) & "-" & mid(text, 7, 2) & " " & mid(text, 9, 2) & ":" & mid(text, 11, 2) & ":" & mid(text, 13, 2)
end function

' 截取一个文本指定两个字符串之间的文本
function between(text, left, right)
	leftPos = instr(text, left)
	if leftPos = 0 then
		between = ""
		exit function
	end if
	
	leftPos = leftPos + len(left)
	rightPos = instr(leftPos, text, right)
	if rightPos = 0 then
		between = mid(text, leftPos) 
		exit function
	end if 
	
	between = mid(text, leftPos, rightPos -  leftPos)
end function