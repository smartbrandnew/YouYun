package com.broada.carrier.monitor.impl.db.xugu.cluster;

public class ClusterNodeStat {
	
	private int node_id;    //  节点号
	private String node_ip;    //  节点IP
	private int node_type;  //  节点类型
	private String node_type_txt;  //  节点类型文本表示
	private int node_state; //  节点状态
	private String node_state_txt;  // 节点状态文本表示
	private int store_num;    //  存储数
	private int major_num;    //  主版本存储数
	
	public int getNode_id() {
		return node_id;
	}
	
	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}
	
	public String getNode_ip() {
		return node_ip;
	}
	
	public void setNode_ip(String node_ip) {
		this.node_ip = node_ip;
	}
	public int getNode_type() {
		return node_type;
	}
	
	public void setNode_type(int node_type) {
		this.node_type = node_type;
		if(node_type == 29)
			node_type_txt = "主Master";
		else if(node_type == 30)
			node_type_txt = "副Master";
		else
			node_type_txt = "工作节点";
	}
	
	public int getNode_state() {
		return node_state;
	}
	
	public void setNode_state(int node_state) {
		this.node_state = node_state;
		if(node_state == 2)
			node_state_txt = "活跃状态";
		else if(node_state == 3)
			node_state_txt = "死亡状态";
	}
	
	public long getStore_num() {
		return store_num;
	}
	
	public void setStore_num(int store_num) {
		this.store_num = store_num;
	}
	
	public long getMajor_num() {
		return major_num;
	}
	
	public void setMajor_num(int major_num) {
		this.major_num = major_num;
	}
	
	public String getNode_type_txt() {
		return node_type_txt;
	}
	
	public String getNode_state_txt() {
		return node_state_txt;
	}
	
}
