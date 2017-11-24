package com.broada.carrier.monitor.common.swing.tree;

import java.util.LinkedList;

import javax.swing.tree.DefaultTreeModel;

public class BaseTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = 1L;

	public BaseTreeModel(BaseTreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}

	public BaseTreeModel(BaseTreeNode root) {
		super(root);
	}
	
	public BaseTreeNode getRoot() {
		return (BaseTreeNode) super.getRoot();
	}

	public void setFilter(BaseTreeNodeFilter filter) {
		getRoot().setFilter(filter);
		fireTreeStructureChanged(this, new Object[] { getRoot() }, null, null);
	}
	
	public BaseTreeNode getNode(Object key) {
		if (key == null)
			return null;
		
		BaseTreeNode root = getRoot();
		if (key.equals(root.getKey()))
			return root;
		
		LinkedList<BaseTreeNode> stack = new LinkedList<BaseTreeNode>();
		stack.add(root);
		while (!stack.isEmpty()) {
			BaseTreeNode node = stack.pop();			
			for (int i = 0; i < node.getChildCount(); i++) {
				BaseTreeNode child = node.getChildAt(i);
				if (key.equals(child.getKey()))
					return child;
				stack.push(child);
			}
		}
		return null;
	}
}