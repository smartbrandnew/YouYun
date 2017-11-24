export LANG=zh_CN.UTF-8
#!/bin/sh 

SH_HOME=$(cd "$(dirname "$0")"; pwd)
BASE_HOME=$SH_HOME/..
cd $BASE_HOME

printf "\033[32miniting database...\n\033[0m\n"
#执行数据库初始化
cd $BASE_HOME/console/bin/
sh console.sh > $BASE_HOME/DBInit.log

#默认前7行的异常无伤大雅
has_err=$(cat $BASE_HOME/DBInit.log | tail -n +7 | grep -E "WARN|ERROR" || echo "no")
if [ "$has_err" != "no" ]; then
	printf "\033[31m
maybe init database error.please check the log file:

$BASE_HOME/DBInit.log

$has_err
\n\033[0m\n"
	exit
else
	printf '\033[32m
dbinit complete.
\n\033[0m\n'
fi
