package com.broada.carrier.monitor.client.impl.common;

import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;

public abstract class TargetTreeNode extends BaseTreeNode {
	private MonitorTarget target;

	public TargetTreeNode(MonitorTarget target) {
		this.target = target;
	}

	public MonitorTarget getTarget() {
		return target;
	}

	public void setTarget(MonitorTarget target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("%s", getTarget().getName());
	}
}