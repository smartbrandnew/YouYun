package com.broada.carrier.monitor.impl.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.broada.carrier.monitor.method.cli.CLIMethodConfiger;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.jdbc.JdbcMethodConfiger;
import com.broada.carrier.monitor.method.jdbc.JdbcMonitorMethodOption;
import com.broada.carrier.monitor.method.script.ScriptMethod;
import com.broada.carrier.monitor.method.script.ScriptMethodConfiger;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethodConfiger;
import com.broada.carrier.monitor.method.storage.StorageMethodConfig;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class GenericMonitorPackage implements MonitorPackage {

	/*
	 * 该配置文件只在server，通过配置该文件即可为额外类型添加icmp监测
	 */
	public static final String CONFIG_FILE = System.getProperty("user.dir") + "/conf/genericExtendConfig.properties";
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
		Set<String> types = new HashSet<String>();
		types.add("Server");
		types.add("OS");
		types.add("StorageDev");

		if (pro != null) {
			Set<Object> set = pro.keySet();
			for (Object o : set)
				types.add((String) o);
		}

		String[] methodTypeIds = new String[] { ScriptMethod.TYPE_ID, SmisMethod.TYPE_ID, SnmpMethod.TYPE_ID,
				CLIMonitorMethodOption.TYPE_ID, JdbcMonitorMethodOption.TYPE_ID };
		int index = 1;
		// todo
		return new MonitorType[] { new MonitorType("DYNAMIC_MONITOR", "DYNAMICMONITOR", "通用监测器",
				"监测动态配置的通用监测器的各种运行参数情况。", GenericConfiger.class.getName(), GenericMonitor.class.getName(), index++,
				types.toArray(new String[0]), methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(ScriptMethod.TYPE_ID, "通用监测协议", ScriptMethodConfiger.class),
				new MonitorMethodType("ProtocolSmis", "SMI-S监测协议", StorageMethodConfig.class),
				new MonitorMethodType("ProtocolSnmp", "SNMP监测协议", SnmpMethodConfiger.class),
				new MonitorMethodType("ProtocolCcli", "CLI监测协议", CLIMethodConfiger.class),
				new MonitorMethodType(JdbcMonitorMethodOption.TYPE_ID, "JDBC监测协议", JdbcMethodConfiger.class), };
	}
}
