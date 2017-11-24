package com.broada.carrier.monitor.impl.mw.weblogic.agent.webappstatus;

/**
 * WEB应用状态监测实体类
 * 
 * @author zhuhong
 * 
 */
public class WebAppStatusInstance {
	Boolean isWacthed = Boolean.FALSE;
	String appName = "";
	String appStatus = "";
	String instanceKey = "";
	String desc = "";

	public WebAppStatusInstance() {
	}

	public String getAppName() {
		return appName;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(String appStatus) {
		this.appStatus = appStatus;
	}

	public String getInstanceKey() {
		return instanceKey;
	}

	public void setInstanceKey(String instanceKey) {
		this.instanceKey = instanceKey;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}