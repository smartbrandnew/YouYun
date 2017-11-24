package com.broada.carrier.monitor.impl.db.informix.tbrecord;

/**
 * <p>Title: InformixTable</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.4
 */

public class InformixTableRecord {
  private Boolean isWacthed = Boolean.FALSE;

  private String name = "";

  private String type = "";

  private Integer curRecords = new Integer(0);

  private Integer maxRecords = new Integer(0);

  public Integer getCurRecords() {
    return curRecords;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public Integer getMaxRecords() {
    return maxRecords;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    switch (this.type.charAt(0)) {
    case 'T':
      return "表";
    case 'V':
      return "视图";
    default:
      return "表";
    }
  }

  public void setCurRecords(Integer curRecords) {
    this.curRecords = curRecords;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public void setMaxRecords(Integer maxRecords) {
    this.maxRecords = maxRecords;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }

}
