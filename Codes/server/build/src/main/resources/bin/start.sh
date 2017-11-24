export LANG=zh_CN.UTF-8
#!/bin/sh

SH_HOME=$(cd "$(dirname "$0")"; pwd) 
BASE_HOME=$SH_HOME/../
DAEMO_LIST=$BASE_HOME/.daemos
touch $DAEMO_LIST;

function addDaemo()
{
  sed -i '/'$1'/d' $DAEMO_LIST;
  echo $1"|`date "+%Y-%m-%d %H:%M:%S"`" >>$DAEMO_LIST;
}

function out() 
{
    if [ $? -eq 0 ]
    then
       if [ ! -d "$BASE_HOME/temp" ]
       then
		mkdir $BASE_HOME/temp 
       fi
       /bin/echo -n $! > "$BASE_HOME/temp/$1.pid"
      if [ $? -eq 0 ];
      then
        sleep 1
        printf "%-10s STARTED\n" $1
      else
        echo FAILED TO WRITE PID
        exit 1
      fi
    else
      echo SERVER DID NOT START
      exit 1
    fi
}

function startMonitorService()
{
	if [ -f "$BASE_HOME/temp/$1.pid" ]; then
		read pid < "$BASE_HOME/temp/$1.pid"
		if kill -0 $pid 2>/dev/null ; then 
			printf "%-10s is running\n" $1
		else
			echo "starting $1...."
			addDaemo $1
			cd $BASE_HOME/$1/bin 
			if [ $1 = "web-api" ]; then
				nohup ./web.sh >/dev/null 2>&1 & 
			else
				nohup ./$1.sh >/dev/null 2>&1 & 
			fi
			out $1
		fi
	else
		echo "starting $1..."
		addDaemo $1
		cd $BASE_HOME/$1/bin 
		if [ $1 = "web-api" ]; then
			nohup ./web.sh >/dev/null 2>&1 & 
		else
			nohup ./$1.sh >/dev/null 2>&1 & 
		fi
		out $1
	fi
}

function startNginx()
{
	if [ -f "$BASE_HOME/nginx/logs/nginx.pid" ]; then
		read pid < "$BASE_HOME/nginx/logs/nginx.pid"
		if kill -0 $pid 2>/dev/null ; then 
			printf "%-10s is running\n" nginx
		else
			echo "starting nginx..."
			addDaemo nginx
			cd $BASE_HOME/nginx/sbin
			nohup ./nginx -p .. >/dev/null 2>&1 &
			if [ $? -eq 0 ]; then
			  echo nginx STARTED
			else
			  echo SERVER DID NOT START
			fi
		fi
	else
		echo "starting nginx..."
		addDaemo nginx
		cd $BASE_HOME/nginx/sbin
		nohup ./nginx -p .. >/dev/null 2>&1 &
		if [ $? -eq 0 ]; then
			echo nginx STARTED
		else
			echo SERVER DID NOT START
		fi
	fi
}

startMonitorService dashboard
startMonitorService event
startMonitorService favourite
startMonitorService datastore
startMonitorService monitor
startMonitorService gateway
startMonitorService agent
startMonitorService report
startMonitorService web-api 

startNginx
sleep 1
