package com.broada.carrier.monitor.impl.db.oracle.sql;

/**
 * Oracle SQL实体类
 * 
 * @author Wangx (wangx@broada.com)
 * Create By 2008-6-11 下午04:02:19
 */
public class OracleSqlInfo {
  private String sid;

  private String userName;

  private String sqlText;

  /**
   * 毫秒 ms
   */
  private Double execTime;

  /**
   * KB
   */
  private Double runtimeMem;

  public Double getExecTime() {
    return execTime;
  }

  public void setExecTime(Double execTime) {
    this.execTime = execTime;
  }

  public Double getRuntimeMem() {
    return runtimeMem;
  }

  public void setRuntimeMem(Double runtimeMem) {
    this.runtimeMem = runtimeMem;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public String getSqlText() {
    return sqlText;
  }

  public void setSqlText(String sqlText) {
    this.sqlText = sqlText;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
