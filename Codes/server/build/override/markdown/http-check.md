
# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 http_check.yaml.example为http_check.yaml
```
cp http_check.yaml.example http_check.yaml
```
## 配置插件
编辑配置文件 conf.d/http_check.yaml

```

instances:  
  - name: My first service  
    url: https://www.uyuntest.cn  
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
http_check  
    Instance #0 
         OK  
    Collected 0 metrics, 0 events and 2 service check
```  