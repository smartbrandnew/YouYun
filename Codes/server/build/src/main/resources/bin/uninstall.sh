#!/bin/sh

SH_HOME=$(cd "$(dirname "$0")"; pwd)

if [ -e "/usr/lib/systemd/system/uyun-monitor.service" ]; then
	systemctl disable uyun-monitor.service
    systemctl stop uyun-monitor
fi

if [ -e "$SH_HOME/stop.sh" ]; then
	sh $SH_HOME/stop.sh
fi

cd $SH_HOME/..
BASE_HOME=$(pwd)
rm -rf $BASE_HOME