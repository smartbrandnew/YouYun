package com.broada.carrier.monitor.impl.db.dm.transactions;

/**
 * 事务监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午3:13:29
 */
public class DmTrx {
  private Boolean isWacthed = Boolean.FALSE;
  private Long trID;
  private String state;
  private String isolation; //隔离级
  private String readOnly;
  private Long sessID;
  private Integer insCnt;
  private Integer delCnt;
  private Integer uptCnt;
  private Integer uptInsCnt;
  private Integer urecSeq;
  private Integer wait;
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public Long getTrID() {
		return trID;
	}
	public void setTrID(Long trID) {
		this.trID = trID;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getIsolation() {
		return isolation;
	}
	public void setIsolation(String isolation) {
		this.isolation = isolation;
	}
	public String getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}
	public Long getSessID() {
		return sessID;
	}
	public void setSessID(Long sessID) {
		this.sessID = sessID;
	}
	public Integer getInsCnt() {
		return insCnt;
	}
	public void setInsCnt(Integer insCnt) {
		this.insCnt = insCnt;
	}
	public Integer getDelCnt() {
		return delCnt;
	}
	public void setDelCnt(Integer delCnt) {
		this.delCnt = delCnt;
	}
	public Integer getUptCnt() {
		return uptCnt;
	}
	public void setUptCnt(Integer uptCnt) {
		this.uptCnt = uptCnt;
	}
	public Integer getUptInsCnt() {
		return uptInsCnt;
	}
	public void setUptInsCnt(Integer uptInsCnt) {
		this.uptInsCnt = uptInsCnt;
	}
	public Integer getUrecSeq() {
		return urecSeq;
	}
	public void setUrecSeq(Integer urecSeq) {
		this.urecSeq = urecSeq;
	}
	public Integer getWait() {
		return wait;
	}
	public void setWait(Integer wait) {
		this.wait = wait;
	}
  
}