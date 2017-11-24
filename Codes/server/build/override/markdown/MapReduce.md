# 配置Agent
## 启用插件
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制 mapreduce.yaml.example 为mapreduce.yaml
```
cp mapreduce.yaml.example mapreduce.yaml
```

## 配置插件
编辑配置文件 conf.d/mapreduce.yaml
```
instances:
  # The MapReduce check retrieves metrics from YARN's ResourceManager. This
  # check must be run from the Master Node and the ResourceManager URI must
  # be specified below. The ResourceManager URI is composed of the
  # ResourceManager's hostname and port.
  # The ResourceManager port can be found in the yarn-site.xml conf file under
  # the property yarn.resourcemanager.webapp.address
  - resourcemanager_uri: http://localhost:8088

init_config:
 general_counters:
    - counter_group_name: 'org.apache.hadoop.mapreduce.TaskCounter'
      counters:
        - counter_name: 'MAP_INPUT_RECORDS'
        - counter_name: 'MAP_OUTPUT_RECORDS'
        - counter_name: 'REDUCE_INPUT_RECORDS'
        - counter_name: 'REDUCE_OUTPUT_RECORDS'

    # Additional counter's can be specified as following
    # - counter_group_name: 'org.apache.hadoop.mapreduce.FileSystemCounter'
    #   counters:
    #     - counter_name: 'HDFS_BYTES_READ'

```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
mapreduce
---------
     - instance #0 [OK]
     - Collected 8 metrics , 0 events & 2 service checks

```
