package com.broada.carrier.monitor.impl.db.xugu;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.impl.db.xugu.cluster.ClusterNodeStat;
import com.broada.carrier.monitor.impl.db.xugu.session.SessionInfo;
import com.broada.carrier.monitor.impl.db.xugu.transaction.NodeTransactionInfo;
import com.broada.utils.StringUtil;

public class XuguManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(XuguManager.class);
	
	private String ip;
	private int port;
	private String database;
	private String username;
	private String password;
	private int conType;
	private String ips;
	private Connection conn;
	private String url;
	// 集群节点状态信息
	private static final String CLUSTER_NODE_STAT = "select node_id, node_ip, node_type, node_state, store_num, major_num from sys_clusters;";
	// 数据库连接信息(包括连接时长，建议时长超过10分钟即告警)
	private static final String SESSION_INFO = "select nodeid, user_name, ip, db_name, start_t, status, auto_commit, curr_tid, trans_start_t, sysdate-trans_start_t last_time from sys_all_sessions where db_name='BABJTDB' and curr_tid is not null order by last_time desc;";
	// 节点事务执行信息(建议事务号差达到350万即告警)
	private static final String TRANSACTION_INFO = "select nodeid, curr_t, buff_r_n, disk_r_bytes, disk_w_bytes, net_r_bytes, net_w_bytes, min_trans_id, max_trans_id, act_trans_num, max_trans_id-min_trans_id balance from sys_all_run_info;";
	
	public XuguManager(){}
	
	public XuguManager(String ip, int port, String database, String username,
			String password, int conType, String ips) throws Exception{
		this.ip = ip;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.conType = conType;
		this.ips = ips;
		if (!verify(conType, ips))
			throw new Exception("xugu.yaml配置文件的con_type和ips字段配置不符合要求");
		if(conType == 1)
			this.url = MessageFormat.format("jdbc:xugu://{0}:{1}/{2}?user={3}&password={4}&con_type={5}&&ips={6}", ip, String.valueOf(port), database,
					username, password, String.valueOf(conType), ips);
		else
			this.url = MessageFormat.format("jdbc:xugu://{0}:{1}/{2}?user={3}&password={4}", ip, String.valueOf(port), database,
					username, password);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getConType() {
		return conType;
	}

	public void setConType(int conType) {
		this.conType = conType;
	}

	public String getIps() {
		return ips;
	}

	public void setIps(String ips) {
		this.ips = ips;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * 初始化链接
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void initConnection() throws ClassNotFoundException, SQLException{
		if(conn == null)
			try {
				Class.forName("com.xugu.cloudjdbc.Driver");
				conn = DriverManager.getConnection(getUrl());
			} catch (ClassNotFoundException e) {
				LOG.error("找不到com.xugu.cloudjdbc.Driver类:" + e);
				throw e;
			} catch (SQLException e) {
				LOG.error("不能获取到链接:" + e);
				throw e;
			}
	}
	
	public void releaseConnection() throws SQLException{
		if(conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("关闭链接异常:" + e);
				throw e;
			}
	}
	
	/**
	 * 校验虚谷配置是否有问题
	 * @param conType
	 * @param ips
	 * @return
	 */
	private boolean verify(int conType, String ips){
		boolean flag = true;
		if(conType == 1){   //配置了con_type
			if(!StringUtil.isNullOrBlank(ips)){
				String[] ip_array = ips.split(",");
				if(ip_array != null && ip_array.length > 0)
					for(String ip:ip_array){
						flag = IPUtil.isIPAddress(ip);
						if(!flag)
							return false;
					}
			}else
				return false;

		}
		return flag;
	}
	
	/**
	 * 统一关闭资源
	 * @param conn
	 * @param stmt
	 * @param rs
	 */
	private void close(Connection conn, Statement stmt, ResultSet rs){
		try{
			if(rs != null) rs.close();
		}catch (Exception e) {
			LOG.error("关闭ResultSet失败");
		}
		try{
			if(stmt != null) stmt.close();
		}catch (Exception e) {
			LOG.error("关闭Statement失败");
		}
		try{
			if(conn != null) conn.close();
		}catch (Exception e) {
			LOG.error("关闭Connection失败");
		}
	}
	
	/**
	 * 查询集群节点状态
	 * @return
	 * @throws Exception
	 */
	public List<ClusterNodeStat> queryClusterNodeStat() throws Exception{
		List<ClusterNodeStat> list = new ArrayList<ClusterNodeStat>();
		Statement stmt = null;
		ResultSet rs = null;
		if(conn == null)
			initConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery(CLUSTER_NODE_STAT);
		while(rs != null && rs.next()){
			ClusterNodeStat stat = new ClusterNodeStat();
			stat.setNode_id(rs.getInt("node_id"));
			stat.setNode_ip(rs.getString("node_ip"));
			stat.setNode_type(rs.getInt("node_type"));
			stat.setNode_state(rs.getInt("node_state"));
			stat.setStore_num(rs.getInt("store_num"));
			stat.setMajor_num(rs.getInt("major_num"));
			list.add(stat);
		}
		close(conn, stmt, rs);
		return list;
	}
	
	/**
	 * 查询数据库连接信息
	 * @return
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public List<SessionInfo> queryDatabaseSessionInfo() throws Exception{
		List<SessionInfo> infos = new ArrayList<SessionInfo>();
		Statement stmt = null;
		ResultSet rs = null;
		if(conn == null)
			initConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery(SESSION_INFO);
		while(rs != null && rs.next()){
			SessionInfo info = new SessionInfo();
			info.setNode_id(rs.getInt("nodeid"));
			info.setUser_name(rs.getString("user_name"));
			info.setIp(rs.getString("ip"));
			info.setDb_name(rs.getString("db_name"));
			info.setStart_t(rs.getDate("start_t"));
			info.setStatus(rs.getInt("status"));
			info.setAuto_commit(rs.getBoolean("auto_commit"));
			info.setCurr_id(rs.getLong("curr_tid"));
			info.setTrans_start_t(rs.getTimestamp("trans_start_t"));
			// 返回值参考 "0 00:01:30";  统一成分钟
			info.setLast_time(formatTimeToMin(rs.getString("last_time")));
			infos.add(info);
		}
		close(conn, stmt, rs);
		return infos;
	}
	
	/**
	 * 查询节点事务执行信息
	 * @return
	 * @throws Exception
	 */
	public List<NodeTransactionInfo> queryNodeTransactionInfo() throws Exception{
		List<NodeTransactionInfo> infos = new ArrayList<NodeTransactionInfo>();
		Statement stmt = null;
		ResultSet rs = null;
		if(conn == null)
			initConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery(TRANSACTION_INFO);
		while(rs != null && rs.next()){
			NodeTransactionInfo info = new NodeTransactionInfo();
			info.setNode_id(rs.getInt("nodeid"));
			info.setCurr_t(rs.getTimestamp("curr_t"));
			info.setBuff_r_n(rs.getLong("buff_r_n"));
			info.setDisk_r_bytes(rs.getLong("disk_r_bytes"));
			info.setDisk_w_bytes(rs.getLong("disk_w_bytes"));
			info.setNet_r_bytes(rs.getLong("net_r_bytes"));
			info.setNet_w_bytes(rs.getLong("net_w_bytes"));
			info.setMin_trans_id(rs.getLong("min_trans_id"));
			info.setMax_trans_id(rs.getLong("max_trans_id"));
			info.setAct_trans_num(rs.getInt("act_trans_num"));
			info.setBalance(rs.getLong("balance"));
			infos.add(info);
		}
		close(conn, stmt, rs);
		return infos;
	}
	
	/**
	 * 返回时间戳的分钟表示
	 * @param timestamp
	 * @return
	 */
	private double formatTimeToMin(String timestamp){
		// 时间戳格式为：0 0:0:15.945
		if(StringUtil.isNullOrBlank(timestamp)) return 0d;
		String[] array0 = timestamp.split(" ");
		int day = Integer.valueOf(array0[0]);
		String[] array1 = array0[1].split(":");
		int hour = Integer.valueOf(array1[0]);
		int minute = Integer.valueOf(array1[1]);
		double second = Double.valueOf(array1[2]);
		int total = 0;
		if(day != 0)
			total += (day * 24 * 60 * 60);
		if(hour != 0)
			total += (hour * 60 * 60);
		if(minute != 0)
			total += (minute * 60);
		if(second != 0)
			total += second;
		return new BigDecimal(total/60).setScale(1, RoundingMode.HALF_UP).doubleValue();
	}
	
}
