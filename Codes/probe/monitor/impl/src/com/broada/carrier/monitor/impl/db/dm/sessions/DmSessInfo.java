package com.broada.carrier.monitor.impl.db.dm.sessions;

/**
 * 会话实体类
 * 
 * @author Zhouqa Create By 2016年4月14日 上午9:44:09
 */
public class DmSessInfo {
	private String sessId = ""; // 会话ID
	private String userName = ""; // 当前用户
	private String sessSql = ""; // sql语句
	private String sessState = ""; // 会话状态
	private String currSch = ""; // 当前模式
	private String createTime = ""; // 会话创建时间
	private String clntType = ""; // 客户类型
	private String autoCmt = ""; // 是否自动提交
	private String clntHost = ""; // 客户主机名
	private Boolean isWacthed = Boolean.FALSE;

	public void setSessId(String sessId) {
		this.sessId = sessId;
	}

	public String getSessId() {
		return sessId;
	}

	public String getSessSql() {
		return sessSql;
	}

	public void setSessSql(String sessSql) {
		this.sessSql = sessSql;
	}

	public String getSessState() {
		return sessState;
	}

	public void setSessState(String sessState) {
		this.sessState = sessState;
	}

	public String getCurrSch() {
		return currSch;
	}

	public void setCurrSch(String currSch) {
		this.currSch = currSch;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getClntType() {
		return clntType;
	}

	public void setClntType(String clntType) {
		this.clntType = clntType;
	}

	public String getAutoCmt() {
		return autoCmt;
	}

	public void setAutoCmt(String autoCmt) {
		this.autoCmt = autoCmt;
	}

	public String getClntHost() {
		return clntHost;
	}

	public void setClntHost(String clntHost) {
		this.clntHost = clntHost;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public Boolean getIsWacthed() {
		return isWacthed == null ? Boolean.FALSE : isWacthed;
	}
}
