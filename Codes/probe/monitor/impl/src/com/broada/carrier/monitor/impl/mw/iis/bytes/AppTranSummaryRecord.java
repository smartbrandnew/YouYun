package com.broada.carrier.monitor.impl.mw.iis.bytes;


/**
 * 用于保存一个WEB应用的发送与接收字节数合计记录
 */
public class AppTranSummaryRecord {
	private String name;
	private long upTime;
	private long sendBytes;
	private long receBytes;
	
	public AppTranSummaryRecord(String name, long upTime, long sendBytes, long receBytes) {
		super();
		this.name = name;
		this.upTime = upTime;
		this.sendBytes = sendBytes;
		this.receBytes = receBytes;
	}

	/**
	 * 应用名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 应用启动时间
	 * @return
	 */
	public long getUpTime() {
		return upTime;
	}

	/**
	 * 发送字节数累计
	 * @return
	 */
	public long getSendBytes() {
		return sendBytes;
	}

	/**
	 * 接收字节数累计
	 * @return
	 */
	public long getReceBytes() {
		return receBytes;
	}
}
