package uyun.bat.web.api.resource.entity;

import java.util.Date;

/**
 * 展现事件内容 发生时间 事件等级
 */
public class EventVO {
	private String msgContent;
	private Date occurTime;
	private Short serverity;

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public Date getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}

	public Short getServerity() {
		return serverity;
	}

	public void setServerity(Short serverity) {
		this.serverity = serverity;
	}

	public EventVO() {
	}

	public EventVO(String msgContent, Date occurTime, Short serverity) {
		this.msgContent = msgContent;
		this.occurTime = occurTime;
		this.serverity = serverity;
	}
}
