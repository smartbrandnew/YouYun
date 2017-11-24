package com.broada.carrier.monitor.impl.mw.iis.conns;

public class IISConns {
  private Boolean isWacthed = Boolean.FALSE;

  private String webName;

  private Integer currentConnections;

  private Integer currentConnectionsValue = new Integer(4);

  private Integer maximumConnections;

  private Integer maximumConnectionsValue = new Integer(10);;

  private Integer connectionAttemptsPersec;

  private Integer connectionAttemptsPersecValue = new Integer(40);

  private Integer logonAttemptsPersec;

  private Integer logonAttemptsPersecValue = new Integer(20);

  public Integer getConnectionAttemptsPersec() {
    return connectionAttemptsPersec;
  }

  public void setConnectionAttemptsPersec(Integer connectionAttemptsPersec) {
    this.connectionAttemptsPersec = connectionAttemptsPersec;
  }

  public Integer getConnectionAttemptsPersecValue() {
    return connectionAttemptsPersecValue;
  }

  public void setConnectionAttemptsPersecValue(Integer connectionAttemptsPersecValue) {
    this.connectionAttemptsPersecValue = connectionAttemptsPersecValue;
  }

  public Integer getCurrentConnections() {
    return currentConnections;
  }

  public void setCurrentConnections(Integer currentConnections) {
    this.currentConnections = currentConnections;
  }

  public Integer getCurrentConnectionsValue() {
    return currentConnectionsValue;
  }

  public void setCurrentConnectionsValue(Integer currentConnectionsValue) {
    this.currentConnectionsValue = currentConnectionsValue;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public Integer getLogonAttemptsPersec() {
    return logonAttemptsPersec;
  }

  public void setLogonAttemptsPersec(Integer logonAttemptsPersec) {
    this.logonAttemptsPersec = logonAttemptsPersec;
  }

  public Integer getLogonAttemptsPersecValue() {
    return logonAttemptsPersecValue;
  }

  public void setLogonAttemptsPersecValue(Integer logonAttemptsPersecValue) {
    this.logonAttemptsPersecValue = logonAttemptsPersecValue;
  }

  public Integer getMaximumConnections() {
    return maximumConnections;
  }

  public void setMaximumConnections(Integer maximumConnections) {
    this.maximumConnections = maximumConnections;
  }

  public Integer getMaximumConnectionsValue() {
    return maximumConnectionsValue;
  }

  public void setMaximumConnectionsValue(Integer maximumConnectionsValue) {
    this.maximumConnectionsValue = maximumConnectionsValue;
  }

  public String getWebName() {
    return webName;
  }

  public void setWebName(String webName) {
    this.webName = webName;
  }
}
