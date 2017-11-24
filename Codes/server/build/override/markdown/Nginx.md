# 配置Nginx
确认您已经安装了Nginx的[http stub status](http://nginx.org/en/docs/http/ngx_http_stub_status_module.html)模块。
# 配置Agent
## 启用插件
 1. windows环境
windows环境，通过窗口开启监控（Enable）。
 2. linux 环境
在/etc/monitor-agent/conf.d中复制 nginx.yaml.example 为nginx.yaml

```
cp nginx.yaml.example nginx.yaml
```

## 配置插件
编辑配置文件 conf.d/nginx.yaml

```
init_config:

instances:
    # For every instance, you have an `nginx_status_url` and (optionally)
    # a list of tags.

    -   nginx_status_url: http://example.com/nginx_status/
        tags:
            -   instance:foo

    -   nginx_status_url: http://example2.com:1234/nginx_status/
        tags:
            -   instance:bar
```

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过service datamonitor-agent info命令查看，验证配置是否成功。当出现以下信息，说明配置成功

```
nginx
-----
      - instance #0 [OK]
      - Collected 8 metrics & 0 events
```
