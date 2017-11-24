# 配置Apache
确认您已经安装了apache的status模块并且打开了ExtendedStatus。  
# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 Apache.yaml.example 为Apache.yaml
```
cp apache.yaml.example apache.yaml
```

## 配置插件
编辑配置文件 conf.d/apache.yaml:

```
init_config:

instances:
    -   apache_status_url: http://localhost/c-server-status
        # apache_user: example_user
        # apache_password: example_password
        tags:
            -   instance:foo
```
## 重启 Agent
```
service datamonitor-agent restart
```

# 确认上报
通过info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
apache
------
    - instance #0 [OK]
    - Collected 5 metrics, 0 events & 2 service checks
```
