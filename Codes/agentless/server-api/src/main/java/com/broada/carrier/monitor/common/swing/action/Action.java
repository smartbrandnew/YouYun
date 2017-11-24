package com.broada.carrier.monitor.common.swing.action;

/**
 * 操作实现基类，以便使工具栏按钮与右键菜单的操作一致化
 * @author Jiangjw
 */
public abstract class Action {
	/**
	 * 操作展现名称
	 * @return
	 */
	public abstract String getText();

	/**
	 * 执行操作
	 * @param obj
	 */
	public abstract void execute(Object obj);

	/**
	 * 是否可见
	 * @param obj
	 * @return
	 */
	public boolean isVisible(Object obj) {
		return obj != null;
	}

	/**
	 * 是否可用
	 * @param obj
	 * @return
	 */
	public boolean isEnabled(Object obj) {
		return obj != null;
	}
}
