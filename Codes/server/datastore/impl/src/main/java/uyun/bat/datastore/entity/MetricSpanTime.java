package uyun.bat.datastore.entity;

public class MetricSpanTime {
	private long startTime;
	private long endTime;
	private long spanTime;
	private String tenantId;

	public MetricSpanTime() {

	}

	public MetricSpanTime(long startTime, long endTime, long spanTime, String tenantId) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.spanTime = spanTime;
		this.tenantId = tenantId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getSpanTime() {
		return spanTime;
	}

	public void setSpanTime(long spanTime) {
		this.spanTime = spanTime;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public String toString() {
		return "MetricSpanTime [startTime=" + startTime + ", endTime=" + endTime + ", spanTime=" + spanTime + ", tenantId="
				+ tenantId + "]";
	}

	
}
