# 配置Postfix
确认运行Agent的用户拥有管理员权限执行find命令
# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 postfix.yaml.example 为postfix.yaml
```
cp postfix.yaml.example postfix.yaml
```

## 配置插件
编辑配置文件 conf.d/postfix.yaml:

```
# The user running monitor-agent must have passwordless sudo access for the find
# command to run the postfix check.  Here's an example:
#
# example /etc/sudoers entry:
#          monitor-agent ALL=(ALL) NOPASSWD:/usr/bin/find
  init_config:
  instances:
    - directory: /var/spool/postfix
      queues:
          - incoming
          - active
          - deferred
      tags:
          - optional_tag1
          - optional_tag2
    - directory: /var/spool/postfix-2
      queues:
          - incoming
          - active
          - deferred
      tags:
          - optional_tag3
          - optional_tag4
```
## 重启 Agent
```
service datamonitor-agent restart
```

# 确认上报
通过info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
postfix
-------
     - instance #0 [OK]
     - Collected 8 metrics & 0 events
```
