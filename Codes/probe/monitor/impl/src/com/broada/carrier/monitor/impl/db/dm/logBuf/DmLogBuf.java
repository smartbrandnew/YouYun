package com.broada.carrier.monitor.impl.db.dm.logBuf;

/**
 * 达梦数据库日志缓存监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午2:21:24
 */
public class DmLogBuf {
  private Boolean isWacthed = Boolean.FALSE;
  private String beginSln = ""; //地址
  private String endSln;	//类型
  private Long totalPage;//总页数
  private Long fixedPage; //已用页数
  private Double useRate; //利用率
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public String getBeginSln() {
		return beginSln;
	}
	public void setBeginSln(String beginSln) {
		this.beginSln = beginSln;
	}
	public String getEndSln() {
		return endSln;
	}
	public void setEndSln(String endSln) {
		this.endSln = endSln;
	}
	public Long getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(Long totalPage) {
		this.totalPage = totalPage;
	}
	public Long getFixedPage() {
		return fixedPage;
	}
	public void setFixedPage(Long fixedPage) {
		this.fixedPage = fixedPage;
	}
	public Double getUseRate() {
		return useRate;
	}
	public void setUseRate(Double useRate) {
		this.useRate = useRate;
	}

  
}