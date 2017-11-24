package com.broada.carrier.monitor.client.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.broada.carrier.monitor.client.impl.cache.ClientCache;
import com.broada.carrier.monitor.client.impl.common.TargetTreeNode;
import com.broada.carrier.monitor.client.impl.common.TargetTypeIconLibrary;
import com.broada.carrier.monitor.client.impl.resource.ResourceTreeNode;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;

public class NodeTreeNode extends TargetTreeNode {
	private static Icon DEFAULT_ICON = IconLibrary.getDefault().getIcon("resources/images/tree_target.png");
	private Icon icon;

	@Override
	public Icon getIcon() {
		if (icon == null) {
			if (getTarget() != null)
				icon = TargetTypeIconLibrary.getDefault().getIcon(getTarget().getTypeId());
			if (icon == null)
				icon = DEFAULT_ICON;
		}
		return icon;
	}

	public NodeTreeNode(MonitorNode node) {
		super(node);
	}

	public MonitorNode getNode() {
		return (MonitorNode) getTarget();
	}
	
	public void setNode(MonitorNode node) {
		setTarget(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	protected BaseTreeNode[] loadChilds() {
		Map<String, MonitorResource> resourceMap = ClientCache.getResourceMap();
		MonitorResource[] resources = null;
		List<MonitorResource> list = new ArrayList<MonitorResource>();
		for(String resourceId : resourceMap.keySet()){
			if(resourceMap.get(resourceId) != null && resourceMap.get(resourceId).getNodeId().equals(getNode().getId()))
				list.add(resourceMap.get(resourceId));
		}
		resources = list.toArray(new MonitorResource[0]);
		BaseTreeNode[] childs = new BaseTreeNode[resources.length];
		for (int i = 0; i < childs.length; i++)
			childs[i] = new ResourceTreeNode(resources[i]);
		Arrays.sort(childs);
		return childs;
	}

	@Override
	public Object getKey() {
		return getNode().getId();
	}
}