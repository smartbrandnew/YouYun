package com.broada.carrier.monitor.probe.impl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorState;

@Entity
@Table(name = "mon_record")
public class ProbeSideMonitorRecord extends MonitorRecord {
	private static final long serialVersionUID = 1L;

	public ProbeSideMonitorRecord() {
	}

	public ProbeSideMonitorRecord(MonitorRecord copy) {
		super(copy);
	}

	@Id
	@Override
	public String getTaskId() {
		return super.getTaskId();
	}

	@Override
	public MonitorState getState() {
		return super.getState();
	}

	@Override
	public Date getTime() {
		return super.getTime();
	}

	@Column(length = 2000)
	@Override
	public String getMessage() {
		return super.getMessage();
	}

}
