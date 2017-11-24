package com.broada.carrier.monitor.impl.db.dm.bufferPool;

/**
 * 达梦内存缓冲区实体类
 * 
 * @author Zhouqa Create By 2016年4月14日 上午10:23:35
 */
public class DmBuffer {
	private String bufName = ""; // 缓存区名字
	private Long pageSize; // 页大小
	private Long pageNo; // 页数
	private Long usePage;// 正使用页数
	private Long freePage;// 空闲页数
	private Long dirty_page;// 脏页数
	private Long busyPage;// 非空闲页数
	private Long maxPage;// 最大页数
	private Long logicReads; // read命中的页数
	private Long discard; // 淘汰的页数
	private Long phyReads;// 未命中的页数
	private Long multiReads;// 批量读的次数
	private Double hitRate = new Double(0);// 命中率

	private Boolean isWacthed = Boolean.FALSE;

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public String getBufName() {
		return bufName;
	}

	public void setBufName(String bufName) {
		this.bufName = bufName;
	}

	public Long getPageSize() {
		return pageSize;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	public Long getPageNo() {
		return pageNo;
	}

	public void setPageNo(Long pageNo) {
		this.pageNo = pageNo;
	}

	public Long getUsePage() {
		return usePage;
	}

	public void setUsePage(Long usePage) {
		this.usePage = usePage;
	}

	public Long getFreePage() {
		return freePage;
	}

	public void setFreePage(Long freePage) {
		this.freePage = freePage;
	}

	public Long getDirty_page() {
		return dirty_page;
	}

	public void setDirty_page(Long dirty_page) {
		this.dirty_page = dirty_page;
	}

	public Long getBusyPage() {
		return busyPage;
	}

	public void setBusyPage(Long busyPage) {
		this.busyPage = busyPage;
	}

	public Long getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(Long maxPage) {
		this.maxPage = maxPage;
	}

	public Long getLogicReads() {
		return logicReads;
	}

	public void setLogicReads(Long logicReads) {
		this.logicReads = logicReads;
	}

	public Long getDiscard() {
		return discard;
	}

	public void setDiscard(Long discard) {
		this.discard = discard;
	}

	public Long getPhyReads() {
		return phyReads;
	}

	public void setPhyReads(Long phyReads) {
		this.phyReads = phyReads;
	}

	public Long getMultiReads() {
		return multiReads;
	}

	public void setMultiReads(Long multiReads) {
		this.multiReads = multiReads;
	}

	public Double getHitRate() {
		return hitRate;
	}

	public void setHitRate(Double hitRate) {
		this.hitRate = hitRate;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

}