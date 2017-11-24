package com.broada.carrier.monitor.client.impl.resource;

import javax.swing.Icon;

import com.broada.carrier.monitor.client.impl.common.TargetTreeNode;
import com.broada.carrier.monitor.client.impl.common.TargetTypeIconLibrary;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;

public class ResourceTreeNode extends TargetTreeNode {
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

	public ResourceTreeNode(MonitorResource resource) {
		super(resource);
	}

	public MonitorResource getResource() {
		return (MonitorResource) getTarget();
	}
	
	public void setResource(MonitorResource resource) {
		setTarget(resource);
		icon = null;
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	protected BaseTreeNode[] loadChilds() {
		return null;
	}

	@Override
	public Object getKey() {
		return getResource().getId();
	}
}
