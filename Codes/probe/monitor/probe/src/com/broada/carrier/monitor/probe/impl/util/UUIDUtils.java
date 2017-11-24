package com.broada.carrier.monitor.probe.impl.util;

import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.probe.impl.config.Config;

public class UUIDUtils {

	public static String generateId(String str) {
		return EncryptUtil.string2MD5(str);
	}

	/**
	 * 根据macAddr、ipaddr、hostname、port 生成probeId
	 * 
	 * @return
	 */
	public static String getProbeId() {
		String macAddr = MacUtil.getMacAddr();
		if (macAddr == null)
			macAddr = "";
		String probeIp = Config.getDefault().getProbeIp();
		if (probeIp == null || HostIpUtil.getLocalHost().equals(probeIp.trim())) {
			probeIp = HostUtil.getIP();
		}
		String hostname = Config.getDefault().getProbeHostName();
		if (hostname == null || HostIpUtil.getLocalHost().equals(hostname.trim()) || "localhost".equalsIgnoreCase(hostname.trim()))
			hostname = HostUtil.getHostname();
		int port=Config.getDefault().getProbePort();
		return EncryptUtil.string2MD5(macAddr + probeIp + hostname+port);
	}

}
