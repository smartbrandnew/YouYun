export LANG=zh_CN.UTF-8
#!/bin/sh

#设置环境变量
if [ ! -n "${JAVA_OPTS}" ]; then
	JAVA_OPTS="-Dbat.developer.mode=false -Ddisconf.enable.remote.conf=true -Ddisconf.app=${DISCONF_APP}  -Ddisconf.env=${DISCONF_ENV} -Ddisconf.version=${DISCONF_VERSION} -Ddisconf.conf_server_host=${DISCONF_HOST}"
    export JAVA_OPTS
fi
