package com.broada.carrier.monitor.probe.impl.dispatch;

public class BaseItem {
	private long startTime;
	private long endTime;
	
	/**
	 * 进行执行状态
	 */
	public void start() {
		startTime = MonitorResultUploader.getDefault().getServerTime().getTime();
	}

	/**
	 * 获取启动时间
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 获取结束时间
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * 执行结束
	 */
	public void stop() {
		endTime = MonitorResultUploader.getDefault().getServerTime().getTime();
	}
	
	/**
	 * 运行耗时，单位ms
	 * @return
	 */
	public long getTime() {
		return endTime - startTime;
	}
}
