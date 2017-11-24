#!/bin/sh
BASE_HOME=`pwd`

probecode="127.0.0.1"
probename="127.0.0.1"
probeip="127.0.0.1"
probeport="9145"
serverip="127.0.0.1"
serverport="8890"


function checkip(){
if [[ $1 =~ ^([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\.([0-9]{1,2}|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$ ]]
then
return 0
else
return 1
fi
}

function checkport(){
if [[ $1 =~ ^[0-9]*[1-9][0-9]*$ && $1 -le 65535 ]]
then
return 0
else
return 1
fi
}

function checknull(){
if [[ -n "$1" ]]
then
return 0
else
return 1
fi
}


if [ -n "$ANT_PROBE_CODE" ]; then
    probecode=$ANT_PROBE_CODE
else
    printf "ANT_PROBE_CODE not available in ANT_PROBE_CODE environment variable."
    exit 1;
fi

if [ -n "$ANT_PROBE_NAME" ]; then
    probename=$ANT_PROBE_NAME
else
    printf "ANT_PROBE_NAME not available in ANT_PROBE_NAME environment variable."
    exit 1;
fi

if [ -n "$ANT_PROBE_IP" ]; then
    probeip=$ANT_PROBE_IP
else
    printf "ANT_PROBE_IP not available in ANT_PROBE_IP environment variable."
    exit 1;
fi

if [ -n "$ANT_PROBE_PORT" ]; then
    probeport=$ANT_PROBE_PORT
else
    printf "ANT_PROBE_PORT not available in ANT_PROBE_PORT environment variable."
    exit 1;
fi

if [ -n "$ANT_PROBE_SERVICE" ]; then
    probeservice=$ANT_PROBE_SERVICE
else
    printf "ANT_PROBE_SERVICE not available in ANT_PROBE_SERVICE environment variable."
    exit 1;
fi


echo "********************************"
echo probe.code=$probecode
echo probe.name=$probename
echo probe.webserver.port=$probeport
echo probe.ipaddr=$probeip
echo "********************************"



sed -i "s/probe.code=[^$]*/probe.code=$probecode/g" ../conf/config.properties
sed -i "s/probe.name=[^$]*/probe.name=$probename/g" ../conf/config.properties
sed -i "s/probe.webserver.port=[^$]*/probe.webserver.port=$probeport/g" ../conf/config.properties
sed -i "s/probe.ipaddr=[^$]*/probe.ipaddr=$probeip/g" ../conf/config.properties


sed -i "s/APP_NAME=[^$]*/APP_NAME=monitor-agentless-$probeport/g" auto-restart-monitoragentless.sh
sed -i "s/APP_NAME=[^$]*/APP_NAME=monitor-agentless-$probeport/g" stop.sh
sed -i "s/APP_NAME=[^$]*/APP_NAME=monitor-agentless-$probeport/g" monitor-agentless.sh
sed -i "s/APP_NAME=[^$]*/APP_NAME=monitor-agentless-$probeport/g" uninstall.sh
sed -i "s/APP_NAME=[^$]*/APP_NAME=monitor-agentless-$probeport/g" .probe

echo "设置任何用户都有x权限...."
find -name '*.sh' | xargs chmod +x
chmod +x ../jre/bin/java
chmod +x ../jre/jre/bin/java
echo "授权成功"

echo "注册系统服务[monitor-agentless]...."
PWD=`pwd`
USER=`whoami`
PWD=${PWD//\//\\\/}
REG_USER="s/INSTALLER=.*/INSTALLER=$USER/g";
REG_PATH="s/EXEC_PATH=.*/EXEC_PATH=$PWD/g";
sed -i $REG_USER .probe
sed -i $REG_PATH .probe
su root -c "cp .probe /etc/init.d/$probeservice && /sbin/chkconfig  --add $probeservice && chmod +x /etc/init.d/$probeservice"

sed -i 's/\r$//' *.sh
sed -i 's/\r$//' /etc/init.d/$probeservice

/sbin/service $probeservice stop
echo "系统服务注册成功"

Base=`pwd`
cd ../conf/ipmitool-linux/src/
chmod +x *

echo "系统初始化配置完成"

/sbin/service $probeservice start