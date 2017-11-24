package com.broada.carrier.monitor.probe.impl.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.config.BaseConfig;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

/**
 * 配置读取类
 * 
 * @author Jiangjw
 */
public class Config extends BaseConfig {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	private static Config instance;

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static Config getDefault() {
		if (instance == null) {
			synchronized (Config.class) {
				if (instance == null)
					instance = new Config();
			}
		}
		return instance;
	}

	/**
	 * 是否进行自动注册
	 * 
	 * @return
	 */
	public boolean isAutoRegister() {
		return getProps().get("probe.autoRegistry.enable", true);
	}

	// /**
	// * PMDB事件服务IP
	// * @return
	// */
	// public String getPMDBEventIp() {
	// //return getProps().get("PMDB.event.ip", EventClient.SERVICE_IP_DEFAULT);
	// }

	public MonitorProbe getProbe() {
		return new MonitorProbe(0, getProps().check("probe.code"), getProps().get("probe.hostname",
				getProps().check("probe.code")), getProps().get("probe.descr"), getProps().check("probe.ipaddr"),
				getProps().get("probe.webserver.port", 9145));
	}

	public int getDatabasePort() {
		return getProps().get("probe.database.port", 0);
	}

	public String getMonitorPolicy() {
		return getProps().get("monitor.policy", "default,1,0");
	}

	public String getAvailableTestIps() {
		String ips = getProps().get("available.test.ips");
		if (ips == null || ips.isEmpty())
			ips = getGatewayIp();
		return ips;
	}

	// TODO 实现linux版本
	private static String getGatewayIp() {
		BufferedReader output = null;
		InputStreamReader isr = null;
		try {
			Process result = Runtime.getRuntime().exec("cmd /c netstat -rn");
			isr = new InputStreamReader(result.getInputStream());
			output = new BufferedReader(isr);
			String line = output.readLine();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("default") || line.startsWith("0.0.0.0") == true) {
					break;
				}
				line = output.readLine();
			}
			if (line == null) {
				return null;
			}
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();
			st.nextToken();
			return st.nextToken();
		} catch (Exception e) {
			logger.warn("获取网关地址失败，错误：" + e);
			logger.debug("堆栈：", e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public int getAvailableTestInterval() {
		return getProps().get("available.test.interval", 30);
	}

	public String getServerWebProtocol() {
		return getProps().get("server.webserver.protocol", "http");
	}

	public String getServerIp() {
		return getProps().get("server.ipaddr", "127.0.0.1");
	}

	public int getServerWebPort() {
		return getProps().get("server.webserver.port", 8890);
	}

	public int getAutosyncCheckInterval() {
		return getProps().get("autosync.interval", 120);
	}

	public int getProperty(String name, int defaultVal) {
		return getProps().get(name, defaultVal);
	}

	public String getProperty(String name, String defaultVal) {
		return getProps().get(name, defaultVal);
	}

	public String getProperty(String name) {
		return getProps().get(name);
	}

	public long getProperty(String name, long defaultVal) {
		return getProps().get(name, defaultVal);
	}

	public String getProbeIp() {
		return getProps().get("probe.ipaddr", "127.0.0.1");
	}

	public String getProbeCode() {
		return getProps().get("probe.code", "127.0.0.1");
	}

	public String getProbeHostName() {
		return getProps().get("probe.hostname", "127.0.0.1");
	}

	public int getProbePort() {
		return getProps().get("probe.webserver.port", 9145);
	}

	public List<String> getProbeTags() {
		String str = getProps().get("probe.tags", "");
		String[] array = str.split(";");
		List<String> tags=new ArrayList<String>();
		for (String s : array) {
			if (StringUtils.isNotNullAndTrimBlank(s)) {
				tags.add(s);
			}
		}
		return tags;
	}
	
	public static String getYamlDir(){
		return getConfDir() + "/conf.d";
	}
}
