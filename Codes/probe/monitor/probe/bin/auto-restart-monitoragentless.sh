BASE_HOME=`pwd`
APP_NAME=monitor-agentless-9166
num=`ps -ef | grep $APP_NAME | grep -v dist-probe-mc | grep -v grep |grep -E "java" | wc -l`;
  if [ $num -eq 0 ]; then
  cd $BASE_HOME 
  echo "启动探针.."
  nohup sh monitor-agentless.sh&
  fi

