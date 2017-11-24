# 配置SQL Server
确认您的SQL Server实例支持“SQL Server and Windows Authentication mode”，该属性位于服务器属性中  
创建一个只读用户去连接您的服务器  

```   
CREATE LOGIN uyunAgent WITH PASSWORD = '123456';  
CREATE USER uyunAgent FOR LOGIN uyunAgent;  
GRANT SELECT on sys.dm_os_performance_counters to uyunAgent;  
GRANT VIEW SERVER STATE to uyunAgent;
```  

# 配置Agent
## 启用插件
windows环境，通过窗口开启监控（Enable）。
## 配置插件
配置agent连接您的数据库服务器  
在agent管理器中编辑“sqlserver”配置文件，添加这个服务器到实例中

```
instances:
    -   host: MY_HOST,MY_PORT
        username: uyunAgent
        password: 123456
```

请确认将MY_HOST和MY_PORT修改成自己的主机和端口号，默认的主机号和端口号是127.0.0.1，1433.  

## 重启Agent
windows环境，通过窗口开启监控（Restart）。

# 确认上报
在agent管理器的service datamonitor-agent info页面看到下列信息说明配置成功
```
sqlserver  
      - instance #0 [OK]
      - Collected 8 metrics & 0 events
```
