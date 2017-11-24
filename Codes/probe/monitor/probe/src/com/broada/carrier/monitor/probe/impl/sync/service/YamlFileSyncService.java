package com.broada.carrier.monitor.probe.impl.sync.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.impl.generic.ExtParameter;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.dispatch.MonitorDispatcher;
import com.broada.carrier.monitor.probe.impl.dispatch.MonitorResultUploader;
import com.broada.carrier.monitor.probe.impl.method.generator.CliMethodGenerator;
import com.broada.carrier.monitor.probe.impl.method.generator.DB2MethodGenerator;
import com.broada.carrier.monitor.probe.impl.sync.entity.FileList;
import com.broada.carrier.monitor.probe.impl.util.FileUtil;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.broada.carrier.monitor.probe.impl.util.Md5Util;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.probe.impl.util.UUIDUtils;
import com.broada.carrier.monitor.probe.impl.yaml.ResourceType;
import com.broada.carrier.monitor.probe.impl.yaml.YamlBean;
import com.broada.carrier.monitor.probe.impl.yaml.YamlDynamicMap;
import com.broada.carrier.monitor.probe.impl.yaml.YamlHost;
import com.broada.carrier.monitor.probe.impl.yaml.YamlUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.numen.agent.script.entity.DynamicParam;
import com.broada.utils.StringUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlFileSyncService {
	
	private static Logger LOG = LoggerFactory.getLogger(YamlFileSyncService.class);
	
	public static final String FILE_LIST_URL = Config.getDefault().getProperty("agentapi.config.list") + "&id="
			+ UUIDUtils.getProbeId();
	public static String FILE_CONTENT_URL = Config.getDefault().getProperty("agentapi.config.file") + "&id="
			+ UUIDUtils.getProbeId();
	private static final String CHAR_SET = "UTF-8";
	private static final Logger logger = LoggerFactory.getLogger(YamlFileSyncService.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private static YamlFileSyncService instance = new YamlFileSyncService();
	private static Map<String, Set<String>> typeTaskIdsMap = YamlUtil.getInstance().getTypeTaskIdsMap();
	private static Map<String, List<String>> typeMap = YamlUtil.getInstance().getTypeMap();
	private static Yaml yaml = new Yaml();
	@Autowired
	private ProbeServiceFactory probeFactory;

	public static YamlFileSyncService getInstance() {
		return instance;
	}

	private List<FileList> getSyncFileList() {
		try {
			String content = HTTPClientUtils.get(FILE_LIST_URL, CHAR_SET);
			JavaType type = mapper.getTypeFactory().constructParametricType(List.class, FileList.class);
			List<FileList> list = mapper.readValue(content, type);
			return list;
		} catch (Exception e) {
			logger.warn("从服务端获取更新的yaml文件列表失败:", e);
		}
		return new ArrayList<FileList>();
	}

	public void makeFile() {
		List<FileList> list = getSyncFileList();
		for (FileList fileList : list) {
			String name = fileList.getFileName();
			if(fileList.isEnabled()){
				// 处理ip校验，处理结束后再进行修改
				Set<String> ips = fileList.getIpList();
				MonitorResultUploader.getDefault().addVerifyStat(name, ips);
			}
			File file = new File(Config.getYamlDir() + File.separator + name + ".yaml");
			File file_bak = new File(Config.getYamlDir() + File.separator + fileList.getFileName() + ".yaml.disabled");
			String sever_md5 = fileList.getMd5();
			boolean enabled = fileList.isEnabled();
			boolean deleted = fileList.isDeleted();
			try{
				if (file.exists()) {    //  有yaml文件
					resolveFile(sever_md5, fileList, enabled, deleted, file, file_bak, name + ".yaml", true);
				} else if(file_bak.exists()){     // 存在备份文件
					resolveFile(sever_md5, fileList, enabled, deleted, file, file_bak, name + ".yaml", false);
				} else {       //什么都没有直接下载
					String content = getYamlFileContent(name);
					if(StringUtil.isNullOrBlank(content)) continue;
					if(!enabled){
						logger.info("直接更新备份文件:" + name + ".yaml.disabled");
						makeFile(name + ".yaml.disabled", content);
					}else{
						logger.info("直接更新配置文件:" + name + ".yaml");
						updateTask(fileList.getFileName().toLowerCase(), content);
						makeFile(name + ".yaml", content);
					}
				}
			}catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
	}

	public void sync() {
		makeFile();

	}

	private String getYamlFileContent(String fileName) {
		try {
			String url = FILE_CONTENT_URL + "&name=" + fileName + ".yaml";
			String content = HTTPClientUtils.get(url, CHAR_SET);
			return content;
		} catch (Exception e) {
			logger.warn("从服务端获取yaml文件内容失败: ", e);
		}
		return null;
	}

	private void makeFile(String fileName, String content) {
		try {
			File file = new File(Config.getYamlDir() + "/" + fileName);
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), CHAR_SET);
			out.write(content);
			out.close();
		} catch (Exception e) {
			logger.warn("更新{}配置文件失败, 异常:{}", fileName, e);
		}
	}

	@SuppressWarnings("unused")
	private void makeFile(File file, String content) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), CHAR_SET);
			out.write(content);
			out.close();
		} catch (Exception e) {
			logger.warn("更新{}配置文件失败, 异常:{}", file.getName(), e);
		}
	}

	private void disableExistsTaskByType(String monitorType) {
		Set<String> set = typeTaskIdsMap.get(monitorType.toUpperCase());
		if (set != null) {
			for (String taskId : set) {
				if (taskId != null) {
					MonitorTask task = probeFactory.getTaskService().getTask(taskId);
					if(task != null){
						task.setEnabled(false);
						probeFactory.getTaskService().saveTask(task, null, new MonitorRecord(task.getId()));
					}
				}
			}
		}
	}

	private void updateTask(String monitorType, String content) {
		disableExistsTaskByType(monitorType);
		YamlBean bean = yaml.loadAs(content, YamlBean.class);
		updateMonitorNode(bean, monitorType);
		Map<String, String> methodTypeMap = new HashMap<String, String>();
		Map<String, MonitorMethod> methodMap = updateMonitorMethods(bean, monitorType, methodTypeMap);
		updateMonitorPolicyAndTasks(bean, monitorType, methodTypeMap, methodMap);

	}

	private void updateMonitorNode(YamlBean yamlBean, String monitorType) {
		List<MonitorNode> nodeList = new ArrayList<MonitorNode>();
		List<YamlHost> hosts = yamlBean.getHosts();
		for (YamlHost host : hosts) {
			String ip = host.getIp();
			if (!IPUtil.isValidate(ip)) {
				logger.warn("从服务端更新{}文件错误, ip字段非法", monitorType + ".yaml");
				continue;
			}
			if(HostIpUtil.getLocalHost().equals(ip))
				throw new RuntimeException("文件："+monitorType+".yaml"+"里的hosts标签中的ip不能配置成"+HostIpUtil.getLocalHost());
			// 添加resourceType校验
			if(!StringUtil.isNullOrBlank(host.getType()) &&
					!host.getType().equals(ResourceType.SERVER.getCode()) &&
					!host.getType().equals(ResourceType.VM.getCode()) &&
					!host.getType().equals(ResourceType.NETWORK.getCode()) &&
					!host.getType().equals(ResourceType.MINISERVER.getCode()) &&
					!host.getType().equals(ResourceType.DISKARRAY.getCode()) &&
					!host.getType().equals(ResourceType.NAS.getCode())){
				throw new RuntimeException("文件：" + monitorType + ".yaml" + "里的hosts标签中的type暂不支持" 
						+ host.getType());
			}
			MonitorNode probeNode = new MonitorNode();
			if (StringUtils.isNullOrBlank(host.getId())) {
				String domain = "default";
				String resourceType = "server";
				// 根据域+资源类型+IP生成唯一ID,资源类型暂时写死
				String id = UUIDUtils.generateId(domain + resourceType + ip);
				probeNode.setId(id);
			}else{
				probeNode.setId(host.getId());
			}
			probeNode.setIp(ip);
			probeNode.setHost(host.getHost());
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
				probeNode.setTypeId(host.getType());   // 优先使用操作系统，区分网络设备，如果都不是的话，再以资源类型作为标识
			// 默认domainId
			probeNode.setDomainId("rootDomain");
			probeNode.setOs(host.getOs());
			if (StringUtils.isNotNullAndTrimBlank(tags))
				probeNode.setTags(tags);
			host.setTags(tags);
			nodeList.add(probeNode);
		}
		for (MonitorNode node : nodeList) {
			probeFactory.getNodeService().saveNode(node);
		}

	}

	private Map<String, MonitorMethod> updateMonitorMethods(YamlBean yamlBean, String monitorType,
			Map<String, String> methodTypeMap) {
		Map<String, MonitorMethod> monitorMethodMap = new HashMap<String, MonitorMethod>();
		List<Map<String, Object>> methods = yamlBean.getCollect_methods();
		for (Map<String, Object> methodMap : methods) {
			String type = (String) methodMap.get("type");
			if(monitorType.equals("icmp")){   //  icmp 特殊处理
				type = "ProcotolIcmp";
			}
			if (!StringUtils.isNotNullAndTrimBlank(type)) {
				logger.warn("从服务端获取{}更新文件错误, collect_method标签列表type属性不能为空 ", monitorType + ".yaml");
				continue;
			}
			String name = (String) methodMap.get("name");
			if (!StringUtils.isNotNullAndTrimBlank(name)) {
				logger.warn("从服务端获取{}更新文件错误, collect_method标签列表name属性不能为空", monitorType + ".yaml");
				continue;
			}
			MonitorMethod probeMethod = new MonitorMethod();
			methodTypeMap.put(name, type);
			String codeName = monitorType + type + name;
			String code = UUIDUtils.generateId(codeName);
			probeMethod.setCode(code);
			// yaml 文件密码解密
			YamlUtil.decrypt(methodMap);
			probeMethod.setProperties(methodMap);
			probeMethod.setModified(System.currentTimeMillis());
			probeMethod.setName(name);
			probeMethod.setTypeId(type);
			monitorMethodMap.put(code, probeMethod);
		}
		for (MonitorMethod method : monitorMethodMap.values()) {
			probeFactory.getMethodService().saveMethod(method);
		}
		return monitorMethodMap;
	}

	private void updateMonitorPolicyAndTasks(YamlBean yamlBean, String monitorType, Map<String, String> methodTypeMap,
			Map<String, MonitorMethod> monitorMethodMap) {
		List<YamlHost> hosts = yamlBean.getHosts();
		List<MonitorPolicy> policyList = new ArrayList<MonitorPolicy>();
		List<MonitorTask> taskList = new ArrayList<MonitorTask>();
		for (YamlHost host : hosts) {
			if (!IPUtil.isValidate(host.getIp()))
				continue;
			String methodName = host.getCollect_method();
			if (methodName == null)
				continue;
			String type = methodTypeMap.get(methodName);
			if (type == null) {
				logger.warn("从服务端更新{}文件host列表中的collect_method属性与collect_methods中name属性一致", monitorType + ".yaml");
				continue;
			}
			// 防止出现相同监测方法名称添加任务失败
			String monitorType1 = monitorType;
			String methodCode = UUIDUtils.generateId(monitorType1 + type + methodName);
			MonitorMethod method = monitorMethodMap.get(methodCode);
			if (method != null) {
				if (("ProtocolCcli".equalsIgnoreCase(method.getTypeId())
						|| ("ProtocolDb2".equalsIgnoreCase(method.getTypeId()) && StringUtils
								.isNotNullAndTrimBlank((String) method.getProperties().get("sysname"))))
								&& !"dynamic_monitor".equalsIgnoreCase(monitorType) && !monitorType.equalsIgnoreCase("iis")) {
					MonitorNode node = new MonitorNode(host.getIp());
					CLIMonitorMethodOption option = new CLIMonitorMethodOption(method);
					// cli监控主机要做特殊处理
					if ("ProtocolDb2".equalsIgnoreCase(method.getTypeId())) {
						// db2的不需要修改
					} else if ("windows".equalsIgnoreCase(option.getSysname()))
						monitorType1 = "windows";
					else if ("linux".equalsIgnoreCase(option.getSysname()))
						monitorType1 = "linux";
					else
						monitorType1 = "unix";

					option = CliMethodGenerator.getInstance().getOptions(node, monitorType + ".yaml",
							option.getSessionName(), option.getSysname(), option.getRemotePort(),
							option.getLoginName(), option.getPassword(), option.getPrompt(), option.getLoginTimeout());
					logger.debug("获取远程主机操作系统：{}, 系统版本: {}", option, option.getSysversion());
					// 修改为自动获取sysversion
					if (option != null) {
						MonitorMethod method1 = new MonitorMethod(method);
						method1.getProperties().set("sysversion", option.getSysversion());
						methodCode = UUIDUtils.generateId(monitorType + type + methodName + host.getIp());
						method1.setCode(methodCode);
						monitorMethodMap.put(methodCode, method1);
						probeFactory.getMethodService().saveMethod(method1);
					}
					// 自动获取db2 version

					if ("db2".equalsIgnoreCase(monitorType1)) {
						MonitorMethod method1 = new MonitorMethod(method);
						DB2MonitorMethodOption op = new DB2MonitorMethodOption(method1);
						method1 = DB2MethodGenerator.getInstance().getDB2MonitorMethodOption(node, op);
						method1.setCode(methodCode);
						probeFactory.getMethodService().saveMethod(method1);
						monitorMethodMap.put(methodCode, method1);
					}

				} else if ("weblogic".equalsIgnoreCase(monitorType1)) {
					if ("ProtocolSnmp".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "weblogic-snmp";
					} else if ("ProtocolWLAgent".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "weblogic-agent";
					}
				} else if("oracle".equalsIgnoreCase(monitorType1)){
					if ("ProtocolOracle".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "oracle";
					} else if ("ProtocolOracleRAC".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "oracle-rac";
					}
				} else if("oracle12c".equalsIgnoreCase(monitorType1)){
					if ("ProtocolOracle".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "oracle12c";
					} else if ("ProtocolOracleRAC".equalsIgnoreCase(method.getTypeId())) {
						monitorType1 = "oracle12c-rac";
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
			String code = UUIDUtils.generateId(monitorType1 + host.getIp() + methodName);
			policy.setCode(code);
			policy.setName(methodName);
			policy.setInterval(interval);
			// 错误的监测间隔最小设为60 s
			policy.setErrorInterval(errorInterval > 60 ? errorInterval : 60);
			policy.setModified(System.currentTimeMillis());
			policyList.add(policy);

			// 添加监测任务
			List<String> typeIds = typeMap.get(monitorType1.toUpperCase());
			if (typeIds != null) {
				for (String typeId : typeIds) {
					if (host.getExcludeList().contains(typeId))
						continue;
					String id = UUIDUtils.generateId(monitorType1 + host.getIp() + type + typeId);
					MonitorTask probeTask = new MonitorTask();
					probeTask.setId(id);
					probeTask.setEnabled(true);
					String policyCode = UUIDUtils.generateId(monitorType1 + host.getIp() + host.getCollect_method());
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
					probeTask.setName(monitorType1 + host.getIp() + typeId);
					probeTask.setTypeId(typeId);
					String hostname=host.getHost();
					if(StringUtils.isNullOrBlank(hostname))
						hostname=host.getIp();
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
						if (path.endsWith(".groovy")){
							groovyFile = new File(System.getProperty("user.dir") + "/" + path);
							extParameter.setScriptFilePath(path);
						}else{
							groovyFile = new File(System.getProperty("user.dir") + "/" + path, typeId.toLowerCase()
									+ ".groovy");
							extParameter.setScriptFilePath(path+"/"+typeId.toLowerCase()
									+ ".groovy");
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
				}
			}
		}
		for (MonitorPolicy policy : policyList) {
			probeFactory.getPolicyService().savePolicy(policy);
		}
		for (MonitorTask task : taskList) {
			probeFactory.getTaskService().saveTask(task, null, new MonitorRecord(task.getId()));
			MonitorDispatcher.getDefault().scheduleQueue(task);
		}
	}
	
	/**
	 * 备份文件
	 * @param src 待备份文件
	 * @param dst 备份文件
	 */
	private void backupFile(File src, File dst){
		try {
			FileUtils.copyFile(src, dst);
			FileUtils.forceDelete(src);
		} catch (IOException e) {
			logger.error("配置文件/备份文件转换过程发生异常," + e.getMessage());
		}
	}
	
	/**
	 * 适配 yaml 和  yaml.bak 文件
	 * @param sever_md5
	 * @param fileList
	 * @param enabled
	 * @param deleted
	 * @param file
	 * @param file_bak
	 * @param name
	 * @param exist_yaml 是否存在yaml文件
	 */
	private void resolveFile(String sever_md5, FileList fileList, boolean enabled, boolean deleted, 
			File file, File file_bak, String name, boolean exist_yaml){
		File target = null;
		String probe_md5 = "";
		if(exist_yaml){
			probe_md5 = Md5Util.digest(file);
			target = file;
		}
		else{
			probe_md5 = Md5Util.digest(file_bak);
			target = file_bak;
		}
		if(deleted && target.exists()){ // 删除文件
			if(exist_yaml){ // 存在配置文件则先去掉task 
				deleteTaskByType(name.substring(0, name.lastIndexOf(".")));
				logger.info("删除配置文件:" + name);
			}
			target.delete();
		}
		if (probe_md5 != null && probe_md5.equals(sever_md5)) {  // 内容没变只改变状态
			if(exist_yaml && !enabled){    // 存在配置文件但是禁用 
				// 更新节点情况
				deleteTaskByType(name.substring(0, name.lastIndexOf(".")));
				backupFile(file, file_bak);
				logger.warn("禁用文件:" + name);
			} else if(!exist_yaml && enabled){   // 存在备份文件但是启用
				backupFile(file_bak, file);
				updateTask(name.substring(0, name.lastIndexOf(".")), FileUtil.getFileContent(file));
				logger.info("启用文件:" + name + ".disabled");
			}
		} else {       //  内容改变直接下载
			String content = getYamlFileContent(name.substring(0, name.lastIndexOf(".")));
			if(StringUtil.isNullOrBlank(content)) return ;
			if(!enabled){
				if(exist_yaml)
					deleteTaskByType(name.substring(0, name.lastIndexOf(".")));
				if(file.exists());
					file.delete();
				makeFile(name + ".disabled", content);   // 备用文件发生变化和task无关
				logger.info("更新了备份文件: " + name + ".disabled");
			} else{
				if(exist_yaml)
					deleteTaskByType(name.substring(0, name.lastIndexOf(".")));
				if(file_bak.exists())
					file_bak.delete();
				updateTask(name.substring(0, name.lastIndexOf(".")), content);
				makeFile(name, content);
				logger.info("更新了配置文件:" + name);
			}
		}
	}
	
	/**
	 * 删除数据库中的任务和相关节点，防止节点资源会继续上报<p>
	 * 防止出现两个监测任务监测同一个主机的误删除，所以要做两次检验，已确定是否真的删除监测节点
	 * @param pluginName
	 */
	public void deleteTaskByType(String pluginName){
		Set<String> nodeIds_del = new HashSet<String>();
		MonitorTask[] tasks = probeFactory.getTaskService().getTasks();
		if(tasks != null && tasks.length > 0)
			for(MonitorTask task:tasks)
				if(task.getTypeId().toLowerCase().contains(pluginName.toLowerCase())){
					nodeIds_del.add(task.getNodeId());
					probeFactory.getTaskService().deleteTask(task.getId());
				}
		boolean del = true;
		if(!nodeIds_del.isEmpty()){
			tasks = probeFactory.getTaskService().getTasks();
			for(String nodeId:nodeIds_del){
				for(MonitorTask task:tasks)
					if(task.getNodeId().equalsIgnoreCase(nodeId)){
						// 还有其他任务监测该节点，则该节点不删除
						del = false;
						break;
					}
				if(del)
					probeFactory.getNodeService().deleteNode(nodeId);
			}
		}
	}
	
}
