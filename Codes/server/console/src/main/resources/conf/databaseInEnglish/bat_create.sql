﻿--
--version 1.0.0,build 2016-04-16
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS dashboard;
CREATE  TABLE IF NOT EXISTS `dashboard` (
  `id` binary(16) NOT NULL ,
  `name` VARCHAR(256) NOT NULL COMMENT 'title' ,
  `dashwindows` BLOB NULL DEFAULT NULL COMMENT 'dashwindows id列表' ,
  `type` VARCHAR(16) NOT NULL COMMENT '仪表盘类型' ,
  `template` BIT(1) NULL DEFAULT FALSE COMMENT '是否为内置模板' ,
  `modified` DATETIME NOT NULL COMMENT '最后修改时间' ,
  `user_id` binary(16) NULL DEFAULT NULL COMMENT '创建用户id' ,
  `tenant_id` binary(16) NULL DEFAULT NULL COMMENT '租户id' ,
  `descr` VARCHAR(256) NULL DEFAULT NULL COMMENT '描述' ,
  PRIMARY KEY (`id`) ,
  INDEX idx_dashboard_tenant_id (tenant_id)
)ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '仪表盘';

--version 1.0.0,build 2016-04-19
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS dashwindow;
CREATE  TABLE IF NOT EXISTS `dashwindow` (
  `id` binary(16) NOT NULL ,
  `dash_id` binary(16) NOT NULL ,
  `name` VARCHAR(256) NOT NULL COMMENT 'title' ,
  `viz` VARCHAR(36) NOT NULL COMMENT 'dashwindow类型' ,
  `requests` BLOB NOT NULL COMMENT '查询条件列表' ,
  PRIMARY KEY (`id`) ,
  INDEX idx_dashwindow_dash_id (dash_id)
)ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '仪表窗';

--version 1.0.0,build 2016-04-20
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS favorite_dashboard;
CREATE  TABLE IF NOT EXISTS `favorite_dashboard` (
  `user_id` binary(16) NOT NULL COMMENT '用户id' ,
  `dash_id` binary(16) NOT NULL ,
  PRIMARY KEY (`user_id`,`dash_id`)
)ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '我收藏的仪表盘';

--version 1.0.0,build 2016-04-21
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS monitor;

CREATE  TABLE  `monitor` (
  `id` binary(16) NOT NULL ,
  `creator_id` binary(16) NOT NULL COMMENT '创建人Id' ,
  `message` LONGTEXT NULL COMMENT '通知信息' ,
  `name` VARCHAR(100) NULL COMMENT '监测器名称/消息标题' ,
  `query` VARCHAR(512) NULL COMMENT '查询条件' ,
  `type` tinyint NOT NULL COMMENT '监测器类型' ,
  `state` tinyint NOT NULL COMMENT '监测器状态' ,
  `tenant_id` binary(16) NULL COMMENT '租户id' ,
  `notify` BIT(1) NULL COMMENT '是否通知用户' ,
  `enable` BIT(1) NULL COMMENT '是否启用' ,
  `modified` DATETIME NOT NULL COMMENT '最后修改时间' ,
  `notify_user_ids` VARCHAR(4000) NULL COMMENT '通知用户列表' ,
  `options` TEXT NULL COMMENT '可选项如阈值等' ,
  PRIMARY KEY (`id`) ,
  INDEX `idx_monitor_tanant_id` (`tenant_id`) )
ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '监测器表';

--version 1.0.0,build 2016-04-27
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS resource;

CREATE  TABLE  `resource` (
 `id` binary(16) NOT NULL ,
 `type`  SMALLINT NOT NULL DEFAULT 0 ,
 `modified`  DATE ,
 `hostname` VARCHAR(55) ,
 `ipaddr` VARCHAR(55) ,
 `describtion` VARCHAR(255) ,
 `agent_id`  VARCHAR(65) ,
 `tenant_id` binary(16) ,
  PRIMARY KEY (`id`) )
ENGINE = INNODB DEFAULT CHARACTER SET = utf8 COMMENT = '资源表';


--version 1.0.0,build 2016-04-29   
use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS res_tag;

CREATE  TABLE  `res_tag` (
  `id` binary(16) NOT NULL ,
  `tenant_id` binary(16) ,
  `tagk` VARCHAR(65),
  `tagv` VARCHAR(65),
  PRIMARY KEY (`id`,`tagk`,`tagv`,`tenant_id`) )
ENGINE = INNODB DEFAULT CHARACTER SET = utf8 COMMENT = '资源标签表';

use bat;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS res_app;

CREATE  TABLE  `res_app` (
  `id` binary(16) NOT NULL ,
  `tenant_id` binary(16) ,
  `app_name` varchar(65),
  PRIMARY KEY (`id`,`app_name`,`tenant_id`) )
ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '资源应用表';

--version 1.0.0,build 2016-05-03
ALTER TABLE `resource` MODIFY COLUMN `modified`  DATETIME;


--version 1.0.0,build 2016-05-05

DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
	`id` BINARY(16) NOT NULL ,
  `sort_id` BIGINT NOT NULL ,
  `occur_time` TIMESTAMP(3) NOT NULL COMMENT '时间',
  `res_id` BINARY(16) DEFAULT NULL COMMENT '资源ID',
  `msg_title` varchar(100) DEFAULT NULL COMMENT '标题',
  `msg_content` text DEFAULT NULL COMMENT '内容',
  `source_type` smallint(6) NOT NULL COMMENT '资源类型1:monitor,2:agent',
  `serverity` smallint(6) NOT NULL COMMENT '严重等级0：成功，1：消息，2：警告，3：紧急',
  `tenant_id` BINARY(16) NOT NULL COMMENT '租户ID',
  `monitor_id` BINARY(16) DEFAULT NULL,
  `agent_id` BINARY(16) DEFAULT NULL COMMENT 'agent ID',
  `fault_id` BINARY(16) DEFAULT NULL COMMENT '故障ID',
  `identity` varchar(50) DEFAULT NULL COMMENT '事件标识',
  PRIMARY KEY (`id`),
  KEY `idx_event_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件';

INSERT INTO _sequence_ values('event_seq', 0, 1);

DROP TABLE IF EXISTS `event_relate` ; 
CREATE TABLE `event_relate` (
  `event_id` BIGINT NOT NULL COMMENT '事件ID',
  `relate_event_id` varchar(50) NOT NULL COMMENT '监测到agent的事件而产生的事件ID',
  `tenant_id` varchar(50) NOT NULL COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件关联';

DROP TABLE IF EXISTS `event_tag`;
CREATE TABLE `event_tag` (
  `id` BINARY(16) NOT NULL,
  `object_id` int(50) NOT NULL COMMENT 'object ID',
  `tenant_id` BINARY(16) NOT NULL COMMENT '租户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件标签关联';

--version 1.0.0,build 2016-05-09

ALTER TABLE `resource` ADD  `last_collect_time` DATETIME , ADD `online_status` VARCHAR(50),  ADD `alert_status` VARCHAR(50);
ALTER TABLE `resource` MODIFY COLUMN `type`  VARCHAR(50);

--version 1.0.0,build 2016-05-10

ALTER TABLE `res_tag` DROP PRIMARY KEY;
ALTER TABLE `res_tag` ADD PRIMARY KEY(`id`,`tenant_id`,`tagk`);

--version 1.0.0,build 2016-05-16 
ALTER TABLE `resource` ADD `res_app` BLOB; 
ALTER TABLE `resource` ADD `res_tag` BLOB;

--version 1.0.0,build 2016-05-20

DROP TABLE IF EXISTS `event_fault`;
CREATE TABLE `event_fault` (
  `fault_id`BINARY(16) NOT NULL COMMENT '故障ID',
  `res_id` BINARY(16) NOT NULL COMMENT '资源ID',
  `identity` varchar(50) DEFAULT NULL COMMENT '事件标识',
  `first_time` datetime NOT NULL COMMENT '首次关联事件时间',
  `count` int(11) NOT NULL COMMENT '相关事件数量',
  PRIMARY KEY (`fault_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件故障表';

DROP TABLE IF EXISTS `event_fault_record`;
CREATE TABLE `event_fault_record` (
  `fault_id` BINARY(16) NOT NULL COMMENT '故障ID',
  `res_id` BINARY(16) NOT NULL COMMENT '资源ID',
  `identity` varchar(50) DEFAULT NULL COMMENT '事件标识',
  `first_time` datetime NOT NULL COMMENT '首次关联事件时间',
  `last_time` datetime NOT NULL COMMENT '最后关联事件时间',
  `count` int(11) NOT NULL COMMENT '相关事件数量',
  PRIMARY KEY (`fault_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='事件故障历史表';

DROP TABLE IF EXISTS `metric_meta_data`;
CREATE TABLE `metric_meta_data` (
  `name` varchar(65) NOT NULL COMMENT '指标名称',
  `unit` varchar(65)  COMMENT '单位',
  `value_min` double  COMMENT '值域最小值',
  `value_max` double COMMENT '值域最大值',
  `accuracy` int COMMENT '精度',
  `data_type` varchar(65)  COMMENT '类型',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='指标元数据表';


--version 1.0.0,build 2016-05-27
drop table if exists checkpoint_snapshot;
drop table if exists checkpoint_history;
drop table if exists tag_object;
drop table if exists object;
drop table if exists tag;
drop table if exists state;

create table state (
	id int auto_increment primary key,
	tenant_id varchar(36) not null,
	name varchar(200) not null,
	constraint state_uk unique key (tenant_id, name)
);

create table tag(
	id int auto_increment primary key,
	`key` varchar(65) not null,
	`value` varchar(65) not null,
	constraint tag_uk unique key (`key`, `value`)
);

create table object(
	id int auto_increment primary key,
	tag_count int not null,
	tag_ids varchar(512) not null,
	constraint object_uk unique key (tag_ids)
);

create table tag_object(
	tag_id int not null,
	object_id int not null,
	primary key (tag_id, object_id),
	constraint tag_object_tag foreign key (tag_id) references tag(id),
	constraint tag_object_object foreign key (object_id) references object(id)
);

create table checkpoint_snapshot(
	object_id int not null,
	state_id int not null,
	first_time BIGINT NOT NULL,
	last_time BIGINT NOT NULL,
	`value` varchar(50) not null,
	prior_value varchar(50),
	`count` int,
	primary key (object_id, state_id),
	constraint cp_snapshot_object foreign key (object_id) references object(id),
	constraint cp_snapshot_state foreign key (state_id) references state(id)
);

create table checkpoint_history(
	object_id int not null,
	state_id int not null,
	first_time BIGINT NOT NULL,
	last_time BIGINT NOT NULL,
	`value` varchar(50) not null,
	prior_value varchar(50),
	`count` int,
	primary key (object_id, state_id, first_time),
	constraint cp_history_object foreign key (object_id) references object(id),
	constraint cp_history_state foreign key (state_id) references state(id)
);

--version 1.0.0,build 2016-05-28
DROP TABLE IF EXISTS metric_name;
DROP TABLE IF EXISTS metric_resource;
CREATE TABLE metric_resource(
res_id binary(16),
metric_names BLOB,
PRIMARY KEY(res_id));

CREATE  TABLE IF NOT EXISTS `monitor_notify_record` (
  `id` binary(16) NOT NULL ,
  `name` VARCHAR(4000) NOT NULL COMMENT '通知用户名' ,
  `content` LONGTEXT NOT NULL COMMENT '通知内容' ,
  `time` DATETIME NOT NULL COMMENT '通知时间' ,
  `monitor_id` binary(16) NOT NULL COMMENT '监测器id' ,
  `tenant_id` binary(16) NOT NULL COMMENT '租户id' ,
  PRIMARY KEY (`id`) ,
  INDEX `idx_monitor_notify_history_monitor_id` (`monitor_id`) ,
  INDEX `idx_monitor_notify_history_tenant_id` (`tenant_id`)
)ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '监测器通知记录表';

--version 1.0.0,build 2016-06-02
ALTER TABLE `event` MODIFY COLUMN `msg_title`  VARCHAR(100);
ALTER TABLE `event` MODIFY COLUMN `msg_content` TEXT DEFAULT NULL;
--version 1.0.0,build 2016-06-23
ALTER TABLE `metric_resource` ADD COLUMN `tenant_id` BINARY(16)

--version 1.0.0,build 2016-06-28
CREATE  TABLE IF NOT EXISTS `res_detail` (
  `resource_id` binary(16) NOT NULL COMMENT '资源ID' ,
  `tenant_id` binary(16) NOT NULL COMMENT '租户ID' ,
  `detail` VARCHAR(4000)  COMMENT '资源详情' ,
  `agent_desc` VARCHAR(64) NOT NULL COMMENT '代理信息' ,
  PRIMARY KEY (`resource_id`)
  )ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '资源详情表';

--version 1.0.0,build 2016-06-29
ALTER TABLE `event` MODIFY COLUMN `identity`  VARCHAR(4000);
ALTER TABLE `event_fault` MODIFY COLUMN `identity`  VARCHAR(4000);
ALTER TABLE `event_fault_record` MODIFY COLUMN `identity`  VARCHAR(4000);
UPDATE `monitor` SET `notify`= 1;

--version 1.0.0,build 2016-06-30
-- 重命名旧表
alter table tag_object drop foreign key tag_object_object;
alter table tag_object drop foreign key tag_object_tag;

alter table checkpoint_snapshot drop foreign key cp_snapshot_object;
alter table checkpoint_snapshot drop foreign key cp_snapshot_state;

alter table checkpoint_history drop foreign key cp_history_object;
alter table checkpoint_history drop foreign key cp_history_state;

alter table checkpoint_history rename checkpoint_history_160629;
alter table checkpoint_snapshot rename checkpoint_snapshot_160629;
alter table state rename state_160629;
alter table tag_object rename tag_object_160629;
alter table tag rename tag_160629;
alter table object rename object_160629;

-- 准备标签函数，把[23][24]转换为varbinary
drop table if exists temp_id;

create table temp_id(
  id binary(16)
);

drop function if exists convert_tag_ids;

--<{
create function convert_tag_ids(in_str varchar(256)) returns varbinary(255)
begin
  declare start int;
  declare pos int;
  declare posEnd int;
  declare tempId varchar(512);
  declare result varchar(512);
  declare cursor_id cursor for select lower(hex(id)) from temp_id order by id;
  declare continue handler for not found set pos = 1;

  set start = 1;
  set pos = 0;
  delete from temp_id;

  loop_label: loop
    set pos = locate('[', in_str, start);
    if pos > 0 then
      set posEnd = locate(']', in_str, start);
      set tempId = substring(in_str, pos + 1, posEnd - pos - 1);
      insert into temp_id
      select `uuid` from tag_160629 where id = tempId;
      set start = posEnd + 1;
    else
      leave loop_label;
    end if;
  end loop;

  set pos = 0;
  set result = '';
  open cursor_id;
  loop_label2: loop
    fetch cursor_id into tempId;
    if pos = 0 then
      set result = concat(result, tempId);
    else
      leave loop_label2;
    end if;
  end loop;

  return unhex(result);
end;
--}>

-- 为旧表增加uuid字段
alter table state_160629 add `uuid` binary(16);
update state_160629 set `uuid` = unhex(replace(uuid(), '-', ''));
update state_160629 set tenant_id = uuid() where tenant_id = 'bat';
commit;

alter table tag_160629 add `uuid` binary(16);
update tag_160629 set `uuid` = unhex(replace(uuid(), '-', ''));
commit;

alter table object_160629 add `uuid` binary(16);
update object_160629 set `uuid` = unhex(replace(uuid(), '-', ''));
commit;

-- 重建表
create table tag (
       id binary(16) not null primary key,
       `key` varchar(65) not null,
       `value` varchar(65) not null,
       unique key tag_uk (`key`,`value`)
);

insert into tag(id, `key`, `value`)
select `uuid`, `key`, `value`
from tag_160629;
commit;

create table object (
       id binary(16) not null primary key,
       `tag_count` int(11) not null,
       `tag_ids` varbinary(255),
       unique key `object_uk` (`tag_ids`)
);

insert into object(id, tag_count, tag_ids)
select `uuid`, `tag_count`, convert_tag_ids(tag_ids)
from object_160629;
commit;

create table `tag_object` (
       `tag_id` binary(16) not null,
       `object_id` binary(16) not null,
       primary key (`tag_id`,`object_id`),
       constraint `tag_object_object` foreign key (`object_id`) references `object` (`id`),
       constraint `tag_object_tag` foreign key (`tag_id`) references `tag` (`id`)
);

insert into tag_object(tag_id, object_id)
select t.`uuid`, o.`uuid`
from tag_object_160629 tobj
inner join tag_160629 t on tobj.tag_id = t.id
inner join object_160629 o on tobj.object_id = o.id;
commit;

create table `state` (
       `id` binary(16) not null primary key,
       `tenant_id` binary(16) not null,
       `name` varchar(200) not null,
       unique key `state_uk` (`tenant_id`,`name`)
);

insert into state
select `uuid`, unhex(replace(tenant_id, '-', '')), name
from state_160629;
commit;

create table `checkpoint_snapshot` (
       `object_id` binary(16) not null,
       `state_id` binary(16) not null,
       `first_time` bigint(20) not null,
       `last_time` bigint(20) not null,
       `value` varchar(50) not null,
       `prior_value` varchar(50) default null,
       `count` int(11) default null,
       primary key (`object_id`,`state_id`),
       constraint `cp_snapshot_object` foreign key (`object_id`) references `object` (`id`),
       constraint `cp_snapshot_state` foreign key (`state_id`) references `state` (`id`)
);

insert into checkpoint_snapshot
select o.`uuid`, s.`uuid`, first_time, last_time, `value`, prior_value, `count`
from checkpoint_snapshot_160629 c
inner join object_160629 o on c.object_id = o.id
inner join state_160629 s on c.state_id = s.id;
commit;

create table `checkpoint_history` (
       `object_id` binary(16) not null,
       `state_id` binary(16) not null,
       `first_time` bigint(20) not null,
       `last_time` bigint(20) not null,
       `value` varchar(50) not null,
       `prior_value` varchar(50) default null,
       `count` int(11) default null,
       primary key (`object_id`,`state_id`,`first_time`),
       constraint `cp_history_object` foreign key (`object_id`) references `object` (`id`),
       constraint `cp_history_state` foreign key (`state_id`) references `state` (`id`)
);

insert into checkpoint_history
select o.`uuid`, s.`uuid`, first_time, last_time, `value`, prior_value, `count`
from checkpoint_history_160629 c
inner join object_160629 o on c.object_id = o.id
inner join state_160629 s on c.state_id = s.id;
commit;

alter table event_tag rename event_tag_160629;

create table `event_tag` (
  `id` binary(16) not null,
  `object_id` binary(16) not null comment 'object id',
  `tenant_id` binary(16) not null comment '租户id'
);

insert into event_tag
select e.id, o.`uuid`, e.tenant_id
from event_tag_160629 e
inner join object_160629 o on e.object_id = o.id;
commit;
--version 1.0.0,build 2016-06-31
UPDATE metric_meta_data SET accuracy=2 WHERE NAME='system.mem.pct_usable';

--version 1.0.0,build 2016-07-08
ALTER TABLE `event` ADD occur_minute timestamp NULL DEFAULT NULL;
ALTER TABLE event drop INDEX idx_event_tenant_id;
ALTER TABLE event add INDEX idx_event_fault_sort_tenant_time_fault(fault_id,sort_id,tenant_id,occur_time);
ALTER TABLE event add INDEX idx_event_minute_serverity_tenant(occur_minute,serverity,tenant_id,occur_time);

--version 1.0.0,build 2016-07-13
ALTER TABLE event MODIFY COLUMN occur_time TIMESTAMP(3) NULL DEFAULT NULL;
ALTER TABLE event MODIFY COLUMN occur_minute datetime default null;
update event SET occur_minute = DATE_FORMAT(occur_time,'%Y-%m-%d %H:%i');

--version 1.0.0,build 2016-07-18
ALTER TABLE `resource` ADD COLUMN create_time datetime;

--version 1.0.0,build 2016-07-19
ALTER TABLE `dashboard` ADD create_time DATETIME NOT NULL;
UPDATE `dashboard`  SET create_time = modified;
ALTER TABLE `monitor` ADD create_time DATETIME NOT NULL;
UPDATE `monitor`  SET create_time = modified;
COMMIT;

--version 1.0.0,build 2016-07-20
update `resource`  set create_time = modified;
commit;

--version 1.0.0,build 2016-07-22
ALTER TABLE `dashwindow`  ADD line_datas VARCHAR(256) NULL;
commit;

--version 1.0.0,build 2016-07-23
drop function IF EXISTS get_str_split_length;

--<{
create function get_str_split_length(f_string varchar(512),f_delimiter varchar(512)) returns int
begin  
  return 1 + (length(f_string) - length(replace(f_string, f_delimiter, '')));  
end;
--}>

drop function IF EXISTS get_split_string;

--<{
CREATE FUNCTION get_split_string(f_string varchar(255),f_delimiter varchar(5),f_order int) RETURNS varchar(255) CHARSET utf8 
BEGIN  
  declare result varchar(255) default '';  
  set result = reverse(substring_index(reverse(substring_index(f_string,f_delimiter,f_order)),f_delimiter,1));  
  return result;  
end;
--}>

drop procedure IF EXISTS change_monitor;

--<{
create procedure change_monitor()
begin
	declare _id binary(16);
	declare _query varchar(512) character set utf8;
	declare _options text character set utf8;
   
	declare done int;
	declare length int;
	declare start_index int;
	declare comparator varchar(2) character set utf8;
	declare end_index int;
	declare new_query varchar(512) character set utf8;
	declare period varchar(100) character set utf8;
	declare threadshoulds text character set utf8;
	declare new_threadshoulds text character set utf8 ;
	declare _condition text character set utf8;
	declare new_condition text character set utf8;
	declare new_options text character set utf8;
	
	declare m_cur cursor for select id, query, options from monitor where type=1;
	declare continue handler for not found set done=1;
	
	set done=0;

	open m_cur;
	m_loop:loop
	
	fetch m_cur INTO _id,_query,_options;
	if done = 1 THEN
		leave m_loop;
	end if;

	set length=get_str_split_length(_query, ' ');
	set start_index=locate(':', _query)+1;
	
	if length=4 then
		set comparator=get_split_string(_query, ' ', 4);        
	else
		set comparator=get_split_string(_query, ' ', (length-1));
	end if;
	
	set end_index=locate(comparator, _query)-2;
	set new_query=substring(_query, start_index, (end_index-start_index+1));

	set start_index=locate('(', _query)+1;
	set end_index=locate(')', _query)-1;
	set period=substring(_query, start_index, (end_index-start_index+1));
	
	set start_index=locate('"thresholds":{', _options)+14;
	set end_index=locate('}}', _options)-1;
	set threadshoulds=substring(_options, start_index, (end_index-start_index+1));
	
	set new_threadshoulds=threadshoulds;
	
	set _condition=get_split_string(threadshoulds, ',', 1);
	if locate('null', _condition) = 0 then
		set new_condition=concat(get_split_string(_condition, ':', 1), ':"', period, ' ', comparator, ' ', get_split_string(_condition, ':', 2), '"');
		set new_threadshoulds=replace(new_threadshoulds, _condition, new_condition);
	end if;
	set _condition=get_split_string(threadshoulds, ',', 2);
	if locate('null', _condition) = 0 then
		set new_condition=concat(get_split_string(_condition, ':', 1), ':"', period, ' ', comparator, ' ', get_split_string(_condition, ':', 2), '"');
		set new_threadshoulds=replace(new_threadshoulds, _condition, new_condition);
	end if;
	
	set new_options=replace(_options, threadshoulds, new_threadshoulds);
	
	update monitor set query=new_query, options=new_options where id=_id;

	end loop m_loop;

	close m_cur;
end;
--}>
call change_monitor();
drop procedure change_monitor;
commit;

--version 1.0.0,build 2016-07-26
drop procedure IF EXISTS change_resource_app;

--<{
create procedure change_resource_app(in rep_name varchar(20))
begin
	declare _id binary(16);
	declare _res_app BLOB;
  
	declare done int;
	declare new_res_app BLOB;

	declare r_cur cursor for select id, res_app from resource;
	declare continue handler for not found set done=1;
    
	set done=0;
  
	open r_cur;
	r_loop:loop
    
 	fetch r_cur INTO _id,_res_app;
	if done = 1 THEN
		leave r_loop;
	end if;
    
	set new_res_app=replace(_res_app, rep_name,'');
	set new_res_app=replace(new_res_app, ';;',';');
	set new_res_app=trim(TRAILING ';' from new_res_app);
	update resource set res_app=new_res_app where id=_id;
	delete from res_app where id=_id and app_name=rep_name;

	end loop r_loop;

	close r_cur;
end;
--}>
call change_resource_app('docker_daemon');
call change_resource_app('test_cx_oracle');
commit;

alter table checkpoint_history add descr varchar(2000);
alter table checkpoint_snapshot add descr varchar(2000);

--version 1.0.0,build 2016-07-27
call change_resource_app('datadog');

update resource set res_app=replace(res_app, ';;', ';');
update resource set res_app=trim(TRAILING ';' from res_app) where res_app like '%;';
commit;

drop table state_160629;
drop table checkpoint_history_160629;
drop table checkpoint_snapshot_160629;
drop table event_tag_160629;
drop table tag_object_160629;
drop table tag_160629;
drop table object_160629;

ALTER TABLE temp_id CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE object CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE tag CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE tag_object CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE checkpoint_history CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE checkpoint_snapshot CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE event_tag CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE state CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;

update metric_meta_data set data_type="gauge";
commit;

--version 1.0.0,build 2016-08-17
ALTER TABLE `metric_meta_data`  ADD cn VARCHAR(255) NULL;
ALTER TABLE `metric_meta_data`  ADD cdescr VARCHAR(255) NULL;
TRUNCATE TABLE metric_meta_data;
commit;

--version 1.0.0,build 2016-08-23
call change_resource_app('zk');
commit;

--version 1.0.0,build 2016-09-01
update metric_meta_data set unit="MB" WHERE name="system.mem.usable";
commit;

--version 1.0.0,build 2016-11-04
DROP TABLE IF EXISTS `state_metric_resource`;
CREATE TABLE `state_metric_resource` (
  `res_id` binary(16) NOT NULL,
  `metric_names` blob,
  `tenant_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`res_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '状态资源对应表';
commit;

--version 1.0.0,build 2016-11-07
ALTER TABLE `resource` ADD COLUMN os VARCHAR(50) NULL;
commit;

--version 1.0.0,build 2016-11-18
ALTER TABLE `resource` ADD COLUMN user_tag VARCHAR(255) NULL;
commit;

--version 1.0.0,build 2016-12-06
DROP TABLE IF EXISTS `agent_list`;
CREATE TABLE `agent_list` (
  `id` binary(16) NOT NULL,
  `tenant_id` binary(16) NOT NULL,
  `hostname` VARCHAR(65),
  `ip` VARCHAR(65) NOT NULL,
  `source` VARCHAR(65) NOT NULL,
  `apps` blob,
  `tags` blob,
  `modified` datetime,
  `online_status` VARCHAR(65),
  PRIMARY KEY (`tenant_id`,`id`,`source`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = 'agent信息表';

--version 1.0.0,build 2016-12-07
DROP TABLE IF EXISTS `agent_yaml`;
CREATE TABLE `agent_yaml` (
  `agent_id` binary(16) NOT NULL,
  `tenant_id` binary(16) NOT NULL,
  `filename` VARCHAR(65),
  `md5` VARCHAR(65) NOT NULL,
  `size` int(11),
  `modified` datetime,
  `content` blob,
  `source` VARCHAR(65) NOT NULL,
  PRIMARY KEY (`tenant_id`,`agent_id`,`filename`,`source`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = 'agent的yaml配置文件表';

--version 1.0.0,build 2016-12-08
DROP TABLE IF EXISTS `agent_tag`;
CREATE TABLE `agent_tag` (
  `id` binary(16) NOT NULL,
  `tenant_id` binary(16) NOT NULL,
  `key` varchar(65) NOT NULL,
  `value` varchar(65) NOT NULL,
  PRIMARY KEY (`id`,`tenant_id`,`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='agent标签表';

--version 1.0.0,build 2016-12-10
ALTER TABLE `monitor` ADD COLUMN auto_recovery_params LONGTEXT NULL;
commit;

--version 1.0.0,build 2016-12-14
drop table event;
drop table event_fault;
drop table event_fault_record;
drop table event_relate;
drop table event_tag;

--version 1.0.0,build 2016-12-16
ALTER TABLE `metric_meta_data` ADD COLUMN integration VARCHAR(255) NULL;

--version 1.0.0,build 2016-12-22
ALTER TABLE `metric_meta_data` ADD COLUMN tenant_id binary(16) NULL;

--version 1.0.0,build 2016-12-25
DROP TABLE IF EXISTS `monitor_auto_recover_record`;
CREATE TABLE `monitor_auto_recover_record` (
  `id` binary(16) NOT NULL,
  `monitor_id` binary(16) NOT NULL,
  `monitor_name` varchar(400) NOT NULL,
  `res_id` binary(16) NOT NULL,
  `host` varchar(255) NOT NULL,
  `time` datetime DEFAULT NULL,
  `tenant_id` binary(16) NOT NULL,
  `execute_id` varchar(200) DEFAULT NULL,
  `params` varchar(255) DEFAULT NULL,
  `interval` bigint(20) DEFAULT NULL
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '监测器自愈列表';

--version 1.0.0,build 2017-01-17
ALTER TABLE `resource` ADD COLUMN agentless_tag VARCHAR(255) NULL;

--version 1.0.0,build 2017-01-18
ALTER TABLE `res_detail` MODIFY COLUMN `detail` TEXT;

--version 1.0.0,build 2017-02-09
-- overview
DROP TABLE IF EXISTS `resource_monitor_record`;
CREATE  TABLE `resource_monitor_record` (
  `tenant_id` BINARY(16) NOT NULL COMMENT '租户ID' ,
  `resource_id` BINARY(16) NOT NULL COMMENT '资源ID' ,
  `monitor_id` BINARY(16) NOT NULL COMMENT '监测器ID' ,
  `ok` BIT NOT NULL DEFAULT 1 COMMENT '正常' ,
  `warn` BIT NOT NULL DEFAULT 0 COMMENT '警告' ,
  `error` BIT NOT NULL DEFAULT 0 COMMENT '错误' ,
  `timestamp` BIGINT NOT NULL DEFAULT 0 COMMENT '更新时间' ,
  PRIMARY KEY (`tenant_id`, `resource_id`, `monitor_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '资源-监测器-状态记录表';
DROP TABLE IF EXISTS `overview_tag`;
CREATE  TABLE `overview_tag` (
  `id` BINARY(16) NOT NULL COMMENT '标签ID' ,
  `tenant_id` BINARY(16) NOT NULL COMMENT '租户ID' ,
  `key` VARCHAR(65) NOT NULL COMMENT '标签KEY' ,
  `value` VARCHAR(65) NOT NULL COMMENT '标签VALUE' ,
  PRIMARY KEY (`tenant_id`, `key`, `value`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '总览标签树';
DROP TABLE IF EXISTS `overview_tag_resource`;
CREATE  TABLE `overview_tag_resource` (
  `tenant_id` BINARY(16) NOT NULL COMMENT '租户ID' ,
  `tag_id` BINARY(16) NOT NULL COMMENT '标签ID' ,
  `resource_id` BINARY(16) NOT NULL COMMENT '资源ID' ,
  PRIMARY KEY (`tenant_id`, `tag_id`, `resource_id`) 
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '总览-标签-资源关系表'
--version 1.0.0,build 2017-02-14
DROP TABLE IF EXISTS `tenant_res_template`;
CREATE TABLE `tenant_res_template` (
  `dashboard_id` binary(16) NOT NULL,
  `resource_id` binary(16),
  `tenant_id` binary(16),
  `app_name` varchar(255) NOT NULL,
   PRIMARY KEY (`dashboard_id`,`app_name`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '租户资源自定义模板表';


--version 1.0.0,build 2017-02-22
ALTER TABLE `dashboard` ADD COLUMN is_resource BIT NULL;

--version 1.0.0,build 2017-02-24
DROP TABLE IF EXISTS `simple_resource`;
CREATE TABLE `simple_resource` (
  `id` binary(16) NOT NULL,
  `tenant_id` binary(16) NOT NULL,
  `create_time` datetime,
  `last_collect_time` datetime,
  `tags` blob,
  `online_status` varchar(65),
  `hostname` varchar(255),
  `ipaddr` varchar(255),
  primary key(`id`,`tenant_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '资源业务逻辑表';




--version 1.0.0,build 2017-03-06
DROP TABLE IF EXISTS `res_id_transform`;
CREATE TABLE `res_id_transform` (
  `res_id` binary(16) NOT NULL,
  `tenant_id` binary(16) NOT NULL,
  `unit_id` binary(16) NOT NULL,
  primary key(`res_id`,`tenant_id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '资源ID与统一资源数据库ID映射表';


--version 1.0.0,build 2017-03-16
DROP TABLE IF EXISTS `report`;
create table `report`(
	`report_id` BINARY(16) NOT NULL primary key,
	`report_name` varchar(32) NOT NULL COMMENT '报表名称',
	`report_type` varchar(32) NOT NULL COMMENT '报表类型 日报 周报..',
	`diagram_type` varchar(32) NOT NULL COMMENT '图表类型',
	`status` smallint DEFAULT 1 NOT NULL COMMENT 'report状态 1 正常 0 关闭',
	`group_id` BINARY(16) NOT NULL COMMENT '分组id',
	`modified` timestamp NOT NULL COMMENT '最后修改时间',
	`tenant_id` BINARY(16) NOT NULL,
	`metrics` BLOB NULL COMMENT '指标名称数组',
	`res_tags` BLOB NULL COMMENT '资源tag数组',
	`sort_field` varchar(64) COMMENT '模板默认排序标签',
	`sort_order` varchar(16) COMMENT '模板默认排序方案',
	`default_size` int COMMENT '默认展示size'
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '报表模板';

DROP TABLE IF EXISTS `report_group`;
create table `report_group`(
	`group_id` BINARY(16) NOT NULL primary key,
	`group_name` varchar(32) NOT NULL,
	`parent_id` BINARY(16) COMMENT '父分组id',
	`modified` timestamp NOT NULL COMMENT '最后修改时间',
	`tenant_id` BINARY(16) NOT NULL,
	`status` smallint DEFAULT 1 NOT NULL COMMENT 'report状态 1 正常 -1 删除'
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '报表分组导航';

DROP TABLE IF EXISTS `report_data`;
create table `report_data`(
	`report_data_id` BINARY(16) NOT NULL primary key,
	`report_id` BINARY(16) NOT NULL COMMENT '报表模板id',
	`start_date` DATE NOT NULL,
	`end_date` DATE NOT NULL,
	`create_time` timestamp
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '报表数据表';

DROP TABLE IF EXISTS `report_resource`;
create table `report_resource`(
	`resource_id` BINARY(16) NOT NULL,
	`report_data_id` BINARY(16) NOT NULL,
	`hostname` varchar(64) NOT NULL,
	`ipaddr` varchar(64) NOT NULL,
	`report_id` BINARY(16) NOT NULL,
	primary key(resource_id, report_data_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '报表绑定资源';

DROP TABLE IF EXISTS `report_metric_data`;
create table `report_metric_data`(
	`report_data_id` BINARY(16) NOT NULL,
	`resource_id` BINARY(16) NOT NULL,
	`metric_name` varchar(64) NOT NULL,
	`points` BLOB COMMENT 'json存储时序数据',
	`val_avg` varchar(16) COMMENT '数据均值',
	`unit` varchar(16) COMMENT '单位',
	`report_id` BINARY(16) NOT NULL,
	primary key(metric_name, report_data_id, resource_id),
	index `idx_report_metric_data` (`metric_name`, `val_avg`, `unit`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '报表绑定标签及数据';

--version 1.0.0,build 2017-03-20
ALTER TABLE `report_metric_data` MODIFY COLUMN `val_avg` DOUBLE;

--version 1.0.0,build 2017-03-24
ALTER TABLE `report_data` ADD UNIQUE KEY `report_id_date_uk` (`report_id`, `start_date`);

--version 1.0.0,build 2017-03-27
ALTER TABLE `report` MODIFY `group_id` BINARY(16) COMMENT '分组id';
ALTER TABLE `report` MODIFY `report_name` VARCHAR(64) NOT NULL COMMENT '报表名称';
--指标元数据区分agent和agentless
ALTER TABLE `metric_meta_data` ADD  COLUMN `agent_type` varchar(50);

--version 1.0.0,build 2017-03-30
ALTER TABLE `resource` MODIFY `agent_id` VARCHAR(128);

--version 1.0.0,build 2017-04-19
alter table agent_yaml add column enabled TINYINT(1);

--version 1.0.0,build 2017-05-24
ALTER TABLE `resource` ADD COLUMN store_builtin_tags VARCHAR(500) NULL;

--version 1.0.0,build 2017-05-26
ALTER TABLE `resource` MODIFY COLUMN hostname VARCHAR(64) NULL;

--version 1.0.0,build 2017-06-26
ALTER TABLE resource_monitor_record ADD  COLUMN info  BIT NOT NULL DEFAULT 0  COMMENT '提醒' AFTER `error`;