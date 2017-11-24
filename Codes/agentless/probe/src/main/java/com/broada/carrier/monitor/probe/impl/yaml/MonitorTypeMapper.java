package com.broada.carrier.monitor.probe.impl.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.host.cli.HostCLIMonitorPackage;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;
import com.broada.component.utils.error.ErrorUtil;

/**
 * 获取监测器类型
 * 
 * @author WIN
 * 
 */
public class MonitorTypeMapper {
	private static Map<String, List<String>> typeMap = new HashMap<String, List<String>>();
	private static final Logger logger = LoggerFactory.getLogger(MonitorTypeMapper.class);

	// 加载监测器类型到map中
	static {
		ServiceLoader<MonitorPackage> loader = ServiceLoader.load(MonitorPackage.class);
		try {
			for (MonitorPackage pack : loader) {
				if (HostCLIMonitorPackage.class.getSimpleName().equals(pack.getClass().getSimpleName())) {
					List<String> winList = new ArrayList<String>();
					winList.add("CLI-HOSTINFO");
					winList.add("CLI-HOSTCPU");
					winList.add("CLI-DISKSPACE");
					winList.add("CLI-WINDOWSDEVICEIO");
					winList.add("CLI-HOSTMEMORY");
					winList.add("CLI-PROCESS");
					winList.add("CLI-PROCESSSTATE");
					winList.add("WIN-SERVICE");
					winList.add("WIN-LOGONUSER");
					typeMap.put("WINDOWS", winList);
					List<String> linuxList = new ArrayList<String>();
					linuxList.add("CLI-HOSTINFO");
					linuxList.add("CLI-HOSTCPU");
					linuxList.add("CLI-DISKSPACE");
					linuxList.add("CLI-HOSTMEMORY");
					linuxList.add("CLI-PROCESS");
					linuxList.add("CLI-PROCESSSTATE");
					linuxList.add("CLI-LINUXDEVICEIO");
					linuxList.add("CLI-HOSTUSER");
					typeMap.put("LINUX", linuxList);
					List<String> unixList = new ArrayList<String>();
					unixList.add("CLI-HOSTINFO");
					unixList.add("CLI-HOSTCPU");
					unixList.add("CLI-DISKSPACE");
					unixList.add("CLI-HOSTMEMORY");
					unixList.add("CLI-PROCESS");
					unixList.add("CLI-PROCESSSTATE");
					unixList.add("CLI-LINUXDEVICEIO");
					unixList.add("CLI-HOSTUSER");
					typeMap.put("UNIX", unixList);

				} else if (pack.getTypes() != null) {
					for (MonitorType type : pack.getTypes()) {
						String groupId = type.getGroupId().toUpperCase();
						String[] monitorMethodTypeIds = type.getMethodTypeIds();
						boolean isContains = false;
						if (monitorMethodTypeIds != null) {
							for (String methodTypeId : monitorMethodTypeIds) {
								String id = groupId + "-" + methodTypeId.toUpperCase();
								List<String> list = typeMap.get(id);
								if (list == null)
									list = new ArrayList<String>();
								for (String str : list) {
									if (str != null && type != null && str.equalsIgnoreCase(type.getId())) {
										isContains = true;
										break;
									}
								}
								if (!isContains) {
									list.add(type.getId());
									typeMap.put(id, list);
								}
							}
						} else {
							List<String> list = typeMap.get(groupId);
							if (list == null)
								list = new ArrayList<String>();
							for (String str : list) {
								if (str != null && type != null && str.equalsIgnoreCase(type.getId())) {
									isContains = true;
									break;
								}
							}
							if (!isContains) {
								list.add(type.getId());
								typeMap.put(groupId, list);
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "监测任务扩展包加载失败", e);
		}
	}

	public static List<String> getMonitorType(String groupId, String methodType) {
		return typeMap.get(groupId.toUpperCase() + "-" + methodType.toUpperCase());
	}

	public static Map<String, List<String>> getMonitorTypeMap() {
		return typeMap;
	}
}
