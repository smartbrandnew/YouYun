export LANG=zh_CN.UTF-8

BASE_HOME=`pwd`

echo "启动 monitor-agentless..." 
nohup sh $BASE_HOME/auto-restart-monitoragentless.sh >/dev/null 2>&1 &



