export LANG=zh_CN.UTF-8 
BASE_HOME=`pwd` 
 
function isRun() 
{ 
 isrun=$(ps -ef |grep $1 |grep -v "grep")  
  if [ "$isrun" ] ; then 
   return 0; 
  fi 
   return 1; 
} 
 
function isNeedStart() 
{ 
 ! isRun $1
 return $?; 
} 
 
if isNeedStart carrier-probe ;then 
log "启动 probe..." 
cd $BASE_HOME  
nohup sh carrier-probe.sh&  
fi 
 
