#!/bin/bash

SH_HOME=$(cd "$(dirname "$0")"; pwd) 
BASE_HOME=$SH_HOME/../

function startNginx()
{
	if [ -f "$BASE_HOME/logs/nginx.pid" ]; then
		read pid < "$BASE_HOME/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			printf "%-10s is running\n" nginx
		else
			echo "starting nginx...."
			cd $BASE_HOME/sbin
			nohup ./nginx -p .. >/dev/null 2>&1 &
			if [ $? -eq 0 ]; then
			  echo nginx STARTED
			else
			  echo SERVER DID NOT START
			fi
		fi
	else
		echo "starting nginx..."
		cd $BASE_HOME/sbin
		nohup ./nginx -p .. >/dev/null 2>&1 &
		if [ $? -eq 0 ]; then
			echo nginx STARTED
		else
			echo SERVER DID NOT START
		fi
	fi
}

function stopNginx()
{
	if [ -f "$BASE_HOME/logs/nginx.pid" ]; then
		read pid < "$BASE_HOME/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			echo stoping $pid nginx 
			$BASE_HOME/sbin/nginx -p $BASE_HOME -s stop
		fi
	fi
}

if [ ! -d "$BASE_HOME/logs" ]; then
	mkdir $BASE_HOME/logs 
fi
    
if [ ! -d "$BASE_HOME/temp" ]; then
	mkdir $BASE_HOME/temp 
fi

cmd=$1
if [ ! -n "$1" ]
then
    cmd='status'
fi

case $cmd in
status)
	if [ -f "$BASE_HOME/logs/nginx.pid" ]; then
		read pid < "$BASE_HOME/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			exit 0
		else
			exit 1
		fi
	else
		exit 1
	fi
    ;;
start)
	startNginx
    ;;
stop)
	stopNginx
    ;;
uninstall)
	stopNginx
	cd $BASE_HOME
	rm -rf $(pwd)
    ;;
restart)
	stopNginx
	startNginx
	;;
*)
	echo "require start|stop|status|restart|uninstall"  
	;;
esac
    

sleep 1