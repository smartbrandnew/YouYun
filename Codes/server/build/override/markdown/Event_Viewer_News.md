
# 配置Agent
## 启用插件
 1. windows环境 
windows环境，通过窗口开启监控（Enable）

编辑配置文件 conf.d/win32_event_log.yaml。

```
tags:
 	   - system
type:
 	   - Error
log_file:
 	   - System
     	   
```
## 重启Agent
 1. windows环境  
windows环境，通过窗口重启Agent（restart）。
 2. linux 环境  
 在命令行输入 /etc/init.d/datamonitor-agent restart

# 确认上报


可以通过查看service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
  
```
win32_event_log
     Instance #0
          OK 
     Collected 0 metrics, 0 events and 1 service check 

```