
# 配置Jenkins监控

1. DataMonitor插件要求jenkins版本为1.580.1及以上[下载地址](http://updates.jenkins-ci.org/download/war/)。
2. 在jenkins.war的目录下，运行java -jar jenkins.war
3. 在浏览器中打开http:\\localhost:8080,在系统管理 -> 管理插件中安装DataMonitor插件。可以在系统管理的系统设置的主目录
4. 然后新建任务，构建一个自由风格的软件项目

# 配置Agent

## 启用插件
1. windows环境

    windows环境，通过窗口开启监控(Enable)。

2. linux 环境

    在/etc/monitor-agent/conf.d中复制 jerkins_check.yaml.example 为jerkins_check.yaml

```
cp jerkins_check.yaml.example jerkins_check.yaml
```

## 配置插件
编辑配置文件 conf.d/jerkins_check.yaml
```
init_config:

instances:
  - name: zxc
    jenkins_home: D:\jenkins
	#系统管理 -> 系统设置 -> 主目录路劲
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
jenkins
     Instance #0
          OK 
     Collected 0 metrics, 0 events and 1 service check 
```
