package com.broada.carrier.monitor.client.impl.resource;

import javax.swing.Icon;

import com.broada.carrier.monitor.client.impl.common.TargetTypeIconLibrary;
import com.broada.carrier.monitor.client.impl.common.TargetTypeTreeNode;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

public class ResourceTypeTreeNode extends TargetTypeTreeNode {
	private static Icon DEFAULT_ICON = IconLibrary.getDefault().getIcon("resources/images/tree_target.png");
	private Icon icon;

	@Override
	public Icon getIcon() {
		if (icon == null) {
			if (getTargetType() != null)
				icon = TargetTypeIconLibrary.getDefault().getIcon(getTargetType().getId());
			if (icon == null)
				icon = DEFAULT_ICON;
		}
		return icon;
	}
	
	public ResourceTypeTreeNode() {
		super(false);
	}

	public ResourceTypeTreeNode(MonitorTargetType targetType) {
		super(targetType);
	}
}