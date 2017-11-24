#!/bin/sh

SH_DIR=$(cd "$(dirname "$0")"; pwd)
WORK_DIR=${SH_DIR}/..
BASE_DIR=${WORK_DIR}/..

if [ -f "${BASE_DIR}/bin/set-env.sh" ]; then
	source ${BASE_DIR}/bin/set-env.sh
fi

APP_NAME=bat-report
MAIN_CLASS=uyun.bat.report.Startup
CLASSPATH=${JAVA_HOME}/lib/*:${BASE_DIR}/lib/*:${WORK_DIR}/lib/*

RUN_OPTS="-Dapp.name=$APP_NAME"
RUN_OPTS="$RUN_OPTS -Dwork.dir=$WORK_DIR"
RUN_OPTS="$RUN_OPTS -Dbase.dir=$BASE_DIR"
RUN_OPTS="$RUN_OPTS -Dlogs.dir=$BASE_DIR/logs"
RUN_OPTS="$RUN_OPTS -Dlogback.configurationFile=$WORK_DIR/conf/logback.xml"
RUN_OPTS="$RUN_OPTS -Ddubbo.registry.file=$BASE_DIR/temp/dubbo-registry-$APP_NAME.cache"
RUN_OPTS="$RUN_OPTS -Xmx1024m -XX:MaxMetaspaceSize=128m -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError"

#RUN_OPTS="$RUN_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -Xnoagent"

cd ${SH_DIR}
java -Dfile.encoding=UTF-8 ${RUN_OPTS} ${JAVA_OPTS} -cp ${CLASSPATH} ${MAIN_CLASS}
