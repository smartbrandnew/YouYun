#!/bin/bash
SH_HOME=$(cd "$(dirname "$0")"; pwd)
BAT_HOME=$SH_HOME/..

check_status()
{
	if [ -f "$BAT_HOME/temp/$1.pid" ]; then
		read pid < "$BAT_HOME/temp/$1.pid"
		if kill -0 $pid 2>/dev/null ; then 
			_status_='running'
		else
			_status_='stopped'
		fi
	else
		_status_='stopped'
	fi
	
	if [ $_status_ == 'stopped' ];then
		exit 1
	fi
}


check_nginx_status()
{
	if [ -f "$BAT_HOME/nginx/logs/nginx.pid" ]; then
		read pid < "$BAT_HOME/nginx/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			_status_='running'
		else
			_status_='stopped'
		fi
	else
		_status_='stopped'
	fi
	
	if [ $_status_ == 'stopped' ];then
		exit 1
	fi
}
  
if test -s "$BAT_HOME/temp/" ; then 
    check_status dashboard
    check_status datastore
    check_status favourite
    check_status gateway
    check_status event
    check_status monitor
    check_status agent
    check_status report
    check_status web-api
    check_nginx_status
else
    exit 1
fi

exit 0

