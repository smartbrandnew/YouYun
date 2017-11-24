export LANG=zh_CN.UTF-8
#!/bin/sh
SH_HOME=$(cd "$(dirname "$0")"; pwd)
cd $SH_HOME

logfile="uyun-monitor-install.log"
# Set up a named pipe for logging
npipe=/tmp/$$.tmp
mknod $npipe p

# Log all output to a log for error checking
tee <$npipe $logfile &
exec 1>&-
exec 1>$npipe 2>&1
trap "rm -f $npipe" EXIT

function on_error() {
    printf "\033[31m$ERROR_MESSAGE
It looks like you hit an issue when trying to install the uyun-monitor.

please send an email to us.\n\033[0m\n"
	exit 1
}
trap on_error ERR

DISCONF_APP_UYUN=uyun
DISCONF_VERSION=2_0_0
DISCONF_ENV=local
DISCONF_PORT=8081
DISCONF_USER=admin
DISCONF_PASS=admin
DISCONF_UYUN_MONITOR_KEY=monitor.properties

usage() {
    echo "usage:"
    echo "    sh install.sh optstring parameters"
    echo "    sh install.sh [options] [--] optstring parameters"
    echo "    sh install.sh [options] -o|--options optstring [options] [--] parameters"
    echo ""
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

#jq授权
chmod 777 ./jq

# 获取集群disconf的可用host
DISCONF_IPS=($(echo $DISCONF_HOST|tr "," "\n"))
for DISCONF_IP in ${DISCONF_IPS[@]};do
    LOGIN_URL=http://$DISCONF_IP:$DISCONF_PORT/api/account/signin
    curl -b cookies -c cookies --connect-timeout 5 -sS $LOGIN_URL --data "name=$DISCONF_USER&password=$DISCONF_PASS&remember=0" | grep -v err_no > result
    loginResult=$(cat result | ./jq '.success')
        if [ "$loginResult" == "\"true\"" ]; then
            DISCONF_HOST=$DISCONF_IP
            break
        fi
done

# 解析result文件json是否返回true
checkResult() {
    loginResult=$(cat result | ./jq '.success')
    if [ "$loginResult" != "\"true\"" ]; then
        echo "$1"
        exit 1
    else
        echo "$2"
    fi
}

# 上传配置文件到disconf
uploadFile() {
    upload_url=$3
    if [ -n "$1" ]; then
        echo "Clear the disconf old configuration file"
        curl -b cookies -c cookies -X DELETE -sS "$FILE_DEL_URL/$1" | grep -v err_no &> /dev/null
    fi
    curl -b cookies -c cookies -sS -F "myfilerar=@$2" $upload_url | grep -v err_no &> result
    checkResult "$2 uploaded failed" "$2 uploaded successfully"
}

# 创建并获取租户app ID
getAppId() {
    app_name=$1
    app_id=$(cat result | ./jq ".page.result[] | select(.name == \"$app_name\") | .id")
    if [ -z "$app_id" ]; then
        curl -b cookies -c cookies -sS $APP_CREATE_URL --data "app=$app_name&desc=$app_name" | grep -v err_no > /dev/null
        curl -b cookies -c cookies -sS $APP_LIST_URL | grep -v err_no > result
        app_id=$(cat result | ./jq ".page.result[] | select(.name == \"$app_name\") | .id")
    fi
    echo $app_id
}

#修改企业版参数
sh -c "sed -i 's@bat.developer.mode=true@@' ./monitor.properties"

#----------------------------disconf start----------------------

DISCONF_SERVER_ADDR=http://$DISCONF_HOST:$DISCONF_PORT

#disconf api列表
LOGIN_URL=$DISCONF_SERVER_ADDR/api/account/signin
APP_CREATE_URL=$DISCONF_SERVER_ADDR/api/app
APP_LIST_URL=$DISCONF_SERVER_ADDR/api/app/list
ENV_LIST_URL=$DISCONF_SERVER_ADDR/api/env/list
FILE_DOWNLOAD_URL=$DISCONF_SERVER_ADDR/api/web/config/download/
FILE_UPLOAD_URL=$DISCONF_SERVER_ADDR/api/web/config/file
CONF_LIST_URL=$DISCONF_SERVER_ADDR/api/web/config/simple/list
FILE_DEL_URL=$DISCONF_SERVER_ADDR/api/web/config

#disconf登录
curl -b cookies -c cookies --connect-timeout 5 -sS $LOGIN_URL --data "name=$DISCONF_USER&password=$DISCONF_PASS&remember=0" | grep -v err_no > result
checkResult "Disconf login failed" "Disconf login successful"

# 获取app列表
curl -b cookies -c cookies -sS $APP_LIST_URL | grep -v err_no > result

# 获取uyun appId
UYUN_APP_ID=$(getAppId $DISCONF_APP_UYUN)

#获取envId
curl -b cookies -c cookies -sS $ENV_LIST_URL | grep -v err_no > result
ENV_ID=$(cat result | ./jq '.page.result[] | select(.name == "local") | .id')

# 获取common.properties configId配置项
curl -b cookies -c cookies -sS "$CONF_LIST_URL?appId=$UYUN_APP_ID&envId=$ENV_ID&version=$DISCONF_VERSION" | grep -v err_no > result
CONFIG_ID=$(cat result | ./jq '.page.result[] | select(.key == "common.properties") | .configId')
if [ -z "$CONFIG_ID" ]; then
    echo "配置中心Uyun App中不存在common.properties配置,请先进行配置"
    exit 1
fi

# 获取configId配置项
curl -b cookies -c cookies -sS "$CONF_LIST_URL?appId=$UYUN_APP_ID&envId=$ENV_ID&version=$DISCONF_VERSION" | grep -v err_no > result
MONITOR_CONFIG_ID=$(cat result | ./jq '.page.result[] | select(.key == "monitor.properties") | .configId')

if [ -n "$MONITOR_CONFIG_ID" ]; then
    echo "monitor.properties is exist."
else
	MONITOR_UPLOAD_URL="$FILE_UPLOAD_URL?appId=$UYUN_APP_ID&envId=$ENV_ID&version=$DISCONF_VERSION"
	
	uploadFile "$MONITOR_CONFIG_ID" "monitor.properties" "$MONITOR_UPLOAD_URL"
	
	echo "monitor.properties uploaded."
fi
#---------------------------disconf end---------------------

sh initdb.sh --disconf-host $DISCONF_HOST --disconf-port $DISCONF_PORT --disconf-user $DISCONF_USER --disconf-passwd $DISCONF_PASS




