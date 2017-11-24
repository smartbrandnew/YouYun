# 配置Agent
## 启用插件
### master node
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制  mesos_master.yaml.example 为 mesos_master.yaml
```
cp mesos_master.yaml.example mesos_master.yaml
```

### slave node
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制  mesos_slave.yaml.example 为 mesos_slave.yaml
```
cp mesos_slave.yaml.example mesos_slave.yaml
```

## 配置插件
### master node
编辑配置文件 conf.d/mesos_master.yaml
```
init_config:
  default_timeout: 5
instances:
  # url: the API endpoint of your Mesos master
  - url: https://server:port
```

### slave node
编辑配置文件 conf.d/slave_master.yaml
```
init_config:
  default_timeout: 5
instances:
  # url: the API endpoint of your Mesos slave
  - url: https://server:port
    # tasks: Task's names to monitor
    tasks:
      - Hello
```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
mesos_master
------------
      - instance #0 [OK]
      - Collected 8 metrics & 0 events
```

```
mesos_slave
-----------
     - instance #0 [OK]
     - Collected 8 metrics & 0 events
```
