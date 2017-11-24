package com.broada.carrier.monitor.probe.impl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.EntityConst;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;

@Entity
@Table(name = "mon_resource")
public class ProbeSideMonitorResource extends MonitorResource {
	private static final long serialVersionUID = 1L;
	public ProbeSideMonitorResource() {
	}

	public ProbeSideMonitorResource(ProbeSideMonitorResource copy) {
		super(copy);
	}

	public ProbeSideMonitorResource(MonitorResource copy) {
		super(copy);
	}

	@Id
	@Override
	@Column(length = 50)
	public String getId() {
		return super.getId();
	}

	@Override
	@Column(name = "type_id", length = EntityConst.ID_LENGTH, nullable = false)
	public String getTypeId() {
		return super.getTypeId();
	}

	@Override
	@Column(length = 100)
	public String getName() {
		return super.getName();
	}

	@Override
	public long getModified() {
		return super.getModified();
	}

	@Override
	@Column(name = "node_id", length = EntityConst.ID_LENGTH, nullable = false)
	public String getNodeId() {
		return super.getNodeId();
	}

	@Override
	@Column(name = "domain_id", length = EntityConst.ID_LENGTH, nullable = false)
	public String getDomainId() {
		return super.getDomainId();
	}
}
