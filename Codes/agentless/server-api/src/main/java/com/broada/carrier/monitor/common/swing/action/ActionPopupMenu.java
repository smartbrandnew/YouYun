package com.broada.carrier.monitor.common.swing.action;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * 操作弹出菜单
 * @author Jiangjw
 */
public class ActionPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private ActionTargetGetter getter;
	private JComponent[] items;

	/**
	 * 构造一个弹出菜单
	 * @param getter 操作目标提取器
	 * @param actions 菜单项
	 */
	public ActionPopupMenu(ActionTargetGetter getter, Action[] actions) {
		this.getter = getter;
		this.items = new JComponent[actions.length];
		for (int i = 0; i < actions.length; i++) {
			if (actions[i] == null)
				items[i] = new JPopupMenu.Separator();
			else
				items[i] = new ActionMenuItem(actions[i]);
			add(items[i]);
		}
	}

	private class PopupMouseAdapter extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				showMenu(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			Object target = getter.getTarget();
			boolean hasMenu = false;
			for (JComponent item : items) {
				if (item instanceof ActionMenuItem) {
					ActionMenuItem action = (ActionMenuItem) item;
					action.setTarget(target);
					if (action.isVisible())
						hasMenu = true;
				} else {
					if (hasMenu) {
						item.setVisible(true);
						hasMenu = false;
					} else
						item.setVisible(false);
				}
			}
						
			for (int i = items.length - 1; i > 0; i--) {
				JComponent item = items[i];
				if (item instanceof ActionMenuItem && item.isVisible()) {
					break;
				} else
					item.setVisible(false);
			}
			
			show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * 将此下拉菜单附加到某个控件上
	 * @param component
	 */
	public void addPopup(Component component) {
		component.addMouseListener(new PopupMouseAdapter());
	}
}
