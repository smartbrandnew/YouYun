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

echo -e "请输入探针编号(probe.code):\c"
while true
do 
read input
if checknull $input
then
probecode=$input
break
else
echo  "输入不能为空，请重新输入:"
continue
fi
done

echo -e "请输入探针名称:\c"
while true
do
read input
if checknull $input
then
probename=$input
break
else
echo  "输入不能为空，请重新输入:"
continue
fi
done

echo -e "请输入探针IP地址(probe.ipaddr):\c"
while true
do
read input
if checkip $input
then
probeip=$input
break
else
echo  "IP输入格式有误，请重新输入:"
continue
fi
done

echo -e "请输入探针端口(probe.webserver.port):\c"
while true
do
read input
if checkport $input
then
probeport=$input
break
else
echo  "端口输入格式有误，请重新输入:"
continue
fi
done

echo "********************************"
echo probe.code=$probecode
echo probe.name=$probename
echo probe.webserver.port=$probeport
echo probe.ipaddr=$probeip
echo "********************************"



echo -n "请确定配置信息，是否生效?(yes(y)|no(n)): "
read need
case $need in
yes|y)
chmod 777  ../conf/config.properties
sed -i "s/probe.code=[^$]*/probe.code=$probecode/g" ../conf/config.properties
sed -i "s/probe.name=[^$]*/probe.name=$probename/g" ../conf/config.properties
sed -i "s/probe.webserver.port=[^$]*/probe.webserver.port=$probeport/g" ../conf/config.properties
sed -i "s/probe.ipaddr=[^$]*/probe.ipaddr=$probeip/g" ../conf/config.properties
echo "探针配置成功！"
;;
no|n)  
exit 
;;
*)     
echo "输入有误！程序退出"
exit
;;
esac

echo "请输入探针服务名:"
read probeservice


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
