# 配置 postgres
## 环境设置
创建 uyun-monitor 访问权限设置
为您的 PostgreSQL 服务器建立  uyun-monitor 只读权限。
在 PostgreSQL 中启动 psql，并执行以下指令。
```
create user uyunagent with password '123abc';   grant SELECT ON pg_stat_database to uyunagent ;
```
如何您运行的是docker容器,  可执行下列语句开启访问
```
docker run --name pgno1  -e POSTGRES_PASSWORD=uyuntest -e   POSTGRES_USER=uyuntest  -d -p 5432:5432 postgres
```
# 配置agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 postgres.yaml.example 为 postgres.yaml
```
cp postgres.yaml.example postgres.yaml
```
## 配置插件
编辑配置文件 conf.d/postgres.yaml。
```
 init_config: instances:
   - host: 10.1.11.20
     port: 5432
     username: uyuntest
     password: uyuntest
     ssl: False
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
     postgres
     --------
       - instance #0 [OK]
       - Collected 5 metrics, 0 events & 2 service checks
```