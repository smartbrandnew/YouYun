package com.broada.carrier.monitor.impl.common;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.utils.StringUtil;

/**
 * 监测实现相关的辅助工具类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class MonitorUtil {  
	/**
	 * 得到合理的double值
	 * @param value
	 * @return
	 */
	public static Double getAvailableDoubleValue(Object value) {
		if (value == null || StringUtil.isBlank(value.toString())) {
			return null;
		}
		return Double.valueOf(value.toString());
	}
	
	/**
	 * 得到合理的String值
	 * @param value
	 * @return
	 */
	public static String getAvailableStringValue(Object value) {
		if (value == null) {
			return null; 
		}
		return value.toString();
	}

	public static boolean isUnknownDoubleValue(Double value) {
		return value == MonitorConstant.UNKNOWN_DOUBLE_VALUE;
	}
	
	public static boolean isUnknownDoubleValue(Object value) {
		return value == null || StringUtil.isBlank(value.toString())
				|| Double.valueOf(value.toString()) == MonitorConstant.UNKNOWN_DOUBLE_VALUE;
	}
	
	public static boolean isUnknownStringValue(Object value) {
		return value == null || MonitorConstant.UNKNOWN_STRING_VALUE.equals(value);
	}
}