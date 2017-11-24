package uyun.bat.web.api.resource.entity;

import java.util.Date;

/**
 * 返回资源关联的监测器信息
 */
public class ResMonitorInfo {
	private String integration;
	private String threshold;
	private Short serverity;
	private String msgContent;
	private Date occurTime;
	private String monitorId;


	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public Short getServerity() {
		return serverity;
	}

	public void setServerity(Short serverity) {
		this.serverity = serverity;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public Date getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}

	public ResMonitorInfo() {
	}

	public ResMonitorInfo(String integration, String threshold, Short serverity, String msgContent, Date occurTime, String monitorId) {
		this.integration = integration;
		this.threshold = threshold;
		this.serverity = serverity;
		this.msgContent = msgContent;
		this.occurTime = occurTime;
		this.monitorId = monitorId;
	}
}
