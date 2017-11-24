package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleJDBCUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleUrlUtil;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.StringUtil;

/**
 * <p>Title: OracleSecAccessMonitor</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleSecAccessMonitor extends BaseMonitor {

	public static final String DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";
	public static final String GET_CONNECTIONS = "select sid,serial#,client_info,machine,username,osuser,program from v$session where username is not null";
	public static final String GET_HOST_ADDRESS = "select utl_inaddr.get_host_address(?) ip from dual";
	private static final String UNKOWN_HOST_IP = "unkown host ip";
	private static final String CURRENT_SID_1 = "select userenv('sid') sid from dual";
	private static final String CURRENT_SID_2 = "select to_number(substrb(dbms_session.unique_session_id,1,4),'xxxx') sid from dual";
	private String CURRENT_SID = CURRENT_SID_1;

	private Map<Integer, Ip> cache = Collections.synchronizedMap(new HashMap<Integer, Ip>());
	private static class Ip {
		int serial;
		String ip;
		String hostName;
	}

	@SuppressWarnings({ "rawtypes"})
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		OracleSecAccessParameter p = context.getParameterObject(OracleSecAccessParameter.class);
		OracleMethod option = new OracleMethod(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String sid = option.getSid();
		StringBuffer msg = new StringBuffer(); // 监测结果信息描述

		// 监测数据库的其他情况
		if (sid == null || sid.length() == 0) {
			sid = "orcl";
		}

		String url = null;
		if(!StringUtil.isNullOrBlank(option.getServiceName()))   // 配置了service_name
			url = OracleUrlUtil.getUrl(ip, port, option.getServiceName(), true);
		else
			url = OracleUrlUtil.getUrl(ip, port, sid, false);
		String user = option.getUsername();
		if (user == null) {
			user = "testphf110";
		}
		String pass = option.getPassword();
		if (pass == null) {
			pass = "testphf";
		}

		// 建立连接
		Connection conn = null;
		int errCode = 0; // 0表示正常
		// 开始监测
		try {
			Exception exception = null;
			long replyTime = System.currentTimeMillis();
			try {
				conn = OracleJDBCUtil.createConnection(url, user, pass);
			} catch (ClassNotFoundException ex) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				msg.append("系统内部错误:" + DRIVER_CLASS + "没有发现.");
				result.setResultDesc(msg.toString());
				return result;
			} catch (SQLException ex) {
				errCode = ex.getErrorCode();
				exception = ex;
				OracleJDBCUtil.close(conn);
			}

			// 根据错误号来判断错误类型
			// 17002 IO 异常，因为前面已经判断端口可以连接，那么原因只能是实例名错误
			// 12505 实例名错误
			// 1034 ORACLE not available,数据库没打开
			// 1017 用户名或密码错误
			String errMsg = ((exception == null || exception.getMessage() == null) ? "" : exception.getMessage());
			if (errMsg.indexOf("ORA-12505") >= 0 || errMsg.indexOf("ERR=12505") >= 0) {
				errCode = 12505;
			}
			//添加oracle错误号
			msg.append("oracle错误号:ORA-"+errCode+".\n");
			if (errCode == 3136) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				msg.append("连接超时.\n");
				result.setResultDesc(msg.toString());
				return result;
			}else if (errCode == 12505) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				msg.append("配置的实例名错误,无法建立连接.\n");
				result.setResultDesc(msg.toString());
				return result;
			}else if (errCode == 17002) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				msg.append("IO错误,可能指定的端口不是Oracle监听端口.\n");
				result.setResultDesc(msg.toString());
				return result;
			}else if (errCode != 0 && errCode != 1017) {
				msg.append("数据库可能没有装载或打开,错误:" + exception.getMessage() + "\n");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(msg.toString());
				return result;
			} else {
				msg.append("数据库已经打开.\n");
			}

			// 用户登陆
			if (errCode == 1017) {
				result.setResponseTime(replyTime);
				msg.append("没有提供有效的登录用户.\n");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(msg.toString());
				return result;
			} else {
				msg.append("用户登录成功.\n");
			}

			// 检查该用户是否有DBA或OEM_MONITOR角色，当视图v$session存在时，表示有该角色；不存在，无该角色.
			String sql = "select count(*) from v$session";
			Statement stm = null;
			boolean exist = false;
			try {
				stm = conn.createStatement();
				stm.execute(sql);
				exist = true;
			} catch (SQLException ex) {
			} finally {
				OracleJDBCUtil.close(stm);
			}
			if (!exist) {
				msg.append("指定用户无DBA或OEM_MONITOR角色.\n");
				result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
				result.setResultDesc(msg.toString());
				return result;
			}

			// 监测数据库连接合法性
			List records = null;
			try {
				records = getConnections(conn);
			} catch (SQLException e) {
				msg.append("获取所有数据连接失败:" + e.getMessage() + "\n");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(msg.toString());
				return result;
			}
			List conditions = p.getConditions();
			String localHostName = "";
			try {
				localHostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
			}
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime(replyTime);

			OracleSecAccessRules osar = new OracleSecAccessRules(conditions, records, localHostName, user);
			boolean wonted = osar.matchAllRecords();
			if (wonted) {
				result.setState(MonitorConstant.MONITORSTATE_NICER);
			} else {
				result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
				result.setResultDesc(osar.getMsg().toString());
			}
		} catch (Throwable e) {
			//发生不可预测的异常
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("发生不可预测的错误:" + e.getMessage());
		} finally {
			OracleJDBCUtil.close(conn);
		}
		return result;
	}

	private String getIp(int sid, int serial, String hostName, Map<String, String> localCache, boolean get_host_address) {
		Ip ip = cache.get(sid);
		if (ip != null) {
			if (ip.serial == serial) {
				if (!localCache.containsKey(ip.hostName))
					localCache.put(ip.hostName, ip.ip);
				return ip.ip;
			}
			cache.remove(sid);
		}
		if (StringUtils.isNotBlank(hostName)) {
			String ipAddr = localCache.get(hostName);
			if (ipAddr != null && (get_host_address || ipAddr != UNKOWN_HOST_IP))
				cacheIp(sid, serial, ipAddr, hostName);
			return ipAddr;
		}
		return null;
	}

	private void cacheIp(int sid, int serial, String ip, String hostName) {
		Ip ip_ = new Ip();
		ip_.serial = serial;
		ip_.ip = ip;
		ip_.hostName = hostName;
		cache.put(sid, ip_);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getConnections(Connection con) throws SQLException {
		List conns = new ArrayList();
		// 缓存IP避免重复获取
		Map<String, String> hostIps = new HashMap<String, String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psIp = null;
		ResultSet rsIp = null;
		// 是否尝试使用数据库的utl_inaddr.get_host_address获取IP
		boolean get_host_address = true;
		// 获取当前连接的SID
		int currentSid = getCurrentSid(con);
		try {
			ps = con.prepareStatement(GET_CONNECTIONS);
			rs = ps.executeQuery();
			while (rs.next()) {
				OracleAccess oa = new OracleAccess();
				int sid = rs.getInt("sid");
				// 排除当前连接
				if (sid == currentSid)
					continue;
				int serial = rs.getInt("serial#");

				String hostName = rs.getString("machine");
				hostName = (hostName == null ? "" : hostName.trim());

				boolean failedFromJDBC = false;
				// 首先，试着从缓存中取IP，避免大量的数据库查询
				String ipAddr = getIp(sid, serial, hostName, hostIps, get_host_address);
				// 然后，试着用数据库的utl_inaddr.get_host_address获取IP
				if (get_host_address && ipAddr == null && StringUtils.isNotBlank(hostName)) {
					if (psIp == null)
						psIp = con.prepareStatement(GET_HOST_ADDRESS);
					psIp.setString(1, hostName.substring(hostName.indexOf('\\') + 1));
					try {
						rsIp = psIp.executeQuery();
						if (rsIp.next()) {
							ipAddr = rsIp.getString(1);
							// 缓存IP，提高效率
							cacheIp(sid, serial, ipAddr, hostName);
							hostIps.put(hostName, ipAddr);
						}
					} catch (SQLException e) {
						String errMsg = e.getMessage();
						// 如果没有调用utl_inaddr.get_host_address函数的权限
						if (StringUtils.contains(errMsg, "ORA-24247"))
							get_host_address = false;
						// 如果产生的异常是“未知的主机”
						else if (StringUtils.contains(errMsg, "ORA-29257"))
							failedFromJDBC = true;
						else
							throw e;
					} finally {
						OracleJDBCUtil.close(rsIp);
						rsIp = null;
					}
				}
				// 试着通过Java的API获取IP
				if (failedFromJDBC || ipAddr == null && StringUtils.isNotBlank(hostName)) {
					try {
						ipAddr = InetAddress.getByName(hostName.substring(hostName.indexOf('\\') + 1)).getHostAddress();
						cacheIp(sid, serial, ipAddr, hostName);
						hostIps.put(hostName, ipAddr);
					} catch (UnknownHostException e) {
						// 缓存结果
						if (failedFromJDBC)
							cacheIp(sid, serial, UNKOWN_HOST_IP, hostName);
						hostIps.put(hostName, UNKOWN_HOST_IP);
					}
				}
				// 试着从client_info字段中获取IP
				if (ipAddr == null || ipAddr == UNKOWN_HOST_IP) {
					ipAddr = rs.getString("client_info");
					ipAddr = (ipAddr == null ? "" : ipAddr.trim());
				}

				String dbUser = rs.getString("username");
				dbUser = (dbUser == null ? "" : dbUser.trim());

				String osUser = rs.getString("osuser");
				try {
					if (osUser != null) {
						osUser = new String(osUser.getBytes(), "GBK");
					}
				} catch (UnsupportedEncodingException e) {
				}
				osUser = (osUser == null ? "" : osUser.trim());

				String program = rs.getString("program");
				program = (program == null ? "" : program.trim());

				oa.setIpAddr(ipAddr);
				oa.setHostName(hostName);
				oa.setDbUser(dbUser);
				oa.setOsUser(osUser);
				oa.setProgram(program);
				conns.add(oa);
			}
		} finally {
			OracleJDBCUtil.close(rs, ps);
			OracleJDBCUtil.close(psIp);
		}
		return conns;
	}

	private int getCurrentSid(Connection conn) throws SQLException {
		while (true) {
			Statement statement = null;
			ResultSet resultSet = null;
			try {
				statement = conn.createStatement();
				resultSet = statement.executeQuery(CURRENT_SID);
				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
				return Integer.MIN_VALUE;
			} catch (SQLException e) {
				int errCode = e.getErrorCode();
				if (errCode == 2003) {
					CURRENT_SID = CURRENT_SID_2;
				} else {
					throw e;
				}
			} finally {
				OracleJDBCUtil.close(resultSet, statement);
			}
		}
	} 
}
