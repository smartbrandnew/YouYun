package com.broada.carrier.monitor.server.impl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.broada.carrier.monitor.server.api.entity.MonitorNode;

@Entity
@Table(name = "mon_node")
public class ServerSideMonitorNode {
	private String id;
	private int probeId;
	
	public ServerSideMonitorNode() {		
	}
	
	public ServerSideMonitorNode(String id, int probeId) {
		this.id = id;
		this.probeId = probeId;
	}

	public ServerSideMonitorNode(MonitorNode node) {
		set(node);
	}

	@Id
	@Column(length = 50)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "probe_id")
	public int getProbeId() {
		return probeId;
	}

	public void setProbeId(int probeId) {
		this.probeId = probeId;
	}

	public void get(MonitorNode node) {
		node.setProbeId(probeId);
	}

	public void set(MonitorNode node) {
		this.id = node.getId();
		this.probeId = node.getProbeId();
	}
}
