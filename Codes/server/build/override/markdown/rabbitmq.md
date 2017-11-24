# 配置agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 rabbitmq.yaml.example 为 rabbitmq.yaml
```
cp rabbitmq.yaml.example rabbitmq.yaml
```
## 配置插件
编辑配置文件 conf.d/rabbitmq.yaml。
```
 init_config: instances:
   - rabbitmq_api_url: http://10.1.11.20:15673/api/
     rabbitmq_user: admin
     rabbitmq_pass: 123abc
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
   rabbitmq
     --------
       - instance #0 [OK]
       - Collected 5 metrics, 0 events & 2 service checks

```