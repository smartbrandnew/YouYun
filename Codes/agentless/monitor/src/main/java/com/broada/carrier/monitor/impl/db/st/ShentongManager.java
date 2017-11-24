package com.broada.carrier.monitor.impl.db.st;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.st.basic.ShentongBaseConfiger;
import com.broada.carrier.monitor.impl.db.st.buffer.ShentongBuffer;
import com.broada.carrier.monitor.impl.db.st.dtfile.ShentongDtfile;
import com.broada.carrier.monitor.impl.db.st.fts.ShentongFTSInfo;
import com.broada.carrier.monitor.impl.db.st.patchRate.ShentongPatchRate;
import com.broada.carrier.monitor.impl.db.st.session.ShentongSessInfo;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.JDBCUtil;

public class ShentongManager {
	private static final Log logger = LogFactory.getLog(ShentongManager.class);

	// 获取基本配置信息
	private static final String ST_SQL_DB_BASEINFO = "select ins.status,ins.host_name hostname,ins.version version,ins.instance_name instancename,ins.startup_time startupTime ,db.version version1, db.archive_path archivepath from v$instance ins,v$database db";
	// 获取当前连接数
	private static final String ST_SQL_DB_CURRENT_CONNS = "select count(*) c from v$session where curr_user is not null and status<>'KILLED'";
	// 获取检查点数信息
	private static final String ST_CHECKPOINT_STARTD_COMPLETED = "Select * From V$sysstat Where Name In('BACKGROUND CHECKPOINTS STARTED', 'BACKGROUND CHECKPOINTS COMPLETED')";
	// 获取数据文件信息
	private static final String ST_SQL_DATAFILE = "select df.path \"dfName\",df.size \"dfSize\", df.status \"dfStatus\",fs.PHYSICAL_BLOCK_READS \"dfRTS\", fs.PHYSICAL_BLOCK_READS PHYBLKRD,fs.PHYSICAL_BLOCK_WRITES PHYBLKWRT,fs.PHYSICAL_BLOCK_READS+fs.PHYSICAL_BLOCK_WRITES TOTALBLOCK,fs.PHYSICAL_WRITES \"dfWRTS\", fs.AVERAGE_READ_time \"dfRTim\",fs.AVERAGE_write_time \"dfWRTim\" from V$DATAFILE df,V$filemetric fs WHERE df.FILE_ID = fs.FILE_ID";
	// 获取全盘扫描信息
	private static final String ST_SQL_FTS = "select name,value from v$sysstat where name in ('TABLE SCANS','TABLE SCAN ROWS GOTTEN','TABLE FETCH BY ROWID')";
	// 获取数据库碎片信息
	private static final String ST_SQL_PatchRate = "SELECT TABLESPACE_NAME, round(sqrt(max(bytes)/sum(bytes))*(100/sqrt(sqrt(count(bytes)))),2) FSFI FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME";

	private static final String ST_SQL_CACHEHITRATIO = "SELECT (1 - (phy.value / cur.value))*100 \"ratio\" FROM v$sysstat cur, v$sysstat phy WHERE cur.name = 'DB BLOCK GETS' AND phy.name = 'PHYSICAL READ REQUESTS'";

	private static final String ST_SQL_DISKMEMORYSORTRATIO = "select (disk.value/mem.value)*100 \"ratio\" from v$sysstat mem, v$sysstat disk where mem.name = 'SORTS(MEMORY)' and disk.name = 'SORTS(DISK)'";

	public static final String ST_SQL_SESS_MONITOR_HEAD = "select ss.session_id sid,ss.value value from v$sessstat ss,v$session se,v$statname st where st.stat_id = ss.statistic_id and  se.sid=ss.session_id and ";

	public static final String[] ST_SQL_SESS_MONITORITEM = new String[] { "st.stat_name = 'SORTS(MEMORY)'",
			"st.stat_name in ('TABLE SCANS')", "st.stat_name ='PHYSICAL READ REQUESTS'",
			"st.stat_name = 'PHYSICAL WRITE REQUESTS'", "st.stat_name = 'USER COMMITS'",
			"st.stat_name = 'OPENED CURSORS CUMULATIVE'" };

	private static final String ST_SQL_SESS_SESSION = "select sid,curr_user value from v$session where curr_user is not null order by sid";

	private static final String get_broken_num = "select count(*) as broken_num from dba_jobs where broken = 'Y'";

	public static final String get_failure_num = "select count(*) as failure_num from dba_jobs where failures != 0";

	public static final String get_overdue_num = "select count(*) as overdue_num from dba_jobs t where (select sysdate from dual) > t.NEXT_DATE";

	private static final String ST_SQL_BUFFERPOOLS = "select buffers,free_buffers,dirty_buffers,buffer_read_blocks,buffer_write_blocks from v$buffer_statistics";

	private String ip;
	private ShentongMethod method;

	private Connection inConn = null;

	public ShentongManager(String ip, ShentongMethod method) {
		this.ip = ip;
		this.method = method;
	}

	/**
	 * 初始化数据库链接
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void initConnection() throws SQLException, ClassNotFoundException {
		if (inConn == null) {
			inConn = getConnectionChkRole();
		}
	}

	/**
	 * 初始化数据库链接
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void initConnectionIgnoreRole() throws SQLException, ClassNotFoundException {
		if (inConn == null) {
			inConn = getConnectionIgnoreRole();
		}
	}

	/**
	 * 创建并返回链接
	 * 
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private Connection getConnectionIgnoreRole() throws SQLException, ClassNotFoundException {
		if (method.getUsername() == null || method.getUsername().equals("")) {
			throw new MonitorException("未指定数据库用户.");
		}

		String url = "jdbc:oscar://" + this.ip + ":" + method.getPort() + "/" + method.getSid();
		inConn = JDBCUtil.createConnection("com.oscar.Driver", url, method.getUsername(), method.getPassword());
		Exception exception = null;
		Connection conn = null;
		try {
			conn = JDBCUtil.createConnection("com.oscar.Driver", url, method.getUsername(), method.getPassword());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("无法获取Shentong驱动.");
		} catch (SQLException ex) {
			exception = ex;
			JDBCUtil.close(conn);
		}

		if (exception != null) {// 如果有未知异常则继续抛出
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
	 * 获取链接并检查权限
	 * 
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private Connection getConnectionChkRole() throws SQLException, ClassNotFoundException {
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
			JDBCUtil.close(stm);
		}

		if (!hasRole) {
			JDBCUtil.close(conn);
			throw new LogonDeniedException("指定用户无DBA或OEM_MONITOR角色.");
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
	 * 获取神通数据库基本配置信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Object> getShentongBaseInfo() throws SQLException {
		Map<String, Object> shentongBase = new HashMap<String, Object>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ST_SQL_DB_BASEINFO);
			rs = ps.executeQuery();
			if (rs.next()) {
				String version = rs.getString("version") == null || "".equals(rs.getString("version").trim())
						|| rs.getString("version").trim().length() == 0 ? rs.getString("version1") : rs.getString("version");
				shentongBase.put(ShentongBaseConfiger.keys[0], rs.getString("status"));
				shentongBase.put(ShentongBaseConfiger.keys[1], rs.getString("hostname"));
				shentongBase.put(ShentongBaseConfiger.keys[2], "");
				shentongBase.put(ShentongBaseConfiger.keys[3], version);
				shentongBase.put(ShentongBaseConfiger.keys[4], rs.getString("instancename"));
				shentongBase.put(ShentongBaseConfiger.keys[5], rs.getDate("startupTime"));
				shentongBase.put(ShentongBaseConfiger.keys[6], "");
				shentongBase.put(ShentongBaseConfiger.keys[7], rs.getString("archivepath"));
				// 获取当前连接数
				ps2 = conn.prepareStatement(ST_SQL_DB_CURRENT_CONNS);
				rs2 = ps2.executeQuery();
				if (rs2.next())
					shentongBase.put(ShentongBaseConfiger.keys[8], rs2.getLong("c"));

			}
		} catch (SQLException e) {
			logger.error("sqlError:"+e);
			throw e;
		} finally {
			JDBCUtil.close(rs2);
			JDBCUtil.close(ps2);
			JDBCUtil.close(rs, ps);
		}
		return shentongBase;
	}

	/**
	 * 关闭并释放资源
	 */
	public void close() {
		if (inConn != null) {
			JDBCUtil.close(inConn);
			inConn = null;
		}
	}

	/**
	 * 获取神通数据库检查点信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public int[] getCheckpoint() throws SQLException {
		int checkpointArr[] = new int[2];
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(ST_CHECKPOINT_STARTD_COMPLETED);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("name").equals("BACKGROUND CHECKPOINTS STARTED")) {
					checkpointArr[0] = rs.getInt("value");
				} else if (rs.getString("name").equals("BACKGROUND CHECKPOINTS COMPLETED")) {
					checkpointArr[1] = rs.getInt("value");
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
	 * 获取shentong数据文件信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<ShentongDtfile> getAllDtfiles() throws SQLException {
		List dtfileList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ST_SQL_DATAFILE);
			rs = ps.executeQuery();
			while (rs.next()) {
				ShentongDtfile oDtfile = new ShentongDtfile();
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
			JDBCUtil.close(rs, ps);
		}
		return dtfileList;
	}

	/**
	 * 获取神通数据库全盘扫描信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public ShentongFTSInfo getShentongFTSInfo() throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ShentongFTSInfo info = new ShentongFTSInfo();
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ST_SQL_FTS);
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				long value = rs.getLong("value");
				if ("TABLE SCAN ROWS GOTTEN".equals(name)) {
					info.setFtsRows(value);
				} else if ("TABLE FETCH BY ROWID".equals(name)) {
					info.setFbiRows(value);
				}
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return info;
	}

	/**
	 * 获取shentong数据库碎片FSFI使用信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<ShentongPatchRate> getAllPatchRate() throws SQLException {
		List tsList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ST_SQL_PatchRate);
			rs = ps.executeQuery();
			while (rs.next()) {
				ShentongPatchRate ots = new ShentongPatchRate();
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
	 * 获取高速缓存区命中率
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
			ps = conn.prepareStatement(ST_SQL_CACHEHITRATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return ratio;
	}

	/**
	 * 获取磁盘排序与内存排序比率
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
			ps = conn.prepareStatement(ST_SQL_DISKMEMORYSORTRATIO);
			rs = ps.executeQuery();
			while (rs.next()) {
				ratio = rs.getDouble("ratio");
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return ratio;
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
			return ST_SQL_SESS_SESSION;
		case 1:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[0];
		case 2:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[1];
		case 3:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[2];
		case 4:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[3];
		case 5:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[4];
		case 6:
			return ST_SQL_SESS_MONITOR_HEAD + ST_SQL_SESS_MONITORITEM[5];
		default:
			return "";
		}
	}

	/**
	 * 获取会话信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<ShentongSessInfo> getAllSessInfos() throws SQLException {
		List sessInfoList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map[] sessMap = new HashMap[6];
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
					JDBCUtil.close(rs, ps);
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
				ShentongSessInfo sess = new ShentongSessInfo();
				String key = (String) iter.next();
				sess.setSessId(key);
				sess.setUserName(sessMap[0].get(key).toString());
				sess.setSessSorts((Double) sessMap[1].get(key));
				sess.setTableScans((Double) sessMap[2].get(key));
				sess.setSessReads((Double) sessMap[3].get(key));
				sess.setSessWrites((Double) sessMap[4].get(key));
				sess.setSessCommits((Double) sessMap[5].get(key));

				sessInfoList.add(sess);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return sessInfoList;
	}

	/**
	 * 获取破损队列数
	 * 
	 * @return
	 * @throws SQLException
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
			JDBCUtil.close(rs, ps);
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
			JDBCUtil.close(rs, ps);
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
			JDBCUtil.close(rs, ps);
		}
		return broken_num;
	}

	/**
	 * 获取缓冲区文件信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<ShentongBuffer> getAllBufferPools() throws SQLException {
		List bufList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conn = getConnection();
			ps = conn.prepareStatement(ST_SQL_BUFFERPOOLS);
			rs = ps.executeQuery();
			while (rs.next()) {
				ShentongBuffer buf = new ShentongBuffer();
				buf.setPageSize(rs.getLong("buffers"));
				buf.setDirty_page(rs.getLong("dirty_buffers"));
				buf.setFreePage(rs.getLong("free_buffers"));
				buf.setReadBlock(rs.getLong("buffer_read_blocks"));
				buf.setWriteBlock(rs.getLong("buffer_write_blocks"));
				bufList.add(buf);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return bufList;
	}

}
