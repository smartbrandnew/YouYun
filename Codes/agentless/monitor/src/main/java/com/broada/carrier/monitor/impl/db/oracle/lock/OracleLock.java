package com.broada.carrier.monitor.impl.db.oracle.lock;

/**
 * <p>Title: OracleLock</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleLock {
  private String owner;
  private String objName;
  private String objType;
  private int ctime;
  
  public int getCtime() {
    return ctime;
  }
  public String getObjName() {
    return objName == null ? "" : objName;
  }
  public String getObjType() {
    return objType == null ? "" : objType;
  }
  public String getOwner() {
    return owner == null ? "" : owner;
  }
  
  public void setCtime(int ctime) {
    this.ctime = ctime;
  }
  public void setObjName(String objName) {
    this.objName = objName;
  }
  public void setObjType(String objType) {
    this.objType = objType;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }
  
}
