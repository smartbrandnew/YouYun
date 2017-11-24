package com.broada.carrier.monitor.impl.db.dm.ramPool;

/**
 * DM内存池监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午2:36:02
 */
public class DmRamPool {
  private Boolean isWacthed = Boolean.FALSE;
  private Integer counts; // 个数
  private Double totalSize;
  private Double freeSize;
  private Double usedSize;
  private Double useRate;
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public Integer getCounts() {
		return counts;
	}
	public void setCounts(Integer counts) {
		this.counts = counts;
	}
	public Double getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Double totalSize) {
		this.totalSize = totalSize;
	}
	public Double getFreeSize() {
		return freeSize;
	}
	public void setFreeSize(Double freeSize) {
		this.freeSize = freeSize;
	}
	public Double getUsedSize() {
		return usedSize;
	}
	public void setUsedSize(Double usedSize) {
		this.usedSize = usedSize;
	}
	public Double getUseRate() {
		return useRate;
	}
	public void setUseRate(Double useRate) {
		this.useRate = useRate;
	}
  
}