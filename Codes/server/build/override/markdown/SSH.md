# 配置Agent
## 启用插件
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制 ssh_check.yaml.example 为ssh_check.yaml
```
cp ssh_check.yaml.example ssh_check.yaml
```

## 配置插件
编辑配置文件 conf.d/ssh_check.yaml

```
	init_config:
	
	instances:
	
	    - host: localhost #目标机器的IP
	      port: 22
	      username: test #目标机器有ssh权限的用户名
	      password: abcd #有ssh权限用户的密码
	      sftp_check: True
	      private_key_file:
	      add_missing_keys: True

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

	  ssh_check
	  ---------
	      - instance #0 [OK]
	      - Collected 8 metrics & 0 events

```