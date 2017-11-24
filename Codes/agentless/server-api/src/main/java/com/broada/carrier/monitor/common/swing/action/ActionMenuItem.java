package com.broada.carrier.monitor.common.swing.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * 操作菜单项
 * @author Jiangjw
 */
public class ActionMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	private Action action;
	private Object target;

	public ActionMenuItem(Action action) {
		this.action = action;				
		this.setText(action.getText());
		this.addActionListener(new MenuItemActionListener());
	}
	
	/**
	 * 设置操作目标，以便更新菜单项状态。对于下拉菜单来说，此操作一般在展现下拉菜单时调用
	 * @param target
	 */
	public void setTarget(Object target) {
		this.target = target;
		boolean visible = action.isVisible(target); 
		this.setVisible(visible);
		if (visible)
			this.setEnabled(action.isEnabled(target));
	}
	
	private class MenuItemActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			action.execute(target);
		}
	}
}
