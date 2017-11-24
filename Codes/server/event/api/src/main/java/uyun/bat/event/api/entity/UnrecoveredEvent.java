package uyun.bat.event.api.entity;

import java.io.Serializable;
import java.util.Date;

import uyun.bat.event.api.util.StringUtils;

public class UnrecoveredEvent implements Serializable, Comparable {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	private Date occurTime;
	private String resId;
	private String resName;
	private String msgTitle;
	private String msgContent;
	private Short serverity;
	private String tenantId;
	private String ip;
	private String tags;
	private String faultId;
	private String monitorId;
	
	public UnrecoveredEvent() {

	}

	public UnrecoveredEvent(String id, Date occurTime, Short serverity, String resId, String resName, String msgTitle,
			String msgContent, String tenantId, String ip, String tags, String faultId, String monitorId) {
		this.id = id;
		this.occurTime = occurTime;
		this.serverity = serverity;
		this.resId = resId;
		this.resName = resName;
		this.msgTitle = msgTitle;
		this.msgContent = msgContent;
		this.tenantId = tenantId;
		this.ip = ip;
		this.tags = tags;
		this.faultId = faultId;
		this.monitorId = monitorId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public String getMsgTitle() {
		return msgTitle;
	}

	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public Short getServerity() {
		return serverity;
	}

	public void setServerity(Short serverity) {
		this.serverity = serverity;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public String getFaultId() {
		return faultId;
	}

	public void verifyEvent() {
		if (StringUtils.isEmpty(msgTitle)) {
			throw new IllegalArgumentException("Event title is not allowed null");
		}
		if (StringUtils.isEmpty(msgContent)) {
			throw new IllegalArgumentException("Event content is not allowed null");
		}
		msgTitle = StringUtils.getLimitLengthString(msgTitle, 100);
		msgContent = StringUtils.getLimitLengthString(msgContent, 4000);

	}

	@Override
	public int compareTo(Object o) {
		UnrecoveredEvent ue = (UnrecoveredEvent) o;
		if (occurTime.compareTo(ue.getOccurTime()) > 0) {
			return -1;
		}
		if (occurTime.compareTo(ue.getOccurTime()) < 0) {
			return 1;
		}
		return 0;
	}

}
