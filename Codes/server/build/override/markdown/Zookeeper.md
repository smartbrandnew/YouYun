# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 zk.yaml.example 为zk.yaml
```
cp zk.yaml.example zk.yaml
```

## 配置插件
编辑配置文件 conf.d/zk.yaml

```
init_config:

instances:
   -   host: localhost
       port: 2181
       timeout: 3
```

## 重启 Agent
```
service datamonitor-agent restart
```

# 确认上传
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功

```
zk
-------
      - instance #0 [OK]
      - Collected 2 metrics, 0 events & 2 service checks
```
