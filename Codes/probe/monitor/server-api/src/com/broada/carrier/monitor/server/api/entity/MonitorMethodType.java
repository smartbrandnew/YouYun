package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

/**
 * 监测器方法类型定义
 * @author Jiangjw
 */
public class MonitorMethodType implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String descr;
	private String configer;

	public MonitorMethodType() {
	}

	public MonitorMethodType(String id, String name, Class<?> configerClass) {
		this(id, name, null, configerClass.getName());
	}

	public MonitorMethodType(String id, String name, String descr, String configer) {
		this.id = id;
		this.name = name;
		this.descr = descr;
		this.configer = configer;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getConfiger() {
		return configer;
	}

	public void setConfiger(String configer) {
		this.configer = configer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		MonitorMethodType other = (MonitorMethodType) obj;
		return this.getId().equals(other.getId());
	}

	@Override
	public String toString() {
		return String.format("%s[id: %s name: %s configer: %s]", getClass().getSimpleName(),
				getId(), getName(), getConfiger());
	}
}
