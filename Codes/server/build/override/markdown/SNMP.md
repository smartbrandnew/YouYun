# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 SNMP.yaml.example 为snmp.yaml

```
cp snmp.yaml.example snmp.yaml
```

## 配置插件
编辑配置文件 conf.d/snmp.yaml

```  
init_config:
     mibs_folder: /path/to/your/additional/mibs

instances:
   -   ip_address: localhost  
       port: 161  
       community_string: public  
       tags:
            - optional_tag1
            - optional_tag2
       metrics:
            - MIB: UDP-MIB
              symbol: udpInDatagrams
            - OID: 1.3.6.1.2.1.6.5
              name: tcpPassiveOpens
            - MIB: IF-MIB
              table: ifTable
              symbols:
                - ifInOctets
                - ifOutOctets
              metric_tags:
                - tag: interface
                  column: ifDescr
```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认生效
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功

```
snmp
----
  - instance #0 [OK]
  - Collected 8 metrics & 0 events
```
