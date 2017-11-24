# 配置 mongodb 
## 环境设置
创建 uyun-monitor 访问权限设置，需要 admin 权限才能完整采集 MongoDB 性能数据。
需要 admin 权限才能完整采集 MongoDB 性能数据。
在 Mongo Shell 中执行以下指令(适用于mongodb2.x版本)

```
use admin
db.auth("admin", "admin-password")#用于认证admin用户的，如果admin没有设置密码，则跳过
db.addUser("mongodb", "passward", true)
```
如果您使用的是 3.0 和以上版本的 MongoDB，那请执行以下指令，来创建只读 Admin 权限。
```
use admin
db.auth("admin", "admin-password") #用于认证admin用户的，如果admin没有设置密码，则跳过
db.createUser({"user":"monitor", "pwd": "passward", "roles" : [ {role: 'read', db: 'admin' }, {role: 'clusterMonitor', db: 'admin'}, {role: 'read', db: 'local' }]})
```
# 配置 agent 
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 mongo.yaml.example 为 mongo.yaml
```
cp mongo.yaml.example mongo.yaml
```
## 配置插件
编辑配置文件 conf.d/mongo.yaml。
```
 init_config: instances:
   - server: mongodb://monitor:passward@localhost:27017/admin
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
     mongo
     -----
       - instance #0 [OK]
       - Collected 145 metrics, 0 events & 2 service checks
       - Dependencies:
           - pymongo: 3.2
```