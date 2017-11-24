package uyun.bat.monitor.api.entity;

import java.util.Date;

/**
 * 监测器通知记录
 */
public class NotifyRecord {
	private String id;
	/**
	 * 通知用户<br>
	 * 中文名1，中文名2
	 */
	private String name;

	/**
	 * 通知内容
	 */
	private String content;

	/**
	 * 通知时间<br>
	 * 非邮件发出时间，只是事件保存时间
	 */
	private Date time;

	/**
	 * 监测器id
	 */
	private String monitorId;

	private String tenantId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}
