#!/bin/sh

SH_DIR=$(cd "$(dirname "$0")"; pwd)
WORK_DIR=${SH_DIR}/..

if [ ! -n "${JAVA_OPTS}" ]; then
	JAVA_OPTS="-Dbat.developer.mode=false -Ddisconf.enable.remote.conf=true -Ddisconf.app=uyun -Ddisconf.env=local -Ddisconf.version=2_0_0 -Ddisconf.conf_server_host=10.1.240.88:8081"
fi
if [ ! -d "$WORK_DIR/logs" ]; then
	mkdir $WORK_DIR/logs 
fi
    
if [ ! -d "$WORK_DIR/temp" ]; then
	mkdir $WORK_DIR/temp 
fi
APP_NAME=bat-console
MAIN_CLASS=uyun.bat.console.env.Startup
CLASSPATH=${JAVA_HOME}/lib/*:${WORK_DIR}/lib/*

RUN_OPTS="-Dapp.name=$APP_NAME"
RUN_OPTS="$RUN_OPTS -Dwork.dir=$WORK_DIR"
RUN_OPTS="$RUN_OPTS -Dlogs.dir=$WORK_DIR/logs"
RUN_OPTS="$RUN_OPTS -Dlogback.configurationFile=$WORK_DIR/conf/logback.xml"
RUN_OPTS="$RUN_OPTS -Ddubbo.registry.file=$WORK_DIR/temp/dubbo-registry-$APP_NAME.cache"
RUN_OPTS="$RUN_OPTS -Xmx512m -XX:MaxMetaspaceSize=128m"

#RUN_OPTS="$RUN_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Xnoagent"

cd ${SH_DIR}
java ${RUN_OPTS} ${JAVA_OPTS} -cp ${CLASSPATH} ${MAIN_CLASS}
