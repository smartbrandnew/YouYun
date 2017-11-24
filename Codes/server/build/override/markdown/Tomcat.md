# 配置Tomcat
确认您的Tomcat服务器上已经开启了JMX远程端口
# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 tomcat.yaml.example 为tomcat.yaml
```
cp tomcat.yaml.example tomcat.yaml
```

# 配置插件
编辑配置文件 conf.d/tomcat.yaml

```
instances:
    -   host: localhost
        port: 7199
      #  user: username
      #  password: password
      #  name: tomcat_instance

  # List of metrics to be collected by the integration
init_config:
  conf:
    - include:
        type: ThreadPool
        attribute:
          maxThreads:
            alias: tomcat.threads.max
            metric_type: gauge
          currentThreadCount:
            alias: tomcat.threads.count
            metric_type: gauge
          currentThreadsBusy:
            alias: tomcat.threads.busy
            metric_type: gauge
    - include:
        type: GlobalRequestProcessor
        attribute:
          bytesSent:
            alias: tomcat.bytes_sent
            metric_type: counter
          bytesReceived:
            alias: tomcat.bytes_rcvd
            metric_type: counter
          errorCount:
            alias: tomcat.error_count
            metric_type: counter
          requestCount:
            alias: tomcat.request_count
            metric_type: counter
          maxTime:
            alias: tomcat.max_time
            metric_type: gauge
          processingTime:
            alias: tomcat.processing_time
            metric_type: counter
    - include:
        j2eeType: Servlet
        attribute:
          processingTime:
            alias: tomcat.servlet.processing_time
            metric_type: counter
          errorCount:
            alias: tomcat.servlet.error_count
            metric_type: counter
          requestCount:
            alias: tomcat.servlet.request_count
            metric_type: counter
    - include:
        type: Cache
        accessCount:
          alias: tomcat.cache.access_count
          metric_type: counter
        hitsCounts:
          alias: tomcat.cache.hits_count
          metric_type: counter
    - include:
        type: JspMonitor
        jspCount:
          alias: tomcat.jsp.count
          metric_type: counter
        jspReloadCount:
          alias: tomcat.jsp.reload_count
          metric_type: counter
```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
tomcat
------
      - instance #0 [OK]
      - Collected 8 metrics & 0 events
```
