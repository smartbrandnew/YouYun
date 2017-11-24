package com.broada.carrier.monitor.client.impl.common;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

public class TargetTypeTreeNode extends BaseTreeNode {
	private MonitorTargetType targetType;
	private boolean nodeType;

	public TargetTypeTreeNode(boolean nodeType) {
		this.nodeType = nodeType;
	}

	public TargetTypeTreeNode(MonitorTargetType targetType) {
		this.targetType = targetType;
		this.nodeType = targetType.isNode();
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	public MonitorTargetType getTargetType() {
		return targetType;
	}

	@Override
	protected BaseTreeNode[] loadChilds() {
		MonitorTargetType[] types;
		if (targetType == null) {
			if (nodeType)
				types = ServerContext.getTargetTypeService().getTargetTypesByNode();
			else
				types = ServerContext.getTargetTypeService().getTargetTypesByResource();
		} else
			types = ServerContext.getTargetTypeService().getTargetTypesByParentId(targetType.getId());
		BaseTreeNode[] nodes = new BaseTreeNode[types.length];
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = new TargetTypeTreeNode(types[i]);
		return nodes;
	}

	@Override
	public String toString() {
		return targetType == null ? null : targetType.getName();
	}

	@Override
	public String getKey() {
		return targetType == null ? null : targetType.getId();
	}
}