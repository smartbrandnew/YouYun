BASE_HOME=`pwd`
APP_NAME=monitor-agentless-9166
while true
do
  num1=`ps -ef | grep "sh $BASE_HOME/auto-restart-monitoragentless.sh"|grep -v grep | wc -l`;
 
  if [ $num1 -eq 0 ]; then
    break
  fi
  
  pid1=`ps -ef | grep "sh $BASE_HOME/auto-restart-monitoragentless.sh"| grep -v grep | awk 'NR==1{print $2}'`;
  
  if [ $pid1 -gt 0 ]; then
    echo -n stoping $pid1
    kill $pid1

    count=20
    while [ $count -gt 0 ];
    do
      echo -n .
      sleep 1
      num1=`ps -ef | grep $pid1 | grep -v grep | grep -E "nginx|java" | wc -l`
      if [ $num1 -eq 0 ]; then
        break;
      fi
      count=$(($count - 1))
    done
    echo 

    if [ $num1 -gt 0 ]; then
      echo force stop $pid1
     kill -9 $pid1
    fi
  fi
done
echo auto-restart-monitoragentless stoped

while true
do
  num2=`ps -ef | grep $APP_NAME | grep -v grep | grep -v dist-probe-mc | grep -v stop.sh | grep -E "nginx|java" | wc -l`;
 
  if [ $num2 -eq 0 ]; then
    break
  fi
  
  pid2=`ps -ef | grep $APP_NAME | grep -v grep | grep -v dist-probe-mc | grep -v stop.sh | grep -E "nginx|java" | awk 'NR==1{print $2}'`;
  

if [ $pid2 -gt 0 ]; then
    echo -n stoping $pid2
    kill $pid2

    count=20
    while [ $count -gt 0 ];
    do
      echo -n .
      sleep 1
      num2=`ps -ef | grep $pid2 | grep -v grep | grep -E "nginx|java" | wc -l`
      if [ $num2 -eq 0 ]; then
        break;
      fi
      count=$(($count - 1))
    done
    echo 

    if [ $num2 -gt 0 ]; then
      echo force stop $pid2
     kill -9 $pid2
    fi
  fi
done

echo monitor-agentless stoped

