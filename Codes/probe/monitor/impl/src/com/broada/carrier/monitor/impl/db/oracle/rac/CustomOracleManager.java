package com.broada.carrier.monitor.impl.db.oracle.rac;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.rac.racinfo.OracleRacInstance;
import com.broada.utils.JDBCUtil;
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class CustomOracleManager  {

	private static final Log LOG = LogFactory.getLog(CustomOracleManager.class);

	public CustomOracleManager(String ip, String sid, int port, String user, String pass) {
		this._ip = ip;
		this._pass = pass;
		this._port = port;
		this._sid = sid;
		this._user = user;
	}
	private String _ip;

	private String _sid;

	private int _port;

	private String _user;

	private String _pass;
	private Connection inConn = null;

	//获取oracle RAC 的负载实例，有多个
	public static final String ORA_RAC_MULTI_INSTANCE = "SELECT inst_id, instance_number, instance_name, parallel, status, database_status, active_state, host_name,TO_CHAR (startup_time, 'yyyy-mm-dd hh24:mi:ss') startup_time FROM gv$instance ORDER BY inst_id";


	public String get_ip() {
		return _ip;
	}

	public void set_ip(String _ip) {
		this._ip = _ip;
	}

	public String get_sid() {
		return _sid;
	}

	public void set_sid(String _sid) {
		this._sid = _sid;
	}

	public int get_port() {
		return _port;
	}

	public void set_port(int _port) {
		this._port = _port;
	}

	public String get_user() {
		return _user;
	}

	public void set_user(String _user) {
		this._user = _user;
	}

	public String get_pass() {
		return _pass;
	}

	public void set_pass(String _pass) {
		this._pass = _pass;
	}

	public Connection getInConn() {
		return inConn;
	}

	public void setInConn(Connection inConn) {
		this.inConn = inConn;
	}

	/**
	 * 初始化数据库链接
	 * @throws SQLException
	 */
	public void initConnection() throws SQLException {
		if (inConn == null) {
			inConn = getConnectionChkRole();
		}
	}

	/**
	 * 获取链接并检查权限
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
	 * 创建并返回链接
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnectionIgnoreRole() throws SQLException {
		return getConnectionIgnoreRole(false);
	}

	/**
	 * 重构方法，添加是否为多线程(MTS)thin连接参数判断
	 * 
	 * @param isMTSConn
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnectionIgnoreRole(boolean isMTSConn) throws SQLException {
		if (_user == null || _user.equals("")) {
			throw new SQLException("未指定数据库用户.");
		}

		int errCode = 0; // 0表示正常

		if (_sid == null || _sid.length() == 0) {
			_sid = "orcl";
		}
		String url = "";
		/*if (isMTSConn){
      url= OracleUrlUtil.getUrl(_ip, _port, _sid);
    } else {
      url = "jdbc:oracle:thin:@" + _ip + ":" + _port + ":" + _sid;
    }*/
		url= OracleUrlUtils.getUrl(_ip, _port, _sid);
		LOG.info("URL:" + url);
		Exception exception = null;
		Connection conn = null;
		try {
			conn = JDBCUtil.createConnection("oracle.jdbc.driver.OracleDriver", url, _user, _pass);
		} catch (ClassNotFoundException ex) {
			throw new SQLException("无法获取Oracle驱动.");
		} catch (SQLException ex) {
			errCode = ex.getErrorCode();
			exception = ex;
			JDBCUtil.close(conn);
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
			SQLException se = new SQLException("连接失败,可能是实例名错误.");
			se.initCause(exception);
			throw se;
		} else if (errCode == 17002) {
			SQLException se = new SQLException("连接失败,可能指定的端口不是Oracle监听端口.");
			se.initCause(exception);
			throw se;
		} else if (errCode != 0 && errCode != 1017) {
			SQLException se = new SQLException("数据库没有装载或打开,错误:" + exception.getMessage());
			se.initCause(exception);
			throw se;
		} else if (errCode == 1017) {
			LogonDeniedException le = new LogonDeniedException("用户名/密码错误,连接失败.");
			le.initCause(exception);
			throw le;
		} else if (exception != null) {//如果有未知异常则继续抛出
			SQLException se = new SQLException("连接失败,请查看实例名或相关配置是否正确.");
			se.initCause(exception);
			throw se;
		} else if (conn == null) {//判断连接是否有效
			SQLException se = new SQLException("连接失败,请检查配置是否正确.");
			throw se;
		}

		return conn;
	}

	/**
	 * 获取所有表空间信息
	 * @return
	 * @throws SQLException
	 */
	
	public List getMultiRacInstance() throws SQLException {
		List tsList = new ArrayList();
		PreparedStatement ps = null;
		PreparedStatement psIoAvg = null;
		PreparedStatement psNextExtent = null;
		PreparedStatement psVer = null;
		ResultSet rs = null;
		Map ioAvgMap = new HashMap();
		Map nextExtentMap = new HashMap();
		try {
			Connection conn = getInConn();
			if(conn == null){
				throw new SQLException("数据库连接出错");
			}
			ps = conn.prepareStatement(ORA_RAC_MULTI_INSTANCE);
			rs = ps.executeQuery();
			if (rs == null) {
				throw new SQLException("获取Oracle Rac 多实例信息出错");
			}
			while (rs.next()) {
				OracleRacInstance ori = new OracleRacInstance();
				ori.setInstId(rs.getString("inst_id"));
				ori.setInstNumber(rs.getString("instance_number"));
				ori.setInstanceName(rs.getString("instance_name"));
				ori.setParallel(rs.getString("parallel"));
				ori.setActiveState(rs.getString("active_state"));
				ori.setDatabaseStatus(rs.getString("database_status"));
				ori.setHostName(rs.getString("host_name"));
				ori.setStartUpTime(rs.getString("startup_time"));
				ori.setStatus(rs.getString("status"));
				tsList.add(ori);
			}
		} finally {
			JDBCUtil.close(rs, ps);
		}
		return tsList;

	}
}
