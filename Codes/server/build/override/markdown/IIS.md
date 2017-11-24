# 配置IIS
为了确认IIS性能计数器会被发送到WMI，需要重新同步WMI计数器
Windows 2003及之前的版本在控制台中运行以下命令：

```
winmgmt /clearadap
winmgmt /resyncperf
```
Windows 2008及之后的版本在控制台中运行以下命令：
```
winmgmt /resyncperf
```
# 配置Agent
## 启用插件
windows环境，通过窗口开启监控（Enable）。
## 配置插件
编辑配置文件 conf.d/IIS.yaml
```
instances:
    -   host: . # "." means the current host
        tags:
            - mytag1
            - mytag2
```
## 重启agent
windows环境，通过窗口开启监控（Restart）。

# 确认生效
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
iis
-------
    - instance #0 [OK]
    - Collected 45 metrics, 0 events & 3 service checks
```
