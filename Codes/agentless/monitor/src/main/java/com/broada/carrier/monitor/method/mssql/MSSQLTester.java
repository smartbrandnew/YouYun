package com.broada.carrier.monitor.method.mssql;

import java.sql.Connection;

import org.apache.commons.lang.StringUtils;

import com.broada.utils.JDBCUtil;

public class MSSQLTester {
	private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

	private static final String URL = "jdbc:jtds:sqlserver://%s:%d;databaseName=%s";
	
	private static final String URL_DOMAIN = "jdbc:jtds:sqlserver://%s:%d;domain=%s&databaseName=%s";

	public String doTest(String host,Integer port, String userName,String pwd, String domain, String instanceName) {
		Connection connection = null;
		String url;
		if (instanceName == null)
		  instanceName = "";
		if (StringUtils.isBlank(domain))
			url = String.format(URL, host, port, instanceName);
		else
			url = String.format(URL_DOMAIN, host, port, domain, instanceName);
		try {
			connection = JDBCUtil.createConnection(DRIVER, url, userName, pwd);
		} catch (Throwable t) {
			return t.getMessage();
		} finally {
			JDBCUtil.close(connection);
		}
		return "true";
	}
	
	public String doTest(String host,Integer port, String userName,String pwd) {
	  return doTest(host, port, userName, pwd, null, null);
	}

}
