package uyun.bat.monitor.core.logic;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;

/**
 * 检查者 <br>
 * 执行监测器的检查逻辑
 */
public interface Checker {
	/**
	 * 检查是否监测器的状态翻转 <br>
	 * 返回此次检查，监测器的状态
	 */
	public MonitorState checkIfMonitorStatusRollover();

	/**
	 * 获取对应的监测器
	 */
	public Monitor getMonitor();

	/**
	 * 本方法被{@link CheckController}调用<br>
	 * {@link CheckController}会依次调用<br>
	 * checkIfMonitorStatusRollover获取监测器状态<br>
	 * 若状态变更，则更新监测器<br>
	 * doAfterChecke<br>
	 * {@link Checker}若触发事件，则调用{@link CheckController}执行事件触发及发送邮件
	 */
	public void doAfterCheck();
}
