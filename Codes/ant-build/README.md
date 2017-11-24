# ⚡ Ant-Build
用来构建Dispatcher,agent,manager及插件。

## 使用

使用build cli执行命令:

```
python bin/build --build-project=<project_name> --branch=<branch>  [--system=<build_system>] [--with-module=<jre,namp,agent,discovery>]  [--noencrypt]
```

执行命令查看帮助：

```
python bin/build -h
```

##### 1.  agent打包:

 - 打包windows64位agent：
 
```
bin/build --build-project=agent --branch=release/V2.0.R12.x --system=windows64
```

 - 打包windows32位agent：
 
```
bin/build --build-project=agent --branch=release/V2.0.R12.x  --system=windows32
```

 - 打包linux64位agent：
 
```
bin/build --build-project=agent --branch=release/V2.0.R12.x  --system=linux64
```

##### 2. 打包discovery：

```
bin/build --build-project=discovery --branch=release/2.1.x
```

##### 3. dispatcher打包:

```
bin/build --build-project=dispatcher --branch=release/V2.0.R12.x --system=linux64 --with-module=agent,jre,nmap,discovery
```
##### 4. 打包nginx,node,python

```
bin/build --build-project=nginx --branch=release/V2.0.R12.x  --system=linux64
```

```
bin/build --build-project=node --branch=release/V2.0.R12.x --system=linux64
```

```
bin/build --build-project=python --branch=release/V2.0.R12.x --system=linux64
```

##### 5. 打包manager：
   
```
bin/build --build-project=manager --branch=master --set-version=release/V2.0.R12.x  --system=linux64
```

 
# docker打包
## manager打包
1. 将manager打包产物、manager_init.sh、manager_Dockerfile移动到一个空目录
2. 进入目录执行

```bash
docker build --build-arg VERSION=$version -t platform/ant-manager:$version ./
```

## dispatcher打包
1. dispatcher、node、python、nginx、dispatcher_Dockerfile、dispatcher_init.sh 6个文件移动到一个空目录
2. 进入目录执行

```bash
docker build --build-arg VERSION=$version -t platform/ant-dispatcher:$version ./
```

## 打标签
```bash
docker tag platform/ant-manager:$version dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker tag platform/ant-dispatcher:$version dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag
```


## 上传服务器
```bash
docker push dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker push dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag
```

## 镜像下载
```bash
docker pull dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker pull dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag
```
## 运行容器
```bash
docker run -d -e JAVA_OPTS="-Xms128m -Xmx1024m -Ddisconf.conf_server_host=xxx -Ddisconf.env=xxx -Ddisconf.version=xxx" --name ant-manager -p 7595:7595 dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker run -d -e DISCONF_HOST="xxx" -e  DISCONF_PORT="xxx" -e DISCONF_URL DECRYPT_URL="xxx" --name ant-dispatcher -p 7599:7599 dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag
```

## 快捷方式
分别执行：
```
sh docker_build.sh R12
```

```
sh docker_run.sh R12
```

执行脚本的参数为安装包对应的版本(R12,R11等)
