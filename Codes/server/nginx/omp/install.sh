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

INSTALL_DIR=/opt/uyun/monitor
mkdir -p ${INSTALL_DIR}
\cp -r nginx/ ${INSTALL_DIR}
chmod +x ${INSTALL_DIR}/nginx/sbin/nginx
chmod +x ${INSTALL_DIR}/nginx/bin/bat-nginx.sh

\cp -f monitor-nginx.service /usr/lib/systemd/system
systemctl enable monitor-nginx.service
systemctl daemon-reload

