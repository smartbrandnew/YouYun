package com.broada.carrier.monitor.common.swing;

import java.awt.Window;

import javax.swing.JPanel;

/**
 * 实体对象编辑面板基类
 * @author Jiangjw
 */
public abstract class BeanEditPanel<T> extends JPanel implements BeanEditor<T> {
	private static final long serialVersionUID = 1L;

	/**
	 * 获取标题
	 * @return
	 */
	public abstract String getTitle();	
	
	/**
	 * 获取窗口
	 * @return
	 */
	protected Window getWindow() {
		return WinUtil.getWindowForComponent(this);
	}
}
