package com.broada.carrier.monitor.impl.db.db2;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.CLICollector;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.pool.CollectorPool;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;

public class DB2AgentExecutor {
	private static final Log logger = LogFactory.getLog(DB2AgentExecutor.class);

	private String uid = "-1";

	public DB2AgentExecutor(String uid) {
		this.uid = uid;
	}

	public List<String> execute(String ip, DB2MonitorMethodOption options, String cmd, String[] params)
			throws CLIException, SQLException {
		CLICollector collector = null;
		try {
			collector = CollectorPool.getCLICollector(options.gotCliOption().toOptions(ip), uid, true);
			if (params != null && params.length > 0) {
				for (int i = 0; i < params.length; i++) {
					params[i] = params[i].trim().replaceAll("\n", " ");
				}
			}
			List<String> result = collector.executeSQL(cmd, params);
			collector.close();
			if (result.size() > 0) {
				Object maybeExcep = (Object) result.get(0);// 如果返回的结果不是异常，那么就是正常获取得到的数据
				if (maybeExcep instanceof SQLException) {
					throw (SQLException) maybeExcep;
				} else if (maybeExcep instanceof Exception) {
					throw new SQLException(((Exception) maybeExcep).getMessage());
				} else if (maybeExcep instanceof String && maybeExcep.toString().startsWith("SQL")
						&& maybeExcep.toString().contains("SQLSTATE=")) {
					String msg = new String();
					String[] msgs = maybeExcep.toString().split("\n");
					if (msgs.length == 3) {
						String s = msgs[0] + msgs[1];
						int first_index = s.indexOf(" ");
						s = s.substring(first_index);
						int last_index = s.lastIndexOf(" ");
						s = s.substring(0, last_index);
						msg = s;
					} else {
						msg = maybeExcep.toString();
					}
					throw new SQLException("无法连接DB2数据库：" + msg);
				} else if (maybeExcep instanceof String
						&& maybeExcep.toString().contains("User \"" + params[0] + "\" does not exist.")) {
					throw new CLILoginFailException("用户：\"" + params[0] + "\"不存在", new Throwable(maybeExcep.toString()));
				}
			}
			return result;
		} catch (CLIConnectException ce) {
			logger.error("连接" + ip + "/" + options.getRemotePort() + "失败.", ce);
			throw ce;
		} catch (CLILoginFailException lfe) {
			logger.error("登录" + ip + "失败,用户:" + options.getAgentName(), lfe);
			throw lfe;
		} catch (CLIException e) {
			logger.error("执行" + cmd + "命令失败,主机[" + ip + "].", e);
			throw e;
		}
	}
}
