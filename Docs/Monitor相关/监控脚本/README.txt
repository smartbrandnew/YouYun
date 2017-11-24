配置说明：
目前所有的hostid和hostname是写死的。
用的10.1.51.243这台centos测试机的
hostid：c0ba787fc33d517d8cf51aa5000b8c59
hostname：localhost.localdomain


crontab配置：
如需定时执行脚本，只需crontab -e添加行，比如
5 * * * * bash 绝对路径/monitor_service.sh      每五分钟执行一次monitor_service.sh脚本


运行方式
bash monitor_service.sh
bash monitor_log.sh  /var/log/anaconda/ anaconda.log "INFO DEBUG"   后面第一个参数代表文件路径，第二个参数是文件名，第三个参数是关键字列表，注意要加上""
bash monitor_file.sh /var/log/  all                          后面第一个参数代表路径， 第二个参数可选，如果不加第二个参数，默认统计文件夹下面