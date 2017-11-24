package com.broada.carrier.monitor.impl.db.sybase.basic;

import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;

public class SybaseUtils {
  private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";
  private static final String URL = "jdbc:jtds:sybase://{0}:{1}";
  private static final String TR = "<tr><th style=''font-family:宋体;font-size:10px;width:120px;text-align:right''>{0}</th><td style=''color:blue;font-family:Courier New;font-size:12px''>{1}</td></tr>";
  public static String getSybaseInfoDesc(String host, int port, String dbName, String user, String password) throws Exception{
    SybaseInfo sybaseInfo = SybaseUtils.getSybaseInfo(host, port, dbName, user, password);
    if(sybaseInfo != null){
      StringBuffer buffer = new StringBuffer("<html><>head><style>table td{font-family:'宋体';font-size:12px}</style></head><body><table>");
      buffer.append(MessageFormat.format(TR, new Object[]{"数据库管理系统名称:",sybaseInfo.getDbmsName()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"数据库管理系统版本:",sybaseInfo.getDbmsVer()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"数据库产品名称:",sybaseInfo.getDatabaseProductName()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"数据库产品版本:",sybaseInfo.getDatabaseProductVersion()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"主版本号:","" + sybaseInfo.getDatabaseMajorVersion()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"次版本号:","" + sybaseInfo.getDatabaseMinorVersion()}));
      buffer.append(MessageFormat.format(TR,new Object[]{"系统信息:",sybaseInfo.getVersion()}));
      buffer.append("</table></body></html>");
      return buffer.toString();
    }
    return null;
  }

	public static SybaseInfo getSybaseInfo(String host, int port, String dbName, String user, String password)
			throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = JDBCUtil.createConnection(DRIVER, MessageFormat.format(URL, new Object[] { host, String.valueOf(port) }),
					user, StringUtil.convertNull2Blank(password));
			DatabaseMetaData meta = conn.getMetaData();

			SybaseInfo sybaseInfo = new SybaseInfo();
			sybaseInfo.setDatabaseProductName(meta.getDatabaseProductName());
			sybaseInfo.setDatabaseProductVersion(meta.getDatabaseProductVersion());
			sybaseInfo.setDatabaseMajorVersion(meta.getDatabaseMajorVersion());
			sybaseInfo.setDatabaseMinorVersion(meta.getDatabaseMinorVersion());

			stmt = conn.createStatement();
			if (stmt.execute("select @@version as version")) {
				rs = stmt.getResultSet();
				if (rs.next()) {
					sybaseInfo.setVersion(rs.getString("version"));
				}
			}
			rs.close();

			rs = stmt.executeQuery("exec sp_server_info");
			while (rs.next()) {
				String attrName = rs.getString("attribute_name");
				if (isMatch(attrName, "DBMS_NAME"))
					sybaseInfo.setDbmsName(rs.getString("attribute_value"));
				else if (isMatch(attrName, "DBMS_VER"))
					sybaseInfo.setDbmsVer(rs.getString("attribute_value"));
				else if (isMatch(attrName, "SYS_SPROC_VERSION"))
					sybaseInfo.setSysSprocVersion(rs.getString("attribute_value"));
			}
			return sybaseInfo;
		} catch (Exception e) {
			throw e;
		} finally {
			JDBCUtil.close(rs, stmt, conn);
		}
	}

  private static boolean isMatch(String attrName, String targetName){
    if(attrName != null){
      attrName = attrName.trim().toUpperCase();
      return attrName.equals(targetName);
    }
    return false;
  }
}
