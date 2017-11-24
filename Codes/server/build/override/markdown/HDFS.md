
# 配置Agent

## 启用插件
1. windows环境

    windows环境，通过窗口开启监控(Enable)。

2. linux 环境

    在/etc/monitor-agent/conf.d中复制 hdfs_namenode.yaml.example 为 hdfs_namenode.yaml  
    复制 hdfs_namenode.yaml.example 为 hdfs_namenode.yaml

```	
cp hdfs_namenode.yaml.example hdfs_namenode.yaml  
cp hdfs_datanode.yaml.example hdfs_datanode.yaml
```

## 配置插件
编辑配置文件 conf.d/hdfs_namenode.yaml
```
init_config:

instances:
  #
  # The HDFS NameNode check retrieves metrics from the HDFS NameNode's JMX
  # interface. This check must be installed on the NameNode. The HDFS
  # NameNode JMX URI is composed of the NameNode's hostname and port.
  #
  # The hostname and port can be found in the hdfs-site.xml conf file under
  # the property dfs.http.address or dfs.namenode.http-address
  #
  -  hdfs_namenode_jmx_uri: http://localhost:50070
```
编辑配置文件 conf.d/hdfs_datanode.yaml
```
init_config:

instances:
  #
  # The HDFS DataNode check retrieves metrics from the HDFS DataNode's JMX
  # interface. This check must be installed on a HDFS DataNode. The HDFS
  # DataNode JMX URI is composed of the DataNode's hostname and port.
  #
  # The hostname and port can be found in the hdfs-site.xml conf file under
  # the property dfs.datanode.http.address
  #
  - hdfs_datanode_jmx_uri: http://localhost:50075
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
hdfs_datanode
-------------
- instance #0 [OK]
- Collected 8 metrics, 0 events & 2 service checks


hdfs_namenode
-------------
- instance #0 [OK]
- Collected 21 metrics, 0 events & 2 service checks

```
	
	

