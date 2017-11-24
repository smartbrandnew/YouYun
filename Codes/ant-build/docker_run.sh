
#!/bin/bash
version=$1
tag="V2.0.$version.${BUILD_NUMBER}"
disconf_host=10.1.51.101
disconf_port=8081
host=0.0.0.0
disconf_url="/api/config/file?app=uyun&env=rd&version=2_0_0&key=common.properties"
java_opts="-Xms128m -Xmx1024m -Ddisconf.conf_server_host=${disconf_host}:${disconf_port} -Ddisconf.env=rd -Ddisconf.app=uyun -Ddisconf.version=2_0_0"

docker pull dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker pull dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag

docker ps -a|grep 'platform/ant-dispatcher'|awk '{print "docker rm -f "$1 }'|sh
docker ps -a|grep 'platform/ant-manager'|awk '{print "docker rm -f "$1 }'|sh
docker run  -d -e JAVA_OPTS="${java_opts}" -p 7595:7595 --name ant-manager dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker run  -d -e DISCONF_HOST="${disconf_host}" -e HOST="${host}" -e DISCONF_URL="${disconf_url}" -e DISCONF_PORT="${disconf_port}" -p 7599:7599 -p 7597:7597 --name ant-dispatcher dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag