#!/bin/bash


cp install.* ./aix64/

cd ../../
cp -r requirements.txt version-manifest.json  ./build/linux_build/monitor-agent/
cp -r conf.d/  ./build/linux_build/monitor-agent/
cp -r checks.d/ checks/ scripts/ utils/  ./build/linux_build/monitor-agent/agent/
cp datamonitor.conf datamonitor_template supervisor.conf  ./build/linux_build/monitor-agent/conf/
cp monitoragent.py  monitorstatsd.py jmxfetch.py updater_process.py script_monitor_process.py  ./build/linux_build/monitor-agent/agent/
cp agent.py gohai.py aggregator.py graphite.py modules.py config.py daemon.py emitter.py transaction.py util.py datamonitor-cert.pem   ./build/linux_build/monitor-agent/agent/

cd ./build/linux_build/

rm -rf ./aix64/install.* ./aix64/monitor-agent
rm -rf ./centos32/install.* ./centos32/monitor-agent
rm -rf ./centos64/install.* ./centos64/monitor-agent
rm -rf ./centos5_64/install.* ./centos5_64/monitor-agent
rm -rf ./debian32/install.* ./debian32/monitor-agent
rm -rf ./debian64/install.* ./debian64/monitor-agent
rm -rf ./suse64/install.* ./suse64/monitor-agent
rm -rf ./suse10_64/install.* ./suse10_64/monitor-agent
rm -rf ./turbo64/install.* ./turbo64/monitor-agent

cp -r install.* monitor-agent ./aix64/
cp -r install.* monitor-agent ./centos32/
cp -r install.* monitor-agent ./centos64/
cp -r install.* monitor-agent ./centos5_64/
cp -r install.* monitor-agent ./debian32/
cp -r install.* monitor-agent ./debian64/
cp -r install.* monitor-agent ./turbo64/
cp -r install.* monitor-agent ./suse64/
cp -r install.* monitor-agent ./suse10_64/

#curl 10.1.100.100/python/monitor-python-windows32.zip > embedded32.zip

tar zxvf /tmp/monitor-agent/monitor-python-centos-32.tgz -C ./centos32/monitor-agent/
tar zxvf /tmp/monitor-agent/centos32-bin.tgz -C ./centos32/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-centos5-64.tgz -C ./centos5_64/monitor-agent/
tar zxvf /tmp/monitor-agent/centos5_32-bin.tgz -C ./centos5_64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-centos-64.tgz -C ./centos64/monitor-agent/
tar zxvf /tmp/monitor-agent/centos64-bin.tgz -C ./centos64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-debian-32.tgz -C ./debian32/monitor-agent/
tar zxvf /tmp/monitor-agent/debian32-bin.tgz -C ./debian32/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-debian-64.tgz -C ./debian64/monitor-agent/
tar zxvf /tmp/monitor-agent/debian64-bin.tgz -C ./debian64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-suse-64.tgz -C ./suse64/monitor-agent/
tar zxvf /tmp/monitor-agent/suse64-bin.tgz -C ./suse64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-suse10-64.tgz -C ./suse10_64/monitor-agent/
tar zxvf /tmp/monitor-agent/suse10_64-bin.tgz -C ./suse10_64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-turbo-64.tgz -C ./turbo64/monitor-agent/
tar zxvf /tmp/monitor-agent/centos64-bin.tgz -C ./turbo64/monitor-agent/

tar zxvf /tmp/monitor-agent/monitor-python-aix-64.tgz -C ./aix64/monitor-agent/
tar zxvf /tmp/monitor-agent/aix64-bin.tgz -C ./aix64/monitor-agent/

rm -rf ./monitor-agent/agent/* ./monitor-agent/conf/* ./monitor-agent/conf.d/*

tar czvf monitor-agent-aix64.tgz aix64
tar czvf monitor-agent-centos32.tgz centos32
tar czvf monitor-agent-centos64.tgz centos64
tar czvf monitor-agent-centos5_64.tgz centos5_64
tar czvf monitor-agent-debian32.tgz debian32
tar czvf monitor-agent-debian64.tgz debian64
tar czvf monitor-agent-suse64.tgz suse64
tar czvf monitor-agent-suse10_64.tgz suse10_64
tar czvf monitor-agent-turbo64.tgz turbo64
