package com.broada.carrier.monitor.impl.host.cli.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.AfterExecuteListener;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.lang.SimpleProperties;
import com.broada.utils.NumberUtil;
import com.broada.utils.StringUtil;

public class CLIHostInfoExecutor {
	private static final Log logger = LogFactory.getLog(CLIHostInfoExecutor.class);

	public static List<CLIHostInfoMonitorCondition> getHostInfoConditions(String taskId, CollectContext context) throws CLIException {
		MonitorNode monitorNode = context.getNode();
		CLIResult result = new CLIExecutor(taskId).execute(monitorNode, new CLIMonitorMethodOption(context.getMethod()),
				CLIConstant.COMMAND_HOSTINFO, (AfterExecuteListener) null);

		Properties p = result.getPropResult();
		String[] infos = new String[] { p.getProperty("processorCount"), p.getProperty("machineType"),
				p.getProperty("systemName"), p.getProperty("systemVersion"), p.getProperty("memorySize"),
				p.getProperty("processCount"), "", "" };
		if (p.size() >= 7) {
			infos[6] = convertIntfList(p.getProperty("intfList"));
		}

		String disksInfo = "";
		if (p.size() == 8) {
			StringBuffer strBuffer = new StringBuffer();
			infos[7] = p.getProperty("diskInfo");
			// 过滤磁盘信息
			String sep = "@";
			if (infos[7].contains("%"))
				sep = "%";
			String[] dInfos = infos[7].split(sep);
			for (String diskInfo : dInfos) {
				if (StringUtil.isNullOrBlank(diskInfo)) {
					continue;
				}
				strBuffer.append(diskInfo + "、");
			}
			disksInfo = strBuffer.toString();
			if (disksInfo.length() > 0) {
				disksInfo = disksInfo.substring(0, disksInfo.length() - 1);
			}
		}
		try {
			Double.parseDouble(infos[4]);
			Double.parseDouble(infos[5]);
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("Error HostInfo:" + p);
			throw new CLIResultParseException("内存大小[" + infos[4] + "]或进程数[" + infos[5] + "]不符合规范，解析主机信息失败。");
		}
		List<CLIHostInfoMonitorCondition> hostInfoList = new ArrayList<CLIHostInfoMonitorCondition>();

		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-1", infos[0]));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-2", infos[1]));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-3", infos[2]));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-4", infos[3]));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-5", monitorNode.getIp()));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-6", infos[4]));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-7", infos[5]));
		// 这里取最小MAC地址
		String macAddr = infos[6];
		if (!StringUtil.isNullOrBlank(macAddr)) {
			hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-8", macAddr));
		}

		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-9", disksInfo));
		return hostInfoList;
	}

	private static String convertIntfList(String text) {
		String result;

		if (text == null)
			result = null;
		else if (text.startsWith("aix;")) {
			StringBuffer sb = new StringBuffer();

			String[] items = text.split(";");
			String name = "";
			String mac = "";
			String ip = "";
			for (int i = 1; i < items.length; i++) {
				String[] fields = items[i].split(",");
				if (fields.length != 2)
					continue;

				if (name.length() > 0 && !fields[0].equals(name) && mac.length() > 0) {
					if (sb.length() > 0)
						sb.append(";");
					sb.append(name).append("=").append(ip).append("/").append(mac);
					ip = "";
					mac = "";
				}

				String[] values = fields[1].split("\\.");
				if (values.length == 4)
					ip = fields[1];
				else if (values.length == 6) {
					mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", Integer.parseInt(values[0], 16),
							Integer.parseInt(values[1], 16), Integer.parseInt(values[2], 16), Integer.parseInt(values[3], 16),
							Integer.parseInt(values[4], 16), Integer.parseInt(values[5], 16));
				} else
					continue;

				name = fields[0];
			}

			if (name.length() > 0 && mac.length() > 0) {
				if (sb.length() > 0)
					sb.append(";");
				sb.append(name).append("=").append(ip).append("/").append(mac);
			}
			result = sb.toString();
		} else if (text.startsWith("solaris;")) {// solaris
			StringBuffer sb = new StringBuffer();
			String[] fields = text.split(";");
			String name = "";
			String mac = "";
			String ip = "";

			for (int i = 0; i < fields.length; i++) {
				// 取有MAC的接口
				if (fields[i].contains("ether") && i > 2) {
					String[] ethmac = fields[i].split(",");
					if (ethmac.length != 2 || ethmac[1].indexOf(":") < 0)
						continue;
					String tmpMac = ethmac[1];
					String[] values = tmpMac.split(":");
					// MAC格式化
					mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", Integer.parseInt(values[0], 16),
							Integer.parseInt(values[1], 16), Integer.parseInt(values[2], 16), Integer.parseInt(values[3], 16),
							Integer.parseInt(values[4], 16), Integer.parseInt(values[5], 16));

					// get IP
					String[] inetIp = fields[i - 1].split(",");
					ip = inetIp[1];

					// get name
					String[] tmpName = fields[i - 2].split(",");
					name = tmpName[0].substring(0, tmpName[0].length() - 1);

					sb.append(name).append("=").append(ip).append("/").append(mac).append(";");
				}
			}

			result = sb.toString();
			result = result.substring(0, result.length() - 1);
		} else if (text.contains("%;")) { // HPUX
			StringBuffer sb = new StringBuffer();
			String[] fields = text.split("%;");
			String name = "";
			String mac = "";
			String ip = "";

			if (fields.length == 2) {
				// get all IP
				Map<String, String> ipMaps = new HashMap<String, String>();
				String[] ips = fields[0].split(";");
				for (String ipName : ips) {
					String[] nameAndIp = ipName.split(",");
					if (nameAndIp.length < 2)
						continue;
					ipMaps.put(nameAndIp[0], nameAndIp[1]);
				}

				String[] macs = fields[1].split(";");
				for (int index = 0; index < macs.length; index++) {
					if (macs[index].startsWith("0x") && macs[index].indexOf(",") > 0) {
						String[] macAndName = macs[index].split(",");
						mac = macAndName[0];
						name = macAndName[1];

						// 针对HP-UX MAC处理
						if (mac.startsWith("0x") && mac.length() == 14) {
							StringBuffer strBuf = new StringBuffer();
							int idx = 2;
							while (idx < mac.length()) {
								strBuf.append(mac.substring(idx, idx + 2));
								strBuf.append(":");
								idx += 2;
							}
							mac = strBuf.toString().substring(0, strBuf.length() - 1);
						}

						// get corresponding IP
						if (ipMaps.containsKey(name)) {
							ip = ipMaps.get(name);

							sb.append(name).append("=").append(ip).append("/").append(mac).append(";");
						}
					}
				}
			}

			result = sb.toString();
			result = result.substring(0, result.length() - 1);
		} else if (text.contains(",")) { // linux 类型
			StringBuffer sb = new StringBuffer();

			String[] fields = text.split(",");
			String name = "";
			String mac = "";
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].contains("encap:")) {
					if (name.length() > 0 && mac.length() > 0) {
						if (sb.length() > 0)
							sb.append(";");
						sb.append(name).append("=").append("/").append(mac);
					}
					name = fields[i - 2];
					mac = "";
				}

				if (fields[i].contains("HWaddr"))
					mac = fields[i + 1];

				if (fields[i].contains("addr:") && mac.length() > 0) {
					if (sb.length() > 0)
						sb.append(";");
					sb.append(name).append("=").append(fields[i].substring(5)).append("/").append(mac);
					name = "";
					mac = "";
				}
			}
			result = sb.toString();
		} else
			result = text;

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("原始端口列表信息转换前：%s\n转换后：%s", text, result));
		}

		return result;
	}

	public static List<CLIHostInfoMonitorCondition> getHostInfoConditions(String taskId, CollectContext context, int tryTimes) throws CLIException {
		MonitorNode monitorNode = context.getNode();
		CLIResult result = new CLIExecutor(taskId).execute(monitorNode, new CLIMonitorMethodOption(context.getMethod()),
				CLIConstant.COMMAND_HOSTINFO, tryTimes, (AfterExecuteListener) null);

		SimpleProperties p = new SimpleProperties(result.getPropResult());
		String systemName = p.get("systemName");
		String version = (String) context.getMethod().getProperties().get("sysversion");
		String typeId = (String) context.getMethod().getProperties().get("sysname");
		if ("Linux".equalsIgnoreCase(typeId) && !systemName.contains("inux")) {
			systemName = "Linux " + version;
		} else if ("Unix".equalsIgnoreCase(typeId) && !systemName.contains("nix")) {
			systemName = "Unix " + version;
		} else if ("Aix".equalsIgnoreCase(typeId)) {
			systemName = "AIX " + version;
		}
		List<CLIHostInfoMonitorCondition> hostInfoList = new ArrayList<CLIHostInfoMonitorCondition>();
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-1", p.get("processorCount")));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-2", p.get("machineType")));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-3", systemName));
		hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-4", version));
		hostInfoList.add(new CLIHostInfoMonitorCondition("host-name", p.get("systemName").equals(systemName) ? p
				.get("hostname") : p.get("systemName")));

		double memorySize = p.get("memorySize", 0.0);
		if (memorySize > 0)
			hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-6", Double.toString(NumberUtil
					.round(memorySize, 2))));

		int processCount = p.get("processCount", 0);
		if (processCount > 0)
			hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-7", Integer.toString(processCount)));

		String intfList = p.get("intfList");
		if (!StringUtil.isNullOrBlank(intfList)) {
			hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-8", intfList));

			String[] intfs = intfList.split(";");
			String minMac = null;
			for (String intf : intfs) {
				int pos = intf.indexOf("=");
				if (pos < 0)
					continue;
				pos = intf.indexOf("/", pos);
				if (pos < 0)
					continue;
				String mac = intf.substring(pos + 1);
				if (mac.equalsIgnoreCase("00:00:00:00:00:00") || mac.equalsIgnoreCase("FF:FF:FF:FF:FF:FF"))
					continue;
				if (minMac == null || minMac.compareTo(mac) > 0)
					minMac = mac;
			}

			if (minMac != null)
				hostInfoList.add(new CLIHostInfoMonitorCondition("host-mac-min", minMac));
		}

		String disksInfo = p.get("diskInfo");
		if (!StringUtil.isNullOrBlank(disksInfo)) {
			StringBuffer strBuffer = new StringBuffer();
			String sep = "@";
			if (disksInfo.contains("%"))
				sep = "%";
			String[] dInfos = disksInfo.split(sep);
			for (String diskInfo : dInfos) {
				if (StringUtil.isNullOrBlank(diskInfo)) {
					continue;
				}
				strBuffer.append(diskInfo + "、");
			}
			disksInfo = strBuffer.toString();
			if (disksInfo.length() > 0) {
				disksInfo = disksInfo.substring(0, disksInfo.length() - 1);
			}
			hostInfoList.add(new CLIHostInfoMonitorCondition("CLI-HOSTINFO-9", disksInfo));
		}

		return hostInfoList;
	}

}
