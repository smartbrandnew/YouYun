*** Settings ***
Resource    resource.robot



*** Test Cases ***

安装agent
    Install Agent    ['10.1.100.220']    root    ant123456
    Agent Install Info Should Be In Store    ["10.1.100.220"]
    Agent Install Directory Should Be Exists    ["10.1.100.220"]    root    ant123456    /home/ant/uyun-ant/agent
    Agent Process Shuold Be Exists On    ["10.1.100.220"]    root    ant123456

卸载agent
    Agent Should Have Been Uninstalled    ["10.1.100.220"]
    Agent Install Directory Should Be delete    ["10.1.100.220"]    root    ant123456    /home/ant/uyun-ant/agent
    Agent Process Shuold Not Be Exists On    ["10.1.100.220"]    root    ant123456
    Agent

更新agent
    Upload Assign Version Agent    ["10.1.100.220"]    R11     linxu-64
    Install agent    ["10.1.100.220"]    root    ant123456
    Agent Install Info Should Be In Store    ["10.1.100.220"]
    Agent Install Directory Should Be Exists    ["10.1.100.220"]    root    ant123456    /home/ant/uyun-ant/agent
    Agent Process Shuold Be Exists On    ["10.1.100.220"]    root    ant123456
    Upload Assign Version Agent    ["10.1.100.220"]    R12     linxu-64
    Upgrade Agent    ["10.1.100.220"]
    Agent Install Info Should Be In Store    ["10.1.100.220"]
    Agent Install Directory Should Be Exists    ["10.1.100.220"]    root    ant123456    /home/ant/uyun-ant/agent
    Agent Process Shuold Be Exists On    ["10.1.100.220"]    root    ant123456


    Agent Version Should Be Upgrade

