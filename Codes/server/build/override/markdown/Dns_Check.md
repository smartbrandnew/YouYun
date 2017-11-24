# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 dns_check.yaml.example   为dns_check.yaml
```
cp dns_check.yaml.example dns_check.yaml
```
## 配置插件
编辑配置文件 conf.d/dns_check.yaml
```
instances:   
  - hostname: www.xijiximo.cn  
    nameserver: 10.1.2.252  
    timeout: 8   
```
## 重启Agent
 1. windows环境  
windows环境，通过窗口重启Agent（restart）。
 2. linux 环境  
 在命令行输入 /etc/init.d/datamonitor-agent restart

# 确认上报

可以通过查看service datamonitor-agent info命令，验证配置是否成功。  
当出现以下信息，则开启监控成功。
```
dns_check
    Instance #0
         OK 
    Collected 1 metrics, 0 events and 2 service check    
```