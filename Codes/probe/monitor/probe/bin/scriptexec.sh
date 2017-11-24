BASE_HOME=`pwd | awk '{print substr($1,1,length($1)-4)}'`
vmConf=""

vmConf="$vmConf -Duser.dir=$BASE_HOME "
vmConf="$vmConf -Dnet.sf.ehcache.skipUpdateCheck=true "
vmConf="$vmConf -Dlogback.configurationFile=$BASE_HOME/conf/logback-scriptexec.xml "
vmConf="$vmConf -Djava.library.path=$BASE_HOME/bin "
vmConf="$vmConf -Djava.io.tmpdir=$BASE_HOME/temp "


vmConf="$vmConf -Djava.system.class.loader=com.broada.module.autosync.client.api.startup.PatchableClassLoader "
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

java -cp $CLASSPATH com.broada.numen.agent.script.impl.Executer $*
