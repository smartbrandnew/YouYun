package com.broada.carrier.monitor.impl.db.oracle.tablespace;

/**
 * <p>
 * Title: OracleTableSpace
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class OracleTableSpace {
	public static String[] alermItem = new String[] { "_Used", "_FreeExtents" };
	private Boolean isWacthed = Boolean.FALSE;
	private String tsName = "";
	private Double totalSize = new Double(0); // 表空间总大小
	private Double maxUsedTS = new Double(90);//已使用率阈值
	private Double curAvailTS = new Double(0);// 当前可使用率
	private Double maxAvailTS = new Double(10);// 可使用率阈值
	private Double avgReadTim = new Double(0);
	private Double avgWriteTim = new Double(0);
	private Double extentCount = new Double(0); // 表空间扩展次数
	private Double nextExtent = new Double(0); // 表空间next extent 大小，单位KB
	private Double maxExtents = new Double(2147483645);// 最大扩展extents
	private Double freeExtents = new Double(2147483645);// 空闲extents
	private Double maxFreeExtents = new Double(1024);// 空闲extents阈值
	private String spaceType = "";// 表空间类型
	private String segmentManagementType = "";// 表空间扩展类型
	private double maxSpace;		// 表空间允许的最大值，单位MB，可能是“无限制”
	private String isAutoExtend;

	public Double getMaxUsedTS() {
		return maxUsedTS;
	}

	public void setMaxUsedTS(Double maxUsedTS) {
		this.maxUsedTS = maxUsedTS;
	}

	public double getMaxSpace() {
		return maxSpace;
	}

	public void setMaxSpace(double maxSpace) {
		this.maxSpace = maxSpace;
	}

	public String getAutoExtend() {
		return isAutoExtend;
	}

	public void setAutoExtend(String isAutoExtend) {
		this.isAutoExtend = isAutoExtend;
	}

	public String getSegmentManagementType() {
		return segmentManagementType;
	}

	public void setSegmentManagementType(String segmentManagementType) {
		this.segmentManagementType = segmentManagementType;
	}

	public Double getFreeExtents() {
		return freeExtents;
	}

	public void setFreeExtents(Double freeExtents) {
		this.freeExtents = freeExtents;
	}

	public Double getMaxExtents() {
		return maxExtents;
	}

	public void setMaxExtents(Double maxExtents) {
		this.maxExtents = maxExtents;
	}

	public Double getMaxFreeExtents() {
		return maxFreeExtents;
	}

	public void setMaxFreeExtents(Double maxFreeExtents) {
		this.maxFreeExtents = maxFreeExtents;
	}

	public String getSpaceType() {
		return spaceType;
	}

	public void setSpaceType(String spaceType) {
		this.spaceType = spaceType;
	}

	public Double getTotalSize() {
		return totalSize;
	}

	public Double getCurAvailTS() {
		return curAvailTS;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public Double getMaxAvailTS() {
		return maxAvailTS;
	}

	public String getTsName() {
		return tsName;
	}

	public Double getAvgReadTim() {
		return avgReadTim;
	}

	public Double getAvgWriteTim() {
		return avgWriteTim;
	}

	public Double getExtentCount() {
		return extentCount;
	}

	public Double getNextExtent() {
		return nextExtent;
	}

	public void setTotalSize(Double totalSize) {
		this.totalSize = totalSize;
	}

	public void setCurAvailTS(Double curAvailTS) {
		this.curAvailTS = curAvailTS;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public void setMaxAvailTS(Double maxAvailTS) {
		this.maxAvailTS = maxAvailTS;
	}

	public void setTsName(String tsName) {
		this.tsName = tsName;
	}

	/**
	 * 当前表空间已使用容量大小
	 * 
	 * @return
	 */
	public double getUsed() {
		return totalSize.doubleValue() - getFree();
	}
	
	/**
	 * 获取已使用率
	 * @return
	 */
	public double getUsedPct() {
		return 100 - getCurAvailTS();
	}	
	
	/**
	 * 获取当前表空间未使用容量大小
	 * @return
	 */
	public double getFree() {
		return totalSize.doubleValue() * curAvailTS.doubleValue() / 100;		
	}

	public void setAvgReadTim(Double avgReadTim) {
		this.avgReadTim = avgReadTim;
	}

	public void setAvgWriteTim(Double avgWriteTim) {
		this.avgWriteTim = avgWriteTim;
	}

	public void setExtentCount(Double extentCount) {
		this.extentCount = extentCount;
	}

	public void setNextExtent(Double nextExtent) {
		this.nextExtent = nextExtent;
	}

	public String getAvailConditionName() {
		return tsName + alermItem[0];
	}

	public String getFreeExtentsConditionName() {
		return tsName + alermItem[1];
	}
}