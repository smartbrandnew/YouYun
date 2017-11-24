package com.broada.carrier.monitor.method.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.db.db2.DB2AgentExecutor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;
import com.tongtech.logging.Logger;

public class DB2Tester {
	private static final org.slf4j.Logger logger=LoggerFactory.getLogger(DB2Tester.class);
	public static final String NORMAL = "Normal";

	public static final String AS400 = "As400";

	public static final String DRIVER_NORMAL = "com.ibm.db2.jcc.DB2Driver";

	public static final String DRIVER_AS400 = "com.ibm.as400.access.AS400JDBCDriver";

	public static final String URL_NORMAL = "jdbc:db2://{0}:{1}/{2}";

	public static final String URL_AS400 = "jdbc:as400://{0}:{1}/{2}";
	// 10以下版本的权限检查语句
	private static String sqlAuth1 = "SELECT count(*) FROM TABLE( SNAPSHOT_DBM(-1)) as SNAPSHOT_DBM";
	// 10以上版本的权限检查语句
	private static String sqlAuth2 = "SELECT count(*) FROM TABLE( SNAP_GET_DBM(-1)) as SNAP_GET_DBM";
	// 获取版本号
	private static String sqlVsersion = "SELECT service_level FROM TABLE (sysproc.env_get_inst_info()) as INSTANCEINFO";
	
	public static String doTest(ServerProbeService service, int probeId, String driverType, String host, String port, String sid, String user, String psw) {
		return (String) service.executeMethod(probeId, DB2Tester.class.getName(), "doTest", driverType, host, port, sid, user, psw);
	}
	
	public static String doTestCli(ServerProbeService service, int probeId, String ip, String optType, String sysname, String sessionName, int remotePort, String sysversion, String db, String username) {
		return (String) service.executeMethod(probeId, DB2Tester.class.getName(), "doTestCli", ip, optType, sysname, sessionName, String.valueOf(remotePort), sysversion, db, username);
	}
	
	public String doTestCli(String ip, String optType, String sysname, String sessionName, String remotePort, String sysversion, String db, String username){
		CLIMonitorMethodOption optionCli = new CLIMonitorMethodOption();
  	optionCli.setSysname(sysname);
  	optionCli.setSessionName(sessionName);
  	optionCli.setRemotePort(Integer.parseInt(remotePort));
  	optionCli.setSysversion(sysversion);
		
		DB2MonitorMethodOption option = new DB2MonitorMethodOption();
		option.setOptType(optType);
    option.updateCliOption(optionCli);
    option.setDb(db);
    option.setUsername(username);

		// 连接参数的获取
    try {
    	List<String> listVersion = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT, new String[] { option.getUsername(), option.getDb(), sqlVsersion });
    	String version = getVsersion(listVersion.get(0));
    	if(version == null)
    	return "false,未能获取数据库版本号";
    	logger.info("版本信息: {}",version);
    	if(ifNewVersion(version))
    		new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT, new String[] { option.getUsername(), option.getDb(), sqlAuth2 });
    	else
    		new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT, new String[] { option.getUsername(), option.getDb(), sqlAuth1 });
    	return "true," + version;
		} catch (Exception e) {
			return "false," + e.getMessage();
		}
	}

	public String doTest(String driverType, String host, String port, String sid, String user, String psw) {
		Connection conn = null;
		Statement state = null;
		ResultSet rs = null;
		String url = "";
		String driver = "";
		if (StringUtil.isNullOrBlank(driverType) || driverType.equalsIgnoreCase(NORMAL)) {
			url = URL_NORMAL;
			driver = DRIVER_NORMAL;
		} else if (driverType.equalsIgnoreCase(AS400)) {
			url = URL_AS400;
			driver = DRIVER_AS400;
		}

		String realUrl = MessageFormat.format(url, new Object[] { host, port, sid });
		try {
			String version = null;
			conn = JDBCUtil.createConnection(driver, realUrl, user, psw);
			state = conn.createStatement();
			rs = state.executeQuery(sqlVsersion);
			if(rs.next())
				version = getVsersion(rs.getString(1));
			else
				return "false,未能获取数据库版本号";
			
			if(ifNewVersion(version))
				rs = state.executeQuery(sqlAuth2);
			else
				rs = state.executeQuery(sqlAuth1);
			
			if(rs.next())
				return "true," + version;
			else
				return "false,缺少dba权限";
		} catch (Throwable t) {
			t.printStackTrace();
			return "false," + t.getMessage();
		} finally {
			JDBCUtil.close(rs);
			JDBCUtil.close(state);
			JDBCUtil.close(conn);
		}
	}
	
	private String getVsersion(String version){
		int index = version.indexOf("DB2 v");
  	if(index == -1)
  		return null;
  	version = version.substring(index + 5);
  	index = version.indexOf(" ");
  	if(index == -1)
  		return version;
  	return version.substring(0, index);
	}
	
	/**
	 * 判断数据库版本是否大于10
	 * @param version
	 * @return
	 * @throws Exception
	 */
	private boolean ifNewVersion(String version) throws Exception{
		if(version == null)
			throw new Exception("false,未能获取数据库版本号");
		int index = version.indexOf(".");
		if(index == -1)
			index = version.length();
		return Integer.parseInt(version.substring(0, index)) >= 10;
	}
}
