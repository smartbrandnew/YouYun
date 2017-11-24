# 配置agent
## 启用插件

 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 elastic.yaml.example 为 elastic.yaml
``` bash
cp elastic.yaml.example elastic.yaml
```
## 配置插件
编辑配置文件 conf.d/elastic.yaml。
```
  init_config: 	
  instances:
    - url: http://10.1.53.254:7202
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
 elastic
     -------
       - instance #0 [OK]
       - Collected 108 metrics, 0 events & 3 service checks
```