
# 配置ActiveMQ

1. 在ActiveMQ的配置文件activemq.xml，添加以下代码
```
    	 <managementContext>
    	      <managementContext connectorPort="1099" createConnector="true"/>
    	 </managementContext>
```
2. 开启ActiveMQ的服务

# 配置Agent

## 启用插件

 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
使用activemq_xml.yaml文件监控，需要配置如下

``` yaml
init_config:
instances:
url: http://10.1.53.98:8161
the url will probably be something like http://<hostname>:8161
the agent check will append /admin/xml/queues.jsp to the url
username: admin
password: admin
``` 

## 配置插件
如果你使用ActiveMQ版本小于5.8 编辑配置文件conf.d/activemq.yaml

```
init_config:
instances:
 - host: localhost
post：8161
```     
如果你使用ActiveMQ版本号大于等于5.8，编辑配置文件conf.d/activemq_58.yaml

```
init_config:
instances:
- host: localhost
post：8161
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
 activemq_58
      Instance #activemq_58-localhost-8161
           OK 
     Collected 17 metrics, 0 events and 0 service check 
```