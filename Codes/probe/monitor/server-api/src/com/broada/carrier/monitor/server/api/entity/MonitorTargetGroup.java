package com.broada.carrier.monitor.server.api.entity;

/**
 * 监测项导航节点
 * @author Jiangjw
 */
public class MonitorTargetGroup {
	private String id;
	private String name;
	private String parentId;
	private String targetTypeId;
	private String domainId;

	public MonitorTargetGroup() {
	}

	public MonitorTargetGroup(String id, String name, String parentId, String targetTypeId, String domainId) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.targetTypeId = targetTypeId;
		this.domainId = domainId;
	}

	/**
	 * 本导航节点的基础监测项类型
	 * 可能是MonitorNodeType或是MonitorResourceType
	 * @return
	 */
	public String getTargetTypeId() {
		return targetTypeId;
	}

	public void setTargetTypeId(String targetTypeId) {
		this.targetTypeId = targetTypeId;
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

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		MonitorTargetGroup other = (MonitorTargetGroup) obj;
		return this.id.equals(other.id);
	}

	@Override
	public String toString() {
		return String.format("%s[id: %s name: %s parentId: %s domainId: %s]", getClass().getSimpleName(), getId(), getName(), getParentId(), getDomainId());
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
}
