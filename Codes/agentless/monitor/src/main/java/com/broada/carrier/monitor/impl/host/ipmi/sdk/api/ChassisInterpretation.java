package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

/**
 * 底盘信息解释器
 * 底盘英文信息解释为中文信息
 * @author pippo 
 * Create By 2014年6月6日 下午5:42:40
 */
public class ChassisInterpretation {
	
	/**
	 * ON/OFF解释
	 * @param str
	 * @return
	 */
	public static ResultType resolveOn(String str) {
		if ("on".equalsIgnoreCase(str)) {
			return ResultType.TRUE;
		}
		return ResultType.FALSE;
	}
	
	/**
	 * TRUE/FALSE解释
	 * @param str
	 * @return
	 */
	public static ResultType resolveBool(boolean bool) {
		if (bool) {
			return ResultType.TRUE;
		}
		return ResultType.FALSE;
	}
	
	/**
	 * inactive解释
	 * @param str
	 * @return
	 */
	public static ResultType resolveActive(String str) {
		if ("inactive".equalsIgnoreCase(str)) {
			return ResultType.FALSE;
		}
		return ResultType.TRUE;
	}
}
