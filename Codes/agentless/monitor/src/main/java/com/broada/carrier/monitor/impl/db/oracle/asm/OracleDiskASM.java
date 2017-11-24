package com.broada.carrier.monitor.impl.db.oracle.asm;

import java.io.Serializable;

public class OracleDiskASM implements Serializable {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String ITEM_USEDATE = "oracle_asm_useRate";
	  public static final String ITEM_AVAILABLESIZE = "oracle_asm_availableSize";
	  public static final String ITEM_TOTALSIZE ="oracle_asm_totalSize";
	private String instanceName;// 实例名
	private Double availableSize;// 可用空间
	private Double totalSize;// 总空间
	private Double useRate;// 空间使用率
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public Double getAvailableSize() {
		return availableSize;
	}
	public void setAvailableSize(Double availableSize) {
		this.availableSize = availableSize;
	}
	public Double getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Double totalSize) {
		this.totalSize = totalSize;
	}
	public Double getUseRate() {
		return useRate;
	}
	public void setUseRate(Double useRate) {
		this.useRate = useRate;
	}
}
