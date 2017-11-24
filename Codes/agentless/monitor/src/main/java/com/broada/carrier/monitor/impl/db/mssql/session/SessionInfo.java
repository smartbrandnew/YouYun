package com.broada.carrier.monitor.impl.db.mssql.session;

public class SessionInfo{
  private String id;
  
  private String status;
  
  private String user;
  
  private String host;
  
  private String program;
  
  private long memory;
  
  private long cpuTime;
  
  private String database;
  
  private String command;
  
  private String loginTime;
  
  private String lastBatchTime;
  
  private boolean watched = true;
  
  public static final String INFO_SEPARATOR = "-";

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status.trim();
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public long getMemory() {
    return memory;
  }

  public void setMemory(long memory) {
    this.memory = memory;
  }

  public long getCpuTime() {
    return cpuTime;
  }

  public void setCpuTime(long cpuTime) {
    this.cpuTime = cpuTime;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getLoginTime() {
    return loginTime;
  }

  public void setLoginTime(String loginTime) {
    this.loginTime = loginTime;
  }

  public String getLastBatchTime() {
    return lastBatchTime;
  }

  public void setLastBatchTime(String lastBatchTime) {
    this.lastBatchTime = lastBatchTime;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  /**
   * 是否被监测
   */
  public boolean isWatched() {
    return watched;
  }

  /**
   * 是否被监测
   * @param watched
   */
  public void setWatched(boolean watched) {
    this.watched = watched;
  }
}
