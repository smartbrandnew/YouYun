#!/bin/bash

CURRENT_DIR=$(cd "$(dirname "$0")"; pwd)
cd $CURRENT_DIR

usage() {
    echo "usage:"
    echo "    sh install.sh optstring parameters"
    echo "    sh install.sh [options] [--] optstring parameters"
    echo "    sh install.sh [options] -o|--options optstring [options] [--] parameters"
    echo " "
    echo "Options:"
    echo "    --disconf-host                     disconf ip list, e.g: 10.1.241.2"
    echo "    --disconf-port                     disconf port, default: 8081"
    echo "    --disconf-user                     disconf user, default: admin"
    echo "    --disconf-passwd                   disconf passwd, default: admin"
    echo "    -h, --help                         help"
}

ARGS=`getopt -o h:: --long disconf-host:,disconf-port:,disconf-user:,disconf-passwd:,local-ip:,remote-ips:,install-role:,help:: -n 'install.sh' -- "$@"`

if [ $? != 0 ]; then
	usage
	exit 1
fi

# note the quotes around `$ARGS': they are essential!
#set 会重新排列参数的顺序，也就是改变$1,$2...$n的值，这些值在getopt中重新排列过了
eval set -- "$ARGS"

#经过getopt的处理，下面处理具体选项。
while true; do
	case "$1" in
		--disconf-host)
			DISCONF_HOST=$2
			shift 2
			;;
		--disconf-port)
			DISCONF_PORT=$2
			shift 2
			;;
		--disconf-user)
			DISCONF_USER=$2
			shift 2
			;;
		--disconf-passwd)
			DISCONF_PASS=$2
			shift 2
			;;
        --local-ip)
            LOCAL_IP=$2
            shift 2
            ;;
        --remote-ips)
            REMOTE_IPS=$2
            shift 2
            ;;
        --install-role)
            INSTALL_ROLE=$2
            shift 2
            ;;
		-h|--help)
			usage
			exit 1
			;;
		--)
			break
			;;
		*)
			echo "Invalid parameter";
			exit 1
			;;
	esac
done

mkdir -p /opt/uyun/monitor
cd console/bin
find . -name "*.sh" | xargs chmod +x
JAVA_OPTS=`sh -c "echo '\"-Dbat.developer.mode=false -Ddisconf.enable.remote.conf=true -Ddisconf.app=uyun -Ddisconf.env=local -Ddisconf.version=2_0_0 -Ddisconf.conf_server_host=$DISCONF_HOST:$DISCONF_PORT\"'"`
sh -c "sed -i 's@JAVA_OPTS=.*@JAVA_OPTS=$JAVA_OPTS@' console.sh"
sh console.sh > /opt/uyun/monitor/temp.log

has_err=$(cat /opt/uyun/monitor/temp.log | tail -n +7 | grep -E "WARN|ERROR" || echo "no")
if [ "$has_err" != "no" ]; then
	echo "maybe init database error.please check the log file:/opt/uyun/monitor/temp.log"
	exit 1
else
	echo "dbinit complete."
fi



