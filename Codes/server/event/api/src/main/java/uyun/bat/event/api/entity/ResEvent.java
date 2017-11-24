package uyun.bat.event.api.entity;

import java.io.Serializable;
import java.util.Date;

public class ResEvent implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	private String id;
	private Short serverity;
	private String faultId;
	private String monitorId;
	private String identity;
	private Date occurTime;
	private String msgContent;

	public ResEvent() {

	}

	public ResEvent(String id, Short serverity, String faultId, String monitorId, String identity, Date occurTime, String msgContent) {
		this.id = id;
		this.serverity = serverity;
		this.faultId = faultId;
		this.monitorId = monitorId;
		this.identity = identity;
		this.occurTime = occurTime;
		this.msgContent = msgContent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Short getServerity() {
		return serverity;
	}

	public void setServerity(Short serverity) {
		this.serverity = serverity;
	}


	public String getMonitorId() {
		return monitorId;
	}

	public String getFaultId() {
		return faultId;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public Date getOccurTime() {
		return occurTime;
	}

	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	@Override
	public int compareTo(Object o) {
		ResEvent re = (ResEvent) o;
		if (occurTime.compareTo(re.getOccurTime()) > 0) {
			return -1;
		}
		if (occurTime.compareTo(re.getOccurTime()) < 0) {
			return 1;
		}
		return 0;
	}
}
