# 概述
本项目是bat产品的采集代理项目，用于存放bat采集端的相关进程与源码。

# R10功能特性
|功能添加|   优化 |  兼容性 |
|:--:|:--:|:--:|:----:|
|oracle采集支持|  取消m-agent用户  |      取消gohai|
|             |  可以指定安装目录  |      支持RedHat5操作系统|
|             | 合并agent文件目录  |     支持turbo11.3 X64|

# 项目结构
```bash
├──                          /** 根目录下未归类文件: forwarder, monitorstatsd, jmxfetch Linux进程包装脚本，emitter, config, aggreator 等项目内部辅助方法、类实现，setup.py 构建脚本， 配置文件、readme、证书 等等...*/
    ├── checks               /** 基础进程源码*/
    │   ├── libs             /** 基础进程依赖的辅助类和jar包(jmxfetch)*/
    │   └── system           /** 类unix系统和win32系统基本指标监测源码*/
    └── ci                   /** ruby文件实现ruby budler下载安装官方开源项目指定的python依赖包*/
    ├── checks.d             /** 监测插件源码*/
    ├── conf.d               /** 监测插件配置(yaml)*/
    ├── monitorstream        /** 日志解析源码 弥补部分服务无法用监测插件实现*/
    ├── gohai                /** win agent所需的gohai.exe文件用于采集本机基本数据*/
    ├── linux_build          /** uyun实现的linux agent打包相关*/
    ├── packaging            /** 各平台 agent 构建相关*/
    │   ├── centos           /** centos 构建相关*/
    │   ├── datamonitor-agent/** smartos source win32 平台构建相关文件*/
    │   │   ├── smartos      /** smartos构建打包相关文件*/
    │   │   ├── source       /** source构建打包相关文件*/
    │   │   └── win32        /** win32构建打包相关文件*/
    │   └── debian           /** debian构建打包相关文件*/
    │   ├── osx              /** osx构建打包相关文件*/
    └── resources            /** 不理解此文件夹作用 官方最新源码结构中已将此废弃*/
    ├── scripts              /** 运行用户自定义脚本的脚本存放文件夹 内附示例*/
    ├── tests                /** 单元测试*/
    │   ├── checks           /** checks.d/* 扩展插件的测试相关文件*/
    │   │   ├── integration  /** 包含所有真实集成测试 (运行于 Travis/Appveyor)*/
    │   │   ├── mock         /** 包含所有mock测试 (运行于 Travis)*/
    │   │   └── fixtures     /** 测试所需文件 (配置文件, mocks, ...)*/
    │   └── core             /** agent核心测试 (单元测试和集成测试, 运行于Travis)*/
    │   │   └── fixtures     /** 测试所需文件*/
    └── utils                /** 项目自己的辅助python包*/
    ├── uyun                 /** uyun扩展的python包*/
    │   ├── bat              /** bat项目*/
    │   │   ├── net_eqip     /** 网络设备python包*/
    │   │   ├── ping         /** 支持并发提交的ping python包*/
    │   └─ └── snmp          /** 对pysnmp再包装的python包*/
    └── win32                /** win32平台进程管理器脚本，gui界面脚本等等*/
```


# agent类型
redhat，centos， fedora，Amazon linux    共用centos构建出的包
ubuntu  和debian 共用debian构建出的包
