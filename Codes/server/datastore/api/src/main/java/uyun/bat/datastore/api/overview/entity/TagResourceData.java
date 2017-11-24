package uyun.bat.datastore.api.overview.entity;

/**
 * 标签资源数据统计
 */
public class TagResourceData {
	private String key;
	private String value;
	private long resourceCount;
	private long warnCount;
	private long errorCount;
	private long infoCount;


	public TagResourceData(String key, String value, long resourceCount, long warnCount, long errorCount) {
		super();
		this.key = key;
		this.value = value;
		this.resourceCount = resourceCount;
		this.warnCount = warnCount;
		this.errorCount = errorCount;
	}

	public TagResourceData() {
		super();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getResourceCount() {
		return resourceCount;
	}

	public void setResourceCount(long resourceCount) {
		this.resourceCount = resourceCount;
	}

	public long getWarnCount() {
		return warnCount;
	}

	public void setWarnCount(long warnCount) {
		this.warnCount = warnCount;
	}

	public long getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(long errorCount) {
		this.errorCount = errorCount;
	}
	
	public long getInfoCount() {
		return infoCount;
	}
	
	public void setInfoCount(long infoCount) {
		this.infoCount = infoCount;
	}

}
