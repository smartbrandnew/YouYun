package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

/**
 * 监测器指标定义
 * 
 * @author Jiangjw
 */
public class MonitorItem implements Serializable {
	private String id;
	private static final long serialVersionUID = 1L;
	private String code;
	private String name;
	private String unit;
	private String descr;
	private MonitorItemType type;

	public MonitorItem() {
	}

	public MonitorItem(String id,String code, String name, String unit, String descr, MonitorItemType type) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.unit = unit;
		this.descr = descr;
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public MonitorItemType getType() {
		return type;
	}

	public void setType(MonitorItemType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return getCode().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		MonitorItem other = (MonitorItem) obj;
		return this.getCode().equals(other.getCode());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "MonitorItem [id=" + id + ", code=" + code + ", name=" + name + ", unit=" + unit + ", descr=" + descr
				+ ", type=" + type + "]";
	}

}
