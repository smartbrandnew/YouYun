# 配置agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 redisdb.yaml.example 为redisdb.yaml
``` bash
cp redisdb.yaml.example redisdb.yaml
```

## 配置插件
编辑配置文件 conf.d/redisdb.yaml。
``` yaml
init_config: 
instances:
  - host: 10.1.53.104
    port: 6379
    db: 0
    password: 12345678
    socket_timeout: 5
    tags:
      - redis:uyun.redis.53.104
```      
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
``` 
    redisdb
       -------
         - instance #0 [OK]
         - Collected 45 metrics, 0 events & 3 service checks
         - Dependencies:
             - redis: 2.10.3
 ```