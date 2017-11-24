APP_NAME=monitor-agentless-9166
BASE_HOME=`pwd | awk '{print substr($1,1,length($1)-4)}'`
vmConf=""
vmConf="$vmConf -Dapp.name=$APP_NAME "
vmConf="$vmConf -Duser.dir=$BASE_HOME "
vmConf="$vmConf -Dmonitor.logs.dir=$BASE_HOME/logs "
vmConf="$vmConf -Dmonitor.db.config=$BASE_HOME/conf/jdbc.properties"
vmConf="$vmConf -Dlogback.configurationFile=$BASE_HOME/conf/logback.xml"
vmConf="$vmConf -Dnet.sf.ehcache.skipUpdateCheck=true "
vmConf="$vmConf -Djava.library.path=$BASE_HOME/bin "
vmConf="$vmConf -Djava.io.tmpdir=$BASE_HOME/temp "
vmConf="$vmConf -Dcid.action.library.dir=$BASE_HOME/scripts "
vmConf="$vmConf -Dcid.action.worker.subproc=false "


vmConf="$vmConf -Djava.system.class.loader=com.broada.module.autosync.client.api.startup.PatchableClassLoader "
vmConf="$vmConf -Djava.class.path=${SELF_HOME}/webapp/WEB-INF/lib/platform.module.autosync.client.startup-2.jar "
vmConf="$vmConf -Dpatch.dir=${SELF_HOME}/patch "
vmConf="$vmConf -Dpatch.patterns=carrier.*;custom.* "


vmConf="-Xmx900m $vmConf "
vmConf="-XX:MaxPermSize=256m $vmConf "

JAVA_HOME=$BASE_HOME/jre
PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=""
for i in $BASE_HOME/plugins/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done
for i in $BASE_HOME/webapp/WEB-INF/lib/*.jar
do
     CLASSPATH=$i:"$CLASSPATH"
done

chmod +x ${JAVA_HOME}/bin/java
${JAVA_HOME}/bin/java -Duser.timezone=GMT+08 $vmConf -cp "$CLASSPATH" com.broada.carrier.monitor.probe.impl.tomcat.TomcatStartup
