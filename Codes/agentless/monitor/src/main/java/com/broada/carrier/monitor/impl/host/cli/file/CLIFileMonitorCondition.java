package com.broada.carrier.monitor.impl.host.cli.file;

import java.util.Date;
import java.util.Properties;

import com.broada.carrier.monitor.method.common.MonitorCondition;
import com.broada.utils.SimpleParams;

public class CLIFileMonitorCondition extends MonitorCondition {
	private static final long serialVersionUID = 8154358740246923962L;

	private boolean isExists = true;
	private double size;
	private String group;
	private String user;
	private Date modifiedTime;
	private int links;

	public CLIFileMonitorCondition() {
		super();
	}

	public CLIFileMonitorCondition(String filepath, String group, String user, int links, double size, Date modifiedTime) {
		super();
		setField(filepath);
		this.isExists = true;
		this.links = links;
		this.size = size;
		this.group = group;
		this.user = user;
		this.modifiedTime = modifiedTime;
	}

	public CLIFileMonitorCondition(Properties props) {
		super();
		SimpleParams params = new SimpleParams(props);
		setField(params.checkString("filepath"));
		this.isExists = true;
		this.links = params.getInteger("linkedcnt", 0);		
		this.size = params.checkDouble("size");
		this.group = params.getString("group");
		this.user = params.getString("user");		
		this.modifiedTime = CLIFileMonitor.formateMtimeToDatetim(params.checkString("mother"));
	}

	public int getLinks() {
		return links;
	}

	public void setLinks(int links) {
		this.links = links;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}
	
	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;		 
	}

	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = CLIFileMonitor.formateMtimeToDatetim(modifiedTime);
	}

	public String getDescription() {
		return getField() + "存在";
	}

	public String getFieldCondition() {
		return "存在";
	}

	public String getFieldDescription() {
		return getField();
	}

	public String getFieldName() {
		return getField();
	}

	public String getFilepath() {
		return getField();
	}

	/**
	 * 单位MB
	 * @return
	 */
	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isExists() {
		return isExists;
	}

	public void setExists(boolean isExists) {
		this.isExists = isExists;
	}

	public void setFilepath(String filepath) {
		setField(filepath);
	}
}
