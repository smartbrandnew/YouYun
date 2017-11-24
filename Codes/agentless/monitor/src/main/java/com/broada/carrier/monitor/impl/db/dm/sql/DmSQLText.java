package com.broada.carrier.monitor.impl.db.dm.sql;

/**
 * sql监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午3:27:33
 */
public class DmSQLText {
  private Boolean isWacthed = Boolean.FALSE;
  private Long seqNo;
  private Long sessID;
  private Long trxID;
  private String sqlText;
  private String startTime;
  private Long timeUsed;
  private String isOver;
  private String userName;
  private String clntIp;
  private String appName;
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public Long getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(Long seqNo) {
		this.seqNo = seqNo;
	}
	public Long getSessID() {
		return sessID;
	}
	public void setSessID(Long sessID) {
		this.sessID = sessID;
	}
	public Long getTrxID() {
		return trxID;
	}
	public void setTrxID(Long trxID) {
		this.trxID = trxID;
	}
	public String getSqlText() {
		return sqlText;
	}
	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public Long getTimeUsed() {
		return timeUsed;
	}
	public void setTimeUsed(Long timeUsed) {
		this.timeUsed = timeUsed;
	}
	public String getIsOver() {
		return isOver;
	}
	public void setIsOver(String isOver) {
		this.isOver = isOver;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getClntIp() {
		return clntIp;
	}
	public void setClntIp(String clntIp) {
		this.clntIp = clntIp;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
}