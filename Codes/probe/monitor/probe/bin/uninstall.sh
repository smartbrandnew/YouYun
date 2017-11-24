BASE_HOME=`pwd`
APP_NAME=monitor-agentless-9166
ps -ef | grep monitor-agentless | grep -v grep | awk '{print $2}' | xargs kill -9
cd $BASE_HOME/../
rm -rf *
rm -rf  `pwd` | awk -F "/" '{print $NF}'

