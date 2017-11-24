# 配置WMI Integration

# 配置Agent
## 启用插件
windows环境，通过窗口开启监控（Enable）。
## 配置插件
编辑Wmi Check.yaml,添加所有你想要收集的指标

```
instances:
    # Each WMI query has 2 required options, `class` and `metrics` and four
    # optional options, `filters`, `tag_by`, `constant_tags` and `link_tag`.
    #
    # `class` is the name of the WMI class, for example Win32_OperatingSystem
    # or Win32_PerfFormattedData_PerfProc_Process. You can find many of the
    # standard class names on the MSDN docs at
    # http://msdn.microsoft.com/en-us/library/windows/desktop/aa394084.aspx.
    # The Win32_FormattedData_* classes provide many useful performance counters
    # by default.
    #
    #
    # `metrics` is a list of metrics you want to capture, with each item in the
    # list being a set of [WMI property name, metric name, metric type].
    #
    # - The property name is something like `NumberOfUsers` or `ThreadCount`.
    #   The standard properties are also available on the MSDN docs for each
    #   class.
    #
    # - The metric name is the name you want to show up in Datamonitor.
    #
    # - The metric type is from the standard choices for all agent checks, such
    #   as gauge, rate, histogram or counter.
    #
    #
    # `filters` is a list of filters on the WMI query you may want. For example,
    # for a process-based WMI class you may want metrics for only certain
    # processes running on your machine, so you could add a filter for each
    # process name. See below for an example of this case.
    #
    #
    # `tag_by` optionally lets you tag each metric with a property from the
    # WMI class you're using. This is only useful when you will have multiple
    # values for your WMI query. The examples below show how you can tag your
    # process metrics with the process name (giving a tag of "name:app_name").
    #
    #
    # Setting this will cause any instance number to be removed from tag_by values
    # Params are:
    #   property of source that contains the link value
    #   class to link to
    #   property of target class to link to
    #   property of target class that contains the value to tag with


    # Fetch the number of processes and users
    - class: Win32_OperatingSystem
    metrics:
      - [NumberOfProcesses, system.proc.count, gauge]
      - [NumberOfUsers, system.users.count, gauge]
```

## 重启Agent
windows环境，通过窗口开启监控（Restart）。

# 确认上报
通过service datamonitor-agent info页面查看，验证配置是否成功。当出现以下信息，说明配置成功
```
wmi
-------
      - instance #0 [OK]
      - instance #1 [OK]
      - instance #2 [OK]
      - instance #3 [OK]
      - Collected 2 metrics, 0 events and 1 service check
```
