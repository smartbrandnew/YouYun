# 配置Windows Service

# 配置Agent
## 启用插件
windows环境，通过窗口开启监控（Enable）。
## 配置插件
在Agent管理软件中编辑agent配置文件  

```
init_config:

instances:
    # For each instance you define what host to connect to (defaulting to the
    # current host) as well as a list of services you care about. The service
    # names should match the Service name in the properties and NOT the display
    # name in the services.msc list.
    #
    # If you want to check services on a remote host, you have to specify a
    # hostname and (optional) credentials
    #
    #-  host: MYREMOTESERVER
    #   username: MYREMOTESERVER\fred
    #   password: mysecretpassword
    #   tags:
    #       - fredserver
    #
    # The sample configuration will monitor the WMI Performance Adapter service,
    # named "wmiApSrv" in the service properties.
    #
    -   host: . # "." means the current host
        services:
            - wmiApSrv
```

## 重启Agent
windows环境，通过窗口开启监控（Restart）。

# 确认上传
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功  

```
windows_service
-------
   - instance #0 [OK]
   - Collected 0 metrics, 0 events and 2 service checks
```
