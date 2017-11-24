# 配置OpenStack
## 1.在身份认证服务器上创建角色和用户
```
openstack role create uyunAgent_monitoring
openstack user create uyunAgent --password my_password --project my_project_name
openstack role add uyunAgent_monitoring --project my_project_name --user uyunAgent
```

## 2.更新 policy.json 文件来获得必要的权限  
通过配置下列操作来让uyunAgent_monitoring用户获得必要权限
### nova
```
- "compute_extension:aggregates",
- "compute_extension:hypervisors",
- "compute_extension:server_diagnostics",
- "compute_extension:v3:os-hypervisors",
- "compute_extension:v3:os-server-diagnostics",
- "compute_extension:availability_zone:detail",
- "compute_extension:v3:availability_zone:detail",
- "compute_extension:used_limits_for_admin",
- "os_compute_api:os-aggregates:index",
- "os_compute_api:os-aggregates:show",
- "os_compute_api:os-hypervisors",
- "os_compute_api:os-hypervisors:discoverable",
- "os_compute_api:os-server-diagnostics",
- "os_compute_api:os-used-limits"
```

### Neutron
```
- "get_network"
```

### Keystone
```
- "identity:get_project"
- "identity:list_projects"
```
重启服务保证修改生效  

# 配置Agent
## 启用插件
1. windows环境
windows环境，通过窗口开启监控（Enable）。
2. linux 环境
在/etc/monitor-agent/conf.d中复制 openstack.yaml.example 为openstack.yaml
```
cp openstack.yaml.example openstack.yaml
```

## 配置插件
编辑配置文件 conf.d/openstack.yaml:
```
init_config:
      # All directives prefixed with a '#' sign are optional and will default to sane values when omitted

      # Where your identity server lives. Note that the server must support Identity API v3
      keystone_server_url: "https://my-keystone-server.com:/"

      # The hostname of this machine registered with Nova. Defaults to socket.gethostname()
      # os_host: my_hostname

      # Nova API version to use - this check supports v2 and v2.1 (default)
      # nova_api_version: 'v2.1'

      # IDs of Nova Hypervisors to monitor
      # This is only required when nova_api_version is set to `v2` since
      # indexing hypervsiors is restricted to `admin`s in Compute API v2
      # With v2.1, the check will intelligently discover the locally running
      # hypervisor, based on the hypervisor_hostname

      # hypervisor_ids:
      #    - 1

      # IDs of networks to exclude from monitoring
      # (by default the agent will collect metrics from networks returned by the neutron:get_networks operation)

      # exclude_network_ids:
      #    - network_1

      # Whether to enable SSL certificate verification for HTTP requests. Defaults to true, you may
      # need to set to false when using self-signed certs
      # ssl_verify: true

instances: # Each instance represents a single OpenStack project for the agent to monitor

    - name: instance_1 # A required unique identifier for this instance

      # The authorization scope that will be used to request a token from Identity API v3
      # The auth scope must resolve to 1 of the following structures:
      # {'project': {'name': 'my_project', 'domain': 'my_domain} OR {'project': {'id': 'my_project_id'}}
      auth_scope:
          project:
              id: my_project_id

          # Alternately

          # project:
          #     name: my_project_name
          #     domain:
          #         id: default


      # User credentials
      # Password authentication is the only auth method supported right now
      # User expects username, password, and user domain id

      # `user` should resolve to a structure like:
      # {'password': 'my_password', 'name': 'my_name', 'domain': {'id': 'my_domain_id'}}
      user:
          password: my_password
          name: uyunAgent
          domain:
              id: default

      # In some cases, the nova url is returned without the tenant id suffixed
      # e.g. http://172.0.0.1:8774 rather than http://172.0.0.1:8774/
      # Setting append_tenant_id to true manually adds this suffix for downstream requests
      # append_tenant_id: false

      # IDs of servers to exclude from monitoring
      # (by default the agent will collect metrics from all guest servers for this project that are running on the host

      # exclude_server_ids:
      #    - server_1
      #    - server_2
```
## 配置 RabbitMQ 服务

## 重启Agent
```
service datamonitor-agent restart
```

# 确认上报
通过info命令查看，验证配置是否成功。当出现以下信息，说明配置成功
```
openstack
---------
     - instance #0 [OK]
     - Collected 8 metrics & 0 events
```
