package com.broada.carrier.monitor.impl.db.st.buffer;

/**
 * 神通内存缓冲区实体类
 * 
 * @author Zhouqa Create By 2016年4月14日 上午10:23:35
 */
public class ShentongBuffer {
	private Long pageSize; // 页大小
	private Long readBlock;// 缓冲区读块
	private Long writeBlock;// 缓冲区写块
	private Long freePage;// 空闲页数
	private Long dirty_page;// 脏页数


	private Boolean isWacthed = Boolean.FALSE;

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public Long getPageSize() {
		return pageSize;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	public Long getReadBlock() {
		return readBlock;
	}

	public void setReadBlock(Long readBlock) {
		this.readBlock = readBlock;
	}

	public Long getWriteBlock() {
		return writeBlock;
	}

	public void setWriteBlock(Long writeBlock) {
		this.writeBlock = writeBlock;
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

}