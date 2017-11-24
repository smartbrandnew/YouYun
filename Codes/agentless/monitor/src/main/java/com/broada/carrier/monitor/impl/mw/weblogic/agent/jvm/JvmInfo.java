package com.broada.carrier.monitor.impl.mw.weblogic.agent.jvm;

/**
 * weblogic jvm监测
 * 
 * @author zhuhong
 * 
 */
public class JvmInfo implements java.io.Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * 是否监控
	 */
	private boolean isWatched = true;
	/*
	 * 当前堆大小
	 */
	private String heapCurr;
	/*
	 * 当前可用堆
	 */
	private String heapFree;
	/*
	 * 最大堆大小
	 */
	private String heapMax;
	/*
	 * 堆可用百分比
	 */
	private Double heapPercent;
	/*
	 * 堆可用百分比阈值
	 */
	private Double heapPercentMax = 0.2;
	/*
	 * 关键字
	 */
	private String instKey;

	public JvmInfo() {
	}

	public JvmInfo(String instKey, boolean isWatched, String heapCurr, String heapFree, String heapMax,
			Double heapPercent, Double heapPercentMax) {
		this.instKey = instKey;
		this.isWatched = isWatched;
		this.heapCurr = heapCurr;
		this.heapFree = heapFree;
		this.heapMax = heapMax;
		this.heapPercent = heapPercent;
		this.heapPercentMax = heapPercentMax;
	}

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	public boolean isWatched() {
		return isWatched;
	}

	public void setWatched(boolean isWatched) {
		this.isWatched = isWatched;
	}

	public String getHeapCurrStr() {
		return heapCurr;
	}
	
	public double getHeapCurr() {
		try {
			return Double.parseDouble(heapCurr);
		} catch (NumberFormatException e) {
			return 0;
		}		
	}

	public void setHeapCurr(String heapCurr) {
		this.heapCurr = heapCurr;
	}

	public double getHeapFree() {
		try {
			return Double.parseDouble(heapFree);
		} catch (NumberFormatException e) {
			return 0;
		}		
	}
	
	public String getHeapFreeStr() {
		return heapFree;
	}	

	public void setHeapFree(String heapFree) {
		this.heapFree = heapFree;
	}

	public double getHeapMax() {
		try {
			return Double.parseDouble(heapMax);
		} catch (NumberFormatException e) {
			return 0;
		}		
	}
	
	public String getHeapMaxStr() {
		return heapMax;
	}	

	public void setHeapMax(String heapMax) {
		this.heapMax = heapMax;
	}

	public Double getHeapPercent() {
		return heapPercent;
	}

	public void setHeapPercent(Double heapPercent) {
		this.heapPercent = heapPercent;
	}

	public Double getHeapPercentMax() {
		return heapPercentMax;
	}

	public void setHeapPercentMax(Double heapPercentMax) {
		this.heapPercentMax = heapPercentMax;
	}

}
