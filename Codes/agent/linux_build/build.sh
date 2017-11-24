#!/bin/bash
# Datadog Agent install script.
export LC_ALL=C
export LANG=zh_CN.UTF-8

LINUX_DISTRIBUTION=$(grep -Eo "(Debian|Ubuntu|RedHat|CentOS|openSUSE|SUSE)" /etc/issue)
sys=`uname -a|grep x86_64`

######################### Root user detection
if [ $(echo "$UID") = "0" ]; then
    sudo_cmd=''
elif [ $LINUX_DISTRIBUTION = "Ubuntu" ]; then
    sudo_cmd='sudo'
elif [ $LINUX_DISTRIBUTION = "Debian" ]; then
    sudo_cmd='sudo'
elif [ $LINUX_DISTRIBUTION = "openSUSE" ]; then
    sudo_cmd='sudo'
elif [ $LINUX_DISTRIBUTION = "SUSE" ]; then
    sudo_cmd='sudo'
else
    sudo_cmd='su'
fi

######################### gen /etc/dd-agent/
cd ../
$sudo_cmd cp datamonitor.conf datamonitor.conf.example
$sudo_cmd cp -r conf.d linux_build/opt/datadog-agent/
$sudo_cmd cp -r datamonitor.conf datamonitor.conf.example supervisor.conf linux_build/opt/datadog-agent/conf

######################### gen /opt/datadog-agent/
$sudo_cmd cp -r version-manifest.json version-manifest.txt  linux_build/opt/datadog-agent/
$sudo_cmd cp -r LICENSE  linux_build/opt/datadog-agent/embedded/
mkdir linux_build/opt/datadog-agent/agent/
$sudo_cmd cp -r checks checks.d scripts monitorstream resources uyun utils  linux_build/opt/datadog-agent/agent/
$sudo_cmd cp gohai.py uyunuuid.py script_monitor_process.py agent.py monitorstatsd.py aggregator.py datamonitor-cert.pem graphite.py jmxfetch.py modules.py setup.py updater_process.py config.py daemon.py monitoragent.py emitter.py transaction.py util.py net_collector_process.py  linux_build/opt/datadog-agent/agent/

cd linux_build/opt/datadog-agent/agent
$sudo_cmd chmod 755 agent.py monitoragent.py monitorstatsd.py updater_process.py

cd ../bin/
$sudo_cmd chmod 755 *

cd ../../../
mkdir centos32 centos64 centos5_64 debian32 debian64 suse64 suse10_64
mkdir turbo64

########################## gen 32 bit centos
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/lib32.tar.gz
$sudo_cmd unzip /opt/agent_build_rely/centos32_bin.zip
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ centos32
########################## gen 32 bit debian
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/lib32.tar.gz
$sudo_cmd unzip /opt/agent_build_rely/debian32_bin.zip
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ debian32
########################## gen 64 bit centos
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/lib64.tar.gz
$sudo_cmd unzip /opt/agent_build_rely/centos64_bin.zip
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ centos64
########################## gen 64 bit centos5
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/centos5_lib.tar.gz
$sudo_cmd unzip /opt/agent_build_rely/centos5_bin.zip
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ centos5_64
########################## gen 64 bit debian
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/lib64.tar.gz
$sudo_cmd unzip /opt/agent_build_rely/debian64_bin.zip
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ debian64
########################## gen 64 bit suse
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/suse64_lib.tar.gz
$sudo_cmd tar -zxvf /opt/agent_build_rely/suse64_bin.tar.gz
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ suse64
########################## gen 64 bit suse10_64
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/suse10_lib.tar.gz
$sudo_cmd tar -zxvf /opt/agent_build_rely/suse10_bin.tar.gz
$sudo_cmd cp -r lib/ opt/datadog-agent/embedded/
$sudo_cmd cp -r bin/ opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/
cp -r opt/ suse10_64

########################## gen 64 bit turbo linux
$sudo_cmd rm -rf opt/datadog-agent/embedded/lib/
$sudo_cmd rm -rf opt/datadog-agent/embedded/bin/
$sudo_cmd tar -zxvf /opt/agent_build_rely/centos5_lib.tar.gz
$sudo_cmd tar -zxvf /opt/agent_build_rely/python-linux64.tgz
$sudo_cmd unzip /opt/agent_build_rely/centos5_bin.zip
cp -r opt/ turbo64
cp -r python-linux64/* turbo64/opt/datadog-agent/embedded/
cp -r lib/ turbo64/opt/datadog-agent/embedded/
cp -r bin/ turbo64/opt/datadog-agent/embedded/
$sudo_cmd rm -rf lib/ bin/ python-linux64/
