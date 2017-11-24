package com.broada.carrier.monitor.probe.impl.yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.impl.generic.ExtParameter;
import com.broada.carrier.monitor.impl.host.cli.HostCLIMonitorPackage;
import com.broada.carrier.monitor.impl.mw.weblogic.WeblogicMonitorPackage;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.method.generator.CliMethodGenerator;
import com.broada.carrier.monitor.probe.impl.method.generator.DB2MethodGenerator;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.probe.impl.util.UUIDUtils;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.numen.agent.script.entity.DynamicParam;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlUtil {
	private static final Logger logger = LoggerFactory.getLogger(YamlUtil.class);
	public static YamlUtil instance = new YamlUtil();
	private static List<MonitorNode> nodeList = new ArrayList<MonitorNode>();
	private static List<MonitorPolicy> policyList = new ArrayList<MonitorPolicy>();
	private static List<MonitorTask> taskList = new ArrayList<MonitorTask>();
	private static Map<String, List<String>> typeMap = new HashMap<String, List<String>>();
	private static List<String> updateTaskIds = new ArrayList<String>();
	private static Map<String, MonitorMethod> monitorMethodMap = new HashMap<String, MonitorMethod>();
	private static Map<String, Set<String>> typeTaskIdMap = new HashMap<String, Set<String>>();
	private static final String DECRYPT_URL = Config.getDefault().getProperty("openapi.decrypt");
	private static ObjectMapper mapper = new ObjectMapper();

	public static YamlUtil getInstance() {
		return instance;
	}

	static {
		loadTypesFromPackage();
		String path = Config.getConfDir() + "/conf.d";
		logger.debug("yaml path: " + path);
		File folder = new File(path);
		if (folder.isDirectory()) {
			File[] files = folder.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith(".yaml"))
						return true;
					return false;
				}
			});
			Yaml yaml = new Yaml();
			for (File file : files) {

				try {
					YamlBean yamlBean = yaml.loadAs(new FileInputStream(file), YamlBean.class);
					String filename = file.getPath();
					String monitorType = file.getName().replace(".yaml", "").toLowerCase();
					Map<String, String> methodTypeMap = new HashMap<String, String>();

					// 添加监测节点
					generateMonitorNode(yamlBean, monitorType, filename);

					// 添加监测方法
					generateMonitorMethods(yamlBean, monitorType, filename, methodTypeMap);

					// 添加监测策略和任务
					generateMonitorPolicyAndTasks(yamlBean, monitorType, file, methodTypeMap);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			}
		}

	}

	private static void generateMonitorNode(YamlBean yamlBean, String monitorType, String filename) {
		List<YamlHost> hosts = yamlBean.getHosts();
		for (YamlHost host : hosts) {
			String ip = host.getIp();
			if (!IPUtil.isValidate(ip)) {
				logger.warn("文件{}语法错误, host_list标签列表ip属性不能为空", filename);
				continue;
			}
			if (HostIpUtil.getLocalHost().equals(ip))
				throw new RuntimeException("文件：" + filename + "里的hosts标签中的ip不能配置成" + HostIpUtil.getLocalHost());
			MonitorNode probeNode = new MonitorNode();
			if (StringUtils.isNullOrBlank(host.getId())) {
				String domain = "default";
				String resourceType = "server";
				// 根据域+资源类型+IP生成唯一ID,资源类型暂时写死
				String id = UUIDUtils.generateId(domain + resourceType + ip);
				probeNode.setId(id);
			} else {
				probeNode.setId(host.getId());
			}
			probeNode.setHost(host.getHost());
			probeNode.setIp(ip);
			probeNode.setName(ip);
			probeNode.setModified(System.currentTimeMillis());
			String tags = host.getTags();
			// derby与probe集成，探针Id都设为0
			probeNode.setProbeId(0);
			if ("netdev".equals(host.getOs()))
				probeNode.setTypeId(ResourceType.NETWORK.getCode());
			else if ("Router".equals(host.getOs())) {
				probeNode.setTypeId(ResourceType.NETWORK.getCode());
				if (tags != null)
					tags = tags + ";equipment:Router";
				else
					tags = "equipment:Router";
			} else if ("Switch".equals(host.getOs())) {
				probeNode.setTypeId(ResourceType.NETWORK.getCode());
				if (tags != null)
					tags = tags + ";equipment:Switch";
				else
					tags = "equipment:Switch";
			} else
				probeNode.setTypeId(StringUtils.isNullOrBlank(host.getType())? "": ResourceType.checkByCode(host.getType()).getCode());
			// 默认domainId
			probeNode.setDomainId("rootDomain");
			probeNode.setOs(host.getOs());
			if (StringUtils.isNotNullAndTrimBlank(tags))
				probeNode.setTags(tags);
			host.setTags(tags);
			nodeList.add(probeNode);

		}
	}

	private static void generateMonitorMethods(YamlBean yamlBean, String monitorType, String filename,
			Map<String, String> methodTypeMap) {
		List<Map<String, Object>> methods = yamlBean.getCollect_methods();
		for (Map<String, Object> methodMap : methods) {
			String type = (String) methodMap.get("type");
			if(monitorType.equals("icmp"))
				type = "ProtocolIcmp";
			if (!monitorType.equals("icmp") && !StringUtils.isNotNullAndTrimBlank(type)) {
				logger.warn("文件{}语法错误, collect_method标签列表type属性不能为空, 请保持默认值", filename);
				continue;
			}
			String name = (String) methodMap.get("name");
			if (!StringUtils.isNotNullAndTrimBlank(name)) {
				logger.warn("文件{}语法错误, collect_method标签列表name属性不能为空", filename);
				continue;
			}
			MonitorMethod probeMethod = new MonitorMethod();
			methodTypeMap.put(name, type);
			String codeName = monitorType + type + name;
			String code = UUIDUtils.generateId(codeName);
			probeMethod.setCode(code);
			// yaml 文件密码解密
			decrypt(methodMap);
			probeMethod.setProperties(methodMap);
			probeMethod.setModified(System.currentTimeMillis());
			probeMethod.setName(name);
			probeMethod.setTypeId(type);
			monitorMethodMap.put(code, probeMethod);
		}
	}

	private static void generateMonitorPolicyAndTasks(YamlBean yamlBean, String monitorType, File file,
			Map<String, String> methodTypeMap) {
		List<YamlHost> hosts = yamlBean.getHosts();
		for (YamlHost host : hosts) {
			if (!IPUtil.isValidate(host.getIp()))
				continue;
			String methodName = host.getCollect_method();
			if (methodName == null)
				continue;
			String type = methodTypeMap.get(methodName);
			if (type == null) {
				logger.warn("请确保文件" + file.getPath() + "host列表中的collect_method属性与collect_methods中name属性一致");
				continue;
			}
			monitorType = file.getName().replace(".yaml", "").toLowerCase();
			String methodCode = UUIDUtils.generateId(monitorType + type + methodName);
			MonitorMethod method = monitorMethodMap.get(methodCode);

			if (method != null) {
				if (("ProtocolCcli".equalsIgnoreCase(method.getTypeId()) || ("ProtocolDb2".equalsIgnoreCase(method
						.getTypeId()) && StringUtils.isNotNullAndTrimBlank((String) method.getProperties().get("sysname"))))
						&& !"dynamic_monitor".equalsIgnoreCase(monitorType) && !monitorType.equalsIgnoreCase("iis")) {
					MonitorNode node = new MonitorNode(host.getIp());
					String filename = file.getPath();
					CLIMonitorMethodOption option = new CLIMonitorMethodOption(method);
					// cli监控主机要做特殊处理
					if ("ProtocolDb2".equalsIgnoreCase(method.getTypeId())) {
						// db2的不需要修改
					} else if ("windows".equalsIgnoreCase(option.getSysname()))
						monitorType = "windows";
					else if ("linux".equalsIgnoreCase(option.getSysname()))
						monitorType = "linux";
					else
						monitorType = "unix";

					option = CliMethodGenerator.getInstance().getOptions(node, filename, option.getSessionName(),
							option.getSysname(), option.getRemotePort(), option.getLoginName(), option.getPassword(),
							option.getPrompt(), option.getLoginTimeout());
					logger.debug("获取远程主机操作系统：{}, 系统版本: {}", option, option.getSysversion());
					// 修改为自动获取sysversion
					// 处理同一个yaml文件cli监测方法复用问题
					if (option != null) {
						MonitorMethod method1 = new MonitorMethod(method);
						method1.getProperties().set("sysversion", option.getSysversion());
						methodCode = UUIDUtils.generateId(monitorType + type + methodName + host.getIp());
						method1.setCode(methodCode);
						monitorMethodMap.put(methodCode, method1);
					}
					// 自动获取db2 version

					if ("db2".equalsIgnoreCase(monitorType)) {
						MonitorMethod method1 = new MonitorMethod(method);
						DB2MonitorMethodOption op = new DB2MonitorMethodOption(method1);
						method1 = DB2MethodGenerator.getInstance().getDB2MonitorMethodOption(node, op);
						method1.setCode(methodCode);
						monitorMethodMap.put(methodCode, method1);
					}

				} else if ("weblogic".equalsIgnoreCase(file.getName().replace(".yaml", ""))) {
					if ("ProtocolSnmp".equalsIgnoreCase(method.getTypeId())) {
						monitorType = "weblogic-snmp";
					} else if ("ProtocolWLAgent".equalsIgnoreCase(method.getTypeId())) {
						monitorType = "weblogic-agent";
					}
				} else if ("oracle".equalsIgnoreCase(file.getName().replace(".yaml", ""))) {
					if ("ProtocolOracle".equalsIgnoreCase(method.getTypeId())) {
						monitorType = "oracle";
					} else if ("ProtocolOracleRAC".equalsIgnoreCase(method.getTypeId())) {
						monitorType = "oracle-rac";
					}
				}

			}

			if (method == null)
				continue;
			// 添加监测策略
			// 默认监测间隔 60 s
			int interval = method.getProperties().get("interval", 60);
			int errorInterval = interval / 2;
			MonitorPolicy policy = new MonitorPolicy();
			String code = UUIDUtils.generateId(monitorType + host.getIp() + methodName);
			policy.setCode(code);
			policy.setName(methodName);
			policy.setInterval(interval);
			// 错误的监测间隔最小设为60 s
			policy.setErrorInterval(errorInterval > 60 ? errorInterval : 60);
			policy.setModified(System.currentTimeMillis());
			policyList.add(policy);

			// 添加监测任务

			List<String> typeIds = typeMap.get(monitorType.toUpperCase());
			if (typeIds != null) {
				for (String typeId : typeIds) {
					if (host.getExcludeList().contains(typeId))
						continue;
					// 应用类型+主机IP+方法类型+监测器ID+methodCode
					String id = UUIDUtils.generateId(monitorType + host.getIp() + type + typeId + methodCode);
					MonitorTask probeTask = new MonitorTask();
					probeTask.setId(id);
					probeTask.setEnabled(true);
					String policyCode = UUIDUtils.generateId(monitorType + host.getIp() + host.getCollect_method());
					probeTask.setPolicyCode(policyCode);
					probeTask.setModified(new Date());
					if (StringUtils.isNullOrBlank(host.getId())) {
						String domain = "default";
						String resourceType = "server";
						// 根据域+资源类型+IP生成唯一ID,资源类型暂时写死
						String hostId = UUIDUtils.generateId(domain + resourceType + host.getIp());
						probeTask.setNodeId(hostId);
					} else {
						probeTask.setNodeId(host.getId());
					}

					probeTask.setMethodCode(methodCode);
					probeTask.setName(monitorType + host.getIp() + typeId);
					probeTask.setTypeId(typeId);
					String hostname = host.getHost();
					if (StringUtils.isNullOrBlank(hostname))
						hostname = host.getIp();
					probeTask.setHost(hostname);
					probeTask.setTags(host.getTags());
					probeTask.setIp(host.getIp());
					// 解析通用监测器的配置
					if (host.getDynamic_properties() != null) {
						YamlDynamicMap dynamicMap = host.getDynamic_properties();
						String path = dynamicMap.getScriptPath();
						Map<String, Object> prop = dynamicMap.getProperties();
						ExtParameter extParameter = new ExtParameter();
						if (path == null)
							continue;
						File groovyFile = null;
						if (path.endsWith(".groovy")) {
							groovyFile = new File(System.getProperty("user.dir") + "/" + path);
							extParameter.setScriptFilePath(path);
						} else {
							groovyFile = new File(System.getProperty("user.dir") + "/" + path, typeId.toLowerCase()
									+ ".groovy");
							extParameter.setScriptFilePath(path + "/" + typeId.toLowerCase() + ".groovy");
						}
						if (!groovyFile.exists())
							throw new RuntimeException(String.format("监测脚本文件%s丢失", groovyFile.getPath()));
						DynamicParam[] dynamicParams = null;
						if (prop != null) {
							dynamicParams = new DynamicParam[prop.size()];
							int index = 0;
							for (Entry<String, Object> entry : prop.entrySet()) {
								if (entry.getKey() != null && entry.getValue() != null) {
									dynamicParams[index] = new DynamicParam(entry.getKey(), entry.getKey(), false);
									dynamicParams[index].setValue(entry.getValue());
									index++;
								}
							}
						}
						if (dynamicParams != null)
							extParameter.setParams(dynamicParams);
						probeTask.setParameterObject(extParameter);
					}
					// icmp特殊处理
//					if(typeId.equalsIgnoreCase("icmp")){   
//						MonitorMethod method_icmp = monitorMethodMap.get(methodCode);
//						Map<String, Object> map = new HashMap<String, Object>();
//						map.put("timeout", method_icmp.getProperties().get("time_out"));
//						map.put("requestCount", method_icmp.getProperties().get("count"));
//						probeTask.setParameterObject(map);
//					}
					taskList.add(probeTask);
					Set<String> set = typeTaskIdMap.get(id);
					if (set == null)
						set = new HashSet<String>();
					set.add(id);
					typeTaskIdMap.put(monitorType.toUpperCase(), set);
					updateTaskIds.add(id);
				}
			}
		}
	}

	private static void loadTypesFromPackage() {

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
					unixList.add("CLI-NETSTAT");
					typeMap.put("UNIX", unixList);

				} else if (WeblogicMonitorPackage.class.getSimpleName().equals(pack.getClass().getSimpleName())) {
					List<String> snmpList = new ArrayList<String>();
					snmpList.add("WEBLOGIC-WLS-WLEC");
					snmpList.add("WEBLOGIC-WLSTHREAD");
					snmpList.add("WEBLOGIC-WLSSERVLET");
					snmpList.add("WEBLOGIC-WLSJVM");
					snmpList.add("WEBLOGIC-WLSJDBC");
					snmpList.add("WEBLOGIC-WLSJTA");
					snmpList.add("WEBLOGIC-WLSWEBAPP-SNMP");
					typeMap.put("WEBLOGIC-SNMP", snmpList);
					List<String> agentList = new ArrayList<String>();
					agentList.add("WEBLOGIC-WLSEJB");
					agentList.add("WEBLOGIC-WLSWEBAPP");
					agentList.add("WEBLOGIC-WLSBASIC");
					agentList.add("WEBLOGIC-WLS-STATUS");
					agentList.add("WEBLOGIC-WLS-SERVLET");
					agentList.add("WEBLOGIC-WLS-JVM");
					agentList.add("WEBLOGIC-WLS-JDBC");
					agentList.add("WEBLOGIC-WLS-CLUSTER");
					agentList.add("WEBLOGIC-WLS-SERVER");
					agentList.add("WEBLOGIC-WLS-THREAD");
					agentList.add("WEBLOGIC-WLS-SUBSYSTEM");
					typeMap.put("WEBLOGIC-AGENT", agentList);

				} else if (pack.getTypes() != null) {
					for (MonitorType type : pack.getTypes()) {
						boolean isContains = false;
						List<String> list = typeMap.get(type.getGroupId().toUpperCase());
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
							typeMap.put(type.getGroupId().toUpperCase(), list);
						}
					}
				}
			}
			List<String> oracleList = typeMap.get("ORACLE");
			oracleList.remove("ORACLE-RAC");
			typeMap.put("ORACLE", oracleList);
			List<String> oracleRacList = new ArrayList<String>();
			oracleRacList.add("ORACLE-RAC");
			typeMap.put("ORACLE-RAC", oracleRacList);
			// oracle12c扩展
			typeMap.put("ORACLE12C", oracleList);
			typeMap.put("ORACLE12C-RAC", oracleRacList);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "监测任务扩展包加载失败", e);
		}
	}

	public List<MonitorNode> getAllMonitorNode() {
		logger.info("node信息: " + nodeList);
		return nodeList;
	}

	public List<MonitorPolicy> getAllMonitorPolicy() {
		return policyList;
	}

	public List<MonitorMethod> getAllMonitorMethod() {
		List<MonitorMethod> list = new ArrayList<MonitorMethod>();
		list.addAll(monitorMethodMap.values());
		return list;
	}

	public List<MonitorTask> getAllMonitorTask() {
		return taskList;
	}

	public List<String> getUpdateTaskIds() {
		return updateTaskIds;
	}

	public Map<String, List<String>> getTypeMap() {
		return typeMap;
	}

	public Map<String, Set<String>> getTypeTaskIdsMap() {
		return typeTaskIdMap;
	}

	/**
	 * yaml 文件密码解密
	 * @param methodMap
	 */
	public static void decrypt(Map<String, Object> methodMap){
		if(methodMap != null && methodMap.get("password") != null){
			String pwd = (String)methodMap.get("password");
			try {
				if(StringUtils.isNotNullAndBlank(pwd)){
					if(pwd.startsWith(">>>") && pwd.endsWith("<<<")){
						// 加密过的
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("encryptPwd", pwd);
						String password = HTTPClientUtils.post(DECRYPT_URL, mapper.writeValueAsString(map));
						if(StringUtils.isNotNullAndBlank(password)){
							methodMap.put("password",password);
						}
					}else{
						// 未加密的
						methodMap.put("password",pwd);
					}
				}
			} catch (Exception e) {
				logger.error("密码解密异常", e);
			}
		}
	}
}
