package com.broada.carrier.monitor.impl.db.informix.dbspace;

/**
 * <p>Title: InformixDataBaseSpace</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.4
 */

public class InformixDataBaseSpace {
	private Boolean isWacthed = Boolean.FALSE;
	private String name = "";
	private Double curPerDBS = new Double(0);
	private Double maxPerDBS = new Double(90);
  private Double minPerDBS = new Double(1);
	
	public Double getCurPerDBS() {
		return curPerDBS;
	}
  public Boolean getIsWacthed() {
		return isWacthed;
	}
	public Double getMaxPerDBS() {
		return maxPerDBS;
	}
	public String getName() {
		return name;
	}
	
	public void setCurPerDBS(Double curPerDBS) {
		this.curPerDBS = curPerDBS;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public void setMaxPerDBS(Double maxPerDBS) {
		this.maxPerDBS = maxPerDBS;
	}
	public void setName(String name) {
		this.name = name;
	}
  public Double getMinPerDBS() {
    return minPerDBS;
  }
  public void setMinPerDBS(Double minPerDBS) {
    this.minPerDBS = minPerDBS;
  }
}
