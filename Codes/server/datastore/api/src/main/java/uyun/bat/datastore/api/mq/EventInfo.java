package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import uyun.bat.common.tag.entity.Tag;

public class EventInfo implements Serializable {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private Date occurTime;
	private String msgTitle;
	private String msgContent;
	private Short sourceType;
	private Short serverity;
	private String identity;
	private List<Tag> eventTags;

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

	public Short getSourceType() {
		return sourceType;
	}

	public void setSourceType(Short sourceType) {
		this.sourceType = sourceType;
	}

	public Short getServerity() {
		return serverity;
	}

	public void setServerity(Short serverity) {
		this.serverity = serverity;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public List<Tag> getEventTags() {
		return eventTags;
	}

	public void setEventTags(List<Tag> eventTags) {
		this.eventTags = eventTags;
	}

}
