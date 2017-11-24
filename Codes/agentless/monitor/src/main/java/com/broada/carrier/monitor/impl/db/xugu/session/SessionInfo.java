package com.broada.carrier.monitor.impl.db.xugu.session;

import java.sql.Timestamp;
import java.util.Date;

public class SessionInfo {
	
	private int node_id;   // 节点号
	private String user_name; // 登录用户
	private String ip;        // 登录IP
	private String db_name;   // 数据库名
	private Date start_t;     // 登录时间
	private int status;    // 运行状态
	private String status_text;   // 运行状态文本描述
	private boolean auto_commit;  // 自动提交
	private long curr_id;       // 当前事务号
	private Timestamp trans_start_t;   // 事务开始时间
	private double last_time;       // 连接时长
	
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getDb_name() {
		return db_name;
	}
	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}
	public Date getStart_t() {
		return start_t;
	}
	public void setStart_t(Date start_t) {
		this.start_t = start_t;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
		if(status == 112)
			status_text = "空闲连接";
		else if(status == 114)
			status_text = "正在执行命令";
	}
	public boolean isAuto_commit() {
		return auto_commit;
	}
	public void setAuto_commit(boolean auto_commit) {
		this.auto_commit = auto_commit;
	}
	public long getCurr_id() {
		return curr_id;
	}
	public void setCurr_id(long curr_id) {
		this.curr_id = curr_id;
	}
	public Timestamp getTrans_start_t() {
		return trans_start_t;
	}
	public void setTrans_start_t(Timestamp trans_start_t) {
		this.trans_start_t = trans_start_t;
	}
	public double getLast_time() {
		return last_time;
	}
	public void setLast_time(double last_time) {
		this.last_time = last_time;
	}
	public void setStatus_text(String status_text) {
		this.status_text = status_text;
	}
	public String getStatus_text() {
		return status_text;
	}
	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}
	public int getNode_id() {
		return node_id;
	}
	
}
