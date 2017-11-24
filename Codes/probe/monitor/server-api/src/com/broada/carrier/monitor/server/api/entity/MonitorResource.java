package com.broada.carrier.monitor.server.api.entity;

/**
 * 监测资源
 * @author Jiangjw
 */
public class MonitorResource extends MonitorTarget {
	private static final long serialVersionUID = 1L;
	private String nodeId;
	
	public MonitorResource() {
	}

	public MonitorResource(String id, String name, String typeId, MonitorTargetAuditState auditState, String nodeId, long modified, String domainId) {
		super(id, name, typeId, auditState, modified, domainId);		
		this.nodeId = nodeId;
	}
	
	public MonitorResource(MonitorResource copy) {
		super(copy);
		this.nodeId = copy.nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	@Override
	public String toString() {
		return String.format("%s[id: %s typeId: %s name: %s nodeId: %s]", getClass().getSimpleName(), getId(), getTypeId(), getName(), getNodeId());
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals((MonitorResource)obj);
	}
	
	public String retDisplayName() {
		return String.format("%s", getName());
	}	
}
