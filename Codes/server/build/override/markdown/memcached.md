# 配置agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 vsphere.yaml.example 为 vsphere.yaml
```
cp vsphere.yaml.example vsphere.yaml
```
## 配置插件
编辑配置文件 conf.d/vsphere.yaml。
```
   init_config: instances:
     - name: broada-main-vcenter
       host: 10.1.2.230
       username: monitor
       password: monitor
       ssl_verify: false
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
    Checks
       vsphere
       -------
         - instance #0 [OK]
         - Collected 8 metrics, 0 events & 6 service checks
 ```