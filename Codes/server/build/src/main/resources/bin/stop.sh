export LANG=zh_CN.UTF-8
#!/bin/sh
SH_HOME=$(cd "$(dirname "$0")"; pwd)
BASE_HOME=$SH_HOME/..

if [ -f "$BASE_HOME/.daemos" ]; then
	rm $BASE_HOME/.daemos
	touch $BASE_HOME/.daemos
fi
#kill nginx
if [ -f "$BASE_HOME/nginx/logs/nginx.pid" ]; then
	read pid < "$BASE_HOME/nginx/logs/nginx.pid"
	if kill -0 $pid 2>/dev/null ; then 
		echo stoping $pid nginx 
		$BASE_HOME/nginx/sbin/nginx -p $BASE_HOME/nginx -s stop
	fi
fi

#kill进程
while true
do
  num=`ps -ef | grep -E "bat-" | grep -v grep | grep -E "java" | wc -l`;
  if [ $num -eq 0 ]; then
    break
  fi

  pid=`ps -ef | grep -E "bat-" | grep -v grep | grep -E "java" | awk 'NR==1{print $2}'`;
  pName=`ps -ef | grep -E "bat-" | grep -v grep | grep -E "java" | grep -oP '([-]Dapp.name=bat)\S+'|awk 'NR==1{sub(/-Dapp.name=/,"");print}'`;
  if [ $pid -gt 0 ]; then
    echo -n stoping $pid $pName
    kill $pid

    count=20
    while [ $count -gt 0 ];
    do
      echo -n .
      sleep 1
      num=`ps -ef | grep $pid | grep -v grep | grep -E "java" | wc -l`
      if [ $num -eq 0 ]; then
        break;
      fi
      count=$(($count - 1))
    done
    echo 

    if [ $num -gt 0 ]; then
      echo force stop $pid
     kill -9 $pid
    fi
  fi
done

echo bat stoped
