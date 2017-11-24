package com.broada.carrier.monitor.impl.mw.tongweb.dbpool;

import com.broada.carrier.monitor.impl.mw.tongweb.TongWeb;

public class TongWebDBPoolinfoVer4 implements TongWeb {
	private Integer poolSize;
	private Integer getConnectionTimeOut;
	private Integer currentNumberOfJDBCConnectionOpen;
	private Integer jDBCMinConnPool;
	private Integer jDBCMaxConnPool;
	private String type;
	private String name;
	private String domain;
	public String getDomain() {
		return domain;
	}

	public Integer getCurrentNumberOfJDBCConnectionOpen() {
		return currentNumberOfJDBCConnectionOpen;
	}

	public void setCurrentNumberOfJDBCConnectionOpen(Integer currentNumberOfJDBCConnectionOpen) {
		this.currentNumberOfJDBCConnectionOpen = currentNumberOfJDBCConnectionOpen;
	}

	

	public Integer getjDBCMinConnPool() {
		return jDBCMinConnPool;
	}

	public void setjDBCMinConnPool(Integer jDBCMinConnPool) {
		this.jDBCMinConnPool = jDBCMinConnPool;
	}

	public Integer getjDBCMaxConnPool() {
		return jDBCMaxConnPool;
	}

	public void setjDBCMaxConnPool(Integer jDBCMaxConnPool) {
		this.jDBCMaxConnPool = jDBCMaxConnPool;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	

	public Integer getGetConnectionTimeOut() {
		return getConnectionTimeOut;
	}

	public void setGetConnectionTimeOut(Integer getConnectionTimeOut) {
		this.getConnectionTimeOut = getConnectionTimeOut;
	}

	

}
