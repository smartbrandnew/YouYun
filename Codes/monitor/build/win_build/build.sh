#!/bin/bash

mkdir monitor-agent

curl 10.1.100.100/python/monitor-python-windows32.zip > embedded32.zip
unzip embedded32.zip -d monitor-agent
rm -rf embedded32.zip

cp nssm.exe install.py start.bat manifest.yaml monitor-agent/

cd ../../
cp -r conf.d/ checks.d/ checks/ scripts/ utils/ win32/ build/win_build/monitor-agent/
cp datamonitor.conf datamonitor-cert.pem  datamonitor_template build/win_build/monitor-agent/
cp monitoragent.py  monitorstatsd.py jmxfetch.py  build/win_build/monitor-agent/
cp gohai.py aggregator.py graphite.py modules.py config.py daemon.py emitter.py transaction.py util.py build/win_build/monitor-agent/

mkdir build/win_build/monitor-agent/run/
mkdir build/win_build/monitor-agent/logs/


cd ./build/win_build
zip -r monitor-agent.zip monitor-agent
rm -rf ./monitor-agent
