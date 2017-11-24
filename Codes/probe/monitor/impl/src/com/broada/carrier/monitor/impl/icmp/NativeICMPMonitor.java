package com.broada.carrier.monitor.impl.icmp;

import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.impl.common.net.PingUtil;
import com.broada.carrier.monitor.impl.icmp.ICMPMonitor.MonitorProcesser;
import com.broada.utils.StringUtil;

/**
 * 使用本地方法的ICMP协议(Nsock)来进行连通性测试的监测实现
 * 
 * @author zhouww
 */
public class NativeICMPMonitor implements MonitorProcesser {

	/*
	 * 
	 * @see com.broada.srvmonitor.impl.icmp.ICMPMonitor.MonitorProcesser#monitor(com.broada.srvmonitor.impl.icmp.ICMPParameter,
	 *      java.util.List, java.lang.StringBuffer)
	 */
	public int monitor(ICMPParameter p, List<String> addrs, StringBuffer desc) {
		int ruleType = p.getRuleType();
		int timeout = p.getTimeout();
		int count = p.getRequestCount();
		int requestInterval = p.getRequestInterval();
		int ttlCount = 0;
		int ttlSum = 0;

		for (Iterator<String> iter = addrs.iterator(); iter.hasNext();) {
			String ip = (String) iter.next();
			if (StringUtil.isNullOrBlank(ip) || !IPUtil.isIPAddress(ip)) {
				continue;
			}
			StringBuilder ttl = new StringBuilder();
			boolean isConnected = false;
			try {
				isConnected = PingUtil.ping(ip, count, requestInterval, timeout, ttl);
			} catch (Exception e) {
				desc.append("Ping" + ip + "时发生错误." + e.getMessage() + "\n");
				continue;
			}
			if (!isConnected) {
				desc.append("目标<" + ip + ">ICMP不可达(在" + timeout + "毫秒内)\n");
			}

			if (ruleType == ICMPParameter.RULE_LOOSE) {// 不严格监测
				if (isConnected) {
					return Integer.valueOf(ttl.toString()).intValue();
				}
				continue;
			} else {// 严格监测
				if (!isConnected) {
					return -1;
				}
				ttlCount++;
				ttlSum = ttlSum + Integer.valueOf(ttl.toString());
			}
		}
		return -1 == ttlSum ? -1 : ttlSum / ttlCount;
	}

}