# 配置Docker
确认您已经安装了Docker并且它运行在您的服务器上  
把运行agent的用户添加到Docker组中(Docker组指运行docker的用户)
```
usermod -a -G docker monitor-agent
```
# 配置Agent
## 启用插件
在/etc/monitor-agent/conf.d中复制 docker_daemon.yaml.example 为docker_daemon.yaml
```
cp docker_daemon.yaml.example docker_daemon.yaml
```

## 配置插件
编辑配置文件 conf.d/docker_daemon.yaml  

```
init_config:
instances:
      - url: "unix://var/run/docker.sock"
        new_tag_names: true
```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功

```
docker_daemon
-------------
       - instance #0 [OK]
       - Collected 22 metrics, 0 events & 2 service checks

```
