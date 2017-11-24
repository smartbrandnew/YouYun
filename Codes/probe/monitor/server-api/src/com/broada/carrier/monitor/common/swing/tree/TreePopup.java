package com.broada.carrier.monitor.common.swing.tree;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

/**
 * <p>Title: JTreeComboBox</p>
 * <p>Description: TreePopup</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author  <a href="mailto:rockis@msn.com">zhanglei</a>
 *  && <a href="mailto:sunkingxie@hotmail.com">SunKing</a>
 * @version 1.0
 */
class TreePopup extends JPopupMenu implements ComboPopup {
	private static final long serialVersionUID = 1L;
	protected TreeComboBox comboBox;
	protected JScrollPane scrollPane;

	protected MouseMotionListener mouseMotionListener;
	protected MouseListener mouseListener;
	private MouseListener treeSelectListener = new MouseAdapter() {
		public void mouseReleased(MouseEvent e) {
			JTree tree = (JTree) e.getSource();
			TreePath tp = tree.getPathForLocation(e.getPoint().x,
					e.getPoint().y);
			if (tp == null) {
				return;
			}
			comboBox.setSelectedItem(tp.getLastPathComponent());
			togglePopup();
			MenuSelectionManager.defaultManager().clearSelectedPath();
		}
	};

	public TreePopup(JComboBox comboBox) {
		this.comboBox = (TreeComboBox) comboBox;
		setBorder(BorderFactory.createLineBorder(Color.black));
		setLayout(new BorderLayout());
		setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
		JTree tree = this.comboBox.getTree();
		if (tree != null) {
			scrollPane = new JScrollPane(tree);
			scrollPane.setBorder(null);
			add(scrollPane, BorderLayout.CENTER);
			tree.addMouseListener(treeSelectListener);
		}
	}

	@SuppressWarnings("deprecation")
	public void show() {
		updatePopup();
		show(comboBox, 0, comboBox.getHeight());
		comboBox.getTree().requestFocus();
	}

	@SuppressWarnings("deprecation")
	public void hide() {
		setVisible(false);
		comboBox.firePropertyChange("popupVisible", true, false);
	}

	protected JList list = new JList();

	public JList getList() {
		return list;
	}

	public MouseMotionListener getMouseMotionListener() {
		if (mouseMotionListener == null) {
			mouseMotionListener = new MouseMotionAdapter() {
			};
		}
		return mouseMotionListener;
	}

	public KeyListener getKeyListener() {
		return null;
	}

	public void uninstallingUI() {
	}

	/**
	 * Implementation of ComboPopup.getMouseListener().
	 *
	 * @return a <code>MouseListener</code> or null
	 * @see ComboPopup#getMouseListener
	 */
	public MouseListener getMouseListener() {
		if (mouseListener == null) {
			mouseListener = new InvocationMouseHandler();
		}
		return mouseListener;
	}

	protected void togglePopup() {
		if (isVisible()) {
			hide();
		} else {
			show();
		}
	}

	protected void updatePopup() {
		setPreferredSize(new Dimension(comboBox.getSize().width, 200));
		Object selectedObj = comboBox.getSelectedItem();
		if (selectedObj != null) {			
			((TreeComboBox) comboBox).getTree().setSelectionNode((BaseTreeNode) selectedObj);
		}
	}

	protected class InvocationMouseHandler extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
				return;
			}
			if (comboBox.isEditable()) {
				Component comp = comboBox.getEditor().getEditorComponent();
				if ((!(comp instanceof JComponent)) ||
						((JComponent) comp).isRequestFocusEnabled()) {
					comp.requestFocus();
				}
			} else if (comboBox.isRequestFocusEnabled()) {
				comboBox.requestFocus();
			}
			togglePopup();
		}
	}
}