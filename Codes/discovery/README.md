# ⚡ Hawk-Discovery
Discovery插件，用于Ant体系中agent上本地和远程的资源发现。

## 依赖包安装
### pypi源
公司私有pypi源地址为http://10.1.100.100:3141/ ，使用前必须修改本机的pip配置文件
+ Windows 在C:\Users\Administrator\pip\新建或修改pip.ini文件
（Administrator是指当前系统用户）
+ Linux 在 ~/.pip/ 下新建或者修改文件pip.conf

### pip配置
```bash
[global]
index_url = http://10.1.100.100:3141/root/dev/+simple/
[search]
index = http://10.1.100.100:3141/root/dev/
[install]
trusted-host=10.1.100.100
```
### 安装依赖
+ Windows
```bash
pip install -r requirements\production.txt
```
+ Linux
```bash
pip install -r requirements\production.txt
```

## 运行方式
1 启动discoverer插件。进入discovery，执行
```bash
python -m discovery.plugin.main
```

2 使用cli执行命令。执行命令查看帮助：
```bash
python bin/dis -h
```
