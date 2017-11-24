package com.broada.carrier.monitor.probe.impl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;

@Entity
@Table(name = "mon_policy")
public class ProbeSideMonitorPolicy extends MonitorPolicy {
	private static final long serialVersionUID = 1L;
	
	public ProbeSideMonitorPolicy() {
	}
	
	public ProbeSideMonitorPolicy(MonitorPolicy copy) {
		super(copy);
	}

	@Column(length = 100)
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public int getInterval() {
		return super.getInterval();
	}

	@Column(name="error_interval")
	@Override
	public int getErrorInterval() {
		return super.getErrorInterval();
	}

	@Column(name="work_week_days", length = 10)
	@Override
	public String getWorkWeekDays() {		
		return super.getWorkWeekDays();
	}

	@Column(name="work_time_range", length = 50)
	@Override
	public String getWorkTimeRange() {		
		return super.getWorkTimeRange();
	}

	@Override
	public String getStopTimeRanges() {		
		return super.getStopTimeRanges();
	}

	@Id
	@Column(length = 50)
	@Override
	public String getCode() {		
		return super.getCode();
	}

	@Override
	public long getModified() {		
		return super.getModified();
	}

	@Override
	@Column(length = 500)
	public String getDescr() {		
		return super.getDescr();
	}
}
