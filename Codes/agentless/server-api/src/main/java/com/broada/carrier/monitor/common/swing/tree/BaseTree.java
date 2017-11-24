package com.broada.carrier.monitor.common.swing.tree;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class BaseTree extends JTree {
	private static final long serialVersionUID = 1L;

	public BaseTree(BaseTreeModel model) {
		super(model);
		setRowHeight(22);
		setCellRenderer(new BaseTreeCellRenderer());
	}

	public BaseTree() {
		this(null);
	}
	
	@Override
	public BaseTreeModel getModel() {
		return (BaseTreeModel)super.getModel();
	}

	public void refresh(BaseTreeNode node, boolean includeChilds) {
		if (includeChilds)
			node.removeChilds();
		updateUI();
	}
	
	public void expend() {
		TreeUtil.expandTree(this);
	}

	public void setSelectionNode(BaseTreeNode node) {
		TreePath path = null;
		if (node != null)			
			path = getPath(node);
		setSelectionPath(path);
	}

	public TreePath getPath(BaseTreeNode node) {
		return new TreePath(getModel().getPathToRoot(node));
	}
}
