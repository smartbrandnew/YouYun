--version 1.0.0,build 2016-04-14 bat 系统版本信息初始化
--
--import#bat_init.sql#1.0.0,build 2016-04-14#1.0.0,build 2016-04-14
--
--version 1.0.0,build 2016-04-16 bat表初始化
--
--import#bat_function.sql#1.0.0,build 2016-04-16#1.0.0,build 2016-04-16
--import#bat_create.sql#1.0.0,build 2016-04-16#1.0.0,build 2016-04-16
--
--version 1.0.0,build 2016-04-19 dashwindow表初始化
--
--import#bat_create.sql#1.0.0,build 2016-04-19#1.0.0,build 2016-04-19
--
--version 1.0.0,build 2016-04-20 favorite_dashboard表初始化
--
--import#bat_create.sql#1.0.0,build 2016-04-20#1.0.0,build 2016-04-20
--
--version 1.0.0,build 2016-04-21 monitor表初始化
--
--import#bat_create.sql#1.0.0,build 2016-04-21#1.0.0,build 2016-04-21
--
--version 1.0.0,build 2016-04-25 指标名初始化
--
--import#bat_create.sql#1.0.0,build 2016-04-25#1.0.0,build 2016-04-25

--version 1.0.0,build 2016-04-27 资源模块初始化
--
--import#bat_create.sql#1.0.0,build 2016-04-27#1.0.0,build 2016-04-27

--version 1.0.0,build 2016-04-29 资源模块更新
--
--import#bat_create.sql#1.0.0,build 2016-04-29#1.0.0,build 2016-04-29

--version 1.0.0,build 2016-05-03 更新resource modified字段为datetime类型
--
--import#bat_create.sql#1.0.0,build 2016-05-03#1.0.0,build 2016-05-03

--import#bat_create.sql#1.0.0,build 2016-05-05#1.0.0,build 2016-05-05

--version 1.0.0,build 2016-05-09 增加resource表last_collect、online_status、alert_status字段
--
--import#bat_create.sql#1.0.0,build 2016-05-09#1.0.0,build 2016-05-09

--version 1.0.0,build 2016-05-10 修改res_tag表主键，调整tagk只对应唯一tagv
--
--import#bat_create.sql#1.0.0,build 2016-05-10#1.0.0,build 2016-05-10

--version 1.0.0,build 2016-05-16 增加resource表res_app、res_tag列
--
--import#bat_create.sql#1.0.0,build 2016-05-16#1.0.0,build 2016-05-16

--version 1.0.0,build 2016-05-20 增加event表fault_id、identity列修改monitor类型增加故障、故障历史表
--
--import#bat_create.sql#1.0.0,build 2016-05-20#1.0.0,build 2016-05-20

--import#bat_create.sql#1.0.0,build 2016-05-24#1.0.0,build 2016-05-24

--version 1.0.0,build 2016-05-25 增加指标元数据
--
--import#bat_data.sql#1.0.0,build 2016-05-25#1.0.0,build 2016-05-25

--version 1.0.0,build 2016-05-27 重新调整状态相关表
--import#bat_create.sql#1.0.0,build 2016-05-27#1.0.0,build 2016-05-27

--version 1.0.0,build 2016-05-28 增加metric_resource表
--import#bat_create.sql#1.0.0,build 2016-05-28#1.0.0,build 2016-05-28

--version 1.0.0,build 2016-05-28 修改event表标题和内容长度
--import#bat_create.sql#1.0.0,build 2016-06-02#1.0.0,build 2016-06-02

--version 1.0.0,build 2016-06-23 metric_resource表增加tenant_id字段
--import#bat_create.sql#1.0.0,build 2016-06-23#1.0.0,build 2016-06-23


--version 1.0.0,build 2016-06-28 增加资源详情表
--import#bat_create.sql#1.0.0,build 2016-06-28#1.0.0,build 2016-06-28

--version 1.0.0,build 2016-06-29 修改事件identity字段
--import#bat_create.sql#1.0.0,build 2016-06-29#1.0.0,build 2016-06-29

--version 1.0.0,build 2016-06-30 升级state, tag相关uuid
--import#bat_create.sql#1.0.0,build 2016-06-30#1.0.0,build 2016-06-30

--version 1.0.0,build 2016-06-31 更新system.mem.pct_usable指标的精度
--import#bat_create.sql#1.0.0,build 2016-06-31#1.0.0,build 2016-06-31

--version 1.0.0,build 2016-07-08 优化事件台查询
--import#bat_create.sql#1.0.0,build 2016-07-08#1.0.0,build 2016-07-08

--version 1.0.0,build 2016-07-13 修改时间字段类型
--import#bat_create.sql#1.0.0,build 2016-07-13#1.0.0,build 2016-07-13

--version 1.0.0,build 2016-07-18 resource表增加create_time字段
--import#bat_create.sql#1.0.0,build 2016-07-18#1.0.0,build 2016-07-18

--version 1.0.0,build 2016-07-19 仪表盘和监测器增加创建时间字段
--import#bat_create.sql#1.0.0,build 2016-07-19#1.0.0,build 2016-07-19

--version 1.0.0,build 2016-07-20 resource表create_time字段赋值
--import#bat_create.sql#1.0.0,build 2016-07-20#1.0.0,build 2016-07-20

--version 1.0.0,build 2016-07-22 dashwindow表增加line_datas字段
--import#bat_create.sql#1.0.0,build 2016-07-22#1.0.0,build 2016-07-22

--version 1.0.0,build 2016-07-23 变更监测器表数据模型
--import#bat_create.sql#1.0.0,build 2016-07-23#1.0.0,build 2016-07-23

--version 1.0.0,build 2016-07-26 BAT-319去除过期的apps,增加状态修改时间
--import#bat_create.sql#1.0.0,build 2016-07-26#1.0.0,build 2016-07-26
--初始化4、5级元数据
--import#bat_data.sql#1.0.0,build 2016-07-26#1.0.0,build 2016-07-26

--version 1.0.0,build 2016-07-27 处理apps升级失败问题
--import#bat_create.sql#1.0.0,build 2016-07-27#1.0.0,build 2016-07-27
--初始化oracle和网络设备的指标
--import#bat_data.sql#1.0.0,build 2016-07-27#1.0.0,build 2016-07-27

--version 1.0.0,build 2016-08-17 指标元数据增加中文名和中文描述
--import#bat_create.sql#1.0.0,build 2016-08-17#1.0.0,build 2016-08-17
--import#bat_data.sql#1.0.0,build 2016-08-17#1.0.0,build 2016-08-17

--version 1.0.0,build 2016-08-23 去除过期的app名字
--import#bat_create.sql#1.0.0,build 2016-08-23#1.0.0,build 2016-08-23

--version 1.0.0,build 2016-09-01 修改错误的指标单位
--import#bat_create.sql#1.0.0,build 2016-09-01#1.0.0,build 2016-09-01

--version 1.0.0,build 2016-09-27 修改错误的指标单位null/XX问题
--import#bat_data.sql#1.0.0,build 2016-09-27#1.0.0,build 2016-09-27

--version 1.0.0,build 2016-10-10 补充与进程相关的指标元数据并定义单位
--import#bat_data.sql#1.0.0,build 2016-10-10#1.0.0,build 2016-10-10

--version 1.0.0,build 2016-10-17 补充一些用户更直观的指标
--import#bat_data.sql#1.0.0,build 2016-10-17#1.0.0,build 2016-10-17

--version 1.0.0,build 2016-10-24 补充一些用户更直观的指标
--import#bat_data.sql#1.0.0,build 2016-10-24#1.0.0,build 2016-10-24

--version 1.0.0,build 2016-10-31 BAT-495增加业务指标元数据
--import#bat_data.sql#1.0.0,build 2016-10-31#1.0.0,build 2016-10-31

--version 1.0.0,build 2016-11-02 增加sqlserver指标元数据
--import#bat_data.sql#1.0.0,build 2016-11-02#1.0.0,build 2016-11-02

--version 1.0.0,build 2016-11-04 添加状态资源对应表
--import#bat_create.sql#1.0.0,build 2016-11-04#1.0.0,build 2016-11-04

--version 1.0.0,build 2016-11-07 resource表增加os字段
--import#bat_create.sql#1.0.0,build 2016-11-07#1.0.0,build 2016-11-07

--version 1.0.0,build 2016-11-18 resource表增加user_tag字段
--import#bat_create.sql#1.0.0,build 2016-11-18#1.0.0,build 2016-11-18

--version 1.0.0,build 2016-11-29 删除mysql模板中的system相关仪表
--import#bat_data.sql#1.0.0,build 2016-11-29#1.0.0,build 2016-11-29

--version 1.0.0,build 2016-12-02 更正元数据
--import#bat_data.sql#1.0.0,build 2016-12-02#1.0.0,build 2016-12-02

--version 1.0.0,build 2016-12-07 BAT-724
--import#bat_data.sql#1.0.0,build 2016-12-07#1.0.0,build 2016-12-07

--version 1.0.0,build 2016-12-08 BAT-724
--import#bat_data.sql#1.0.0,build 2016-12-08#1.0.0,build 2016-12-08

--version 1.0.0,build 2016-12-10 采集集中配置与监测器增加自愈字段
--import#bat_create.sql#1.0.0,build 2016-12-06#1.0.0,build 2016-12-10

--version 1.0.0,build 2016-12-13 修改system模板展现
--import#bat_data.sql#1.0.0,build 2016-12-13#1.0.0,build 2016-12-13

--version 1.0.0,build 2016-12-14 去除不用的相关event表
--import#bat_create.sql#1.0.0,build 2016-12-14#1.0.0,build 2016-12-14

--version 1.0.0,build 2016-12-16 增加integration
--import#bat_create.sql#1.0.0,build 2016-12-16#1.0.0,build 2016-12-16
--import#bat_data.sql#1.0.0,build 2016-12-16#1.0.0,build 2016-12-16

--version 1.0.0,build 2016-12-17 增加probe元数据
--import#bat_data.sql#1.0.0,build 2016-12-17#1.0.0,build 2016-12-17

--version 1.0.0,build 2016-12-22
--import#bat_data.sql#1.0.0,build 2016-12-22#1.0.0,build 2016-12-22
--import#bat_create.sql#1.0.0,build 2016-12-22#1.0.0,build 2016-12-22

--version 1.0.0,build 2016-12-25 增加监测器自愈表
--import#bat_create.sql#1.0.0,build 2016-12-25#1.0.0,build 2016-12-25

--version 1.0.0,build 2016-12-29 增加指标
--import#bat_data.sql#1.0.0,build 2016-12-28#1.0.0,build 2016-12-29

--version 1.0.0,build 2017-01-07 修改system模板
--import#bat_data.sql#1.0.0,build 2017-01-07#1.0.0,build 2017-01-07

--version 1.0.0,build 2017-01-17 增加agentless_tag字段
--import#bat_create.sql#1.0.0,build 2017-01-17#1.0.0,build 2017-01-17

--version 1.0.0,build 2017-01-18 修改res_detail表detail字段为text
--import#bat_create.sql#1.0.0,build 2017-01-18#1.0.0,build 2017-01-18

--version 1.0.0,build 2017-02-09 删除不用的序列表、相关方法以及创建总览表
--import#bat_function.sql#1.0.0,build 2017-02-09#1.0.0,build 2017-02-09
--import#bat_create.sql#1.0.0,build 2017-02-09#1.0.0,build 2017-02-09

--version 1.0.0,build 2017-02-14 增加租户资源模板
--import#bat_create.sql#1.0.0,build 2017-02-14#1.0.0,build 2017-02-14

--version 1.0.0,build 2017-02-20 修改模板的磁盘仪表
--import#bat_data.sql#1.0.0,build 2017-02-20#1.0.0,build 2017-02-20

--version 1.0.0,build 2017-02-22 修改dashboard表结构，增加区分仪表盘还是资源详情的区分
--import#bat_create.sql#1.0.0,build 2017-02-22#1.0.0,build 2017-02-22

--version 1.0.0,build 2017-03-02 多了制表符
--import#bat_data.sql#1.0.0,build 2017-03-02#1.0.0,build 2017-03-02

--version 1.0.0,build 2017-03-06 统一资源库与报表
--import#bat_create.sql#1.0.0,build 2017-02-24#1.0.0,build 2017-03-20

--version 1.0.0,build 2017-03-23 BAT-1050
--import#bat_data.sql#1.0.0,build 2017-03-23#1.0.0,build 2017-03-23

--version 1.0.0,build 2017-03-24 报表增加日期与报表模板唯一约束
--import#bat_create.sql#1.0.0,build 2017-03-24#1.0.0,build 2017-03-24

--version 1.0.0,build 2017-03-27 报表字段修改 元数据区分agent agentless
--import#bat_create.sql#1.0.0,build 2017-03-27#1.0.0,build 2017-03-27
--import#bat_data.sql#1.0.0,build 2017-03-27#1.0.0,build 2017-03-27

--version 1.0.0,build 2017-03-30 增加resource agent_id 字段长度
--import#bat_create.sql#1.0.0,build 2017-03-30#1.0.0,build 2017-03-30

--version 1.0.0,build 2017-04-07 更新单位
--import#bat_data.sql#1.0.0,build 2017-04-07#1.0.0,build 2017-04-07

--version 1.0.0,build 2017-04-10 修改网络设备指标命名BUG
--import#bat_data.sql#1.0.0,build 2017-04-10#1.0.0,build 2017-04-10

--version 1.0.0,build 2017-04-19 agent_yaml增加enabled字段
--import#bat_create.sql#1.0.0,build 2017-04-19#1.0.0,build 2017-04-19

--version 1.0.0,build 2017-05-17 增加指标元数据
--import#bat_data.sql#1.0.0,build 2017-05-17#1.0.0,build 2017-05-17

--version 1.0.0,build 2017-05-24 resource增加store内置标签字段
--import#bat_create.sql#1.0.0,build 2017-05-24#1.0.0,build 2017-05-24

--version 1.0.0,build 2017-05-26 resource修改hostname长度
--import#bat_create.sql#1.0.0,build 2017-05-26#1.0.0,build 2017-05-26

--version 1.0.0,build 2017-05-31 更新指标单位
--import#bat_data.sql#1.0.0,build 2017-05-31#1.0.0,build 2017-05-31

--version 1.0.0,build 2017-06-26 增加monitor历史记录中info等级
--import#bat_create.sql#1.0.0,build 2017-06-26#1.0.0,build 2017-06-26

--version 1.0.0,build 2017-07-15 更新mem.free和mem.usable中文描述
--import#bat_data.sql#1.0.0,build 2017-07-15#1.0.0,build 2017-07-15

--version 1.0.0,build 2017-07-18 修改指标元数据表 postgresql的agent类型
--import#bat_data.sql#1.0.0,build 2017-07-18#1.0.0,build 2017-07-18

--version 1.0.0,build 2017-07-24 插入system.uptime指标
--import#bat_data.sql#1.0.0,build 2017-07-24#1.0.0,build 2017-07-24