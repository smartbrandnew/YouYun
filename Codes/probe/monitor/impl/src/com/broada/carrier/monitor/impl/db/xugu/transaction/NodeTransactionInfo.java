package com.broada.carrier.monitor.impl.db.xugu.transaction;

import java.sql.Timestamp;

public class NodeTransactionInfo {
	
	private int node_id;     // 节点号
	private Timestamp curr_t;        // 当前时间
	private long buff_r_n;      // 缓存读次数
	private long disk_r_bytes;  // 磁盘读字节数
	private long disk_w_bytes;  // 磁盘写字节数
	private long net_r_bytes;   // 网络读字节数
	private long net_w_bytes;   // 网络写字节数
	private long min_trans_id;  // 最小活跃事务号
	private long max_trans_id;  // 最大活跃事务号
	private int act_trans_num;  // 活跃事务数
	private long balance;       // 事务号差
	
	public Timestamp getCurr_t() {
		return curr_t;
	}
	public void setCurr_t(Timestamp curr_t) {
		this.curr_t = curr_t;
	}
	public long getBuff_r_n() {
		return buff_r_n;
	}
	public void setBuff_r_n(long buff_r_n) {
		this.buff_r_n = buff_r_n;
	}
	public long getDisk_r_bytes() {
		return disk_r_bytes;
	}
	public void setDisk_r_bytes(long disk_r_bytes) {
		this.disk_r_bytes = disk_r_bytes;
	}
	public long getDisk_w_bytes() {
		return disk_w_bytes;
	}
	public void setDisk_w_bytes(long disk_w_bytes) {
		this.disk_w_bytes = disk_w_bytes;
	}
	public long getNet_r_bytes() {
		return net_r_bytes;
	}
	public void setNet_r_bytes(long net_r_bytes) {
		this.net_r_bytes = net_r_bytes;
	}
	public long getNet_w_bytes() {
		return net_w_bytes;
	}
	public void setNet_w_bytes(long net_w_bytes) {
		this.net_w_bytes = net_w_bytes;
	}
	public long getMin_trans_id() {
		return min_trans_id;
	}
	public void setMin_trans_id(long min_trans_id) {
		this.min_trans_id = min_trans_id;
	}
	public long getMax_trans_id() {
		return max_trans_id;
	}
	public void setMax_trans_id(long max_trans_id) {
		this.max_trans_id = max_trans_id;
	}
	public int getAct_trans_num() {
		return act_trans_num;
	}
	public void setAct_trans_num(int act_trans_num) {
		this.act_trans_num = act_trans_num;
	}
	public long getBalance() {
		return balance;
	}
	public void setBalance(long balance) {
		this.balance = balance;
	}
	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}
	public int getNode_id() {
		return node_id;
	}
	
}
