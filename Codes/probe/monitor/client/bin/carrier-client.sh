#文件运行时还会有一些控制台报错，后续要处理。
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
vmConf="$vmConf -Dlogback.configurationFile=$BASE_HOME/conf/logback.xml"
vmConf="$vmConf -Dnet.sf.ehcache.skipUpdateCheck=true"
vmConf="$vmConf -Dautosync.app.mode=gui"
vmConf="$vmConf -Dautosync.listener=com.broada.module.autosync.client.api.GuiSyncEventListener"

# classpath补丁设置
vmConf="$vmConf -Djava.system.class.loader=com.broada.module.autosync.client.api.startup.PatchableClassLoader"
vmConf="$vmConf -Djava.class.path=$BASE_HOME/webapp/WEB-INF/lib/platform.module.autosync.client.startup-2.jar"
vmConf="$vmConf -Dpatch.dir=$BASE_HOME/patch"
vmConf="$vmConf -Dpatch.patterns=carrier.*;custom.*"

# JVM参数设置
vmConf="-Xmx900m $vmConf "
vmConf="-XX:MaxPermSize=256m $vmConf "

for i in $BASE_HOME/lib/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done
for i in $BASE_HOME/plugins/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done
java $vmConf -cp $CLASSPATH com.broada.module.autosync.client.api.startup.Startup com.broada.carrier.monitor.client.impl.Startup