package com.broada.carrier.monitor.impl.db.sybase.session;

/**
 * Sybase session 实体类
 * 
 * @author Wangx (wangx@broada.com)
 * Create By 2008-5-28 上午11:18:28
 */
public class SybaseSession {
  /**
   * pid
   */
  private String pid;

  /**
   * 状态
   */
  private String status;

  /**
   * 用户
   */
  private String user;

  /**
   * 主机
   */
  private String host;

  /**
   * 程序
   */
  private String program;
  
  /**
   * 数据库
   */
  private String database;

  /**
   * 命令
   */
  private String command;

  /**
   * 已用内存 K
   */
  private Double memUsage;

  /**
   * CPU时间 MS
   */
  private Double cpuTime;

  /**
   * IO读写次数
   */
  private Double ioNumber;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public Double getCpuTime() {
    return cpuTime;
  }

  public void setCpuTime(Double cpuTime) {
    this.cpuTime = cpuTime;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Double getIoNumber() {
    return ioNumber;
  }

  public void setIoNumber(Double ioNumber) {
    this.ioNumber = ioNumber;
  }

  public Double getMemUsage() {
    return memUsage;
  }

  public void setMemUsage(Double memUsage) {
    this.memUsage = memUsage;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
