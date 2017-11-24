#!/bin/bash
#chkconfig:2345 20 90
#description:bat service
#processname:bat
BAT_HOME=/vagrant/dist

check_status()
{
	if [ -f "$BAT_HOME/temp/$1.pid" ]; then
		read pid < "$BAT_HOME/temp/$1.pid"
		if kill -0 $pid 2>/dev/null ; then 
			printf "%-10s running ($pid)\n" $1
		else
			printf  "%-10s is not running\n " $1
		fi
	else
		printf  "%-10s is not running\n" $1
	fi
}

check_nginx_status()
{
	if [ -f "$BAT_HOME/nginx/logs/nginx.pid" ]; then
		read pid < "$BAT_HOME/nginx/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			printf "%-10s running ($pid)\n" nginx
		else
			printf  "%-10s is not running\n" nginx
		fi
	else
		printf  "%-10s is not running\n" nginx
	fi
}
  
  
case $1 in
    start) 
	su -l root $BAT_HOME/bin/start.sh
	;;
    stop) 
	su root $BAT_HOME/bin/stop.sh
	;;
    status) 
	
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
      fi
	
	;;
    restart) 
	su -l root $BAT_HOME/bin/stop.sh
	su root $BAT_HOME/bin/start.sh
	;;
    *)  
	echo "require start|stop|status|restart"  ;;
esac