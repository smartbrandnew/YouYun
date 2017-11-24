
# 配置HAProxy 监控

1. 确保HAProxy服务已经开启

# 配置Agent

## 启用插件
1. windows环境

    windows环境，通过窗口开启监控(Enable)。

2. linux 环境

    在/etc/monitor-agent/conf.d中复制 haproxy.yaml.example 为haproxy.yaml

```
cp haproxy.yaml.example haproxy.yaml
```

## 配置插件
编辑配置文件 conf.d/haproxy.yaml

```
init_config:

instances:
    -   url: https://localhost/admin?stats
        username: root
        password: qwer1234
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
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。

``` 
haproxy
-------
  - instance #0 [OK]
  - Collected 32 metrics, 0 events & 9 service checks
``` 
	

