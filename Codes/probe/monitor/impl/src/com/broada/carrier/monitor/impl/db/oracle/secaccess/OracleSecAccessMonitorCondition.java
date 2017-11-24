package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>Title: OracleSecAccessMonitorCondition</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleSecAccessMonitorCondition implements Serializable {

  private static final long serialVersionUID = 3646496447191353616L;

  public static final String[][] WEEKDESC = new String[][] { { "1", "日" }, { "2", "一" }, { "3", "二" }, { "4", "三" },
      { "5", "四" }, { "6", "五" }, { "7", "六" } };

  protected String ipAddr; //IP地址

  protected String dbUser; //数据库用户

  protected String week; //星期

  protected String startTime; //开始时间

  protected String endTime; //结束时间

  protected String osUser; //操作系统用户

  protected String program; //连接数据库的程序

  public OracleSecAccessMonitorCondition() {

  }

  public OracleSecAccessMonitorCondition(String ipAddr) {
    this.ipAddr = ipAddr;
  }

  public String getDbUser() {
    return dbUser == null ? "" : dbUser;
  }

  public String getEndTime() {
    return endTime == null ? "" : endTime;
  }

  public String getIpAddr() {
    return ipAddr == null ? "" : ipAddr;
  }

  public String getOsUser() {
    return osUser == null ? "" : osUser;
  }

  public String getProgram() {
    return program == null ? "" : program;
  }

  public String getStartTime() {
    return startTime == null ? "" : startTime;
  }

  public String getWeek() {
    return week == null ? "" : week;
  }

  @JsonIgnore
  public String getTimeDesc() {
    if ((startTime == null || startTime.equals("")) && (endTime == null || endTime.equals(""))) {
      return "";
    }
    ;
    StringBuffer desc = new StringBuffer();
    if (startTime != null && !startTime.equals("")) {
      desc.append(startTime + "之后");
    }
    if (endTime != null && !endTime.equals("")) {
      desc.append(endTime + "之前");
    }
    return desc.toString();
  }

  @JsonIgnore
  public String getWeekDesc() {
    if (week == null || week.equals("")) {
      return "";
    }
    ;
    StringBuffer desc = new StringBuffer();
    for (int i = 0; i < WEEKDESC.length; i++) {
      if (week.indexOf(WEEKDESC[i][0]) >= 0) {
        desc.append(WEEKDESC[i][1] + "、");
      }
    }
    return desc.substring(0, desc.length() - 1);
  }

  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public void setIpAddr(String ipAddr) {
    this.ipAddr = ipAddr;
  }

  public void setOsUser(String osUser) {
    this.osUser = osUser;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public void setWeek(String week) {
    this.week = week;
  }
}
