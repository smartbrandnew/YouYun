
# 配置Kubernetes监控

1. 确保Kubernetes服务已经开启

# 配置Agent

## 启用插件
1. windows环境

    windows环境，通过窗口开启监控(Enable)。

2. linux 环境

    在/etc/monitor-agent/conf.d中复制 Kubernetes.yaml.example 为Kubernetes.yaml
```
cp Kubernetes.yaml.example Kubernetes.yaml
```

## 配置插件
编辑配置文件 conf.d/Kubernetes.yaml
```	
init_config:
  #    tags:
  #      - optional_tag1
  #      - optional_tag2	
instances:	
 - port: 4194
   use_histogram: True
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
kubernetes
  -------
      - instance #0 [OK]
      - Collected 8 metrics & 0 events
	
```
