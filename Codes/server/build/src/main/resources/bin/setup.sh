export LANG=zh_CN.UTF-8
#!/bin/sh 
SH_HOME=$(cd "$(dirname "$0")"; pwd)
BASE_HOME=$SH_HOME/..
cd $BASE_HOME
#获取绝对路径
BASE_HOME=$(cd "$(dirname "$0")"; pwd)

printf "\033[32mmaking the file can be executed...\n\033[0m\n"
find . -name "*.sh" | xargs chmod +x
chmod +x $BASE_HOME/nginx/sbin/nginx

has_java=$(which java || echo "no")
if [ "$has_java" = "no" ]; then
	printf "\033[31merror！has not install JDK 1.8_77.\n\033[0m\n"
	exit
fi

printf "\033[32msetting[bat.sh]...\n\033[0m\n"
sh -c "sed -i 's@BAT_HOME=.*@BAT_HOME=$BASE_HOME@' $SH_HOME/bat.sh"






