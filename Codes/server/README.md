# 概述
本项目包含monitor服务端各模块。

# 环境
1. 操作系统：
	1. centos7 64位
	1. windows7 64位（目前没有提供windows版本的运行shell）
1. jdk：1.8.0_77_x64
1. maven-3.3.9
1. mysql：需要确定其服务ip与port
	1. 默认为127.0.0.1:3306
1. kairosdb：需要确定其服务ip与telnet port、http port
	1. 默认为127.0.0.1 4242 8080

# 开发
## 外部环境
1. 由于一些模块是需要依赖外部环境的，因此在单元测试时，请修改以下文件：<br>
server\build\src\main\resources\conf\config.properties<br>
来提供：mysql.ip, mysql.port, kairosdb.ip， kairosdb.http.port, kairosdb.telnet.port 的属性值
2. 由于使用MQ来作为指标数据缓冲队列，当kairosdb异常时，可能会导致大量的数据在队列中，造成mq异常，因此需要监控MQ的队列长度，删除一些旧的消息。
需要在Active MQ里作以下配置：
  1、修改activeMQ/conf目录下的activemq.xml文件。
     在<broker/>标签内做修改，修改brokerName="ip地址"，加上useJmx="true"。
	 示例： <broker xmlns="http://activemq.apache.org/schema/core" brokerName="10.1.10.46" useJmx="true" dataDirectory="${activemq.data}">,其中10.1.10.46是我的测试MQ地址
  2、修改activeMQ/conf目录下的activemq.xml文件。
     在 <managementContext/>标签内修改createConnector="true"，加上connectorHost="ip地址" connectorPort="11099"
	 示例：<managementContext>
            <managementContext createConnector="true" connectorHost="10.1.10.46" connectorPort="11099"/>
        </managementContext>
  3、重启ActiveMQ，进行构建
     注意11099为强制规定的端口号，请不要修改，若必须要修改，请同时修改monitor/conf/config.properties文件中的activemq.jmx.port值，保持一致。
  参考：http://www.uyunsoft.cn/kb/pages/viewpage.action?pageId=11470106
# 构建

## 构建步骤
1. git clone https://git.uyunsoft.cn/bat/server.git
1. cd server
1. mvn package
    1. mvn package -Dmysql.ip=1.1.1.2 -Dmysql.port=3306 -Dkairosdb.ip=1.1.1.2 -Dkairosdb.http.port=8080 -Dkairosdb.telnet.port=4242
	1. 完成后在dist目录可看到生成的内容
1. 自信构建（不进行单元测试） -Dmaven.test.skip=true 

## 常用操作
1. 清除
	1. mvn clean --settings ./settings.xml
1. 构建
	1. mvn package -U --settings ./settings.xml
1. 发布到本地（感觉服务端不需要发布，仅api工程需要发布,q）
	1. mvn install -U --settings ./settings.xml
1. 发布到快照仓库（感觉服务端不需要发布，仅api工程需要发布，需要修改对应的发布版本）
	1. mvn deploy -U --settings ./settings.xml
1. 发布正式仓库(修改需要发布的api模块的pom.xml中的版本,和快照的区别是版本号不带-SNAPSHOT)
	1. mvn deploy -U -P release --settings ./settings.xml

# 运行
## 外部环境
1. 修改conf/config.properties，提供外部环境参数来进行连接

# 目录结构
	|-build
		|-src 搜集所有子模块源码
		|-target
			|-classes 收集所有子模块classes文件
			|-coverage-reports 收集所有子模块jcoco产出
			|-coverage-merged 聚合收集到的jacoco产出
	|-XX模块（多个）
	|-dist 打包产出物
		|-bin
		|-XX模块（多个）
		|-lib 公共类库
		|-jre


	maven父子情况（简介）

	父 bat(server\pom.xml)   包含全局的文件依赖
			子(server\build\pom.xml) 继承父亲依赖
			子(server\dashboard\pom.xml)  包含局部附加依赖
			子(server\gateway\pom.xml)  调用子模块的pom.xml进行构建
					孙子(server\gateway\api\pom.xml) 不继承父亲依赖
					孙子(server\gateway\dd-agent\pom.xml) 继承父亲依赖
					孙子(server\gateway\env\pom.xml) 继承父亲依赖

# 注意
1. 第一个版本正式发布后（相应的api包移到正式仓库后，修改各个依赖api的pom.xml，将依赖版本修改为确定值）
1. 版本更新
	1. 依赖api包的工程，请自行处理依赖的api的版本，若api的版本升级，需要自己重新发布对应版本的api
	1. 待api更新发布后，这样才能成功构建

# 关于windows下检出的sh文件格式为windows而非unix

```
到根目录下,可以用git bash运行命令替换
find . -name "*.sh" | xargs -n 1 -i dos2unix {}
```

# 容器相关

## Docker环境安装

	docker 安装  http://10.1.2.218/kb/pages/viewpage.action?pageId=8946915
	dockerhub  配置  http://10.1.2.218/kb/pages/viewpage.action?pageId=11470176

## 下载相关镜像

    sudo docker pull mysql
    sudo docker pull registry.aliyuncs.com/daydayup/activemq
    sudo docker pull registry.aliyuncs.com/daydayup/cassandra
    sudo docker pull registry.aliyuncs.com/daydayup/kairosdb
    sudo docker pull registry.aliyuncs.com/daydayup/zookeeper
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-datastore:v2.0.0 
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-event:v2.0.0 
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-monitor:v2.0.0 
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-dashboard:v2.0.0 
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-favourite:v2.0.0 
    sudo docker pull dockerhub.uyuntest.cn:5000/bat-web:v2.0.0      

## 运行相关镜像

Step1 设置本机IP地址

	export DEBUG_HOST_IP=1.1.1.2
	export WHALE_CONFIG_IP=1.1.1.2
	# export BIRD_HOME_URL=https:\\/\\/uyun.cn
	export BIRD_HOME_URL=http:\\/\\/uyun-jjw.cn
	export BAT_IMAGE_VER=1.160608

Step2 设置环境，修改IP地址，并选择以下一种

	# 开发者模式，只使用本地配置
    export JAVA_OPTS="-Ddeveloper.mode=true -Ddisconf.enable.remote.conf=false -Dserver.ip=$DEBUG_HOST_IP -Dmysql.port=3306 -Dzookeeper.port=2181 -Dkairosdb.telnet.port=4242 -Dkairosdb.http.port=8080 -Dcassandra.rpc.port=9160 -Djms.port=61616"
    # 开发者模式，如果有远程，则使用远程配置
    export JAVA_OPTS="-Ddeveloper.mode=true -Ddisconf.enable.remote.conf=true -Ddisconf.conf_server_host=$WHALE_CONFIG_IP:8080"
    # 正式模式，如果有远程，则使用远程配置
    export JAVA_OPTS="-Ddeveloper.mode=false -Ddisconf.enable.remote.conf=true -Ddisconf.conf_server_host=$DEBUG_HOST_IP:8080"

Step3 复制并执行以下所有命令

    sudo docker run -d -p 9160:9160 --name=cassandra registry.aliyuncs.com/daydayup/cassandra
    sleep 10
    sudo docker run -d -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 --name=mysql mysql
    sudo docker run -d -p 61616:61616 --name=activemq registry.aliyuncs.com/daydayup/activemq
    sudo docker run -d -p 2181:2181 --name=zookeeper registry.aliyuncs.com/daydayup/zookeeper
    sleep 10
    sudo docker run -d -p 4242:4242 -p 8080:8080 -e CASSANDRA_HOSTS=$DEBUG_HOST_IP:9160 --name=kairosdb registry.aliyuncs.com/daydayup/kairosdb

Step4 初始化数据库：

	sudo docker run -it -e JAVA_OPTS="${JAVA_OPTS}" dockerhub.uyuntest.cn:5000/bat-console:${BAT_VER}

Step5 启动各镜像

    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -p 7309:7309 --name=bat-datastore dockerhub.uyuntest.cn:5000/bat-datastore:${BAT_IMAGE_VER}
    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -p 7304:7304 --name=bat-event dockerhub.uyuntest.cn:5000/bat-event:${BAT_IMAGE_VER}
    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -p 7306:7306 --name=bat-monitor dockerhub.uyuntest.cn:5000/bat-monitor:${BAT_IMAGE_VER}
    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -p 7303:7303 --name=bat-dashboard dockerhub.uyuntest.cn:5000/bat-dashboard:${BAT_IMAGE_VER}
    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -p 7308:7308 --name=bat-favourite dockerhub.uyuntest.cn:5000/bat-favourite:${BAT_IMAGE_VER}
    sudo docker run -d -e JAVA_OPTS="${JAVA_OPTS}" -e BIRD_HOME_URL="${BIRD_HOME_URL}" -p 7301:7301 -p 7310:7310 -p 7380:7380 --name=bat-web dockerhub.uyuntest.cn:5000/bat-web:${BAT_IMAGE_VER}

## 上传相关镜像

    sudo docker push dockerhub.uyuntest.cn:5000/bat-console:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-datastore:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-event:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-monitor:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-dashboard:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-favourite:${BAT_IMAGE_VER}
    sudo docker push dockerhub.uyuntest.cn:5000/bat-web:${BAT_IMAGE_VER}
