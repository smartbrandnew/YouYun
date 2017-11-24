export LANG=zh_CN.UTF-8
#!/bin/bash
# Monitor Agent install script.
set -e
logfile="monitor-agent-install.log"
gist_request=/tmp/agent-gist-request.tmp
gist_response=/tmp/agent-gist-response.tmp

# apikey
if [ -n "$M_API_KEY" ]; then
    apikey=$M_API_KEY
else
	printf "API key not available in M_API_KEY environment variable."
    exit 1;
fi

# 从哪里下载安装包
if [ -n "$REPERTORY_URL" ]; then
    repertory_url=$REPERTORY_URL
else
	repertory_url="http://monitor.uyun.cn"
fi

# 将agent的数据往哪发送
if [ -n "$M_URL" ]; then
    m_url=$M_URL
else
	m_url=$repertory_url/api/v2/gateway/dd-agent
fi

if [ $(command -v curl) ]; then
    dl_cmd="curl -f"
else
    dl_cmd="wget --quiet"
fi

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
It looks like you hit an issue when trying to install the Agent.

maybe you must set hostname in /etc/monitor-agent/datamonitor.conf.

If you're still having problems, please send an email to us.\n\033[0m\n"
}

trap on_error ERR

if [ -n "$HOSTNAME" ]; then
    m_hostname=$HOSTNAME
fi

if [ -n "$NTP" ]; then
    ntp=$NTP
fi
if [ -n "$DD_INSTALL_ONLY" ]; then
    no_start=true
else
    no_start=false
fi

# OS/Distro Detection
# Try lsb_release, fallback with /etc/issue then uname command
KNOWN_DISTRIBUTION="(Debian|Ubuntu|RedHat|CentOS|openSUSE|Amazon|Arista|SUSE)"
DISTRIBUTION=$(lsb_release -d 2>/dev/null | grep -Eo $KNOWN_DISTRIBUTION  || grep -Eo $KNOWN_DISTRIBUTION /etc/issue 2>/dev/null || grep -Eo $KNOWN_DISTRIBUTION /etc/Eos-release 2>/dev/null || uname -s)

if [ $DISTRIBUTION = "Darwin" ]; then
    printf "\033[31mThis script does not support installing on the Mac.

Will be available soon"
    exit 1;

elif [ -f /etc/debian_version -o "$DISTRIBUTION" == "Debian" -o "$DISTRIBUTION" == "Ubuntu" ]; then
    OS="Debian"
elif [ -f /etc/redhat-release -o "$DISTRIBUTION" == "RedHat" -o "$DISTRIBUTION" == "CentOS" -o "$DISTRIBUTION" == "openSUSE" -o "$DISTRIBUTION" == "Amazon" ]; then
    OS="RedHat"
    os_version=`cat /etc/redhat-release | awk '{print $(NF-1)}' | awk -F . '{print $1}'`
# Some newer distros like Amazon may not have a redhat-release file
elif [ -f /etc/system-release -o "$DISTRIBUTION" == "Amazon" ]; then
    OS="RedHat"
# Arista is based off of Fedora14/18 but do not have /etc/redhat-release
elif [ -f /etc/Eos-release -o "$DISTRIBUTION" == "Arista" ]; then
    OS="RedHat"
# openSUSE and SUSE use /etc/SuSE-release
elif [ -f /etc/SuSE-release -o "$DISTRIBUTION" == "SUSE" -o "$DISTRIBUTION" == "openSUSE" ]; then
    OS="SUSE"
    os_version=`cat /etc/SuSE-release | awk '{print $(NF-1)}' | awk -F . '{print $1}'`
fi

# Root user detection
if [ $(echo "$UID") = "0" ]; then
    sudo_cmd=''
else
    sudo_cmd='sudo'
fi

DDBASE=false
# Python Detection
has_python=$(which python || echo "no")
if [ "$has_python" != "no" ]; then
    PY_VERSION=$(python -c 'import sys; print("{0}.{1}".format(sys.version_info[0], sys.version_info[1]))' 2>/dev/null || echo "TOO OLD")
    if [ "$PY_VERSION" = "TOO OLD" ]; then
        DDBASE=true
    fi
fi

UNAME_M=$(uname -m)
if [ "$UNAME_M"  == "i686" -o "$UNAME_M"  == "i386" -o "$UNAME_M"  == "x86" ]; then
	ARCHI="i386"
else
	ARCHI="x86_64"
fi

# Install the necessary package sources
if [ $OS = "RedHat" ]; then
    echo -e "\033[34m* Installing Server sources for Monitor agent\n\033[0m\n"

    UNAME_M=$(uname -m)
    if [ "$UNAME_M"  == "i686" -o "$UNAME_M"  == "i386" -o "$UNAME_M"  == "x86" ]; then
        ARCHI="i386"
    else
        ARCHI="x86_64"
    fi

    printf "\033[34m* Installing the Monitor Agent package\n\033[0m\n"

    if $DDBASE; then
        DD_BASE_INSTALLED=$(yum list installed datadog-agent-base > /dev/null 2>&1 || echo "no")
        if [ "$DD_BASE_INSTALLED" != "no" ]; then
            echo -e "\033[34m\n* Uninstall datadog-agent-base\n\033[0m"
            $sudo_cmd yum -y remove datadog-agent-base
        fi
    fi

    # 下载安装包
	if [ $os_version = '5' ]; then
		$dl_cmd $repertory_url/downloads/agent/$OS/Monitor-Agent-rpm-$ARCHI-$os_version.tar.gz > monitor.tar.gz
	else
		$dl_cmd $repertory_url/downloads/agent/$OS/Monitor-Agent-rpm-$ARCHI.tar.gz > monitor.tar.gz
	fi

    # 解压缩
    $sudo_cmd tar zxf monitor.tar.gz -C .
    # 部署
    if [ $ARCHI == "i386" ]; then
        cd centos32/
    else
    	if [ $os_version = '5' ]; then
    		cd centos${os_version}_64/
        else
        	cd centos64/
        fi
    fi

	$sudo_cmd chmod 777 deploy.sh
    $sudo_cmd sh deploy.sh
    # 删除
	cd ../
    $sudo_cmd rm -rf monitor
    $sudo_cmd rm -rf monitor.tar.gz
elif [ $OS = "Debian" ]; then
   # 下载安装包
    $dl_cmd $repertory_url/downloads/agent/$OS/Monitor-Agent-deb-$ARCHI.tar.gz
	$sudo_cmd mv Monitor-Agent-deb-$ARCHI.tar.gz monitor.tar.gz

    # 解压缩
    $sudo_cmd tar zxf monitor.tar.gz -C .
    # 部署
    if [ $ARCHI == "i386" ]; then
        cd debian32/
    else
        cd debian64/
    fi
	$sudo_cmd chmod 777 deploy.sh
    $sudo_cmd sh deploy.sh
    # 删除
	cd ../
    $sudo_cmd rm -rf monitor
    $sudo_cmd rm -rf monitor.tar.gz
elif [ $OS = "SUSE" ]; then

    UNAME_M=$(uname -m)
    if [ "$UNAME_M"  == "i686" -o "$UNAME_M"  == "i386" -o "$UNAME_M"  == "x86" ]; then
        ARCHI="i386"
    else
        ARCHI="x86_64"
    fi

    if [ "$UNAME_M"  == "i686" -o "$UNAME_M"  == "i386" -o "$UNAME_M"  == "x86" ]; then
        printf "\033[31mThe Datadog Agent installer is only available for 64 bit SUSE Enterprise machines.\033[0m\n"
        exit;
    fi

 	# 下载安装包
	if [ $os_version = '10' ]; then
		$dl_cmd $repertory_url/downloads/agent/$OS/Monitor-Agent-suse-$ARCHI-$os_version.tar.gz > monitor.tar.gz
	else
		$dl_cmd $repertory_url/downloads/agent/$OS/Monitor-Agent-suse-$ARCHI.tar.gz > monitor.tar.gz
	fi

    # 解压缩
    $sudo_cmd tar zxf monitor.tar.gz -C .
    # 部署
    
    if [ $os_version = '10' ]; then
    	cd suse${os_version}_64/
    else
        cd suse64/
    fi
        
	$sudo_cmd chmod 777 deploy.sh
    $sudo_cmd sh deploy.sh
    # 删除
	cd ../
    $sudo_cmd rm -rf monitor
    $sudo_cmd rm -rf monitor.tar.gz
else
    printf "\033[31mYour OS or distribution are not supported by this install script.\033[0m\n"
    exit;
fi

# Set the configuration
    printf "\033[34m\n* Adding your API key to the Agent configuration: /etc/monitor-agent/datamonitor.conf\n\033[0m\n"
    $sudo_cmd sh -c "sed 's/api_key:.*/api_key: $apikey/' /etc/monitor-agent/datamonitor.conf.example > /etc/monitor-agent/datamonitor.conf"
    $sudo_cmd sh -c "sed -i 's%m_url: .*%m_url: $m_url%' /etc/monitor-agent/datamonitor.conf"
    if [ -n "$m_hostname" ]; then
        printf "\033[34m\n* Adding your HOSTNAME to the Agent configuration: /etc/monitor-agent/datamonitor.conf\n\033[0m\n"
        $sudo_cmd sh -c "sed -i 's/#hostname:.*/hostname: $m_hostname/' /etc/monitor-agent/datamonitor.conf"
    fi
	if [ -n "$TAGS" ]; then
		$sudo_cmd sh -c "sed -i 's/#tags: .*/tags: $TAGS/' /etc/monitor-agent/datamonitor.conf"
	fi
    if [ -n "$NTP" ]; then
		$sudo_cmd sh -c "sed -i 's/host:.*/host: $ntp/' /etc/monitor-agent/conf.d/ntp.yaml.default"
	fi
    $sudo_cmd chown m-agent:root /etc/monitor-agent/datamonitor.conf
    $sudo_cmd chmod 640 /etc/monitor-agent/datamonitor.conf

restart_cmd="$sudo_cmd /etc/init.d/datamonitor-agent restart"
if command -v invoke-rc.d >/dev/null 2>&1; then
    restart_cmd="$sudo_cmd invoke-rc.d datamonitor-agent restart"
fi

if $no_start; then
    printf "\033[34m
* INSTALL_ONLY environment variable set: the newly installed version of the agent
will not start by itself. You will have to do it manually using the following
command:

    $restart_cmd

\033[0m\n"
    exit
fi

printf "\033[34m* Starting the Agent...\n\033[0m\n"
eval $restart_cmd

printf "\033[32m

Your Agent is running and functioning properly. It will continue to run in the
background and submit metrics to monitor.

If you ever want to stop the Agent, run:

    sudo /etc/init.d/datamonitor-agent stop

And to run it again run:

    sudo /etc/init.d/datamonitor-agent start

\033[0m"
