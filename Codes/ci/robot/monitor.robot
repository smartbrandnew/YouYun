*** Settings ***
Library    Selenium2Library
Suite Teardown    Close All Browsers

*** Variables ***
${admin_username}    admin@uyun.cn
${admin_password}    Ant123456;
${ANT_Url}    http://10.1.100.110/ant/
${Monitor_Url}    http://10.1.100.110/monitor/#/sources/sourcesList
${浏览器}    chrome
${操作延迟}    180
${remote_user}    root
${remote_passwd}    ant123456
${remote_ip}    10.1.100.221
${local_user}    root
${local_passwd}    ant123456
${local_ip}    10.1.100.228


*** Keywords ***
登录采控平台
    Open Browser    ${ANT_Url}    ${浏览器}
    Set Window Size    ${2000}    ${2000}
    ${url} =    Get Location
    Run Keyword if     'login' in $url    登录ANT租户

登录ANT租户
    Wait Until Page Contains    登录
    Input Text    email    ${admin_username}
    Input Password    passwd    ${admin_password}
    Click Button     //button[contains(.,登录)]
    Wait Until Page Contains    采控平台
    Go To    ${ANT_Url}
    Wait Until Page Contains    AntServer

安装二级节点以及远程发现模块
    Wait Until Page Contains    安装Agent
    Click Element     //a[contains(.,"安装Agent")]
    Wait Until Page Contains    安装信息 1
    Input Text    //input[@placeholder="请输入登录账号"]    ${remote_user}
    Input Password    //input[@placeholder="请输入登录密码"]    ${remote_passwd}
    Input Text    //input[@placeholder="用户须具有对安装目录的读写权限"]    /opt
    Input Text    //input[@class="ant-select-search__field"]    ${remote_ip}
    Press Key    //input[@class="ant-select-search__field"]    \\13
    Click Element     //span[contains(.,"远程发现")]
    Click Button    //button[@class="uy-btn uy-btn-default"]

检查二级节点是否安装成功
    Go To    ${ANT_Url}
    Wait Until Page Contains    安装Agent
    Wait Until Page Contains    ${remote_ip}
    Input Text    //input[@placeholder="输入设备名称或者IP"]    ${remote_ip}
    Click Element    //a[contains(.,"查询")]
    Wait Until Page Contains    共 1 条
    Wait Until Page Contains    在线
    Click Element    //a[contains(.,"重置")]

安装三级节点以及本地监控模块
    Sleep    5s
    Wait Until Element Is Visible    //span[contains(.,"${remote_ip}")]
    Click Element    //span[contains(.,"${remote_ip}")]
    Sleep    2s
    Click Element     //a[contains(.,"安装Agent")]
    Wait Until Page Contains    安装信息 1
    Input Text    //input[@placeholder="请输入登录账号"]    ${local_user}
    Input Password    //input[@placeholder="请输入登录密码"]    ${local_passwd}
    Input Text    //input[@placeholder="用户须具有对安装目录的读写权限"]    /opt
    Input Text    //input[@class="ant-select-search__field"]    ${local_ip}
    Press Key    //input[@class="ant-select-search__field"]    \\13
    Click Element     //span[contains(.,"本地监控")]
    Click Button    //button[@class="uy-btn uy-btn-default"]


检查模块安装是否成功
    Sleep    5s
    Wait Until Element Is Visible    //span[contains(.,"${remote_ip}")]
    Click Element    //span[contains(.,"${remote_ip}")]
    Wait Until Page Contains    ${local_ip}
    Click Element    //a[contains(.,"${local_ip}")]
    Wait Until Element Is Visible    //tr[contains(.,"本地监控已启动")]


检查资源是否上线
    Go To    ${Monitor_Url}
    Wait Until Page Contains    资源库
    Wait Until Page Contains    ${local_ip}
    Click Element    //span[@class="hostMap-table-colum-th" and contains(.,"${local_ip}")]/../../../td[2]//a
    Wait Until Page Contains    在线
    sleep    5s

设置操作延迟
    Set Selenium Timeout    ${操作延迟}



*** Test Cases ***
安装agent与本地监控模块
    设置操作延迟
    登录采控平台
    安装二级节点以及远程发现模块
    检查二级节点是否安装成功
    安装三级节点以及本地监控模块
    检查模块安装是否成功
    检查资源是否上线


*** Test Cases ***
安装local_agent
    设置操作延迟
    登录采控平台
    安装三级节点以及本地监控模块
    检查模块安装是否成功
    检查资源是否上线


*** Test Cases ***
检查资源是否上线
    设置操作延迟
    登录采控平台
    检查资源是否上线