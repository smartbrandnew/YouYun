--
--version 1.0.0,build 2016-04-14
DROP TABLE IF EXISTS sys_version;
CREATE TABLE sys_version (
  version_num varchar(255) NOT NULL,
  build_date date DEFAULT NULL,
  id varchar(255) CHARACTER SET utf8 NOT NULL DEFAULT '',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统版本信息';