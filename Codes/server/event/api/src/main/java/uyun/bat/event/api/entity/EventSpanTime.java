package uyun.bat.event.api.entity;

/**
 * 自监控统计事件跨度
 * @author WIN
 *
 */
public class EventSpanTime {
	private long startTime;
	private long endTime;
	private String tenantId;
	//时间跨度，endTime-startTime
	private long spanTime;

	public EventSpanTime() {
	}

	public EventSpanTime(long startTime, long endTime, String tenantId, long spanTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.tenantId = tenantId;
		this.spanTime = spanTime;
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

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public long getSpanTime() {
		return spanTime;
	}

	public void setSpanTime(long spanTime) {
		this.spanTime = spanTime;
	}

	@Override
	public String toString() {
		return "EventSpanTime [startTime=" + startTime + ", endTime=" + endTime + ", tenantId=" + tenantId + ", spanTime="
				+ spanTime + "]";
	}

	
}
