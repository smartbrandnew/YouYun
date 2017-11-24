package com.broada.carrier.monitor.probe.impl.sync.entity;

import java.util.List;

/**
 * 配置节点的校验状态
 * @author admin
 *
 */
public class LinkStat {
	private String id;
	private String source;
	private String name;
	private List<String> checkedList;
	private List<String> failedList;
	
	public LinkStat() {
		// TODO Auto-generated constructor stub
	}
	
	public LinkStat(String id){
		this.id = id;
		this.source = "agentless";
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getCheckedList() {
		return checkedList;
	}
	public void setCheckedList(List<String> checkedList) {
		this.checkedList = checkedList;
	}
	public List<String> getFailedList() {
		return failedList;
	}
	public void setFailedList(List<String> failedList) {
		this.failedList = failedList;
	}
	
}
