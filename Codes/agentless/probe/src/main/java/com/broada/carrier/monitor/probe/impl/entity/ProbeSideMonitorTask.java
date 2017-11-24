package com.broada.carrier.monitor.probe.impl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.EntityConst;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

@Entity
@Table(name = "mon_task")
public class ProbeSideMonitorTask extends MonitorTask {
	private static final long serialVersionUID = 1L;

	public ProbeSideMonitorTask() {
	}

	public ProbeSideMonitorTask(MonitorTask copy) {
		super(copy);
	}

	@Override
	@Column(name = "policy_code", length = EntityConst.CODE_LENGTH, nullable = false)
	public String getPolicyCode() {
		return super.getPolicyCode();
	}

	@Override
	@Column(name = "method_code", length = EntityConst.CODE_LENGTH)
	public String getMethodCode() {
		return super.getMethodCode();
	}

	@Override
	@Column(nullable = false)
	public Date getModified() {
		return super.getModified();
	}

	@Override
	@Id
	@Column(length = EntityConst.CODE_LENGTH)
	public String getId() {
		return super.getId();
	}

	@Override
	@Column(length = EntityConst.NAME_LENGTH, nullable = false)
	public String getName() {
		return super.getName();
	}

	@Override
	@Column(name = "type_id", length = EntityConst.CODE_LENGTH, nullable = false)
	public String getTypeId() {
		return super.getTypeId();
	}

	@Override
	@Column(name = "node_id", length = EntityConst.CODE_LENGTH, nullable = false)
	public String getNodeId() {
		return super.getNodeId();
	}

	@Override
	@Column(name = "res_id", length = EntityConst.CODE_LENGTH)
	public String getResourceId() {
		return super.getResourceId();
	}

	@Override
	@Column(nullable = false)
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	@Lob
	@Column(columnDefinition = "clob")
	public String getParameter() {
		return super.getParameter();
	}

	@Override
	@Column(length = EntityConst.DESCR_LENGTH)
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	@Column(name = "host", length = EntityConst.CODE_LENGTH, nullable = false)
	public String getHost() {
		return super.getHost();
	}

	@Override
	@Column(name = "tags", length = EntityConst.DATA_LENGTH)
	public String getTags() {
		return super.getTags();
	}

	@Override
	@Column(name = "ip", length = EntityConst.DATA_LENGTH)
	public String getIp() {
		return super.getIp();
	}
	
	
	

}
