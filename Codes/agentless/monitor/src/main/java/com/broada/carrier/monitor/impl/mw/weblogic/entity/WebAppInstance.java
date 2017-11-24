package com.broada.carrier.monitor.impl.mw.weblogic.entity;

/**
 * <p>Title: </p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 1.0
 */

public class WebAppInstance {
  Boolean isWacthed = Boolean.FALSE;
  String appName = "";
  String desc = "";
  Long curSession = new Long(0);
  Long maxSession = new Long(0);
  String instanceKey = "";
  Long curMaxSession = new Long(0);
  Long curTotalSession = new Long(0);

  public WebAppInstance(){}
  
  public WebAppInstance(String instanceKey, String appName, Long curSession){
    this.instanceKey = instanceKey;
    this.appName = appName;
    this.curSession = curSession;
  }

  public String getAppName() {
    return appName;
  }
  public Long getCurSession() {
    return curSession;
  }
  public String getDesc() {
    return desc;
  }
  public String getInstanceKey() {
    return instanceKey;
  }
  public Boolean getIsWacthed() {
    return isWacthed;
  }
  public Long getMaxSession() {
    return maxSession;
  }
  public Long getCurMaxSession() {
    return curMaxSession;
  }
  public Long getCurTotalSession() {
    return curTotalSession;
  }
  public void setAppName(String appName) {
    this.appName = appName;
  }
  public void setCurSession(Long curSession) {
    this.curSession = curSession;
  }
  public void setDesc(String desc) {
    this.desc = desc;
  }
  public void setInstanceKey(String instanceKey) {
    this.instanceKey = instanceKey;
  }
  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  public void setMaxSession(Long maxSession) {
    this.maxSession = maxSession;
  }
  public void setCurMaxSession(Long curMaxSession) {
    this.curMaxSession = curMaxSession;
  }
  public void setCurTotalSession(Long curTotalSession) {
    this.curTotalSession = curTotalSession;
  }

}