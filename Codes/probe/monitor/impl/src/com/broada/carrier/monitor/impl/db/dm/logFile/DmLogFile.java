package com.broada.carrier.monitor.impl.db.dm.logFile;

/**
 * 达梦日志文件实体类
 * 
 * @author Zhouqa Create By 2016年4月14日 上午10:23:35
 */
public class DmLogFile {
	private Long ckptLsn; // 检查点lsn
	private Long fileLsn; // 文件lsn
	private Long flushLsn; // 刷新lsn
	private Long currLsn;// 当前lsn
	private Long nextSeq;// 下一个页序列
	private Long magic;// 页Magic值
	private Long flushPages;// 刷新页数
	private Long flushingPages; // 正在刷新的页数
	private Long currFile;// 当前文件
	private Long currOffset; // 当前偏移量
	private Long ckptFile; // 检查点文件
	private Long ckptOffset;// 检查点文件偏移量
	private Double freeSpace = new Double(0);// 空闲大小KB
	private Double totalSpace = new Double(0);// 总大小 KB
	private Double rate = new Double(0);// 利用率

	private Boolean isWacthed = Boolean.FALSE;

	
	public Long getCkptLsn() {
		return ckptLsn;
	}

	public void setCkptLsn(Long ckptLsn) {
		this.ckptLsn = ckptLsn;
	}

	public Long getFileLsn() {
		return fileLsn;
	}

	public void setFileLsn(Long fileLsn) {
		this.fileLsn = fileLsn;
	}

	public Long getFlushLsn() {
		return flushLsn;
	}

	public void setFlushLsn(Long flushLsn) {
		this.flushLsn = flushLsn;
	}

	public Long getCurrLsn() {
		return currLsn;
	}

	public void setCurrLsn(Long currLsn) {
		this.currLsn = currLsn;
	}

	public Long getNextSeq() {
		return nextSeq;
	}

	public void setNextSeq(Long nextSeq) {
		this.nextSeq = nextSeq;
	}

	public Long getMagic() {
		return magic;
	}

	public void setMagic(Long magic) {
		this.magic = magic;
	}

	public Long getFlushPages() {
		return flushPages;
	}

	public void setFlushPages(Long flushPages) {
		this.flushPages = flushPages;
	}

	public Long getFlushingPages() {
		return flushingPages;
	}

	public void setFlushingPages(Long flushingPages) {
		this.flushingPages = flushingPages;
	}

	public Long getCurrFile() {
		return currFile;
	}

	public void setCurrFile(Long currFile) {
		this.currFile = currFile;
	}

	public Long getCurrOffset() {
		return currOffset;
	}

	public void setCurrOffset(Long currOffset) {
		this.currOffset = currOffset;
	}

	public Long getCkptFile() {
		return ckptFile;
	}

	public void setCkptFile(Long ckptFile) {
		this.ckptFile = ckptFile;
	}

	public Long getCkptOffset() {
		return ckptOffset;
	}

	public void setCkptOffset(Long ckptOffset) {
		this.ckptOffset = ckptOffset;
	}

	public Double getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(Double freeSpace) {
		this.freeSpace = freeSpace;
	}

	public Double getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(Double totalSpace) {
		this.totalSpace = totalSpace;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}
	
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

}