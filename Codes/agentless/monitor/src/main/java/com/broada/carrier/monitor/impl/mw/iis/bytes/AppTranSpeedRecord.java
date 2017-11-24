package com.broada.carrier.monitor.impl.mw.iis.bytes;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.PerfItem;
import com.broada.utils.NumberUtil;

/**
 * 用于保存一个WEB应用的发送与接收字节数速率记录
 */
public class AppTranSpeedRecord implements Serializable {
  public static final String ITEM_SEND = "IIS-TRANSFERBYTES-1";
  public static final String ITEM_RECEIVED = "IIS-TRANSFERBYTES-2";
  public static final String ITEM_TOTAL = "IIS-TRANSFERBYTES-3";
  public static final String ITEM_RUNTIME = "IIS-TRANSFERBYTES-4";		

	private static final long serialVersionUID = 1L;
	private String name;
	private long upTime;
	private double sendBytesPerSec;
	private double receBytesPerSec;
	
	public AppTranSpeedRecord() {
	}

	public AppTranSpeedRecord(String name, long upTime, double sendBytesPerSec, double receBytesPerSec) {
		super();
		this.name = name;
		this.upTime = upTime;
		this.sendBytesPerSec = sendBytesPerSec;
		this.receBytesPerSec = receBytesPerSec;
	}

	/**
	 * 从一条AppTranSummaryRecord中构建出速率对象
	 * 计算方法：
	 * sendBytesPerSec = record.sendBytes / record.upTime;
	 * receBytesPerSec = record.receBytes / record.upTime;
	 * @param record
	 */
	public AppTranSpeedRecord(AppTranSummaryRecord record) {
		this(null, record);
	}
	
	/**
	 * 从两条不同时间点采集的AppTranSummaryRecord中构建出速率对象
	 * 计算方法：
	 * 1. 如果 now.upTime < last.upTime，则转AppTranSpeedRecord(now)
	 * 2. 否则使用以下算法
	 *    sendBytesPerSec = (now.sendBytes - last.sendBytes) / (now.upTime - last.upTime);
	 *    receBytesPerSec = (now.receBytes - last.receBytes) / (now.upTime - last.upTime);
	 * @param last
	 * @param now
	 */
	public AppTranSpeedRecord(AppTranSummaryRecord last, AppTranSummaryRecord now) {
		this.name = now.getName();
		this.upTime = now.getUpTime();
		if (last == null || now.getUpTime() < last.getUpTime()) {
			if (this.upTime == 0) {
				this.sendBytesPerSec = 0;
				this.receBytesPerSec = 0;
			} else {
				this.sendBytesPerSec = NumberUtil.round(now.getSendBytes() * 1.0 / this.upTime, 1);
				this.receBytesPerSec = NumberUtil.round(now.getReceBytes() * 1.0 / this.upTime, 1);			
			}
		} else {
			long time = now.getUpTime() - last.getUpTime();
			if (time == 0 || now.getSendBytes() < last.getSendBytes())
				this.sendBytesPerSec = 0;
			else
				this.sendBytesPerSec = NumberUtil.round((now.getSendBytes() - last.getSendBytes()) * 1.0 / time, 1);
			if (time == 0 || now.getReceBytes() < last.getReceBytes())
				this.receBytesPerSec = 0;
			else
				this.receBytesPerSec = NumberUtil.round((now.getReceBytes() - last.getReceBytes()) * 1.0 / time, 1);		
		}
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
  @PerfItem(code = ITEM_RUNTIME, name = "应用启动时间", unit = "秒")	
	public long getUpTime() {
		return upTime;
	}

	/**
	 * 发送速率
	 * @return
	 */
  @PerfItem(code = ITEM_SEND, name = "发送速率", unit = "B/s")	
	public double getSendBytesPerSec() {
		return sendBytesPerSec;
	}

	/**
	 * 接收速率
	 * @return
	 */
  @PerfItem(code = ITEM_RECEIVED, name = "接收速率", unit = "B/s")	
	public double getReceBytesPerSec() {
		return receBytesPerSec;
	}
  
	/**
	 * 合计速率
	 * @return
	 */
  @PerfItem(code = ITEM_TOTAL, name = "合计速率", unit = "B/s")	
	public double getTotalBytesPerSec() {
		return sendBytesPerSec + receBytesPerSec;
	}  
}
