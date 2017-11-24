package com.broada.carrier.monitor.impl.db.sybase.basic;

public class SybaseInfo {
  private String databaseProductName;
  private String databaseProductVersion;
  private int databaseMajorVersion;
  private int databaseMinorVersion;
  private String version;
  private String dbmsName;
  private String dbmsVer;
  private float memory;
  private int usedmem;
  private String sysSprocVersion;
  public String getDbmsName() {
    return dbmsName;
  }
  public void setDbmsName(String dbmsName) {
    this.dbmsName = dbmsName;
  }
  public String getDbmsVer() {
    return dbmsVer;
  }
  public void setDbmsVer(String dbmsVer) {
    this.dbmsVer = dbmsVer;
  }
  public float getMemory() {
    return memory;
  }
  public void setMemory(float memory) {
    this.memory = memory;
  }
  public int getUsedmem() {
    return usedmem;
  }
  public void setUsedmem(int usedmem) {
    this.usedmem = usedmem;
  }
  public String getDatabaseProductName() {
    return databaseProductName;
  }
  public void setDatabaseProductName(String databaseProductName) {
    this.databaseProductName = databaseProductName;
  }
  public String getDatabaseProductVersion() {
    return databaseProductVersion;
  }
  public void setDatabaseProductVersion(String databaseProductVersion) {
    this.databaseProductVersion = databaseProductVersion;
  }
  
  public int getDatabaseMajorVersion() {
    return databaseMajorVersion;
  }
  public void setDatabaseMajorVersion(int databaseMajorVersion) {
    this.databaseMajorVersion = databaseMajorVersion;
  }
  public int getDatabaseMinorVersion() {
    return databaseMinorVersion;
  }
  public void setDatabaseMinorVersion(int databaseMinorVersion) {
    this.databaseMinorVersion = databaseMinorVersion;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  public String getSysSprocVersion() {
    return sysSprocVersion;
  }
  public void setSysSprocVersion(String sysSprocVersion) {
    this.sysSprocVersion = sysSprocVersion;
  }
}
