package com.broada.carrier.monitor.impl.db.dm.basic;


/**
 * 
 * @author Zhouqa Create By 2016年4月6日 下午3:05:00
 */
public class DmBaseInfo {
	//数据库产品名称
	private String productName;
	// Dm数据库实例名称
	private String dbName;
	// 数据库大小
	private String version;
	// 数据库模式
	private String mode;
	//主机名
	private String hostName;
	//数据库实例状态
	private String status;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
