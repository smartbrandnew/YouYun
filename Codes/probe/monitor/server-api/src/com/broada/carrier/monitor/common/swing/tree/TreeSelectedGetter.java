package com.broada.carrier.monitor.common.swing.tree;

import javax.swing.JTree;

import com.broada.carrier.monitor.common.swing.action.ActionTargetGetter;

public class TreeSelectedGetter implements ActionTargetGetter {
	private JTree tree;

	public TreeSelectedGetter(JTree tree) {
		super();
		this.tree = tree;
	}

	@Override
	public Object getTarget() {
		if (tree.getSelectionPath() == null)
			return null;
		else
			return tree.getSelectionPath().getLastPathComponent(); 
	}
}
