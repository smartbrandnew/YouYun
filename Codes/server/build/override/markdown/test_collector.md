# 配置agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 test_collector.yaml.example 为 test_collector.yaml
```
cp test_collector.yaml.example test_collector.yaml
```
## 配置插件
编辑配置文件 conf.d/test_collector.yaml。
```
 init_config: instances:
   - network_range: 10.1.1.0/24,
     complete_scan_interval: 6
     cpu_ram_port_rate_interval: 5
     ping_interval: 1
     snmp_version: v2c
     v1_v2c_parameters:
       - community_string: broadapublic
```
## 重启Agent
```
service datamonitor-agent restart
```
# 确认上报
可以通过查看 service datamonitor-agent info命令，验证配置是否成功。当出现以下信息，则开启监控成功。
```
  network
     -------
       - instance #0 [OK]
       - Collected 15 metrics, 0 events & 1 service check
```