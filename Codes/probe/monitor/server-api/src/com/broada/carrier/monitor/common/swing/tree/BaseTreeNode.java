package com.broada.carrier.monitor.common.swing.tree;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

public abstract class BaseTreeNode implements TreeNode, Comparable<BaseTreeNode> {
	private BaseTreeNode parent;
	private Vector<BaseTreeNode> childs;
	private Vector<BaseTreeNode> filterChilds;
	private BaseTreeNodeFilter filter;

	protected abstract BaseTreeNode[] loadChilds();

	public Icon getIcon() {
		return null;
	}
	
	public void removeChilds() {
		childs = null;
		filterChilds = null;
	}

	public void setFilter(BaseTreeNodeFilter filter) {
		this.filter = filter;
		this.filterChilds = null;
		if (childs != null) {
			for (BaseTreeNode node : childs) {
				node.setFilter(filter);
			}
		}
	}

	protected Vector<BaseTreeNode> getChilds() {
		if (childs == null) {
			childs = new Vector<BaseTreeNode>();
			BaseTreeNode[] nodes = loadChilds();
			if (nodes != null) {
				for (BaseTreeNode node : nodes) {
					node.parent = this;
					childs.add(node);
				}
			}
		}

		if (filter != null) {
			if (filterChilds == null) {
				filterChilds = new Vector<BaseTreeNode>();
				for (BaseTreeNode node : childs) {
					node.setFilter(filter);
					if (filter.match(node)) {
						node.setFilter(null);
						filterChilds.add(node);
					} else if (node.getChildCount() > 0)
						filterChilds.add(node);
				}
			}
			return filterChilds;
		}

		return childs;
	}

	@Override
	public BaseTreeNode getChildAt(int childIndex) {
		return getChilds().get(childIndex);
	}

	@Override
	public int getChildCount() {
		return getChilds().size();
	}

	@Override
	public BaseTreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return getChilds().indexOf(node);
	}

	@Override
	public boolean isLeaf() {
		return getAllowsChildren() || getChilds().isEmpty();
	}

	@Override
	public Enumeration<?> children() {
		return getChilds().elements();
	}
	
	public abstract Object getKey();

	@Override
	public int compareTo(BaseTreeNode o) {
		return this.toString().compareTo(o.toString());
	}
}