package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public class SpeedUtil {
	/**
	 * 利用存储在当前监测任务临时缓存中的指标数据，来计算两次监测之间的差值每秒增速
	 * @param tempData 监测任务临时缓存
	 * @param item 指标key值
	 * @param currValue 当前值
	 * @param now 当前时间
	 * @return
	 */
	public static Double calSpeed(MonitorTempData tempData, String item, long currValue, long now) {
		Double speed = null;
		DefaultDynamicObject lastData = tempData.getData(DefaultDynamicObject.class);
		if (lastData == null) {
			lastData = new DefaultDynamicObject();
			tempData.setData(lastData);
		}
		
		if (tempData.getTime() != null) {
			if (lastData != null) {
				Object temp = lastData.get(item);
				if (temp != null) {
					long lastValue = (long) lastData.get(item, 0l);
					long diffValue = currValue - lastValue;
					long diffTime = now - tempData.getTime().getTime();
					if (diffValue > 0 && diffTime > 0) {
						speed = diffValue / (diffTime / 1000.0);
					}
				}
			}
		}
		
		lastData.set(item, currValue);
		return speed;
	}
}
