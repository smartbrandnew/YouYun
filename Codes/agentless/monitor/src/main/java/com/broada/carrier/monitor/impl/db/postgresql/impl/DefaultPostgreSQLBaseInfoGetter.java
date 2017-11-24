package com.broada.carrier.monitor.impl.db.postgresql.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.utils.DateUtil;
import com.broada.utils.JDBCUtil;

public class DefaultPostgreSQLBaseInfoGetter {
	private static final Log logger = LogFactory.getLog(DefaultPostgreSQLBaseInfoGetter.class);
	private Connection connection = null;

	public DefaultPostgreSQLBaseInfoGetter(Connection connection) {
		this.connection = connection;
	}

	public Map<String, String> getBasicInfo() {
		Map<String, String> sysInfoMap = new HashMap<String, String>();
		sysInfoMap.put("lastoid", getLastSysOid());
		sysInfoMap.put("strVersion", getStrVersion());
		sysInfoMap.put("startUpTime", getStartTime());
		sysInfoMap.put("intVersion", getVacuumAndVserion("server_version"));
		sysInfoMap.put("autovacuum", getVacuumAndVserion("autovacuum"));
		sysInfoMap.put("sessionCount", getSessionCount());
		return sysInfoMap;
	}

	private String getSessionCount() {
		Statement stmt = null;
		ResultSet rs = null;
		String val = null;
		String sql = "select count(pid) from pg_stat_activity";
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				val = rs.getString("count");
			}
		} catch (SQLException e) {
			logger.error("获取系统信息错误,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return val;
	}

	private String getVacuumAndVserion(String var) {
		Statement stmt = null;
		ResultSet rs = null;
		String val = null;
		String sql = "SELECT setting FROM pg_settings WHERE name ='" + var + "'";
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				val = rs.getString("setting");
			}
		} catch (SQLException e) {
			logger.error("获取系统信息错误,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return val;
	}

	private String getStartTime() {
		Statement stmt = null;
		ResultSet rs = null;
		String startTime = "";
		String sql = "SELECT usecreatedb, usesuper, CASE WHEN usesuper THEN pg_postmaster_start_time() ELSE NULL END as upsince  FROM pg_user WHERE usename=current_user;";
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				startTime = rs.getString("upsince");
			}

			// 特殊处理下，只显示yyyy-MM-dd hh:mm:ss格式的日期
			try {
				Date date = DateUtil.DATETIME_FORMAT.parse(startTime);
				startTime = DateUtil.DATETIME_FORMAT.format(date);
			} catch (ParseException e) {
			}

		} catch (SQLException e) {
			logger.error("获取系统最近启动时间,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return startTime;
	}

	private String getStrVersion() {
		Statement stmt = null;
		ResultSet rs = null;
		String versionDetail = "";
		String sql = "select version() as version";
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				versionDetail = rs.getString("version");
			}
		} catch (SQLException e) {
			logger.error("获取版本字符串错误,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return versionDetail;
	}

	private String getLastSysOid() {
		Statement stmt = null;
		ResultSet rs = null;
		String lastsysoid = "";
		String sql = "SELECT oid, pg_encoding_to_char(encoding) AS encoding, datlastsysoid  FROM pg_database WHERE datname=current_user";
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				lastsysoid = rs.getString("datlastsysoid");
			}
		} catch (SQLException e) {
			logger.error("获取系统最后使用的oid出错,SQL=" + sql, e);
			return null;
		} finally {
			JDBCUtil.close(rs, stmt);
		}
		return lastsysoid;
	}

}
