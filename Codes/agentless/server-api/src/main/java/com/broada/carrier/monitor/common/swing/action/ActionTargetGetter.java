package com.broada.carrier.monitor.common.swing.action;

/**
 * 操作目标提取器
 * @author Jiangjw
 */
public interface ActionTargetGetter {
	/**
	 * 获取当前操作目标，可能会是表格或图形中，用户选定的操作对象
	 * @return 如果用户没有选定任何内容，一般返回null
	 */
	Object getTarget();
}
