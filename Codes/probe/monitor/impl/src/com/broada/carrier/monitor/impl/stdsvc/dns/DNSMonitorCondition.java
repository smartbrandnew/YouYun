package com.broada.carrier.monitor.impl.stdsvc.dns;

import com.broada.carrier.monitor.method.common.MonitorCondition;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class DNSMonitorCondition extends MonitorCondition {	
	private static final long serialVersionUID = 1L;

	// DNS 监测端口
	private int port;

	// 延时
	private int timeout;
	
	//服务器运行
	private String chkrun = "运行正常";
	
	//域名
	private String parsename;
	
	  // private int value;
	public DNSMonitorCondition() {
	    type = 0;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setField(int port,String parsename) {
		this.field = "" + port+"_"+(parsename==null?"":parsename);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getChkrun() {
		return chkrun;
	}

	public void setChkrun(String chkrun) {
		this.chkrun = chkrun;
	}

	public String getParsename() {
		return parsename;
	}

	public void setParsename(String parsename) {
		this.parsename = parsename;
	}
	
	@JsonIgnore
	public boolean isChkParse() {
	    return getParsename() != null&&!getParsename().trim().equals("");
	}

	@JsonIgnore
	public boolean isChkReplyTime() {
	    return getTimeout() != Integer.MIN_VALUE;
	}
}
