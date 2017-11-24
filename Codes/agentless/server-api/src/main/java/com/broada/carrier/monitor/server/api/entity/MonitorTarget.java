package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import com.broada.carrier.monitor.common.util.TextUtil;

/**
 * 监测对象，同pmdb监测项
 * 
 * @author Jiangjw
 */
public abstract class MonitorTarget implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String typeId;
	private MonitorTargetAuditState auditState;
	private long modified;
	private String domainId;

	public MonitorTarget() {
	}

	public MonitorTarget(String id, String name, String typeId, MonitorTargetAuditState auditState, long modified,
			String domainId) {
		set(id, name, typeId, auditState, modified, domainId);
	}

	public MonitorTarget(MonitorTarget copy) {
		set(copy.getId(), copy.getName(), copy.getTypeId(), copy.getAuditState(), copy.getModified(), copy.getDomainId());
	}

	public void set(String id, String name, String typeId, MonitorTargetAuditState auditState, long modified,
			String domainId) {
		this.id = id;
		this.name = name;
		this.typeId = typeId;
		this.auditState = auditState;
		this.modified = modified;
		this.domainId = domainId;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public void set(MonitorTarget copy) {
		set(copy.getId(), copy.getName(), copy.getTypeId(), copy.getAuditState(), copy.getModified(), copy.getDomainId());
	}

	/**
	 * 审核状态
	 * 
	 * @return
	 */
	public MonitorTargetAuditState getAuditState() {
		return auditState;
	}

	public void setAuditState(MonitorTargetAuditState auditState) {
		this.auditState = auditState;
	}

	/**
	 * 监测项类型ID
	 * 
	 * @return
	 */
	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * 同pmdb监测项id
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 同pmdb监测项名称
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s[id: %s typeId: %s name: %s]", getClass().getSimpleName(), getId(), getTypeId(), getName());
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		MonitorTarget other = (MonitorTarget) obj;
		return this.getId().equals(other.getId()) && this.getModified() == other.getModified();
	}

	public void verify() {
		if (TextUtil.isEmpty(getName()))
			throw new NullPointerException("名称不能为空");
		if (TextUtil.isEmpty(getTypeId()))
			throw new NullPointerException("类型不能为空");
	}
}
