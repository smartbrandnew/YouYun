*** Settings ***
Resource    resource.robot

*** Test Cases ***
构建nginx安装包
    ${filepath} =    Build Package    nginx    master
    Check IF Exist File    ${filepath}

构建node安装包 
    ${filepath} =    Build Package    node    master
    Check IF Exist File    ${filepath}

构建python安装包
    ${filepath} =    Build Package    python   master
    Check IF Exist File    ${filepath}

构建dispatcher安装包
    ${filepath} =    Build Package    dispatcher   master
    Check IF Exist File    ${filepath}

构建agent-linux64安装包 
    ${filepath} =    Build Package    agent-linux64    master
    Check IF Exist File    ${filepath}

构建agent-aix64安装包 
    ${filepath} =    Build Package    agent-aix64    master
    Check IF Exist File    ${filepath}

构建agent-suse10_64安装包
    ${filepath} =    Build Package    agent-suse10_64    master
    Check IF Exist File    ${filepath}

构建remote-discovery安装包
    ${filepath} =    Build Package    remote-discovery    master
    Check IF Exist File    ${filepath}

构建remote-monitor安装包
    ${filepath} =    Build Package    remote-monitor    master
    Check IF Exist File    ${filepath}

构建local-monitor安装包
    ${filepath} =    Build Package    local-monitor    master
    Check IF Exist File    ${filepath}


构建remote-automation安装包
    ${filepath} =    Build Package    remote-automation    master
    Check IF Exist File    ${filepath}

构建local-automation安装包
    ${filepath} =    Build Package    local-automation    master
    Check IF Exist File    ${filepath}

构建remote-network安装包
    ${filepath} =    Build Package    remote-network    master
    Check IF Exist File    ${filepath}

