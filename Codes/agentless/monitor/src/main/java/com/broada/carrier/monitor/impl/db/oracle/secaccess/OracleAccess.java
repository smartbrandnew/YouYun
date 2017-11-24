package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import org.apache.commons.lang.StringUtils;

/**
 * <p>Title: OracleSecAccess</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleAccess {
  protected String ipAddr; //IP地址

  protected String hostName; //主机名称

  protected String dbUser; //数据库用户

  protected String osUser; //操作系统用户

  protected String program; //连接数据库的程序

  public String getDbUser() {
    return dbUser;
  }

  public String getIpAddr() {
    return ipAddr;
  }

  public String getHostName() {
    return hostName;
  }

  public String getOsUser() {
    return osUser;
  }

  public String getProgram() {
    return program;
  }

  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }

  public void setIpAddr(String ipAddr) {
    this.ipAddr = ipAddr;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setOsUser(String osUser) {
    this.osUser = osUser;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getDesc() {
    StringBuffer sb = new StringBuffer("");
    //    if (ipAddr != null && ipAddr.length() > 0) {
    //      sb.append(ipAddr + ";");
    //      if (hostName != null && hostName.length() > 0) {
    //        if (hostName.indexOf("\\") >= 0) {
    //          String[] names = hostName.split("\\\\");
    //          hostName = names[names.length - 1];
    //        }
    //        sb.append(hostName + ";");
    //      }
    //    } else {
    //      if (hostName != null && hostName.length() > 0) {
    //        if (hostName.indexOf("\\") >= 0) {
    //          String[] names = hostName.split("\\\\");
    //          hostName = names[names.length - 1];
    //        }
    //        String ip = "";
    //        try {
    //          ip = InetAddress.getByName(hostName).getHostAddress();
    //        } catch (UnknownHostException e) {
    //        }
    //        sb.append(ip + ";" + hostName + ";");
    //      }
    //    }
    //    if(dbUser != null && dbUser.length() > 0){
    //      sb.append(dbUser + ";");
    //    }
    if (StringUtils.isNotBlank(hostName)) {
      sb.append(hostName.substring(hostName.indexOf('\\') + 1));
    } else if (StringUtils.isNotBlank(ipAddr)) {
      sb.append(ipAddr);
    }
    if (StringUtils.isNotBlank(osUser)) {
      sb.append("(").append(osUser).append("),");
    } else {
      sb.append("(未知的用户),");
    }
    //    if(program != null && program.length() > 0){
    //      sb.append(program + ";");
    //    }
    //    Date today = new Date();
    //    SimpleDateFormat sdf = new SimpleDateFormat("E");
    //    sdf.format(today);
    //    String todayDesc = DateUtil.DATETIME_FORMAT.format(today);
    //    sb.append(todayDesc + ";");
    return sb.toString();
  }

}
