package com.broada.carrier.monitor.impl.db.dm.cache;

/**
 * 达梦数据库缓存监测
 * 
 * @author Zhouqa
 * Create By 2016年4月15日 下午2:21:10
 */
public class DmCacheInfo {
  private Boolean isWacthed = Boolean.FALSE;
  private String address = ""; //地址
  private String type;	//类型
  private String overflow;//是否溢出
  private String inPool; //是否在池中
  private String disabled; //是否可用
  private Long fixed; //被引用次数
  private Long timeSize; //时间戳
  
  public Boolean getIsWacthed() {
    return isWacthed;
  }

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOverflow() {
		return overflow;
	}

	public void setOverflow(String overflow) {
		this.overflow = overflow;
	}

	public String getInPool() {
		return inPool;
	}

	public void setInPool(String inPool) {
		this.inPool = inPool;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public Long getFixed() {
		return fixed;
	}

	public void setFixed(Long fixed) {
		this.fixed = fixed;
	}

	public Long getTimeSize() {
		return timeSize;
	}

	public void setTimeSize(Long timeSize) {
		this.timeSize = timeSize;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

}