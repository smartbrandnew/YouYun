package com.broada.carrier.monitor.client.impl.target;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

public class TargetTableRow {	
	private MonitorTarget target;
	private MonitorNode node;
	private MonitorProbe probe;
	private MonitorTargetStatus status;
	private MonitorTargetType targetType;

	public MonitorTargetType getTargetType() {
		if (targetType == null)
			targetType = ServerContext.checkTargetType(target.getTypeId());			
		return targetType;
	}

	public void setStatus(MonitorTargetStatus status) {
		this.status = status;
	}

	public TargetTableRow(MonitorNode node, MonitorTargetStatus status) {
		this.target = node;
		this.node = node;
		this.status = status;
	}

	public TargetTableRow(MonitorResource resource, MonitorNode node, MonitorTargetStatus status) {
		this.target = resource;
		this.node = node;
		this.status = status;
	}

	public MonitorResource getResource() {
		return (MonitorResource) target;
	}

	public MonitorNode getNode() {
		return node;
	}

	public String getIp() {
		if (node == null)
			return "";
		return node.getIp();
	}

	public String getName() {
		return target.getName();
	}

	public MonitorProbe getProbe() {
		if (probe == null) {			
			if (node == null || node.getProbeId() <= 0)
				probe = new MonitorProbe(0, "", "", "", "", 0);
			else
				probe = ServerUtil.checkProbe(ServerContext.getProbeService(), node.getProbeId());
		}
		return probe;
	}

	public String getProbeName() {
		return getProbe().getName();
	}

	public String getTypeName() {
		return getTargetType().getName();
	}

	public MonitorTargetStatus getStatus() {
		if (status == null) {
			if (target instanceof MonitorNode)
				status = ServerContext.getServerFactory().getNodeService().getNodeStatus(target.getId());
			else
				status = ServerContext.getServerFactory().getResourceService().getResourceStatus(target.getId());
		}
		return status;
	}

	public MonitorState getState() {
		return getStatus().getMonitorState();
	}

	public int getTaskCount() {
		return getStatus().getTaskCount();
	}

	public MonitorTarget getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return getTarget().getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		TargetTableRow other = (TargetTableRow) obj;
		return getTarget().getId().equals(other.getTarget().getId());
	}	
}