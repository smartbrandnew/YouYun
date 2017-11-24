package com.broada.carrier.monitor.impl.db.dm.lock;

/**
 * DM内存池监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午2:36:02
 */
public class DmLock {
  private Boolean isWacthed = Boolean.FALSE;
  private String addr;
  private Long sessionID; // 
  private Long trID;
  private String sqlText;
  private String ocurTime;
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public Long getSessionID() {
		return sessionID;
	}
	public void setSessionID(Long sessionID) {
		this.sessionID = sessionID;
	}
	public Long getTrID() {
		return trID;
	}
	public void setTrID(Long trID) {
		this.trID = trID;
	}
	public String getSqlText() {
		return sqlText;
	}
	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
	public String getOcurTime() {
		return ocurTime;
	}
	public void setOcurTime(String ocurTime) {
		this.ocurTime = ocurTime;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	
  
}