--
--version 1.0.0,build 2016-04-16
-- _sequence_
DROP TABLE IF EXISTS _sequence_;
CREATE TABLE _sequence_ (
name              VARCHAR(50) NOT NULL,
current_value BIGINT NOT NULL,
increment       TINYINT NOT NULL DEFAULT 1,
PRIMARY KEY (name)
)ENGINE = InnoDB DEFAULT CHARACTER SET = utf8 COMMENT = '序列表';

-- seq_nextval
SET global log_bin_trust_function_creators=1;

DROP FUNCTION IF EXISTS seq_nextval;
CREATE FUNCTION seq_nextval (seq_name VARCHAR(50))
RETURNS BIGINT
CONTAINS SQL
--<{
BEGIN
   DECLARE lower_name varchar(50);
   DECLARE value BIGINT;
   set lower_name = lower(seq_name);
   UPDATE _sequence_ SET current_value = current_value + increment
   WHERE name = lower_name;
   SELECT current_value INTO value
   FROM _sequence_ WHERE name = lower_name;
   RETURN value;
END;
--}>

--version 1.0.0,build 2017-02-09
-- delete _sequence_
DROP TABLE IF EXISTS _sequence_;
DROP FUNCTION IF EXISTS seq_nextval;
