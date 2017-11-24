# 配置MySQL


1. 在MySQL服务器上创建一个拥有replication权限的用户

```
#进入mysql，创建uyun用户
mysql -u root -p
CREATE USER 'uyun'@'localhost' IDENTIFIED BY '123456';
```


2. 如果想要获取所有的指标目录信息请赋予用户以下权限

```
#进入mysql中，执行
grant  process, select on *.* to 'uyun'@'localhost' identified by "123456";
grant  process, select on *.* to 'uyun'@'%' identified by "123456";
grant  replication client on *.* to 'uyun'@'localhost' identified by "123456";
grant  replication client on *.* to 'uyun'@'%' identified by "123456";



#退出mysql，使用quit命令，运行以下命令检验上述配置

mysql -u uyun --password=123456 -e "show status" | \
grep Uptime && echo -e "\033[0;32mMySQL user - OK\033[0m" || \
echo -e "\033[0;31mCannot connect to MySQL\033[0m"

mysql -u uyun --password=123456 -e "show slave status" && \
echo -e "\033[0;32mMySQL grant - OK\033[0m" || \
echo -e "\033[0;31mMissing REPLICATION CLIENT grant\033[0m"

mysql -u uyun --password=123456 -e "SELECT * FROM performance_schema.threads" && \
echo -e "\033[0;32mMySQL SELECT grant - OK\033[0m" || \
echo -e "\033[0;31mMissing SELECT grant\033[0m"


mysql -u uyun --password=123456 -e "SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST" && \
echo -e "\033[0;32mMySQL PROCESS grant - OK\033[0m" || \
echo -e "\033[0;31mMissing PROCESS grant\033[0m"
```

# 配置Agent
## 启用插件
 1. windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 mysql.yaml.example 为mysql.yaml
```
cp mysql.yaml.example mysql.yaml
```

## 配置插件
编辑配置文件 conf.d/mysql.yaml
```
init_config:

instances:
  - server: localhost
    user: uyun
    pass: '123456'
    port: 3306
    tags:
        - optional_tag1
        - optional_tag2
    options:
        replication: 0
    #密码全数字需要只用引号括起来
```

## 重启agent

```
service datamonitor-agent restart
```

# 确认上报

通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功

```
mysql
-----
  - instance #0 [OK]
  - Collected 21 metrics, 0 events & 2 service checks

```	
