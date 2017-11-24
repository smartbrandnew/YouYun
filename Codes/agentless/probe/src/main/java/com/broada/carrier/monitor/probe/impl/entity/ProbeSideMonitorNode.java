package com.broada.carrier.monitor.probe.impl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.EntityConst;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetAuditState;

@Entity
@Table(name = "mon_node")
public class ProbeSideMonitorNode extends MonitorNode {
	private static final long serialVersionUID = 1L;

	public ProbeSideMonitorNode() {
	}

	public ProbeSideMonitorNode(String id, String name, String typeId, MonitorTargetAuditState auditState, int probeId,
			String ip, long modified, String domainId) {
		super(id, name, typeId, auditState, probeId, ip, modified, domainId);
	}

	public ProbeSideMonitorNode(String ip) {
		super(ip);
	}

	public ProbeSideMonitorNode(ProbeSideMonitorNode copy) {
		super(copy);
	}

	public ProbeSideMonitorNode(MonitorNode copy) {
		super(copy);
	}

	@Id
	@Override
	@Column(length = 50)
	public String getId() {
		return super.getId();
	}

	@Override
	@Column(length = 50)
	public String getIp() {
		return super.getIp();
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
	@Column(name = "domain_id", length = EntityConst.ID_LENGTH, nullable = false)
	public String getDomainId() {
		return super.getDomainId();
	}
	
	@Override
	@Column(name = "tags", length = EntityConst.DATA_LENGTH)
	public String getTags() {
		return super.getTags();
	}
	
	@Override
	@Column(name = "os", length = EntityConst.CODE_LENGTH)
	public String getOs() {
		return super.getOs();
	}

	@Override
	@Column(name = "host", length = EntityConst.CODE_LENGTH)
	public String getHost() {
		return super.getHost();
	}
	
}
