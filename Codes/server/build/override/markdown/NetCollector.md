# 配置网络设备

监控网络设备，需要预先对网络设备进行配置，以满足以下条件：
1. 拥有管理IP，并可以通过Agent ping通；
2. 已开通Snmp（支持v1/v2c/v3），并可以通过Agent访问。

# 配置Agent

## 启用插件

 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 net_collector.yaml.example 为net_collector.yaml
```
cp net_collector.yaml.example net_collector.yaml
```

## 配置插件

### 在哪里配置

1. windows环境
windows环境，通过窗口编辑。
2. linux 环境
在/etc/monitor-agent/conf.d中编辑 net_collector.yaml 文件
```
vi net_collector.yaml
```

### 如何配置

#### 网段参数
在配置文件中 network_segment 参数处配置，每个参数以 ',' 逗号结束。

| 网段 | 格式 |
| :-: | :-: |
| 255掩码方式 | 192.168.0.0/255.255.255.0 |
| 数字掩码方式 | 192.168.0.0/24 |
| 网段式 | 192.168.0.0~192.168.0.255 |
| 单独ip | 192.168.1.52 |

#### ping 参数

在配置文件的 ping 参数处配置

| 参数 | 名称 | 说明 |
| :-: | :-: | --- |
| timeout | 超时 | 单位ms，默认1000ms，用于判断一个ping包的超时等待时间 |
| retries | 重试次数 | 默认4，全部丢包，才表示一个设备离线 |

#### snmp 参数

在配置文件的 snmp 参数处配置。
为了支持配置网段内网络设备有多个snmp版本和snmp参数的情况，可以设置多个v1，v2c的community参数：
```
- community: broadapublic
- community: public
- community: broada
```
以及多套snmp v3参数（与上同理）。

***注意：v1、v2c、v3参数可以选填但不能全部为空。***

| 参数 | 名称 | 说明 |
| :-: | :-: | --- |
| timeout | 超时 | 单位ms，默认1000ms |
| retries | 重试次数 | 重试次数 |
| port | 服务端口 | 默认161 |
| version | 版本 |  v1,v2c,v3  |
| community | 共同体 | v1/v2c的访问密码 |
| securityLevel | 安全级别  | noAuthNoPriv, authNoPriv, authPriv |
| securityUser | 安全用户 |  |
| authProtocol | 验证协议 | md5, sha |
| authPassword | 验证密码 | 一般大于等于8位 |
| privProtocol | 加密协议 | aes128, des |
| privPassword | 加密密码 | 一般大于等于8位 |

#### 采集间隙

| 参数 | 名称 | 说明 |
| :-: | :-: | --- |
| net_discover_interval | 网段扫描间隙 | 单位s，默认86400s，也就是24小时，全网段扫描的间隙时间，判断网段内是否有新上线ip |
| online_status_collect_interval | 网络设备在线状态采集间隙 | 默认30s，如果设置为0则不采集此指标 |
| performance_collect_interval | 网络设备性能消耗采集间隙 | 默认30s，如果设置为0则不采集此指标 |
| port_status_collect_interval | 网络设备端口状态采集间隙 | 默认30s，如果设置为0则不采集此指标 |
| port_rate_collect_interval | 网络设备端口速率采集间隙 | 默认120s，如果设置为0则不采集此指标 |

#### 线程池大小

| 参数 | 名称 | 说明 |
| :-: | :-: | --- |
| thread | 线程池大小 | 默认8，网络设备监测是通过多线程提高了指标采集、上传的实时性，线程池设置过大占用过多计算机资源，过少则影响实时性，请根据本机性能表现选取合适的值 |

#### 标签

| 参数 | 名称 | 说明 |
| :-: | :-: | --- |
| tags | 标签 | 为本机监测到的网络设备指标附带一个特殊标签以供筛选、识别 |

#### 配置举例

```
init_config:

instances:
  - network_segment: 10.1.1.0/24,192.168.1.52,
    ping:
      - timeout: 1000
        retries: 4
    snmp:
      - version: v2c
        v1_v2c_parameters:
          - community: broadapublic
          - community: public
          - community: broada
        v3_parameters:
          - securityLevel:
            securityUser:
            authProtocol:
            authPassword:
            privProtocol:
            privPassword:
        port: 161
        timeout: 1000
        retries: 1
    net_discover_interval: 86400
    online_status_collect_interval: 30
    performance_collect_interval: 30
    port_status_collect_interval: 30
    port_rate_collect_interval: 120
    thread: 8
    tags: first
```

## 重启 Agent

1. windows环境
windows环境，通过gui界面手动重启Agent。
2. linux 环境
```
service datamonitor-agent restart
```

## 确认上报
通过查看日志，验证配置是否成功，网络设备监测日志：
1. windows环境
vista及以上版本系统：
```
C:\ProgramData\Datamonitor\logs\net_collector.log
```
xp及以下版本系统：
```
C:\Documents and Settings\All Users\Application Data\Datamonitor\logs\net_collector.log
```

2. linux 环境
```
/var/log/datamonitor/net_collector.log
```

当日志出现以下信息，说明配置成功
```
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:806) | Amounts of metrics submitted during last 30 seconds:
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:807) | 5 basis info
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:808) | 0 online status
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:809) | 4 performance
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:810) | 4 port status
2016-09-19 11:12:22 中国标准时间 | INFO | checks.net_collector(net_collector.py:811) | 0 port rate & rate-bandwidth-usage
```
