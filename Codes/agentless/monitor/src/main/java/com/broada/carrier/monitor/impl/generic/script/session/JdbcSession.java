package com.broada.carrier.monitor.impl.generic.script.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.method.jdbc.JdbcMonitorMethodOption;
import com.broada.cid.action.protocol.impl.jdbc.DBUtil;
import com.broada.cid.common.api.error.ErrorUtil;
import com.broada.utils.StringUtil;

public class JdbcSession {
	public static final String DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
	public static final String DRIVER_SYBASE = "com.sybase.jdbc2.jdbc.SybDriver";

	private String ip = "";
	private String driver = "";
	private String url = "";
	private String username = "";
	private String password = "";
	private String sid = "";
	private int port = 0;
	private String encoding = "GBK";

	public JdbcSession(JdbcMonitorMethodOption option, String ip) {
		username = option.getUsername();
		password = option.getPassword();
		sid = option.getSid();
		port = option.getPort();
		encoding = option.getEncoding();
		this.ip = ip;
		checkDbType(option.getDbType());

	}

	private void checkDbType(String dbType) {
		// TODO Auto-generated method stub
		if (dbType.equals("oracle")) {
			driver = DRIVER_ORACLE;
			url = "jdbc:oracle:thin:@" + ip + ":" + port + "/" + sid;
		} else if (dbType.equals("mysql")) {
			driver = DRIVER_MYSQL;
			url = "jdbc:mysql://" + ip + ":" + port + "/" + sid
					+ "?useUnicode=true&characterEncoding=" + encoding;
		} else if (dbType.equals("sybase")) {
			driver = DRIVER_SYBASE;
			url = "jdbc:sybase:Tds:" + ip + ":" + port + "/" + sid;
		} else {
			driver = "";
			url = "";
		}
	}

	public List execute(String sql) {
		if (StringUtil.isBlank(url) || StringUtil.isBlank(driver)) {
			return null;
		}
		Connection conn = DBUtil.createConnection(driver, url, username,
				password);
		List rows = new ArrayList();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int count = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < count; i++) {
					String key = rs.getMetaData().getColumnName(i + 1)
							.toLowerCase();
					Object value = rs.getObject(i + 1);
					map.put(key, value);
				}
				rows.add(map);
			}
			return rows;
		} catch (SQLException e) {
			throw ErrorUtil.createError(IllegalArgumentException.class,
					"SQL语句执行失败：" + sql, e);
		} finally {
			DBUtil.close(ps, rs, conn);
		}
	}
}
