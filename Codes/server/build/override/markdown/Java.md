# 配置Java
确认你已经开启了[JMX远程连接](http://docs.oracle.com/javase/1.5.0/docs/guide/management/agent.html)
# 配置Agent
## 启用插件
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制 jmx.yaml.example 为jmx.yaml
```
cp jmx.yaml.example jmx.yaml
```

## 配置插件
编辑配置文件 conf.d/jmx.yaml
```

init_config:

instances:
	   - host: localhost
         port: 1099
         name: jmx_instance
#        user: username
#        password: password
#
#        conf:
#            -include:
#               domain: my_domain
#               bean: my_bean
#               attribute:
#                    attribute1:
#                        metric_type: counter
#                        alias: jmx.my_metric_name
#                    attribute2:
#                        metric_type: gauge
#                        alias: jmx.my2ndattribute
#            - include:
#                domain: 2nd_domain
#            - exclude:
#                bean: excluded_bean
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
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
jmx
    Instance #jmx-localhost-1099
 		OK 
	Collected 13 metrics, 0 events and 0 service check 
```
