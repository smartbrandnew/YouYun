package com.broada.carrier.monitor.impl.db.dm.links;

/**
 * DM内存池监测
 * 
 * @author Zhouqa Create By 2016年4月15日 下午2:36:02
 */
public class DmLinks {
	private Boolean isWacthed = Boolean.FALSE;
	private Long linkID;
	private String linkName;
	private String isPublic;
	private String loginName;
	private String hostName;
	private String port;
	private String loggenOn;
	private String heterrogeneous;
	private String protocol;
	private String inUse;

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public Long getLinkID() {
		return linkID;
	}

	public void setLinkID(Long linkID) {
		this.linkID = linkID;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getLoggenOn() {
		return loggenOn;
	}

	public void setLoggenOn(String loggenOn) {
		this.loggenOn = loggenOn;
	}

	public String getHeterrogeneous() {
		return heterrogeneous;
	}

	public void setHeterrogeneous(String heterrogeneous) {
		this.heterrogeneous = heterrogeneous;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getInUse() {
		return inUse;
	}

	public void setInUse(String inUse) {
		this.inUse = inUse;
	}

}