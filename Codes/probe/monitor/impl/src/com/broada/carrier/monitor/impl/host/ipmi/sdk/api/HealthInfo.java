package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;

/**
 * 健康信息，包括硬盘状态，网卡状态
 * 
 * @author pippo 
 * Create By 2014年8月4日 下午4:50:03
 */
public class HealthInfo implements Serializable{
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -7789796920654195672L;
	private Boolean isWacthed = Boolean.FALSE;
	private String name;
	private HealthType value;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HealthType getValue() {
		return value;
	}
	public void setValue(HealthType value) {
		this.value = value;
	}
	public Boolean isWacthed() {
		return isWacthed;
	}
	public void setWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	
	
}
