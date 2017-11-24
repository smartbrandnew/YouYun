#!/bin/sh
BASE_HOME=`pwd | awk '{print substr($1,1,length($1)-4)}'`
JAVA_HOME=$BASE_HOME/jre
#暂时不自带linux jre
PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=""
vmConf=""
# 如果需要开启JVM远程调试功能，将以下行的注释删除即可
# vmConf="$vmConf -Xdebug -Xrunjdwp:transport=dt_socket,address=9142,server=y,suspend=n"
vmConf="$vmConf -Duser.dir=$BASE_HOME"
vmConf="$vmConf -Dmonitor.logs.dir=$BASE_HOME/logs"
vmConf="$vmConf -Dlogback.configurationFile=$BASE_HOME/conf/impexp-logback.xml"
vmConf="$vmConf -Dnet.sf.ehcache.skipUpdateCheck=true "
# JVM参数设置
vmConf="-Xmx900m $vmConf "
vmConf="-XX:MaxPermSize=256m $vmConf "
for i in $BASE_HOME/lib/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done
for i in $BASE_HOME/patch/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done
for i in $BASE_HOME/plugins/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done

java $vmConf -cp $CLASSPATH com.broada.carrier.monitor.client.impl.impexp.CmdLine $*