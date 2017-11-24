*** Settings ***
Resource    resource.robot

*** Test Cases ***
demo one
    demo    ${126_host}    ${126_username}    ${126_password}


安装nginx
    ${filepath} =    Build Package    nginx    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    nginx    1.8.1
    ${filepath} =    Install Omp Package    nginx    1.8.1
    Check If Exist File    ${filepath}
    Check If Exist Process    nginx

停止nginx
    STOP OMP PACKAGE     nginx
    check If Not Exist Process     nginx

START nginx
    start Omp Package    nginx
    Check If Exist Process    nginx

UNINSTALL nginx
    ${filepath} =    Uninstall Omp Package    nginx
    Check If Not Exist File    ${filepath}
    check if Not Exist Process     nginx

安装python
    ${filepath} =    Build Package    python    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    python    2.7.13
    ${filepath} =    Install Omp Package    python    2.7.13
    Check If Exist File    ${filepath}

卸载python
    ${filepath} =    Uninstall Omp Package    python
    Check If Not Exist File    ${filepath}

安装node
    ${filepath} =    Build Package    node    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    node    7.8.0
    ${filepath} =    Install Omp Package    node    7.8.0
    Check If Exist File    ${filepath}

卸载node
    ${filepath} =    Uninstall Omp Package    node
    Check If Not Exist File    ${filepath}


安装dispatcher
    ${filepath} =    Build Package    python    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    python    2.7.13
    ${filepath} =    Install Omp Package    python    2.7.13
    Check If Exist File    ${filepath}
    ${filepath} =    Build Package    dispatcher    master
    Check IF Exist File    ${filepath}
    ${filepath} =    Build Package    node    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    node    7.8.0
    ${filepath} =    Install Omp Package    node    7.8.0
    Check If Exist File    ${filepath}
    Upload Omp Package    dispatcher    V2.0.R12
    ${filepath} =    Install Omp Package    dispatcher    V2.0.R12
    Check If Exist File    ${filepath}
    CHeck If Exist Process    dispatcher

STOP dispatcher
    STOP OMP PACKAGE     dispatcher
    check If Not Exist Process     dispatcher

启动dispatcher
    start Omp Package    dispatcher
    Check If Exist Process    dispatcher

卸载dispatcher
    ${filepath} =    Uninstall Omp Package    python
    Check If Not Exist File    ${filepath}
    ${filepath} =    Uninstall Omp Package    node
    Check If Not Exist File    ${filepath}
    ${filepath} =    Uninstall Omp Package    dispatcher
    Check If Not Exist File    ${filepath}
    check if Not Exist Process     dispatcher

安装manager
    ${filepath} =    Build Package    manager    master
    Check IF Exist File    ${filepath}
    Upload Omp Package    manager    V2.0.R12
    ${filepath} =    Install Omp Package    manager    V2.0.R12
    Start Omp Package    manager
    Check If Exist File    ${filepath}
    CHeck If Exist Process    manager

STOP manager
    STOP OMP PACKAGE     manager
    check If Not Exist Process     manager

启动manager
    Start Omp Package    manager
    Check If Exist Process    manager

卸载manager
    ${filepath} =    Uninstall Omp Package    manager
    Check If Not Exist File    ${filepath}
    check if Not Exist Process     manager

creat dispatcher manager images
    Create Docker Images    R12
    check if docker images create sucess     platform/ant-dispatcher
    check if docker images create sucess     platform/ant-manager
    check if docker upload sucess    platform/ant-manager
    check if docker upload sucess    platform/ant-dispatcher

启动dispatcher容器

    拉取dispatcher镜像
    启动容器
    检查容器是否启动
    检查容器服务是否可用

启动manager容器
    拉取manager镜像
    启动容器
    检查容器是否启动
    检查容器服务是否可用



