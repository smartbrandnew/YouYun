package com.broada.carrier.monitor.impl.db.oracle.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.oracle.advancedQueue.OracleAdvanceQue;
import com.broada.carrier.monitor.impl.db.oracle.asm.OracleDiskASM;
import com.broada.carrier.monitor.impl.db.oracle.asm.disk.ASMDiskState;
import com.broada.carrier.monitor.impl.db.oracle.asm.diskgroup.ASMDiskGroup;
import com.broada.carrier.monitor.impl.db.oracle.basic.OracleBaseConfiger;
import com.broada.carrier.monitor.impl.db.oracle.dtfile.OracleDtfile;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.fts.OracleFTSInfo;
import com.broada.carrier.monitor.impl.db.oracle.lock.OracleLock;
import com.broada.carrier.monitor.impl.db.oracle.patchRate.OraclePatchRate;
import com.broada.carrier.monitor.impl.db.oracle.pga.OraclePGAInfo;
import com.broada.carrier.monitor.impl.db.oracle.recursion.OracleRecursionInfo;
import com.broada.carrier.monitor.impl.db.oracle.redolog.OracleRedoLogParameter;
import com.broada.carrier.monitor.impl.db.oracle.redolog.RedoLogInfo;
import com.broada.carrier.monitor.impl.db.oracle.rman.OracleRmanSqlBuilder;
import com.broada.carrier.monitor.impl.db.oracle.roll.OracleRollback;
import com.broada.carrier.monitor.impl.db.oracle.session.info.OracleSessInfo;
import com.broada.carrier.monitor.impl.db.oracle.session.wait.LockWait;
import com.broada.carrier.monitor.impl.db.oracle.sql.OracleSqlInfo;
import com.broada.carrier.monitor.impl.db.oracle.tablespace.OracleTableSpace;
import com.broada.carrier.monitor.impl.db.oracle.user.UserState;
import com.broada.carrier.monitor.impl.db.oracle.wait.WaitEvent;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.NumberUtil;
import com.broada.utils.StringUtil;

/**
 * <p>
 * Title: OracleManager
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3 huangjb 2007/10/31 修改表空间EXTENTS字段获取途径
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class OracleManager {
	private static final Log logger = LogFactory.getLog(OracleManager.class);
	// public static final String ORA_SQL_TABLESAPCE =
	// "SELECT D.TABLESPACE_NAME, SPACE, ROUND((1-NVL(FREE_SPACE,0)/SPACE)*100,2) USED_RATE ,"
	// + "    ROUND(FSFI,2) FSFI,EXTENTS"
	// + " FROM "
	// +
	// "    (SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) SPACE,SUM(BLOCKS) BLOCKS"
	// + "          FROM DBA_DATA_FILES  GROUP BY TABLESPACE_NAME) D,"
	// +
	// "    (SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) FREE_SPACE,"
	// +
	// "           sqrt(max(blocks)/sum(blocks))*(100/sqrt(sqrt(count(blocks)))) FSFI "
	// + "          FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME) F ,"
	// +
	// "    (select TABLESPACE_NAME,TOTAL_EXTENTS extents from dba_free_space_coalesced) G"
	// +
	// " WHERE  D.TABLESPACE_NAME = F.TABLESPACE_NAME(+) AND D.TABLESPACE_NAME = G.TABLESPACE_NAME(+) order by D.TABLESPACE_NAME";

	// huangjb modify 2007/10/31
	// 修改以COUNT(TABLESPACE_NAME)替换dba_free_space_coalesced的TOTAL_EXTENTS

	// jiangjw modify 2009/11/18
	// 阐述这里的SQL原理
	// 需要输出的字段				含义						来源
	// 1. MAX_EXTENTS				最大extents数			DBA_TABLESPACES.MAX_EXTENTS
	// 2. CONTENTS					表空间类型				DBA_TABLESPACES.CONTENTS
	// 3. SEGMENT_SPACE_MANAGEMENT	segment管理类型			DBA_TABLESPACES.SEGMENT_SPACE_MANAGEMENT
	// 4. FREE_EXTENTS				未使用extents			MAX_EXTENTS - EXTENTS_COUNT
	// 5. SPACE						当前空间大小				DBA_DATA_FILES.BYTES、DATA_TEMP_FILES.BYTES，所有数据文件当前大小合计
	// 6. FREE_RATE					未使用空间比率			DBA_FREE_SPACE.BYTES，所有未使用空间合计
	// 7. EXTENTS_COUNT				当前extents总计			DBA_SEGMENTS.EXTENTS，所有segemnt所使用extents合计
	// 8. NEXT_EXTENT				下一个extent大小			DBA_TABLESPACES.NEXT_EXTENT
	// 9. MAX_SPACE					最大可以到达的空间大小	DBA_DATA_FILES.MAXBYTES、DATA_TEMP_FILES.MAXBYTES，所有数据文件的合计
	// 10. MAX_AUTOEXTENSIBLE		是否是可以自动扩展		DBA_DATA_FILES.AUTOEXTENSIBLE、DATA_TEMP_FILES.AUTOEXTENSIBLE，如有任意一条记录为自动扩展，则表空间为自动扩展
	public static final String ORA_SQL_TABLESAPCE = "SELECT C.TABLESPACE_NAME,C.MAX_EXTENTS,C.CONTENTS,G.SEGMENT_SPACE_MANAGEMENT, "
		+
		"(C.MAX_EXTENTS - E.CURRENT_EXTENTS) FREE_EXTENTS, SPACE, ROUND((NVL(FREE_SPACE,0)/SPACE)*100,2) FREE_RATE, E.CURRENT_EXTENTS EXTENTS_COUNT, C.NEXT_EXTENT, "
		+
		"MAX_SPACE, MAX_AUTOEXTENSIBLE "
		+
		"FROM  "
		+
		"   (SELECT TABLESPACE_NAME,MAX_EXTENTS,CONTENTS,NEXT_EXTENT FROM DBA_TABLESPACES) C, "
		+
		"   (SELECT TABLESPACE_NAME,SUM(EXTENTS) CURRENT_EXTENTS FROM DBA_SEGMENTS GROUP BY TABLESPACE_NAME) E, "
		+
		"   (SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) SPACE,SUM(BLOCKS) BLOCKS,  "
		+
		"     ROUND(sum(MAXBYTES) / 1024 / 1024, 2) MAX_SPACE, MAX(AUTOEXTENSIBLE) MAX_AUTOEXTENSIBLE   "
		+
		"         FROM DBA_DATA_FILES  GROUP BY TABLESPACE_NAME "
		+
		"     UNION "
		+
		"     SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) SPACE,SUM(BLOCKS) BLOCKS, "
		+
		"     ROUND(sum(MAXBYTES) / 1024 / 1024, 2) MAX_SPACE, MAX(AUTOEXTENSIBLE) MAX_AUTOEXTENSIBLE "
		+
		"         FROM dba_temp_files  GROUP BY TABLESPACE_NAME) D, "
		+
		"   (SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) FREE_SPACE FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME"
		+
		"			UNION "
		+
		"		  SELECT D.TABLESPACE_NAME,ROUND(((F.BYTES_FREE + F.BYTES_USED) - NVL(P.BYTES_USED, 0)) / 1024 /1024,2) FREE_SPACE "
		+
		"		  FROM V$TEMP_SPACE_HEADER F, DBA_TEMP_FILES D, V$TEMP_EXTENT_POOL P "
		+
		"		  WHERE F.TABLESPACE_NAME(+) = D.TABLESPACE_NAME AND F.FILE_ID(+) = D.FILE_ID AND P.FILE_ID(+) = D.FILE_ID "
		+
		"    ) F, "
		+
		"(SELECT NAME, DECODE(BITAND(TS.FLAGS,32), 32,'AUTO', 'MANUAL') SEGMENT_SPACE_MANAGEMENT FROM SYS.TS$ TS) G "
		+
		"WHERE C.TABLESPACE_NAME = E.TABLESPACE_NAME(+) AND C.TABLESPACE_NAME = D.TABLESPACE_NAME(+) AND C.TABLESPACE_NAME = F.TABLESPACE_NAME(+) AND C.TABLESPACE_NAME = G.NAME(+) "
		+
		"Order by C.TABLESPACE_NAME";

	public static final String ORA_SQL_TABLESAPCENAME = "SELECT TABLESPACE_NAME,ROUND(SUM(BYTES)/(1024*1024),2) SPACE          FROM DBA_DATA_FILES  GROUP BY TABLESPACE_NAME order by TABLESPACE_NAME";

	public static final String ORA_SQL_TABLESPACE_IO_AVG = " select ts.NAME, ROUND(decode(sum(fs.PHYRDS),0,0,(sum(fs.readtim)/sum(fs.PHYRDS)/100)),4) as AVGREADTIM, ROUND(decode(sum(fs.PHYWRTS),0,0,sum(fs.writetim)/sum(fs.PHYWRTS)/100),4) as AVGWRITTIM"
		+ "     from v$datafile df,v$filestat fs , v$tablespace ts"
		+ "     where  df.file# = fs.file# and df.TS# = ts.TS#" + "     group by ts.TS#,ts.name";

	public static final String ORA_SQL_LOCK = "select ao.owner,ao.object_name,ao.object_type,l.ctime "
		+ "from v$locked_object lo, v$lock l, all_objects ao " + "where lo.session_id=l.sid and l.lmode=3 "
		+ "and lo.object_id=ao.object_id " + "and lo.object_id=l.id1 " + "and l.ctime >= ?";

	public static final String ORA_SQL_CACHEHITRATIO = "SELECT (1 - (phy.value / (cur.value + con.value)))*100 \"ratio\" "
		+ "FROM v$sysstat cur, v$sysstat con, v$sysstat phy "
		+ "WHERE cur.name = 'db block gets' AND con.name = 'consistent gets' AND phy.name = 'physical reads'";

	public static final String ORA_SQL_RELOADSTOPINSRATIO = "select (sum(pins-reloads)/sum(pins))*100 \"ratio\" from v$librarycache";

	public static final String ORA_SQL_DICTIONARYCACHERATIO = "select (sum(gets-getmisses-usage-fixed)/sum(gets))*100 \"ratio\" from v$rowcache";

	public static final String ORA_SQL_ROLLBACKSEGMENTHEADER = "select sum(waits)*100 /sum(gets) \"ratio\" from v$rollstat";

	public static final String ORA_SQL_DISKMEMORYSORTRATIO = "select (disk.value/mem.value)*100 \"ratio\" "
		+ "from v$sysstat mem, v$sysstat disk " + "where mem.name = 'sorts (memory)' and disk.name = 'sorts (disk)'";

	public static final String ORA_SQL_DATAFILE = "select df.name \"dfName\",df.bytes \"dfSize\",    df.status \"dfStatus\","
		+ "fs.PHYRDS \"dfRTS\", fs.PHYBLKRD PHYBLKRD,fs.PHYBLKWRT PHYBLKWRT,fs.PHYBLKRD+fs.PHYBLKWRT TOTALBLOCK,fs.PHYWRTS \"dfWRTS\",    fs.readtim \"dfRTim\",fs.writetim \"dfWRTim\"  "
		+ "from     V$DATAFILE df,V$FILESTAT fs  WHERE df.FILE# = fs.FILE#";

	public static final String ORA_SQL_ROLLBACK = " select a.name, b.rssize, round(100-nvl(b.waits*100/b.gets,0), 2) hitRate, "
		+ " b.shrinks, b.wraps, b.gets, b.extends, b.xacts, b.waits, b.status, b.aveshrink"
		+ " from v$rollname a, v$rollstat b where a.usn = b.usn";

	// SGA 高速缓冲区大小
	public static final String ORA_SQL_SGA_LIBRARYCACHE = "select bytes/1024/1024 \"size\" from v$sgastat where name ='buffer_cache' or name ='db_block_buffers' and rownum = 1";

	// SGA 重做日志缓冲区大小
	public static final String ORA_SQL_SGA_REDOLOG = "SELECT bytes/1024/1024 \"size\" from v$sgastat where name = 'log_buffer'";

	// SGA 共享池大小
	public static final String ORA_SQL_SGA_SHAREDPOOL = "select sum(bytes)/1024/1024 \"size\" from v$sgastat where pool= 'shared pool'";

	// SGA 数据字典缓存大小
	public static final String ORA_SQL_SGA_DICTIONCACHE = "select bytes/1024/1024 \"size\" from v$sgastat where name ='dictionary cache' or name = 'row cache'";

	// SGA 共享库缓存大小
	public static final String ORA_SQL_SGA_SHAREDCACHE = "select bytes/1024/1024 \"size\" from v$sgastat where name ='library cache'";

	// SGA SQL缓存大小
	public static final String ORA_SQL_SGA_SQLCACHE = "select bytes/1024/1024 \"size\" from v$sgastat where name ='sql area'";
	
	// SGA 命中率
	public static final String ORA_SQL_SGA_HIT_RATE = "select 100 - round((phy.value - lob.value - dir.value)/ses.value,2)*100 \"size\" FROM v$sysstat ses,v$sysstat lob,v$sysstat dir,v$sysstat phy WHERE  ses.name = 'session logical reads' AND dir.name = 'physical reads direct' AND lob.name = 'physical reads direct (lob)' AND phy.name = 'physical reads'";
	
	// 取得所有Session id
	public static final String ORA_SQL_SESS_SESSION = "select sid,username value from v$session where user# > 0 order by sid";
	
	// 查询ASM磁盘的状态
	public static final String ORA_SQL_ASM_DISK_STATE = "select path, mode_status from v$asm_disk where mode_status <> 'ONLINE'";
	
	// 查询ASM磁盘组的信息
	public static final String ORA_SQL_ASM_DISK_GROUP = "select name, round((total_mb-free_mb)/total_mb*100,1) usage_pct from v$asm_diskgroup";
	
	// 查询等待事件
	public static final String ORA_SQL_WAIT_EVENT = "with tmp as (select event,event_act_session,total_act_session,sessions,session_total from (select event,count(*) event_act_session from v$session where status='ACTIVE' and type='USER' and wait_class <>'idle' group by event) a, (select count(*) total_act_session from v$session where status='ACTIVE' and type='USER') b, (select count(*) sessions from v$session) c, (select value session_total from v$parameter where name='sessions') d) select tmp.event event, tmp.event_act_session act_session, round(tmp.event_act_session/tmp.total_act_session*100,1) rate from tmp where event_act_session/total_act_session > 1/3 and event_act_session >20 and (total_act_session/sessions > 1/2 or sessions/session_total > 0.3) and sessions > 50";
	
	// 查询锁等待时间
	public static final String ORA_SQL_LOCK_WAIT = "select sid, seconds_in_wait from v$session where seconds_in_wait > 1200";
	
	// 用户状态
	public static final String ORA_SQL_USER_STATE = "select username, account_status from dba_users where username not in ('ANONYMOUS', 'APEX_040200', 'APEX_PUBLIC_USER', 'APPQOSSYS', 'AUDSYS', 'CTXSYS', 'DBSNMP', 'DIP', 'DVF', 'DVSYS', 'EXFSYS', 'FLOWS_FILES', 'GSMADMIN_INTERNAL', 'GSMCATUSER','GSMUSER', 'LBACSYS', 'MDDATA', 'MDSYS', 'OJVMSYS', 'OLAPSYS', 'ORACLE_OCM', 'ORDDATA', 'ORDPLUGINS', 'ORDSYS', 'OUTLN', 'PDBADMIN', 'SI_INFORMTN_SCHEMA', 'SPATIAL_CSW_ADMIN_USR', 'SPATIAL_WFS_ADMIN_USR', 'SYS', 'SYSBACKUP', 'SYSDG', 'SYSKM', 'SYSTEM', 'WMSYS', 'XDB', 'XS$NULL') and account_status <>'OPEN'";
	
	/**
	 * Oracle Session 监测sql语句 应 ORA_SQL_SESS_MONITOR_HEAD + 条件 +
	 * ORA_SQL_SESS_MONITOR_TRAIL 例如： 获取Session 占用CPU时间 则将
	 * ORA_SQL_SESS_MONITOR_HEAD +
	 * " select statistic# from v$statname where name = 'CPU used by this session'"
	 * + ORA_SQL_SESS_MONITOR_TRAIL
	 */
	public static final String ORA_SQL_SESS_MONITOR_HEAD = "select ss.sid,ss.value from v$sesstat ss,v$session se,v$statname st where st.statistic# = ss.statistic# and  se.sid=ss.sid and se.user# > 0 and ";

	public static final String[] ORA_SQL_SESS_MONITORITEM = new String[] {
		"st.name = 'CPU used by this session'",
		"st.name = 'sorts (memory)'",
		"st.name in ('table scans (short tables)','table scans (long tables)','table scans (rowid ranges)','table scans (cache partitions)','table scans (direct read)')",
		"st.name = 'physical reads'", "st.name = 'physical writes'", "st.name = 'user commits'",
	"st.name = 'opened cursors cumulative'" };

	// 获取会话缓冲区命中率，应该大于90%
	public static final String ORA_SQL_SESS_RATIO = "select v$session.SID, ((CONSISTENT_GETS+BLOCK_GETS-PHYSICAL_READS) / (CONSISTENT_GETS+BLOCK_GETS))*100 value "
		+ "from  v$session, v$sess_io "
		+ "where   v$session.SID = v$sess_io.SID and v$session.user# > 0 and   (CONSISTENT_GETS+BLOCK_GETS) > 0 and   USERNAME is not null";

	/**
	 * 获取数据库基本配置信息（限制模式除外：版本兼容）
	 */
	public static final String ORA_SQL_DB_BASEINFO = "select ins.status,ins.host_name hostname,db.name dbname,ins.version version,ins.instance_name instancename,"
		+ "  ins.startup_time startupTime , db.log_mode logmode, t.version version1 "
		+ "  from v$instance ins,v$database db,product_component_version t";

	// 获取8数据库的读取模式
	public static final String ORA_8_SQL_DB_OPEN_MODE = "SELECT enabled from v$datafile where status='SYSTEM'";
	//获取数据库当前连接数
	public static final String ORA_SQL_DB_CURRENT_CONNS = "select count(*) from v$session where username is not null and status<>'KILLED'";
	// 获取数据库读取模式和数据库spfile文件路径
	public static final String ORA_SQL_DB_OPEN_MODE_SPFILE = "SELECT db.open_mode openmode,para.value spfile FROM v$database db,v$parameter para"
		+ "  where para.name = 'spfile'";

	// 获取数据库限制模式 oracle 7 版本
	public static final String ORA_SQL_DB_RESTRICTED7 = "SELECT * FROM v$instance WHERE key = 'RESTRICTED MODE'";

	// 获取数据库限制模式 oracle 8，9 版本
	public static final String ORA_SQL_DB_RESTRICTED89 = "SELECT logins FROM v$instance";

	// 获取数据库bit
	public static final String ORA_SQL_DB_BIT = "select distinct address from v$sql where rownum<2";

	// 获取数据库的并行状态
	public static final String ORA_SQL_DB_PARALLEL = "select value parallel from v$parameter where name = 'parallel_server'";

	// 获取归档路径
	public static final String ORA_SQL_DB_ARCHIVE = "select value archive from v$parameter where name like '%og_archive_dest%' and name not like '%log_archive_dest_state%'";

	// SQL效率
	public static final String ORA_SQL_DB_SQLINFO = "SELECT c.SID sid,b.username userName,a.SQL_TEXT sqlText,a.CPU_TIME/a.EXECUTIONS/1000 execTime,a.RUNTIME_MEM/1024 runtimeMem FROM v$sqlarea a, dba_users b, v$session c WHERE a.parsing_user_id = b.user_id and c.SQL_ADDRESS=a.ADDRESS and a.EXECUTIONS>0";

	// SQL效率,8版本
	public static final String ORA_SQL_DB_SQLINFO8 = "SELECT c.SID sid,b.username userName,a.SQL_TEXT sqlText,0 execTime,a.RUNTIME_MEM/1024 runtimeMem FROM v$sqlarea a, dba_users b, v$session c WHERE a.parsing_user_id = b.user_id and c.SQL_ADDRESS=a.ADDRESS and a.EXECUTIONS>0";
	// 获取PGA 信息
	public static final String ORA_SQL_PGA_INFO = "SELECT NAME ,VALUE,UNIT FROM V$PGASTAT";
	// 碎片FSFI比率
	public static final String ORA_SQL_PatchRate = "SELECT TABLESPACE_NAME, round(sqrt(max(blocks)/sum(blocks))*(100/sqrt(sqrt(count(blocks)))),2) FSFI "
		+ "FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME";

	// Oracle Instance 全表扫描信息
	public static final String ORA_SQL_FTS = "select name,value from v$sysstat where name in ('table scans (short tables)','table scans (long tables)','table scan rows gotten','table fetch by rowid')";

	// 撤销空间监测状态，查询无空间错误计数和快照太旧计数
	public static final String ORA_SQL_UNDOSTAT = "Select sum(nospaceerrcnt) as sumNospaceerrcnt, sum(ssolderrcnt) as sumSsolderrcnt from V$undostat t where t.END_TIME >";
	// 作业队列
	public static final String get_broken_num = "select count(*) as broken_num from dba_jobs where broken = 'Y'";

	public static final String get_failure_num = "select count(*) as failure_num from dba_jobs where failures != 0";

	public static final String get_overdue_num = "select count(*) as overdue_num from dba_jobs t where (select sysdate from dual) > t.NEXT_DATE";

	// 获取转储空间
	public static final String ORA_SQL_STORAGE = "select * from v$parameter where name = 'background_dump_dest' or name = 'user_dump_dest' or name = 'core_dump_dest'";

	// Oracle 递归调用信息
	public static final String ORA_SQL_RECURSION = "select name,value from v$sysstat where name in ('recursive calls','user calls')";
	// 获取指定队列的消息数
	public static final String ORA_QUEUE_INFO_NUM = "select WAITING+READY+EXPIRED cnt from gv$aq where qid=(select qid from user_queues where name=?)";
	// 监控每个队列中处于 READY 状态的消息数量
	public static final String ORA_QUEUE_READY_INFO_NUM = "select READY from gv$aq where qid=(select qid from user_queues where name=?)";
	// 监控每个队列中秒表示的每个消息的平均等待时间
	public static final String ORA_QUEUE_AVERAGE_WAIT = "select average_wait from gv$aq where qid=(select qid from user_queues where name=?)";
	// 获取所有的高级队列
	public static final String ORA_QUEUE_All_QUENAME = "select qid,name,queue_table from user_queues where QUEUE_TYPE='NORMAL_QUEUE'";
	public static final String ORA_QUEUE_All_QUE = "select ready,average_wait,WAITING+READY+EXPIRED cnt from gv$aq where qid=(select qid from user_queues where name=?)";

	// 获取oracle检查点开始次数，和完成次数
	public static final String ORA_CHECKPOINT_STARTD_COMPLETED = "Select * From V$sysstat Where Name In('background checkpoints started', 'background checkpoints completed')";

	/*
	 * 重做分配锁存和重做复制锁存的丢失量占其获得数的百分比(分成Willing-to-wait和immediate两种类型)
	 */
	public String ORA_SQL_REDOLOG = "select sum(misses) mis,sum(gets) gets,sum(immediate_misses) imm_mis, sum(immediate_gets) imm_gets , round(sum(misses)/sum(gets),3) willing_to_wait_ratio ,"
		+ "round(sum(immediate_misses)/sum(immediate_gets),3) immidiate_ratio from v$latch where name in ('redo allocation','redo copy')";

	// 重做日志缓冲中用户进程不能分配空间的次数
	public static final String ORA_SQL_REDO_UNLOAC = "select name,value from v$sysstat where name='redo buffer allocation retries'";

	// 归档的重做日志文件的数目,重做条目的平均大小
	public static final String ORA_SQL_REDO_ARCHLOG = "select counts, decode(counts,0,0,total_size/counts) ave from "
		+ "(select count(*) counts, NVL(sum(BLOCKS * BLOCK_SIZE / 1024 / 1024), 0) total_size from v$archived_log where creator = 'ARCH')";

	// 获取Oracle数据库的归档日志模式
	public static final String ORA_SQL_ARCHIVE_LOG_MODE = "select log_mode from v$database";
	// 获取归档目的地信息
	public static final String ORA_SQL_ARCHIVE_DEST = "select dest_id,dest_name, status,binding,target,destination,error from v$archive_dest order by dest_id";
	// 获取FlashBack区的信息
	public static final String ORA_SQL_FLASHBACK = "select name,space_limit,space_used from v$recovery_file_dest";

	private String ip;
	private OracleMethod method;

	private Connection inConn = null;

	public OracleManager(String ip, OracleMethod method) {
		this.ip = ip;
		this.method = method;
	}

	/**
	 * 初始化数据库链接
	 * 
	 * @throws SQLException
	 */
	public void initConnection() throws SQLException {
		if (inConn == null) {
			inConn = getConnectionChkRole();
		}
	}

	/**
	 * 初始化数据库链接
	 * 
	 * @throws SQLException
	 */
	public void initConnectionIgnoreRole() throws SQLException {
		if (inConn == null) {
			inConn = getConnectionIgnoreRole();
		}
	}

	/**
	 * 获取链接并检查权限
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnectionChkRole() throws SQLException {
		Connection conn = getConnectionIgnoreRole();

		// 检查该用户是否有DBA或OEM_MONITOR角色,当视图v$session存在时,表示有该角色；不存在,无该角色.
		String sql = "select count(*) from v$session";
		Statement stm = null;
		boolean hasRole = false;
		try {
			stm = conn.createStatement();
			stm.execute(sql);
			hasRole = true;
		} catch (SQLException ex) {
			ErrorUtil.warn(logger, "无法查询v$session视图", ex);
		} finally {
			OracleJDBCUtil.close(stm);
		}

		if (!hasRole) {
			OracleJDBCUtil.close(conn);
			throw new LogonDeniedException("指定用户无DBA或OEM_MONITOR角色.");
		}
		return conn;
	}

	/**
	 * 创建并返回链接
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnectionIgnoreRole() throws SQLException {
		if (method.getUsername() == null || method.getUsername().equals("")) {
			throw new MonitorException("未指定数据库用户.");
		}

		int errCode = 0; // 0表示正常
		
		String url = null;
		if(!StringUtil.isNullOrBlank(method.getServiceName()))   // 配置了service_name
			url = OracleUrlUtil.getUrl(ip, method.getPort(), method.getServiceName(), true);
		else
			url = OracleUrlUtil.getUrl(ip, method.getPort(), method.getSid(), false);
		Exception exception = null;
		Connection conn = null;
		try {
			conn = OracleJDBCUtil.createConnection(url, method.getUsername(), method.getPassword());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("无法获取Oracle驱动.");
		} catch (SQLException ex) {
			errCode = ((SQLException)ex.getCause())!=null?((SQLException)ex.getCause()).getErrorCode():0;
			exception = ex;
			OracleJDBCUtil.close(conn);
		}

		// 根据错误号来判断错误类型
		// 17002 IO 异常，因为前面已经判断端口可以连接，那么原因只能端口不是Oracle监听端口
		// 12505 实例名错误
		// 1034 ORACLE not available,数据库没打开
		// 1017 用户名或密码错误
		String errMsg = ((exception == null || exception.getMessage() == null) ? "" : exception.getMessage());
		if (errMsg.indexOf("ORA-12505") >= 0 || errMsg.indexOf("ERR=12505") >= 0) {
			errCode = 12505;
		}
		if (errCode == 12505) {
			SQLException se = new SQLException("连接失败,可能是实例名错误.oracle错误号:ORA-" + errCode);
			se.initCause(exception);
			throw se;
		} else if (errCode == 17002) {
			SQLException se = new SQLException("连接失败,可能指定的端口不是Oracle监听端口.oracle错误号:ORA-" + errCode);
			se.initCause(exception);
			throw se;
		} else if (errCode == 1017) {
			LogonDeniedException le = new LogonDeniedException("用户名/密码错误,连接失败.oracle错误号:ORA-" + errCode);
			le.initCause(exception);
			throw le;
		} else if (errCode != 0) {
			SQLException se = new SQLException("数据库没有装载或打开,错误:" + exception.getMessage() + ".oracle错误号:ORA-" + errCode);
			se.initCause(exception);
			throw se;
		} else if (exception != null) {// 如果有未知异常则继续抛出
			SQLException se = new SQLException("连接失败,请查看实例名或相关配置是否正确.");
			se.initCause(exception);
			throw se;
		} else if (conn == null) {// 判断连接是否有效
			SQLException se = new SQLException("连接失败,请检查配置是否正确.");
			throw se;
		}

		return conn;
	}

	/**
	 * 内部获取链接用
	 * 
	 * @return
	 */
	private Connection getConnection() {
		if (inConn == null) {
			throw new NullPointerException("数据库链接没有初始化,请先初始化.");
		}
		return inConn;
	}

	/**
	 * 获取所有表空间信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<OracleTableSpace> getAllTableSpaces() throws SQLException {
		List<OracleTableSpace> tsList = new ArrayList<OracleTableSpace>();
		PreparedStatement ps = null;
		PreparedStatement psIoAvg = null;
		ResultSet rs = null;
		ResultSet rsIoAvg = null;
		Map<String, double[]> ioAvgMap = new HashMap<String, double[]>();
		try {
			Connection conn = getConnection();
			psIoAvg = conn.prepareStatement(ORA_SQL_TABLESPACE_IO_AVG);
			rsIoAvg = psIoAvg.executeQuery();
			while (rsIoAvg.next()) {
				if (rsIoAvg.getString("NAME") != null) {
					double[] avgs = new double[2];
					avgs[0] = rsIoAvg.getDouble("AVGREADTIM");
					avgs[1] = rsIoAvg.getDouble("AVGWRITTIM");
					ioAvgMap.put(rsIoAvg.getString("NAME"), avgs);
				}
			}

			ps = conn.prepareStatement(ORA_SQL_TABLESAPCE);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleTableSpace ots = new OracleTableSpace();
				ots.setTsName(rs.getString("TABLESPACE_NAME") == null ? null : rs.getString("TABLESPACE_NAME").trim());
				ots.setTotalSize(new Double(rs.getDouble("SPACE")));
				ots.setCurAvailTS(new Double(rs.getDouble("FREE_RATE")));
				ots.setExtentCount(new Double(rs.getDouble("EXTENTS_COUNT")));
				ots.setMaxExtents(new Double(rs.getDouble("MAX_EXTENTS")));
				ots.setSegmentManagementType(rs.getString("SEGMENT_SPACE_MANAGEMENT") == null ? null : rs.getString(
				"SEGMENT_SPACE_MANAGEMENT").trim());
				ots.setSpaceType(rs.getString("CONTENTS") == null ? null : rs.getString("CONTENTS").trim());
				ots.setFreeExtents(new Double(rs.getDouble("FREE_EXTENTS")));
				if (ioAvgMap.get(ots.getTsName()) != null) {
					double[] avgs = (double[]) ioAvgMap.get(ots.getTsName());
					ots.setAvgReadTim(new Double(avgs[0]));
					ots.setAvgWriteTim(new Double(avgs[1]));
				}
				ots.setNextExtent(new Double(rs.getDouble("NEXT_EXTENT") / 1024));
				ots.setMaxSpace(rs.getDouble("MAX_SPACE"));
				ots.setAutoExtend(rs.getString("MAX_AUTOEXTENSIBLE"));
				tsList.add(ots);
			}
		} finally {
			OracleJDBCUtil.close(rsIoAvg);
			OracleJDBCUtil.close(psIoAvg);
			OracleJDBCUtil.close(rs, ps);
		}
		return tsList;

	}

	/**
	 * 仅获取所有表空间名和空间大小,用于自动发现
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List getAllSpacesNameAndSize() throws SQLException {
		List tsList = new ArrayList();
		PreparedStatement ps = null;
		PreparedStatement psIoAvg = null;
		ResultSet rs = null;
		ResultSet rsIoAvg = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_TABLESAPCENAME);

			rs = ps.executeQuery();
			while (rs.next()) {
				OracleTableSpace ots = new OracleTableSpace();
				ots.setTsName(rs.getString("TABLESPACE_NAME") == null ? null : rs.getString("TABLESPACE_NAME").trim());
				ots.setTotalSize(new Double(rs.getDouble("SPACE")));
				tsList.add(ots);
			}
		} finally {
			OracleJDBCUtil.close(rsIoAvg);
			OracleJDBCUtil.close(psIoAvg);
			OracleJDBCUtil.close(rs, ps);
		}
		return tsList;

	}

	/**
	 * 根据时间获取锁定的资源
	 * 
	 * @param ctime
	 * @return
	 * @throws SQLException
	 */
	
	public List getAllLocksByTime(int ctime) throws SQLException {
		List lockList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_LOCK);
			ps.setInt(1, ctime);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleLock ol = new OracleLock();
				ol.setOwner(rs.getString("OWNER"));
				ol.setObjName(rs.getString("OBJECT_NAME"));
				ol.setObjType(rs.getString("OBJECT_TYPE"));
				ol.setCtime(rs.getInt("CTIME"));
				lockList.add(ol);
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return lockList;
	}

	/**
	 * 获取数据库高速缓存区命中率
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getCacheHitRatio() throws SQLException {
		double ratio = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_CACHEHITRATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 获取数据库共享区库缓存区命中率
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getReloadsToPinsRatio() throws SQLException {
		double ratio = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_RELOADSTOPINSRATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 获取数据库共享区字典缓存区命中率
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getDictionaryCacheRatio() throws SQLException {
		double ratio = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_DICTIONARYCACHERATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 获取数据库回退段等待次数/获取次数比率
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getRollbackSegmentHeaderRatio() throws SQLException {
		double ratio = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_ROLLBACKSEGMENTHEADER);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 获取数据库磁盘排序与内存排序之比
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getDiskMemorySortRatio() throws SQLException {
		double ratio = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_DISKMEMORYSORTRATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 判断获能否正确连接到数据库，不考虑数据库用户的角色
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean testConnectionIgnoreRole() throws SQLException {
		boolean success = false;
		Connection conn = null;
		try {
			conn = getConnectionIgnoreRole();
			success = true;
		} catch (SQLException sqlex) {
			throw sqlex;
		} finally {
			OracleJDBCUtil.close(conn);
		}
		return success;
	}

	/**
	 * 获取OracleRman备份情况(为广东公安定制开发)
	 * 
	 * @return true: 备份成功； false：备份失败
	 * @throws SQLException
	 */
	public boolean getRmanBakStatus() throws SQLException {
		String desc = getRmanBakDesc();
		if (desc.length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 获取OracleRman备份情况描述(为广东公安定制开发)
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String getRmanBakDesc() throws SQLException {
		StringBuffer sb = new StringBuffer();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(OracleRmanSqlBuilder.getSql("BakStatus"));
			rs = ps.executeQuery();
			while (rs.next()) {
				sb.append("bskey：" + rs.getLong("bs_key")).append(
						"，状态：" + getRmanBakStatus(rs.getString("status")) + "。\n");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return sb.toString();
	}

	/**
	 * OracleRman备份状态翻译(为广东公安定制开发)
	 * 
	 * @param status
	 * @return
	 */
	private String getRmanBakStatus(String status) {
		if (status == null)
			return "";
		if (status.equals("D")) {
			return "全部备份失败";
		} else if (status.equals("0")) {
			return "备份不可用";
		} else {
			return "";
		}
	}

	/**
	 * OracleRman备份查看当天全备份大小(为广东公安定制开发)
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getRmanFullBak() throws SQLException {
		double fullBak = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(OracleRmanSqlBuilder.getSql("FullBak"));
			rs = ps.executeQuery();
			while (rs.next()) {
				fullBak = rs.getDouble("full_bk_size_M");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return fullBak;
	}

	/**
	 * OracleRman备份查看当天增量备份大小(为广东公安定制开发)
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getRmanIncBak() throws SQLException {
		double incBak = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(OracleRmanSqlBuilder.getSql("IncBak"));
			rs = ps.executeQuery();
			while (rs.next()) {
				incBak = rs.getDouble("arch_bk_size_M");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return incBak;
	}

	/**
	 * 获取所有数据文件信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List getAllDtfiles() throws SQLException {
		List dtfileList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_DATAFILE);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleDtfile oDtfile = new OracleDtfile();
				oDtfile.setDfName(rs.getString("dfName") == null ? null : rs.getString("dfName").trim());
				oDtfile.setDfSize(new Double(rs.getDouble("dfSize") / 1024 / 1024));
				oDtfile.setDfStatus(rs.getString("dfStatus"));
				oDtfile.setDfRTS(new Double(rs.getDouble("dfRTS")));
				oDtfile.setDfWRTS(new Double(rs.getDouble("dfWRTS")));
				oDtfile.setDfRTim(new Double(rs.getDouble("dfRTim")));
				oDtfile.setDfWRTim(new Double(rs.getDouble("dfWRTim")));
				oDtfile.setPhyblkrd(new Double(rs.getDouble("PHYBLKRD")));
				oDtfile.setPhyblkwrt(new Double(rs.getDouble("PHYBLKWRT")));
				oDtfile.setTotalBlock(new Double(rs.getDouble("TOTALBLOCK")));
				dtfileList.add(oDtfile);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return dtfileList;
	}

	/**
	 * 取得回滚段信息列表
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<OracleRollback> getAllRolls() throws SQLException {
		List<OracleRollback> rollList = new ArrayList<OracleRollback>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_ROLLBACK);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleRollback roll = new OracleRollback();
				roll.setRollName(rs.getString("name") == null ? null : rs.getString("name").trim());
				roll.setRssize(NumberUtil.round(rs.getDouble("rssize") / 1024 / 1024, 2));
				roll.setHitRate(new Double(rs.getString("hitRate")));
				roll.setShrinkCount(rs.getInt("shrinks"));
				roll.setWrapCount(rs.getInt("wraps"));
				roll.setGetCount(rs.getInt("gets"));
				roll.setAveShrink(NumberUtil.round(rs.getDouble("aveshrink") / 1024 / 1024, 2));
				roll.setExtendCount(rs.getInt("extends"));
				roll.setXactCount(rs.getInt("xacts"));
				roll.setWaitCount(rs.getInt("waits"));
				roll.setStatusStr(rs.getString("status"));
				rollList.add(roll);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return rollList;
	}

	/**
	 * SGA 高速缓冲区大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGALibraryCacheSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_LIBRARYCACHE);
	}

	/**
	 * SGA 重做日志缓冲区大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGARedoLogCacheSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_REDOLOG);
	}

	/**
	 * SGA 共享池大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGASharedPoolSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_SHAREDPOOL);
	}

	/**
	 * SGA 数据字典缓存大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGADictionCacheSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_DICTIONCACHE);
	}

	/**
	 * SGA 共享库缓存大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGASharedCacheSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_SHAREDCACHE);
	}

	/**
	 * SGA SQL缓存大小
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGASqlCacheSize() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_SQLCACHE);
	}
	
	/**
	 * SGA 命中率
	 * 
	 * @return
	 * @throws SQLException
	 */
	public double getSGAHitRate() throws SQLException {
		return getSGAParamSize(ORA_SQL_SGA_HIT_RATE);
	}

	/**
	 * 取得 SGA 各个监测参数大小
	 * 
	 * @param paraStr
	 *            检测语句
	 * @return
	 * @throws SQLException
	 */
	private double getSGAParamSize(String paraStr) throws SQLException {
		double size = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(paraStr);
			rs = ps.executeQuery();
			if (rs.next()) {
				size = rs.getDouble("size");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return size;
	}

	/**
	 * 取得所有Oracle会话对象
	 * 
	 * @return
	 */
	public List getAllSessInfos() throws SQLException {
		List sessInfoList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map[] sessMap = new HashMap[9];
		try {
			Connection conn = getConnection();
			for (int i = 0; i < sessMap.length; i++) {
				sessMap[i] = new HashMap();
				try {
					ps = conn.prepareStatement(getSessMonitorSql(i));
					rs = ps.executeQuery();
					if (i > 0) {
						while (rs.next()) {
							sessMap[i].put(Integer.toString(rs.getInt("sid")), new Double(rs.getDouble("value")));
						}
					} else {
						while (rs.next()) {
							sessMap[i].put(Integer.toString(rs.getInt("sid")), rs.getString("value"));
						}
					}
				} finally {
					OracleJDBCUtil.close(rs, ps);
					rs = null;
					ps = null;
				}
			}
			// 对会话SID排序
			List keys = new ArrayList(sessMap[0].keySet());
			Collections.sort(keys, new Comparator() {
				public int compare(Object o1, Object o2) {
					try {
						int v1 = Integer.parseInt((String) o1);
						int v2 = Integer.parseInt((String) o2);
						return (int) (v1 - v2);
					} catch (Exception ee) {
						return 0;
					}
				}
			});

			for (Iterator iter = keys.iterator(); iter.hasNext();) {
				OracleSessInfo sess = new OracleSessInfo();
				String key = (String) iter.next();
				sess.setSessId(key);
				sess.setUserName(sessMap[0].get(key).toString());
				sess.setSessCpu((Double) sessMap[1].get(key));
				sess.setSessSorts((Double) sessMap[2].get(key));
				sess.setTableScans((Double) sessMap[3].get(key));
				sess.setSessReads((Double) sessMap[4].get(key));
				sess.setSessWrites((Double) sessMap[5].get(key));
				sess.setSessCommits((Double) sessMap[6].get(key));
				sess.setSessCursors((Double) sessMap[7].get(key));
				sess.setSessRatio((Double) sessMap[8].get(key));

				sessInfoList.add(sess);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return sessInfoList;
	}

	/**
	 * 为提高监测性能的查询速度，各个语句分开来执行
	 * 
	 * @param index
	 * @return
	 */
	private String getSessMonitorSql(int index) {
		switch (index) {
		case 0:
			return ORA_SQL_SESS_SESSION;
		case 1:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[0];
		case 2:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[1];
		case 3:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[2];
		case 4:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[3];
		case 5:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[4];
		case 6:
			return ORA_SQL_SESS_MONITOR_HEAD + ORA_SQL_SESS_MONITORITEM[5];
		case 7:
			return "select o.sid, osuser, machine, count(*) value from v$open_cursor o, v$session s where o.sid=s.sid group by o.sid, osuser, machine";
		case 8:
			return ORA_SQL_SESS_RATIO;
		default:
			return "";
		}
	}

	/**
	 * 获取数据库基本信息对象
	 * 
	 * @return
	 */
	public Map<String, Object> getOracleBaseInfo() throws SQLException {
		Map<String, Object> orclBase = new HashMap<String, Object>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_DB_BASEINFO);
			rs = ps.executeQuery();
			if (rs.next()) {
				String version = rs.getString("version") == null || "".equals(rs.getString("version").trim())
				|| rs.getString("version").trim().length() == 0 ? rs.getString("version1") : rs.getString("version");
				orclBase.put(OracleBaseConfiger.keys[0], rs.getString("status"));
				orclBase.put(OracleBaseConfiger.keys[1], rs.getString("hostname"));
				orclBase.put(OracleBaseConfiger.keys[2], rs.getString("dbname"));
				orclBase.put(OracleBaseConfiger.keys[3], version);
				orclBase.put(OracleBaseConfiger.keys[6], rs.getString("instancename"));
				orclBase.put(OracleBaseConfiger.keys[7], rs.getDate("startupTime"));
				orclBase.put(OracleBaseConfiger.keys[9], rs.getString("logmode"));
				//获取当前连接数
				ps2 = conn.prepareStatement(ORA_SQL_DB_CURRENT_CONNS);
				rs2 = ps2.executeQuery();
				if (rs2.next())
					orclBase.put(OracleBaseConfiger.keys[13], rs2.getLong("count(*)"));
				// 如数据库是8版本则不获取数据库spfile路径\
				String dbVersion = null;
				try {
					dbVersion = orclBase.get("dbVersion").toString().substring(0, 1);
				} catch (Exception e) {
					logger.warn("未知异常", e);
				}
				if ("8".equalsIgnoreCase(dbVersion)) {
					ps2 = conn.prepareStatement(ORA_8_SQL_DB_OPEN_MODE);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						orclBase.put(OracleBaseConfiger.keys[11], rs2.getString("enabled"));
					}
					orclBase.put(OracleBaseConfiger.keys[12], "");
				} else {
					ps2 = conn.prepareStatement(ORA_SQL_DB_OPEN_MODE_SPFILE);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						orclBase.put(OracleBaseConfiger.keys[11], rs2.getString("openmode"));
						orclBase.put(OracleBaseConfiger.keys[12], rs2.getString("spfile"));
					}
				}
				// 取得数据库限制模式配置
				if ("7".equalsIgnoreCase(dbVersion)) {
					ps2 = conn.prepareStatement(ORA_SQL_DB_RESTRICTED7);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						if (4096 == rs2.getInt("value")) {
							orclBase.put(OracleBaseConfiger.keys[8], "是");
						} else {
							orclBase.put(OracleBaseConfiger.keys[8], "否");
						}
					}
				} else {
					ps2 = conn.prepareStatement(ORA_SQL_DB_RESTRICTED89);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						if ("RESTRICTED".equalsIgnoreCase(rs2.getString("logins"))) {
							orclBase.put(OracleBaseConfiger.keys[8], "是");
						} else {
							orclBase.put(OracleBaseConfiger.keys[8], "否");
						}
					}
				}
			}
			// 获取数据库位数
			ps = conn.prepareStatement(ORA_SQL_DB_BIT);
			rs = ps.executeQuery();
			if (rs.next()) {
				int len = rs.getString("address").length();
				if (len == 16) {
					orclBase.put(OracleBaseConfiger.keys[4], "64");
				} else if (len == 8) {
					orclBase.put(OracleBaseConfiger.keys[4], "32");
				}
			}
			// 获取数据库的并行状态
			ps = conn.prepareStatement(ORA_SQL_DB_PARALLEL);
			rs = ps.executeQuery();
			if (rs.next()) {
				if ("false".equalsIgnoreCase(rs.getString("parallel"))) {
					orclBase.put(OracleBaseConfiger.keys[5], "否");
				} else {
					orclBase.put(OracleBaseConfiger.keys[5], "是");
				}
			}
			// 获取归档路径
			ps = conn.prepareStatement(ORA_SQL_DB_ARCHIVE);
			rs = ps.executeQuery();
			StringBuffer archive = new StringBuffer();
			int idx = 0;
			while (rs.next()) {
				String arch = rs.getString("archive");
				if (arch != null) {
					if (idx > 0)
						archive.append(";");
					archive.append(rs.getString("archive"));
					idx++;
				}
			}
			orclBase.put(OracleBaseConfiger.keys[10], archive.toString());
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs2);
			OracleJDBCUtil.close(ps2);
			OracleJDBCUtil.close(rs, ps);
		}
		return orclBase;
	}

	public List<OracleSqlInfo> getAllSqlInfo() throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		List<OracleSqlInfo> retList = new ArrayList<OracleSqlInfo>();
		try {
			Connection conn = getConnection();
			ps2 = conn.prepareStatement(ORA_SQL_DB_BASEINFO);
			rs2 = ps2.executeQuery();

			if (rs2.next()) {
				// 如数据库是8版本则不获取数据库CPU_TIME
				if ("8".equalsIgnoreCase(rs2.getString("version").toString().substring(0, 1))) {
					ps = conn.prepareStatement(ORA_SQL_DB_SQLINFO8);
				} else {
					ps = conn.prepareStatement(ORA_SQL_DB_SQLINFO);
				}
				rs = ps.executeQuery();
			}
			if (rs == null) {
				throw new SQLException("获取数据库版本信息出错");
			}
			while (rs.next()) {
				OracleSqlInfo info = new OracleSqlInfo();
				info.setExecTime(rs.getDouble("execTime"));
				info.setRuntimeMem(rs.getDouble("runtimeMem"));
				info.setSid(rs.getString("sid"));
				info.setSqlText(rs.getString("sqlText"));
				info.setUserName(rs.getString("userName"));
				retList.add(info);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs2);
			OracleJDBCUtil.close(ps2);
			OracleJDBCUtil.close(rs, ps);
		}

		return retList;
	}

	/**
	 * 取得PGA信息列表
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<OraclePGAInfo> getPgaInfos() throws SQLException {
		List<OraclePGAInfo> pgaList = new ArrayList<OraclePGAInfo>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_PGA_INFO);
			rs = ps.executeQuery();
			while (rs.next()) {
				OraclePGAInfo pga = new OraclePGAInfo();
				pga.setName(rs.getString("name") == null ? null : rs.getString("name").trim());
				pga.setCurrValue(new Double(rs.getDouble("value")));
				String unit = rs.getString("unit");
				if (unit == null || unit.trim().length() == 0) {
					unit = "";
				}
				if ("bytes".equalsIgnoreCase(unit.trim())) {
					pga.setCurrValue(new Double(rs.getDouble("value") / 1024 / 1024));
					unit = "MB";
				}
				if ("percent".equalsIgnoreCase(unit.trim())) {
					unit = "%";
				}
				pga.setUnit(unit);
				pgaList.add(pga);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return pgaList;
	}

	/**
	 * 获取oracle数据库碎片FSFI使用情况
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List getAllPatchRate() throws SQLException {
		List tsList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_PatchRate);
			rs = ps.executeQuery();
			while (rs.next()) {
				OraclePatchRate ots = new OraclePatchRate();
				ots.setTsName(rs.getString("TABLESPACE_NAME") == null ? null : rs.getString("TABLESPACE_NAME").trim());
				ots.setCurrFSFI(new Double(rs.getDouble("FSFI")));
				tsList.add(ots);
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return tsList;

	}

	/**
	 * 获得Oracle全表扫描信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public OracleFTSInfo getOracleFTSInfo() throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		OracleFTSInfo info = new OracleFTSInfo();
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_FTS);
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				long value = rs.getLong("value");
				if ("table scans (short tables)".equals(name)) {
					info.setStScanTimes(value);
				} else if ("table scans (long tables)".equals(name)) {
					info.setLtScanTimes(value);
				} else if ("table scan rows gotten".equals(name)) {
					info.setFtsRows(value);
				} else if ("table fetch by rowid".equals(name)) {
					info.setFbiRows(value);
				}
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return info;
	}

	/**
	 * //撤销空间监测状态，查询无空间错误计数和快照太旧计数
	 * 
	 * @return
	 * @throws SQLException
	 */
	public int[] getUndoStat(String time) throws SQLException {
		int[] result = new int[] { 0, 0 };
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			String sql = ORA_SQL_UNDOSTAT + "to_date('" + time + "','yyyy-mm-dd hh24:mi:ss')";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				result[0] = rs.getInt("sumNospaceerrcnt");
				result[1] = rs.getInt("sumSsolderrcnt");
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return result;
	}

	/**
	 * 关闭并释放链接
	 */
	public void close() {
		if (inConn != null) {
			OracleJDBCUtil.close(inConn);
			inConn = null;
		}
	}

	/**
	 * 获取破损作业数目
	 * 
	 * @return
	 */
	public int getWorkQueueBrokenNum() throws SQLException {
		int broken_num = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(get_broken_num);
			rs = ps.executeQuery();
			while (rs.next()) {
				broken_num = rs.getInt("broken_num");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return broken_num;
	}

	/**
	 * 获取失败作业数目
	 * 
	 * @return
	 */
	public int getWorkQueueFailureNum() throws SQLException {
		int broken_num = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(get_failure_num);
			rs = ps.executeQuery();
			while (rs.next()) {
				broken_num = rs.getInt("failure_num");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return broken_num;
	}

	/**
	 * 获取过期作业数目
	 * 
	 * @return
	 */
	public int getWorkQueueOverdueNum() throws SQLException {
		int broken_num = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(get_overdue_num);
			rs = ps.executeQuery();
			while (rs.next()) {
				broken_num = rs.getInt("overdue_num");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return broken_num;
	}

	public Map<String, String> getOracleStorageInfo() throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, String> map = new HashMap<String, String>();

		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_STORAGE);
			rs = ps.executeQuery();

			while (rs.next()) {
				map.put(rs.getString("name"), rs.getString("value"));
			}
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}

		return map;
	}

	/**
	 * 获取递归调用信息实体类
	 * 
	 * @return
	 * @throws SQLException
	 */
	public OracleRecursionInfo getRecursionInfo() throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		OracleRecursionInfo info = new OracleRecursionInfo();
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_RECURSION);
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				long value = rs.getLong("value");
				if ("user calls".equals(name)) {
					info.setUser_call_num(value);
				} else if ("recursive calls".equals(name)) {
					info.setRecursion_call_num(value);
				}
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return info;
	}

	/**
	 * select语句返回值.
	 */
	public List getSqlReturnValue(String selSql) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Object> retList = new ArrayList<Object>();

		try {
			Connection conn = getConnection();
			if (!StringUtil.isNullOrBlank(selSql) && selSql.endsWith(";")) {
				selSql = selSql.substring(0, selSql.length() - 1);
			}
			ps = conn.prepareStatement(selSql);
			try {
				rs = ps.executeQuery();
			} catch (SQLException ex) {
				throw new SQLException("select语句执行错误");
			}

			// if (rs == null) {
			// return null;
			// }

			while (rs.next()) {
				retList.add(rs.getObject(1));
				// return rs.getObject(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs);
			OracleJDBCUtil.close(ps);
		}
		return retList;
	}

	/**
	 * 获取指定队列的消息数量.
	 * 
	 * @return Double
	 * @throws SQLException
	 */
	public Double getOracleQueueInfoNum(String queueName) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_QUEUE_INFO_NUM);
			ps.setString(1, queueName);
			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getDouble(1);
			} else {
				return new Double(0);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}
	}

	/**
	 * 获取指定队列的READY的消息数量.
	 * 
	 * @return Double
	 * @throws SQLException
	 */
	public Double getOracleQueueReadyInfoNum(String queueName) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_QUEUE_READY_INFO_NUM);
			ps.setString(1, queueName);
			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getDouble(1);
			} else {
				return new Double(0);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}
	}

	/**
	 * 获取指定队列的READY的消息数量.
	 * 
	 * @return Double
	 * @throws SQLException
	 */
	public Double getOracleQueueAvgWait(String queueName) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_QUEUE_AVERAGE_WAIT);
			ps.setString(1, queueName);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getDouble(1);
			} else {
				return new Double(0);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}
	}

	/**
	 * 获取所有高级队列
	 * 
	 * @return Double
	 * @throws SQLException
	 */
	public List<OracleAdvanceQue> getAllOracleQueues(boolean isWithValue) throws SQLException {
		List allQues = new ArrayList();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_QUEUE_All_QUENAME);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleAdvanceQue que = new OracleAdvanceQue();
				que.setQid(String.valueOf(rs.getInt("qid")));
				que.setQueName(rs.getString("name"));
				que.setQueOwner(this.getRealUser());
				que.setQueTable(rs.getString("queue_table"));
				allQues.add(que);
			}
			OracleJDBCUtil.close(rs, ps);
			ps = null;
			rs = null;

			if (isWithValue) {
				for (Iterator it = allQues.iterator(); it.hasNext();) {
					OracleAdvanceQue advanceQue = (OracleAdvanceQue) it.next();
					ResultSet rSet = null;
					ps = conn.prepareStatement(ORA_QUEUE_All_QUE);
					ps.setString(1, advanceQue.getQueName());
					rSet = ps.executeQuery();
					while (rSet.next()) {
						advanceQue.setMsgTotalNum(new Integer(rSet.getInt("cnt")));
						advanceQue.setReadyMsgNum(new Integer(rSet.getInt("ready")));
						advanceQue.setArvWaitTime(new Double(rSet.getInt("average_wait")));
						// 取得异常
						ResultSet rsErr = null;
						try {
							String sql = "select Count(*) from " + advanceQue.getQueOwner() + ".aq$"
							+ advanceQue.getQueTable() + " where queue='" + advanceQue.getQueName()
							+ "' and ((EXCEPTION_QUEUE_OWNER is not null) or (EXCEPTION_QUEUE is not null))";
							ps = conn.prepareStatement(sql);
							rsErr = ps.executeQuery();
							int errNum = 0;
							if (rsErr.next()) {
								errNum = rsErr.getInt(1);
							} else {
								errNum = new Integer(0);
							}
							advanceQue.setErrMsgNum(errNum);
						} catch (SQLException e) {
							advanceQue.setErrMsgNum(-1);
						} finally {
							OracleJDBCUtil.close(rsErr, ps);
							ps = null;
							rsErr = null;
						}
					}
					OracleJDBCUtil.close(rSet, ps);
					ps = null;
					rSet = null;
				}
			}
			return allQues;
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}
	}

	public int[] getCheckpoint() throws SQLException {
		int checkpointArr[] = new int[2];
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ORA_CHECKPOINT_STARTD_COMPLETED);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("name").equals("background checkpoints started")) {
					checkpointArr[0] = rs.getInt("value");
				} else if (rs.getString("name").equals("background checkpoints completed")) {
					checkpointArr[1] = rs.getInt("value");
				}
			}
			return checkpointArr;
		} catch (SQLException e) {
			throw e;
		} finally {
			OracleJDBCUtil.close(rs, ps, conn);
		}
	}

	/**
	 * 获取Oracle Redo日志信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<RedoLogInfo> getRedoLog() throws SQLException {
		List<RedoLogInfo> redos = new ArrayList<RedoLogInfo>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ORA_SQL_REDO_ARCHLOG);
			rs = ps.executeQuery();
			if (rs.next()) {
				buildRedoLog(OracleRedoLogParameter.FIELD_ARCHCOUNT, rs.getInt("counts"), 100, "次", true, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_AVESIZE, rs.getDouble("ave"), 50, "M", true, redos);
			}
			ps = conn.prepareStatement(ORA_SQL_REDO_UNLOAC);
			rs = ps.executeQuery();
			if (rs.next()) {
				buildRedoLog(OracleRedoLogParameter.FIELD_UNALLOCS, rs.getInt("value"), 100, "次", true, redos);
			}
			OracleJDBCUtil.close(rs, ps);
			ps = null;
			rs = null;
			ps = conn.prepareStatement(ORA_SQL_REDOLOG);
			rs = ps.executeQuery();
			if (rs.next()) {
				buildRedoLog(OracleRedoLogParameter.FIELD_TOTAL_MIS, rs.getLong("mis"), 0, "次", false, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_TOTAL_GETS, rs.getLong("gets"), 0, "次", false, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_TOTAL_IMM_MIS, rs.getLong("imm_mis"), 0, "次", false, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_TOTAL_IMM_GETS, rs.getLong("imm_gets"), 0, "次", false, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_WILLING_TO_WAIT_RATIO, rs.getDouble("willing_to_wait_ratio"),
						3, "%", true, redos);
				buildRedoLog(OracleRedoLogParameter.FIELD_IMMIDIATE_RATIO, rs.getDouble("immidiate_ratio"), 3, "%",
						true, redos);
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return redos;
	}

	private void buildRedoLog(String name, double value, double thresholdValue, String unit, boolean isShow,
			List<RedoLogInfo> redos) {
		RedoLogInfo redo = new RedoLogInfo();
		redo.setName(name);
		redo.setCurrValue(value);
		redo.setIsShowInColumn(isShow);
		redo.setUnit(unit);
		redo.setThresholdValue(thresholdValue);
		redos.add(redo);
	}

	private String getRealUser() {
		int index = method.getUsername().indexOf("as");
		if (index >= 0) {
			return method.getUsername().substring(0, index).trim();
		}
		return method.getUsername();
	}
	// 获取Oracle磁盘组信息
	public static final String ORA_SQL_ASM = "select name instanceName,free_mb availableSize,total_mb totalSize,trunc((total_mb-free_mb)/total_mb*100,2) useRate from v$asm_disk";
	public List<OracleDiskASM> getOracleDiskASMList() throws SQLException {
		Map<String, OracleDiskASM> map = new HashMap<String, OracleDiskASM>();
		List<OracleDiskASM> list = new ArrayList<OracleDiskASM>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_ASM);
			rs = ps.executeQuery();
			OracleDiskASM condition = null;
			while (rs.next()) {
				condition = new OracleDiskASM();
				String instanceName = rs.getString("instanceName");
				String tempSize01 = NumberUtil.formatDoubleStrValueWithCarry(rs.getString("availableSize"), 2);
				double availableSize = Double.parseDouble(tempSize01);
				String tempSize02 = NumberUtil.formatDoubleStrValueWithCarry(rs.getString("totalSize"), 2);
				double totalSize = Double.parseDouble(tempSize02);
				String tempSize03 = NumberUtil.formatDoubleStrValueWithCarry(rs.getString("useRate"), 2);
				double useRate = Double.parseDouble(tempSize03);

				condition.setInstanceName(instanceName);
				condition.setAvailableSize(Double.valueOf(availableSize));
				condition.setTotalSize(Double.valueOf(totalSize));
				condition.setUseRate(Double.valueOf(useRate));

				String[] temp = instanceName.split("_");
				if (temp.length > 0 && map.containsKey(temp[0])) {
					OracleDiskASM tempAsm = map.get(temp[0]);
					tempAsm.setInstanceName(temp[0]);
					tempAsm.setAvailableSize(tempAsm.getAvailableSize() + availableSize);
					tempAsm.setTotalSize(tempAsm.getTotalSize() + totalSize);
					DecimalFormat decimalFormat = new DecimalFormat("0.00");
					String useRates = decimalFormat.format(100 - (tempAsm.getAvailableSize() * 100.00 / tempAsm.getTotalSize()));
					tempAsm.setUseRate(Double.parseDouble(useRates));
					map.put(temp[0], tempAsm);
				} else {
					condition.setInstanceName(temp[0]);
					map.put(temp[0], condition);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		list.addAll(map.values());
		return list;
	}
    
	/**
	 * 查询ASM磁盘状态
	 * @return
	 */
	public List<ASMDiskState> getASMDiskState(){
		List<ASMDiskState> list = new ArrayList<ASMDiskState>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_ASM_DISK_STATE);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASMDiskState state = new ASMDiskState();
				String path = rs.getString("path");
				String mode_status = rs.getString("mode_status");
				state.setPath(path);
				state.setMode_status(mode_status);
				list.add(state);
			}
		} catch (SQLException e) {
			logger.error("查询ASM-Disk状态异常,{}", e);
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return list;
	}
	
	/**
	 * 查询asm磁盘组信息[目前仅查询使用率]
	 * @return
	 */
	public List<ASMDiskGroup> getASMDiskGroup(){
		List<ASMDiskGroup> list = new ArrayList<ASMDiskGroup>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_ASM_DISK_GROUP);
			rs = ps.executeQuery();
			while (rs.next()) {
				ASMDiskGroup group = new ASMDiskGroup();
				String name = rs.getString("name");
				String usage_pct = NumberUtil.formatDoubleStrValueWithCarry(rs.getString("usage_pct"), 2);
				group.setName(name);
				group.setUsage_pct(Double.parseDouble(usage_pct));
				list.add(group);
			}
		} catch (SQLException e) {
			logger.error("查询ASM-Disk状态异常,{}", e);
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return list;
	}
	
	/**
	 * 查询等待事件
	 * @return
	 */
	public List<WaitEvent> getWaitEvent(){
		List<WaitEvent> list = new ArrayList<WaitEvent>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_WAIT_EVENT);
			rs = ps.executeQuery();
			while (rs.next()) {
				WaitEvent waitEvent = new WaitEvent();
				String event = rs.getString("event");
				int act_session = rs.getInt("act_session");
				double rate = Double.valueOf(NumberUtil.formatDoubleStrValueWithCarry(rs.getString("rate"), 2));
				waitEvent.setEvent(event);
				waitEvent.setAct_session(act_session);
				waitEvent.setRate(rate);
				list.add(waitEvent);
			}
		} catch (SQLException e) {
			logger.error("查询等待事件异常,{}", e);
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return list;
	}
	
	/**
	 * 查询锁等待
	 * @return
	 */
	public List<LockWait> getLockWait(){
		List<LockWait> list = new ArrayList<LockWait>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_LOCK_WAIT);
			rs = ps.executeQuery();
			while (rs.next()) {
				LockWait lock = new LockWait();
				String sid = rs.getString("sid");
				long seconds_in_wait = rs.getLong("seconds_in_wait");
				lock.setSid(sid);
				lock.setSeconds_in_wait(seconds_in_wait);
				list.add(lock);
			}
		} catch (SQLException e) {
			logger.error("查询等待事件异常,{}", e);
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return list;
	}
	
	/**
	 * 查询用户状态
	 * @return
	 */
	public List<UserState> getUserState(){
		List<UserState> list = new ArrayList<UserState>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = getConnection();
		try {
			ps = conn.prepareStatement(ORA_SQL_USER_STATE);
			rs = ps.executeQuery();
			while (rs.next()) {
				UserState state = new UserState();
				String username = rs.getString("username");
				String account_status = rs.getString("account_status");
				state.setUsername(username);
				state.setStatus(account_status);
				list.add(state);
			}
		} catch (SQLException e) {
			logger.error("查询用户状态异常,{}", e);
		} finally {
			OracleJDBCUtil.close(rs, ps);
		}
		return list;
	}
}