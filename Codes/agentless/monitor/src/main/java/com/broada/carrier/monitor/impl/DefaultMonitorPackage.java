package com.broada.carrier.monitor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import com.broada.carrier.monitor.impl.icmp.ICMPConfiger;
import com.broada.carrier.monitor.impl.icmp.ICMPMonitor;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DefaultMonitorPackage implements MonitorPackage {

	/*
	 * 该配置文件只在server，通过配置该文件即可为额外类型添加icmp监测
	 */
	public static final String CONFIG_FILE = System.getProperty("user.dir") + "/conf/icmpExtendConfig.properties";
	private static Properties pro = null;

	static {
		initProperties();
	}

	/*
	 * 根据配置文件初始化。
	 */
	private static void initProperties() {
		FileInputStream fis = null;
		File confFile = new File(CONFIG_FILE);

		try {
			fis = new FileInputStream(confFile);
		} catch (FileNotFoundException ex) {
			return;
		}

		pro = new Properties();
		try {
			pro.load(fis);
		} catch (IOException e) {
			throw new RuntimeException("读取配置文件时出错，文件路径：" + CONFIG_FILE, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public MonitorType[] getTypes() {
		int index = 1;
		// 默认的三种支持icmp类型
		Set<String> types = new HashSet<String>();
		types.add("NetDev");
		types.add("SecDev");
		types.add("OS");

		if (pro != null) {
			Set<Object> set = pro.keySet();
			for (Object o : set)
				types.add((String) o);
		}

		return new MonitorType[] { new MonitorType("ICMP", "ICMP", "ICMP连通性监测", "使用ICMP监测节点的是否在线可连通。",
				ICMPConfiger.class.getName(), ICMPMonitor.class.getName(), index++, types.toArray(new String[0]), null), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("ICMP","ICMP-1", "延时", "毫秒", "ICMP请求延时", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}

}
