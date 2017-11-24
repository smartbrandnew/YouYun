export LANG=zh_CN.UTF-8
#!/bin/bash
# Monitor Agent install script.
set -e
logfile="monitor-install.log"

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
	repertory_url="https://monitor.uyun.cn"
fi

# 将agent的数据往哪发送
if [ -n "$M_URL" ]; then
    dd_url=$M_URL
else
	dd_url=$repertory_url/api/v2/gateway/dd-agent
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

maybe you must set hostname in /etc/dd-agent/datadog.conf.

If you're still having problems, please send an email to us.\n\033[0m\n"
}
trap on_error ERR

if [ -n "$M_INSTALL_ONLY" ]; then
    no_start=true
else
    no_start=false
fi

# Root user detection
if [ $(echo "$UID") = "0" ]; then
    sudo_cmd=''
else
    sudo_cmd='sudo'
fi

printf "\033[34m* Installing the Monitor Agent docker package\n\033[0m\n"
$dl_cmd $repertory_url/downloads/agent/Docker/docker-dd-agent.tar.gz > docker-dd-agent.tar.gz
$sudo_cmd docker load < docker-dd-agent.tar.gz
$sudo_cmd rm -rf docker-dd-agent.tar.gz

# OS/Distro Detection
# Try lsb_release, fallback with /etc/issue then uname command
KNOWN_DISTRIBUTION="(Debian|Ubuntu|RedHat|CentOS|openSUSE|Amazon|Arista)"
DISTRIBUTION=$(lsb_release -d 2>/dev/null | grep -Eo $KNOWN_DISTRIBUTION  || grep -Eo $KNOWN_DISTRIBUTION /etc/issue 2>/dev/null || grep -Eo $KNOWN_DISTRIBUTION /etc/Eos-release 2>/dev/null || uname -s)

if [ -f /etc/redhat-release -o "$DISTRIBUTION" == "Amazon" ]; then
	OS="Amazon"
# Some newer distros like Amazon may not have a redhat-release file
elif [ -f /etc/system-release -o "$DISTRIBUTION" == "Amazon" ]; then
	OS="Amazon"
else
	OS="isNotAmazon"
fi

if [ "$OS" == "Amazon" ];then
	if [ -n "$HOSTNAME" ]; then
	    start_cmd="$sudo_cmd docker run -d --name monitor-agent -h $HOSTNAME -v /var/run/docker.sock:/var/run/docker.sock -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -e API_KEY=$apikey -e DD_URL=$dd_url -e TAGS=$TAGS docker-dd-agent"
	else
		start_cmd="$sudo_cmd docker run -d --name monitor-agent -h `hostname` -v /var/run/docker.sock:/var/run/docker.sock -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -e API_KEY=$apikey -e DD_URL=$dd_url -e TAGS=$TAGS docker-dd-agent"
	fi
else
	if [ -n "$HOSTNAME" ]; then
	    start_cmd="$sudo_cmd docker run -d --name monitor-agent -h $HOSTNAME -v /var/run/docker.sock:/var/run/docker.sock -v /proc/:/host/proc/:ro -v /cgroup/:/host/sys/fs/cgroup:ro -e API_KEY=$apikey -e DD_URL=$dd_url -e TAGS=$TAGS docker-dd-agent"
	else
		start_cmd="$sudo_cmd docker run -d --name monitor-agent -h `hostname` -v /var/run/docker.sock:/var/run/docker.sock -v /proc/:/host/proc/:ro -v /cgroup/:/host/sys/fs/cgroup:ro -e API_KEY=$apikey -e DD_URL=$dd_url -e TAGS=$TAGS docker-dd-agent"
	fi
fi

if $no_start; then
    printf "\033[34m
* M_INSTALL_ONLY environment variable set: the newly installed version of the agent
will not start by itself. You will have to do it manually using the following
command:

    $start_cmd

\033[0m\n"
    exit
fi

num=`$sudo_cmd docker ps -a | grep monitor-agent | awk '{print $1}' | wc -l`

if [ $num -gt 0 ]; then
	printf "\033[34m* Stoping Docker Agent Container...\n\033[0m\n"
	$sudo_cmd docker stop `docker ps -a | grep monitor-agent | awk '{print $1}'`
	printf "\033[34m* Removing Docker Agent Container...\n\033[0m\n"
	$sudo_cmd docker rm `docker ps -a | grep monitor-agent | awk '{print $1}'`
fi

printf "\033[34m* Starting the Agent...
command:

    $start_cmd

\033[0m\n"

eval $start_cmd

printf "\033[32m

Your Agent is running and functioning properly. It will continue to run in the
background and submit metrics to monitor.

\033[0m"
