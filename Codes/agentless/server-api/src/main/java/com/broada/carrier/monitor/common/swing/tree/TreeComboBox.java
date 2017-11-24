package com.broada.carrier.monitor.common.swing.tree;

import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class TreeComboBox extends JComboBox {
	private static final long serialVersionUID = 1L;
	/**
	 * 显示用的树
	 */
	private BaseTree tree;

	public TreeComboBox() {
		this(new BaseTree());
	}

	public TreeComboBox(BaseTree tree) {
		this.setTree(tree);
	}

	/**
	 * 设置树
	 * @param tree JTree
	 */
	public void setTree(BaseTree tree) {
		this.tree = tree;
		if (tree != null) {
			this.setSelectedItem(tree.getSelectionPath());
		}
		this.updateUI();
	}

	/**
	 * 取得树
	 * @return JTree
	 */
	public BaseTree getTree() {
		return tree;
	}

	@Override
	public void setSelectedItem(Object node) {
		getModel().setSelectedItem(node);
		tree.setSelectionNode((BaseTreeNode) node);
	}

	@Override
	public void updateUI() {
		ComboBoxUI cui = (ComboBoxUI) UIManager.getUI(this);
		if (cui instanceof MetalComboBoxUI) {
			cui = new MetalJTreeComboBoxUI();
		} else if (cui instanceof MotifComboBoxUI) {
			cui = new MotifJTreeComboBoxUI();
		} else {
			cui = new WindowsJTreeComboBoxUI();
		}
		setUI(cui);
	}

	class MetalJTreeComboBoxUI extends MetalComboBoxUI {
		protected ComboPopup createPopup() {
			return new TreePopup(comboBox);
		}
	}

	class WindowsJTreeComboBoxUI extends WindowsComboBoxUI {
		protected ComboPopup createPopup() {
			return new TreePopup(comboBox);
		}
	}

	class MotifJTreeComboBoxUI extends MotifComboBoxUI {
		private static final long serialVersionUID = 1L;

		protected ComboPopup createPopup() {
			return new TreePopup(comboBox);
		}
	}
}