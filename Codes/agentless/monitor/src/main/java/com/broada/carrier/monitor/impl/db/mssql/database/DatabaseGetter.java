package com.broada.carrier.monitor.impl.db.mssql.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.method.mssql.MSSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class DatabaseGetter {
	private static final Log logger = LogFactory.getLog(DatabaseGetter.class);
	private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

	private static final String URL = "jdbc:jtds:sqlserver://{0}:{1};databaseName={2}";

	private static final String URL_DOMAIN = "jdbc:jtds:sqlserver://{0}:{1};domain={2}&databaseName={3}";

	static {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static List getAllDatabases(String ip, MonitorMethod method) throws DataAccessException {
		MSSQLMonitorMethodOption option = new MSSQLMonitorMethodOption(method);
		String host = ip;
		int port = option.getPort();
		String username = option.getUsername();
		String password = option.getPassword();
		String domain = option.getDomain();
		String instanceName = option.getInstanceName();
		if (instanceName == null)
			instanceName = "";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		List databaseinfoList = new ArrayList();
		try {
			DriverManager.setLoginTimeout(30);
			String url;
			if (StringUtils.isEmpty(domain))
				url = MessageFormat.format(URL, new Object[] { host, String.valueOf(port), instanceName });
			else
				url = MessageFormat.format(URL_DOMAIN, new Object[] { host, String.valueOf(port), domain, instanceName });
			conn = DriverManager.getConnection(url, username, password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("exec sp_databases");
			while (rs.next()) {
				DatabaseInfo info = new DatabaseInfo();
				info.setDatabaseName(rs.getString("DATABASE_NAME"));
				info.setSize(rs.getLong("DATABASE_SIZE") / 1024);

				Statement stmt2 = null;
				ResultSet rs2 = null;
				ResultSet rs3 = null;
				try {
					stmt2 = conn.createStatement();
					stmt2.execute("use [" + info.getDatabaseName() + "]");
					rs2 = stmt2.executeQuery("EXEC sp_spaceused");
					if (rs2.next()) {
						info.setUnallocatedSize(getValue(rs2.getString("unallocated space")));
					}
					if (stmt2.getMoreResults()) {
						rs3 = stmt2.getResultSet();
						if (rs3.next()) {
							info.setReserved(getValue(rs3.getString("reserved")));
							info.setDataSize(getValue(rs3.getString("data")));
							info.setIndexSize(getValue(rs3.getString("index_size")));
							info.setUnused(getValue(rs3.getString("unused")));
						}
					}
				} catch (Exception e) {
					throw new DataAccessException("获取数据库信息出错:" + e.getMessage(), e);
				} finally {
					if (rs2 != null) {
						try {
							rs2.close();
						} catch (SQLException e) {
							logger.error("关闭rs2出错", e);
						}
					}
					if (rs3 != null) {
						try {
							rs3.close();
						} catch (SQLException e) {
							logger.error("关闭rs3出错", e);
						}
					}
					if (stmt2 != null) {
						try {
							stmt2.close();
						} catch (SQLException e) {
							logger.error("关闭stmt2出错", e);
						}
					}
				}
				databaseinfoList.add(info);
			}
		} catch (SQLException e) {
			throw new DataAccessException("获取数据库信息出错", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error("关闭rs出错", e);
				}
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error("关闭stmt出错", e);
				}
			}
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("关闭stmt出错", e);
			}
		}
		return databaseinfoList;
	}

	private static float getValue(String val) {
		if (val == null) {
			return 0;
		}
		if (val.endsWith("MB")) {
			return Float.parseFloat(val.replaceAll("MB", "").trim());
		}
		if (val.endsWith("KB")) {
			return Float.parseFloat(val.replaceAll("KB", "").trim()) / 1024;
		}
		return 0;
	}

}
