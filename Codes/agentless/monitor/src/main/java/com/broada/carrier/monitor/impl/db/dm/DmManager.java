package com.broada.carrier.monitor.impl.db.dm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.dm.basic.DmBaseInfo;
import com.broada.carrier.monitor.impl.db.dm.bufferPool.DmBuffer;
import com.broada.carrier.monitor.impl.db.dm.cache.DmCacheInfo;
import com.broada.carrier.monitor.impl.db.dm.dtfile.DmDtfileInfo;
import com.broada.carrier.monitor.impl.db.dm.links.DmLinks;
import com.broada.carrier.monitor.impl.db.dm.lock.DmLock;
import com.broada.carrier.monitor.impl.db.dm.logBuf.DmLogBuf;
import com.broada.carrier.monitor.impl.db.dm.logFile.DmLogFile;
import com.broada.carrier.monitor.impl.db.dm.patchRate.DmPatchRate;
import com.broada.carrier.monitor.impl.db.dm.ramPool.DmRamPool;
import com.broada.carrier.monitor.impl.db.dm.redolog.DmRedoLogParameter;
import com.broada.carrier.monitor.impl.db.dm.redolog.RedoLogInfo;
import com.broada.carrier.monitor.impl.db.dm.sessions.DmSessInfo;
import com.broada.carrier.monitor.impl.db.dm.sql.DmSQLText;
import com.broada.carrier.monitor.impl.db.dm.thread.DmThread;
import com.broada.carrier.monitor.impl.db.dm.transactions.DmTrx;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.utils.JDBCUtil;

/**
 * 
 * 
 * @author Zhouqa Create By 2016年4月6日 上午11:17:29
 */
public class DmManager {
	private Log logger = LogFactory.getLog(DmManager.class);

	private String sid;
	private String ip;
	private int port;
	private String user;
	private String pass;
	private Connection conn = null;

	// DM检测点
	private static final String DM_CHECKPOINT = "select name,stat_val value from V$sysstat where name In('check point total count', 'check point by redo reserve', 'check point time(ms) used')";
	// DM数据文件监测
	private static final String DM_SQL_DATAFILE = "select client_path name,status$ status,total_size,free_size,read_request,write_request,modify_time from v$datafile";
	// DM基础信息
	private static final String DM_DB_BASIC = "select db.name dbname,ins.status$ status,ins.host_name hostname,ins.name instancename,ins.svr_version version,ins.mode$ mode from v$instance ins,v$database db";
	// DM全盘扫描
	public static final String DM_SQL_FTS = "select name,stat_val value from v$sysstat where name in ('table scans (short tables)','table scans (long tables)','table scan rows gotten','table fetch by rowid')";
	// 重做日志缓冲中用户进程不能分配空间的次数
	public static final String DM_SQL_REDO_UNLOAC = "select name,stat_val value from v$sysstat where name='redo buffer allocation retries'";
	// 归档的重做日志文件的数目,重做条目的平均大小
	public static final String DM_SQL_REDO_ARCHLOG = "select counts, decode(counts,0,0,total_size/counts) ave from "
			+ "(select count(*) counts, NVL(sum(BLOCKS * BLOCK_SIZE / 1024 / 1024), 0) total_size from v$archived_log where creator = 'ARCH')";
	// 碎片FSFI比率
	public static final String DM_SQL_PatchRate = "SELECT TABLESPACE_NAME, round(sqrt(max(blocks)/sum(blocks))*(100/sqrt(sqrt(count(blocks)))),2) FSFI "
			+ "FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME";

	private static final String DM_SQL_SESSIONS = "select sess_id,sql_text,state,user_name,curr_sch,create_time,clnt_type,auto_cmt,clnt_host from v$sessions";

	private static final String DM_SQL_BUFFERPOOLS = "select name,page_size,n_pages,n_fixed,free,n_dirty,n_clear,n_max_pages,n_logic_reads,n_discard,n_phy_reads,n_phy_m_reads,rat_hit from v$bufferpool";

	private static final String DM_SQL_LOGFILES = "select * from v$rlog";

	// 获取缓存信息
	private static final String DM_SQL_CACHE = "select * from v$cacheitem";

	// 获取日志缓冲信息
	private static final String DM_SQL_LOGBUF = "select * from v$rlogbuf";
	// 获取内存缓冲池信息
	private static final String DM_SQL_RAMPOOLS = "select count(*) counts,sum(v_size)/1024 total,sum(used)/1024 used from v$vpool";
	// 获取sql监测信息
	private static final String DM_SQL_TEXT = "select sq.seq_no,sq.sess_id,sq.trx_id,sq.top_sql_text,sq.start_time,sq.time_used,sq.is_over,se.user_name,se.clnt_ip,se.appname from v$sql_history sq, v$sessions se where sq.sess_id = se.sess_id order by sq.start_time desc";

	// 获取线程信息
	private static final String DM_SQL_THREAD = "select* from v$threads";
	// 获取事务信息
	private static final String DM_SQL_TRX = "select * from v$trx";
	// 获取锁信息
	private static final String DM_SQL_LOCK = "select l.addr,t.sess_id sessId,t.id trId,s.sql_text sqlText,s.last_send_time ocrrTime from v$lock l, v$trx t,v$sessions s where l.trx_id = t.id and t.sess_id = s.sess_id";

	// 获取数据库连接信息
	private static final String DM_SQL_LINK = "select * from v$dblink";

	public DmManager(String ip, DmMonitorMethodOption method) {
		this(method.getSid(), ip, method.getPort(), method.getUsername(), method.getPassword());
	}

	public DmManager(String sid, String ip, int port, String user, String pass) {
		this.sid = sid;
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}

	/**
	 * 初始化链接
	 * 
	 * @throws Exception
	 */
	public void initConnection() throws Exception {
		if (conn == null) {
			try {
				String url = "jdbc:dm://" + this.ip + ":" + this.port + "/" + this.sid;
				conn = JDBCUtil.createConnection("dm.jdbc.driver.DmDriver", url, user, pass);
			} catch (ClassNotFoundException ex) {
				throw ex;
			} catch (SQLException ex) {
				JDBCUtil.close(conn);
				throw new Exception("无法连接到Dm数据库或连接超时.", ex);
			}
		}
	}

	Connection getConnection() {
		if (conn == null) {
			throw new NullPointerException("数据库链接没有初始化,请先初始化.");
		}
		return conn;
	}

	/**
	 * 获取数据库实例的基本信息
	 * 
	 * @param insName
	 *          如果为空，就获取当前连接所在数据库的实例
	 * @return
	 * @throws Exception
	 */
	public DmBaseInfo getDmBaseInfo(String insName) throws Exception {
		Statement stDbs = null;
		ResultSet rsDbs = null;
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			stDbs = conn.createStatement();
		} catch (SQLException e) {
			JDBCUtil.close(stDbs);
			throw e;
		}
		DmBaseInfo db = null;
		ResultSet rsMore = null;
		try {
			if (insName != "" && insName != null) {
				rsDbs = stDbs.executeQuery(DM_DB_BASIC + " where ins.name='" + insName + "'");
			} else {
				rsDbs = stDbs.executeQuery(DM_DB_BASIC);
			}
			if (rsDbs != null) {
				if (rsDbs.next()) {
					db = new DmBaseInfo();
					db.setProductName(rsDbs.getString("dbname"));
					db.setDbName(rsDbs.getString("instancename"));
					db.setVersion(rsDbs.getString("version"));
					db.setMode(rsDbs.getString("mode"));
					db.setHostName(rsDbs.getString("hostname"));
					db.setStatus(rsDbs.getString("status"));
				}
			}
		} catch (SQLException ee) {
			if (logger.isDebugEnabled())
				logger.debug("获取" + insName + "的信息时发生异常.", ee);
			throw new Exception("获取" + insName + "的信息时发生异常.", ee);
		} finally {
			JDBCUtil.close(rsMore);
			JDBCUtil.close(rsDbs, stDbs);
		}
		return db;
	}

	/**
	 * 关闭并释放链接
	 */
	public void close() {
		if (conn != null) {
			JDBCUtil.close(conn);
			conn = null;
		}
	}

	/**
	 * 获取数据库检测点信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public int[] getCheckpoint() throws SQLException {
		int checkpointArr[] = new int[3];
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(DM_CHECKPOINT);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("name").equals("check point total count")) {
					checkpointArr[0] = rs.getInt("value");
				} else if (rs.getString("name").equals("check point by redo reserve")) {
					checkpointArr[1] = rs.getInt("value");
				} else if (rs.getString("name").equals("check point time(ms) used")) {
					checkpointArr[2] = rs.getInt("value");
				}
			}
			return checkpointArr;
		} catch (SQLException e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, ps, conn);
		}
	}

	/**
	 * 获取数据文件监测信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmDtfileInfo> getAllDtfiles() throws SQLException {
		List dtfileList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_DATAFILE);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmDtfileInfo oDtfile = new DmDtfileInfo();
				oDtfile.setDfName(rs.getString("name") == null ? null : rs.getString("name").trim());
				oDtfile.setDfTotalSize(new Double(rs.getDouble("total_size") / 1024));
				oDtfile.setDfFreeSize(new Double(rs.getDouble("free_size") / 1024));
				oDtfile.setDfStatus(rs.getString("status"));
				oDtfile.setDfRTS(new Double(rs.getDouble("read_request")));
				oDtfile.setDfWRTS(new Double(rs.getDouble("write_request")));
				oDtfile.setDfStatus(rs.getString("modify_time"));
				dtfileList.add(oDtfile);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return dtfileList;
	}

	/**
	 * 获取重做日志信息
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
			ps = conn.prepareStatement(DM_SQL_REDO_ARCHLOG);
			rs = ps.executeQuery();
			if (rs.next()) {
				buildRedoLog(DmRedoLogParameter.FIELD_ARCHCOUNT, rs.getInt("counts"), 100, "次", true, redos);
				buildRedoLog(DmRedoLogParameter.FIELD_AVESIZE, rs.getDouble("ave"), 50, "M", true, redos);
			}
			ps = conn.prepareStatement(DM_SQL_REDO_UNLOAC);
			rs = ps.executeQuery();
			if (rs.next()) {
				buildRedoLog(DmRedoLogParameter.FIELD_UNALLOCS, rs.getInt("value"), 100, "次", true, redos);
			}
		} finally {
			JDBCUtil.close(rs, ps);
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

	/**
	 * 获取dm数据库碎片FSFI使用情况
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmPatchRate> getAllPatchRate() throws SQLException {
		List tsList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_PatchRate);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmPatchRate ots = new DmPatchRate();
				ots.setTsName(rs.getString("TABLESPACE_NAME") == null ? null : rs.getString("TABLESPACE_NAME").trim());
				ots.setCurrFSFI(new Double(rs.getDouble("FSFI")));
				tsList.add(ots);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return tsList;
	}

	/**
	 * 获取dm数据库会话信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmSessInfo> getAllSessInfos() throws SQLException {
		List sessList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_SESSIONS);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmSessInfo sess = new DmSessInfo();
				sess.setSessId(rs.getString("sess_id"));
				sess.setUserName(rs.getString("user_name"));
				sess.setSessState(rs.getString("state"));
				sess.setSessSql(rs.getString("sql_text"));
				sess.setCurrSch(rs.getString("curr_sch"));
				sess.setCreateTime(rs.getString("create_time"));
				sess.setAutoCmt(rs.getString("auto_cmt"));
				sess.setClntHost(rs.getString("clnt_host"));
				sess.setClntType(rs.getString("clnt_type"));
				sessList.add(sess);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return sessList;
	}

	/**
	 * 获取内存缓冲区数据
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmBuffer> getAllBufferPools() throws SQLException {
		List bufList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_BUFFERPOOLS);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmBuffer buf = new DmBuffer();
				buf.setBufName(rs.getString("name"));
				buf.setPageSize(rs.getLong("page_size"));
				buf.setPageNo(rs.getLong("n_pages"));
				buf.setUsePage(rs.getLong("n_fixed"));
				buf.setDirty_page(rs.getLong("n_dirty"));
				buf.setBusyPage(rs.getLong("n_clear"));
				buf.setFreePage(rs.getLong("free"));
				buf.setMaxPage(rs.getLong("n_max_pages"));
				buf.setLogicReads(rs.getLong("n_logic_reads"));
				buf.setDiscard(rs.getLong("n_discard"));
				buf.setPhyReads(rs.getLong("n_phy_reads"));
				buf.setMultiReads(rs.getLong("n_phy_m_reads"));
				buf.setHitRate(rs.getDouble("rat_hit"));
				bufList.add(buf);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return bufList;
	}

	/**
	 * 获取日志文件信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmLogFile> getAllLogs() throws SQLException {
		List logList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_LOGFILES);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmLogFile log = new DmLogFile();
				log.setCkptLsn(rs.getLong("ckpt_lsn"));
				log.setFileLsn(rs.getLong("file_lsn"));
				log.setFlushLsn(rs.getLong("flush_lsn"));
				log.setCurrLsn(rs.getLong("cur_lsn"));
				log.setNextSeq(rs.getLong("next_seq"));
				log.setMagic(rs.getLong("n_magic"));
				log.setFlushPages(rs.getLong("flush_pages"));
				log.setFlushingPages(rs.getLong("flushing_pages"));
				log.setCurrFile(rs.getLong("cur_file"));
				log.setCurrOffset(rs.getLong("cur_offset"));
				log.setCkptFile(rs.getLong("ckpt_file"));
				log.setCkptOffset(rs.getLong("ckpt_offset"));
				log.setFreeSpace(rs.getDouble("free_space"));
				log.setTotalSpace(rs.getDouble("total_space"));
				logList.add(log);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return logList;
	}

	/**
	 * 获取缓存信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmCacheInfo> getAllCacheItem() throws SQLException {
		List cacheList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_CACHE);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmCacheInfo cache = new DmCacheInfo();
				cache.setAddress(rs.getString("address"));
				cache.setType(rs.getString("type$"));
				cache.setOverflow(rs.getString("overflow"));
				cache.setInPool(rs.getString("in_pool"));
				cache.setDisabled(rs.getString("disabled"));
				cache.setFixed(rs.getLong("n_fixed"));
				cache.setTimeSize(rs.getLong("ts_value"));
				cacheList.add(cache);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return cacheList;
	}

	/**
	 * 获取日志缓存信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmLogBuf> getAllLogBufs() throws SQLException {
		List logbufList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_LOGBUF);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmLogBuf logbuf = new DmLogBuf();
				logbuf.setBeginSln(rs.getString("begin_lsn"));
				logbuf.setEndSln(rs.getString("end_lsn"));
				logbuf.setTotalPage(rs.getLong("total_pages"));
				logbuf.setFixedPage(rs.getLong("n_pages"));
				logbuf.setUseRate((double)logbuf.getFixedPage() / (double)logbuf.getTotalPage());
				logbufList.add(logbuf);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return logbufList;
	}

	/**
	 * 获取内存池信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmRamPool> getAllRamPools() throws SQLException {
		List ramList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_RAMPOOLS);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmRamPool ram = new DmRamPool();
				ram.setCounts(rs.getInt("counts"));
				ram.setTotalSize(rs.getDouble("total"));
				ram.setUsedSize(rs.getDouble("used"));
				ram.setFreeSize(ram.getTotalSize() - ram.getUsedSize());
				ram.setUseRate(ram.getUsedSize() * 100 / ram.getTotalSize());
				ramList.add(ram);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return ramList;
	}

	/**
	 * 获取锁信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmLock> getAllLocks() throws SQLException {
		List lockList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_LOCK);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmLock lock = new DmLock();
				lock.setAddr(rs.getString("addr"));
				lock.setSessionID(rs.getLong("sessId"));
				lock.setTrID(rs.getLong("trId"));
				lock.setSqlText(rs.getString("sqlText"));
				lock.setOcurTime(rs.getString("ocrrTime"));
				lockList.add(lock);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return lockList;
	}

	/**
	 * 获取事务信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmTrx> getAllTrx() throws SQLException {
		List trxList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_TRX);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmTrx trx = new DmTrx();
				trx.setTrID(rs.getLong("id"));
				trx.setState(rs.getString("status"));
				trx.setIsolation(rs.getString("isolation"));
				trx.setReadOnly(rs.getString("read_only"));
				trx.setSessID(rs.getLong("sess_id"));
				trx.setInsCnt(rs.getInt("ins_cnt"));
				trx.setDelCnt(rs.getInt("del_cnt"));
				trx.setUptCnt(rs.getInt("upd_cnt"));
				trx.setUptInsCnt(rs.getInt("upd_ins_cnt"));
				trx.setUrecSeq(rs.getInt("urec_seqno"));
				trx.setWait(rs.getInt("waiting"));
				trxList.add(trx);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return trxList;
	}

	/**
	 * 获取sql监测
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmSQLText> getAllSQLS() throws SQLException {
		List sqlList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_TEXT);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmSQLText sql = new DmSQLText();
				sql.setSeqNo(rs.getLong("seq_no"));
				sql.setSessID(rs.getLong("sess_id"));
				sql.setTrxID(rs.getLong("trx_id"));
				sql.setSqlText(rs.getString("top_sql_text"));
				sql.setStartTime(rs.getString("start_time"));
				sql.setTimeUsed(rs.getLong("time_used"));
				sql.setIsOver(rs.getString("is_over"));
				sql.setUserName(rs.getString("user_name"));
				sql.setClntIp(rs.getString("clnt_ip"));
				sql.setAppName(rs.getString("appname"));
				sqlList.add(sql);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return sqlList;
	}

	/**
	 * 获取线程信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmThread> getAllThreads() throws SQLException {
		List threadList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_THREAD);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmThread thread = new DmThread();
				thread.setID(rs.getLong("id"));
				thread.setThreadName(rs.getString("name"));
				thread.setStartTime(rs.getString("start_time"));
				threadList.add(thread);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return threadList;
	}

	/**
	 * 获取所有链接信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<DmLinks> getAllLinks() throws SQLException {
		List linkList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(DM_SQL_LINK);
			rs = ps.executeQuery();
			while (rs.next()) {
				DmLinks link = new DmLinks();
				link.setLinkID(rs.getLong("link_id"));
				link.setLinkName(rs.getString("link_name"));
				link.setIsPublic(rs.getString("is_public"));
				link.setLoginName(rs.getString("login_name"));
				link.setHostName(rs.getString("host_name"));
				link.setPort(rs.getString("port_num"));
				link.setLoggenOn(rs.getString("loggen_on"));
				link.setHeterrogeneous(rs.getString("heterrogeneous"));
				link.setProtocol(rs.getString("protocol"));
				link.setInUse(rs.getString("in_use"));
				linkList.add(link);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return linkList;
	}

}
