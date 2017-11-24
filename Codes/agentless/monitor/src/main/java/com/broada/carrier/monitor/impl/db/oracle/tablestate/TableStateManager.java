package com.broada.carrier.monitor.impl.db.oracle.tablestate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleJDBCUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleUrlUtil;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.StringUtil;

/**
 * <p>Title: oracle表状态监测</p>
 * <p>Description: 产品部</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: Broada</p>
 * @author caikang
 * @version 3.3
 */

public class TableStateManager {
	private static final Log logger = LogFactory.getLog(TableStateManager.class);
	private String ip;
	private String sid;
	private String service_name;    // 扩展service_name
	private int port;
	private String user;
	private String password;
	private Connection conn = null;

	private String tableName;
	private String tableOwner;
	private float nowTable_size;
	private String tableNameFilterText;

	//获取表格大小
	private String sql_table_size;
	//获取索引大小
	private String sql_index_size;

	public TableStateManager(String ip, OracleMethod method) {
		this.ip = ip;
		this.sid = method.getSid();
		this.service_name = method.getServiceName();
		this.port = method.getPort();
		this.user = method.getUsername();
		this.password = method.getPassword();
	}

	public void initConnection() throws SQLException {
		if (conn == null) {
			conn = getConnectionChkRole();
		}
	}

	public Connection getConnectionChkRole() throws SQLException {
		Connection conn = getConnectionIgnoreRole();
		Statement stmt = null;
		boolean hasRole = false;
		// 检查该用户是否有DBA或OEM_MONITOR角色,当视图v$session存在时,表示有该角色；不存在,无该角色.
		String sql = "select count(*) from v$session";
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
			hasRole = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			OracleJDBCUtil.close(stmt);
		}

		if (!hasRole) {
			OracleJDBCUtil.close(conn);
			throw new LogonDeniedException("指定用户无DBA或OEM_MONITOR角色.");
		}
		return conn;
	}

	public Connection getConnectionIgnoreRole() throws SQLException {
		if (user == null || user.equals("")) {
			throw new SQLException("未指定数据库用户.");
		}
		if (sid == null || sid.length() == 0) {
			throw new SQLException("未指定数据库.");
			//sid = "orcl";
		}

		int errCode = 0; //0表示正常
		
		String url = null;
		if(!StringUtil.isNullOrBlank(this.service_name))   // 配置了service_name
			url = OracleUrlUtil.getUrl(ip, port, service_name, true);
		else
			url = OracleUrlUtil.getUrl(ip, port, sid, false);
		Exception exception = null;
		Connection conn = null;
		try {
			conn = OracleJDBCUtil.createConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new SQLException("无法获取Oracle驱动.");
		} catch (SQLException e) {
			e.getErrorCode();//检索此 SQLException 对象的特定于供应商的异常代码。
			exception = e;
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
		} else if (errCode != 0 && errCode != 1017) {
			SQLException se = new SQLException("数据库没有装载或打开,错误:" + exception.getMessage() + ".oracle错误号:ORA-" + errCode);
			se.initCause(exception);
			throw se;
		} else if (errCode == 1017) {
			LogonDeniedException le = new LogonDeniedException("用户名/密码错误,连接失败.oracle错误号:ORA-" + errCode);
			le.initCause(exception);
			throw le;
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

	public ArrayList<OracleTableState> getAllTableStates(String jtfText, int tableNum, int pageIndex) throws SQLException {
		tableNameFilterText = jtfText;
		ArrayList<OracleTableState> tempList = new ArrayList<OracleTableState>();
		
		PreparedStatement ps = null;//SQL 语句被预编译并且存储在 PreparedStatement 对象中。然后可以使用此对象高效地多次执行该语句。
		ResultSet rs = null;

		try {			
			logger.debug("准备获取表数据");
			Connection conn = getConnection();			
			StringBuilder sb = new StringBuilder();
			if (pageIndex > 0)
				sb.append("select table_name,owner from (");
			sb.append("select table_name,owner,rownum no from dba_tables where ");
			if (tableNameFilterText != null && tableNameFilterText.length() > 0)
				sb.append("table_name like '%").append(tableNameFilterText).append("%' and ");			
			sb.append(" rownum <= ").append((pageIndex + 1) * tableNum);
			sb.append(" order by table_name");
			if (pageIndex > 0)
				sb.append(") where no >= ").append(pageIndex * tableNum);
			ps = conn.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next())
			{
				this.tableName = rs.getString("TABLE_NAME");
				this.tableOwner = rs.getString("OWNER");
				OracleTableState ots = new OracleTableState();
				ots.setTablename(this.tableName);
				ots.setUsername(this.tableOwner);
				ots.setTablesize(-1);
				ots.setLastTime(0.0d);
				ots.setIndexsize(-1);
				ots.setGrowthrate(-1);
				tempList.add(ots);
			}			
		}
		catch (SQLException e) {
		  throw e;			
    }
		finally
		{
			OracleJDBCUtil.close(ps);
			OracleJDBCUtil.close(rs);
			OracleJDBCUtil.close(conn);
		}
		return tempList;
	}

	public List<OracleTableState> getListForDoMonitor(MonitorInstance[] instances) throws SQLException
	{
		List<OracleTableState> doMonitorList = new ArrayList<OracleTableState>();		
		//获取当前表状态
		Connection conn = getConnection();
		PreparedStatement ps = null;//SQL 语句被预编译并且存储在 PreparedStatement 对象中。然后可以使用此对象高效地多次执行该语句。
		ResultSet rs = null;
		try {
			for (MonitorInstance instance : instances) {
				String[] fields = instance.getCode().split(".");
				OracleTableState ts = new OracleTableState();
				tableOwner = fields[0];
				tableName = fields[1];				
				ts.setUsername(tableOwner);
				ts.setTablename(tableName);
				sql_table_size = "analyze table "+tableOwner+"."+tableName+" compute statistics";
				ps = conn.prepareStatement(sql_table_size);
				rs = ps.executeQuery();
				sql_table_size = "select TABLE_NAME,OWNER,(num_rows * avg_row_len/1024/1024)as table_size from dba_tables where table_name='"+tableName+"' and owner='"+tableOwner+"'";
				ps = conn.prepareStatement(sql_table_size);
				rs = ps.executeQuery();
				if (rs.next()) {
					nowTable_size = rs.getFloat("table_size");
					ts.setTablesize(nowTable_size);
				}
				rs.close();

				sql_index_size = "select distinct (bytes/1024/1024)as index_size from dba_segments,dba_indexes where dba_segments.segment_name=dba_indexes.index_name and dba_segments.owner='"
						+ tableOwner + "' and dba_indexes.table_name='" + tableName + "'";// and dba_tablespace='"+tableSpace+"'";
				ps = conn.prepareStatement(sql_index_size);
				rs = ps.executeQuery();
				if (rs.next()) {
					ts.setIndexsize(rs.getFloat("INDEX_SIZE"));
				} 
				rs.close();
			}
		} finally {
			OracleJDBCUtil.close(ps);			
			OracleJDBCUtil.close(conn);
		}
		return doMonitorList;
	}

	private Connection getConnection() {
		if (conn == null) {
			throw new NullPointerException("数据库链接没有初始化,请先初始化.");
		}
		return conn;
	}

	public void close() {
		if (conn != null) {
			OracleJDBCUtil.close(conn);
			conn = null;
		}
	}

	public OracleTableState getTableState(String table) {
		PreparedStatement ps = null;// SQL 语句被预编译并且存储在 PreparedStatement
																// 对象中。然后可以使用此对象高效地多次执行该语句。
		ResultSet rs = null;

		try {
			if (logger.isDebugEnabled())
				logger.debug("准备获取表具体数据：" + table);
			Connection conn = getConnection();
			OracleTableState ots = new OracleTableState();
			int pos = table.indexOf('.');
			ots.setUsername(table.substring(0, pos));
			ots.setTablename(table.substring(pos + 1));
			tableName = ots.getTablename();
			tableOwner = ots.getUsername();
			{
				sql_table_size = "analyze table " + tableOwner + "." + tableName + " compute statistics";
				if (logger.isDebugEnabled())
					logger.debug("表状态分析开始：" + sql_table_size);
				ps = conn.prepareStatement(sql_table_size);
				rs = ps.executeQuery();
				sql_table_size = "select (num_rows * avg_row_len/1024/1024)as table_size from dba_tables where table_name='"
						+ tableName + "' and owner='" + tableOwner + "'";
				ps = conn.prepareStatement(sql_table_size);
				rs = ps.executeQuery();
				if (rs.next())
				{
					this.nowTable_size = rs.getFloat("table_size");
					ots.setTablesize(nowTable_size);
					ots.setLastTime(System.currentTimeMillis());
				}
				else
				{
					ots.setTablesize(0);
					ots.setLastTime(0.0d);
				}
				rs.close();
			}

			if (logger.isDebugEnabled())
				logger.info("准备获取表索引数据：" + table);
			// 获取索引大小
			{
				sql_index_size = "select distinct (bytes/1024/1024)as index_size from dba_segments,dba_indexes where dba_segments.segment_name=dba_indexes.index_name and dba_segments.owner='"
						+ tableOwner + "' and dba_indexes.table_name='" + tableName + "'";// and
																																							// dba_tablespace='"+tableSpace+"'";
				ps = conn.prepareStatement(sql_index_size);
				rs = ps.executeQuery();
				if (rs.next())
				{
					ots.setIndexsize(rs.getFloat("INDEX_SIZE"));
				}
				else
				{
					ots.setIndexsize(0);
				}
				rs.close();
				
				if (logger.isDebugEnabled())
					logger.info("获取完成数据：" + table);
			}
			return ots;
		} catch (SQLException e)
		{
			throw ErrorUtil.createRuntimeException("数据库表状态获取错误失败", e);
		} finally
		{
			OracleJDBCUtil.close(ps);
			OracleJDBCUtil.close(rs);
			OracleJDBCUtil.close(conn);
		}
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}

	public String getService_name() {
		return service_name;
	}
}
