# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 tcp_check.yaml.example 为tcp_check.yaml
```
cp tcp_check.yaml.example tcp_check.yaml
```
## 配置插件
编辑配置文件 conf.d/tcp_check.yaml
```
instances:
  - name: http://127.0.0.1:8000/
	host: 127.0.0.1
	port: 8000
	timeout: 1
```

## 重启Agent
 1. windows环境  
windows环境，通过窗口重启Agent（restart）。
 2. linux 环境  
 在命令行输入
```
service datamonitor-agent restart
```
# 确认上报

可以通过查看service datamonitor-agent info命令，验证配置是否成功。  
当出现以下信息，则开启监控成功。
```
tcp_check  
    Instance #0 
         OK  
    Collected 0 metrics, 0 events and 2 service check  
```