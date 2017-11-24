package uyun.bat.agent.impl.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.AgentConfigDetail;
import uyun.bat.agent.api.entity.AgentConfigDetailHost;
import uyun.bat.agent.api.entity.HostCheckStatus;
import uyun.bat.agent.api.entity.YamlBean;
import uyun.bat.agent.api.entity.YamlHost;
import uyun.bat.agent.impl.autosync.common.Md5Util;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.agent.api.entity.FileCharSet;
import uyun.bat.agent.api.service.YamlFileService;
import uyun.bat.agent.impl.logic.LogicManager;
import uyun.bat.agent.impl.util.YamlUtil;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.common.utils.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.whale.common.util.text.DateUtil;

@Service(protocol = "dubbo")
public class YamlFileServiceImpl implements YamlFileService {

	private Logger LOG = LoggerFactory.getLogger(YamlFileServiceImpl.class);

	private static final String AGENT_PLUGIN_ACTIVE = "active";
	private static final String AGENT_PLUGIN_INACTIVE = "inactive";


	@Override
	public boolean upload(String tenantId, List<YamlFile> yamlFiles) {

		for (YamlFile yamlFile : yamlFiles) {
			yamlFile.setModified(new Date());
			yamlFile.setTenantId(tenantId);
			int size = 0;
			String content = yamlFile.getContent();
			if (StringUtils.isNotNullAndTrimBlank(content)) {
				//保存数据时去除最后的空行
				while(content.endsWith("\n")){
					content=content.substring(0, content.lastIndexOf("\n"));
				}
				yamlFile.setContent(content);
				size = content.getBytes(FileCharSet.DEFAULT_CHARSET).length;
			}
			String md5 = Md5Util.digest(yamlFile);
			yamlFile.setMd5(md5);
			yamlFile.setSize(size);
			yamlFile.setEnabled(true);
			LogicManager.getInstance().getYamlFileLogic().save(yamlFile);
		}

		return true;
	}

	@Override
	public String getYamlContent(String tenantId, String agentId, String fileName, String source) {
		return LogicManager.getInstance().getYamlFileLogic().getYamlContent(tenantId, agentId, fileName, source);
	}

	@Override
	public List<String> getAllYamlName(String tenantId, String source) {
		return LogicManager.getInstance().getYamlFileLogic().getAllYamlName(tenantId, source);
	}

	@Override
	public void updateEnabled(String tenantId, String agentId, String fileName, String source, boolean enabled) {
		boolean flag = LogicManager.getInstance().getYamlFileLogic().updateEnabled(tenantId, agentId, fileName, source, enabled);
		if (!flag) {
			// 一般不会更新失败, 假如更新失败返回前端一个提示
			throw new IllegalArgumentException("update failed!");
		}
		// 由禁用转启用
		// 若已配置主机则状态改为未验证
		if (enabled) {
			YamlFile file = LogicManager.getInstance().getYamlFileLogic()
					.getYamlFileByNameAndAgentId(tenantId, agentId, fileName, source);
			changeYamlHostStatus(file, HostCheckStatus.UNCHECKED, tenantId);
		}
	}

	private void changeYamlHostStatus(YamlFile file, HostCheckStatus status, String tenantId) {
		if (file == null) {
			return;
		}
		String content = file.getContent();
		if (content != null) {
			YamlBean bean = YamlUtil.customYamlLoad(content);
			if (bean != null && bean.getHosts() != null && bean.getHosts().size() > 0) {
				boolean modified = false;
				for(YamlHost host : bean.getHosts()) {
					if (host.getCheckStatus() != null &&
							status.getCode() != host.getCheckStatus()) {
						host.setCheckStatus(status.getCode());
						modified = true;
					}
				}
				if (modified) {
					file.setContent(YamlUtil.customYamlDump(bean));
					upload(tenantId, Collections.singletonList(file));
				}
			}
		}
	}

	@Override
	public void deleteYaml(String tenantId, String agentId, String fileName, String source) {
		LogicManager.getInstance().getYamlFileLogic().deleteYaml(tenantId, agentId, fileName, source);
	}

	@Override
	public Map<String, Collection<String>> getAllPluginApp(String tenantId, String source, String id) {
		List<String> allList = LogicManager.getInstance().getYamlFileLogic().getAllYamlName(tenantId, source);
		List<String> disabledApps = getDisabledYamlNames(id);
		List<String> enabledApps = getEnabledYamlNames(id);
		Agent agent = LogicManager.getInstance().getAgentLogic().getAgentById(id, tenantId);
		Map<String, Collection<String>> result = new HashMap<>();
		Set<String> activeApps = new HashSet<>();
		if (agent != null) {
			List<String> apps = agent.getApps();
			if (apps != null && apps.size() > 0) {
				activeApps = apps.stream()
						.filter(StringUtils::isNotBlank)
						.map(m -> m = m.split(".yaml")[0])
						.collect(Collectors.toSet());
			}
			if (activeApps.size() > 0 && disabledApps != null && disabledApps.size() > 0) {
				activeApps.removeAll(disabledApps);
			}
			if (enabledApps != null && enabledApps.size() > 0) {
				activeApps.addAll(enabledApps);
			}
			if (activeApps.size() > 0) {
				allList.removeAll(activeApps);
			}
		}
		List<String> activeAppList = new ArrayList<String>(activeApps);
		result.put(AGENT_PLUGIN_ACTIVE, activeAppList.stream().sorted().collect(Collectors.toList()));
		result.put(AGENT_PLUGIN_INACTIVE, allList);
		return result;
	}

	@Override
	public AgentConfigDetail pluginConfigDetail(String tenantId, String id, String pluginName, String source,
												int current, int pageSize, String checkStatus, String filter) {
		YamlFile file = LogicManager.getInstance().getYamlFileLogic()
				.getYamlFileByNameAndAgentId(tenantId, id, pluginName, source);
		AgentConfigDetail detail = new AgentConfigDetail();
		detail.setStatus(AGENT_PLUGIN_INACTIVE);
		if (checkPluginIsActive(tenantId, id, pluginName, source)) {
			detail.setStatus(AGENT_PLUGIN_ACTIVE);
		}
		if (file != null) {
			if (file.getModified() != null) {
				detail.setActiveTime(DateUtil.format(file.getModified()));
			}
			String yamlFile = file.getContent();
			if (yamlFile != null && yamlFile.length() > 0) {
				YamlBean yamlBean = YamlUtil.customYamlLoad(yamlFile);
				if (yamlBean != null) {
					List<Map<String, Object>> methods = yamlBean.getCollect_methods();
					if (methods != null) {
						detail.setMethods(methods);
						detail.setMethodCount(methods.size());
					} else {
						detail.setMethods(new ArrayList<>());
						detail.setMethodCount(0);
					}
					List<YamlHost> hosts = yamlBean.getHosts();
					if (hosts != null) {
						detail.setHosts(generateDetailHosts(hosts));
						detail.setTotalCount(hosts.size());
					} else {
						detail.setHosts(new ArrayList<>());
						detail.setTotalCount(0);
					}
				}
			}
		}
		// 进行分页以及过滤
		configHostDetailByFilter(current, pageSize, checkStatus, detail, filter);
		return detail;
	}

	// 进行分页以及状态、关键字过滤
	private void configHostDetailByFilter(int current, int pageSize, String checkStatus, AgentConfigDetail detail, String filter) {
		List<AgentConfigDetailHost> hosts = detail.getHosts();
		if (hosts != null && hosts.size() > 0) {
			Iterator<AgentConfigDetailHost> it = hosts.iterator();
			HostCheckStatus[] statusArr = HostCheckStatus.checkName(checkStatus == null ? null : checkStatus.split(","));
			while (it.hasNext()) {
				boolean remove = true;
				AgentConfigDetailHost host = it.next();
				if (statusArr != null && statusArr.length > 0) {
					for (HostCheckStatus status : statusArr) {
						if (status.getName().equals(host.getCheckStatus())) {
							remove = false; // 不需要移除
							break;
						}
					}
					if (remove) {
						it.remove();
						continue;
					}
				}
				if (filter != null && filter.trim().length() > 0) {
					// 主机名 标签 IP 过滤
					String hostname = host.getHostname();
					String ip = host.getIp();
					List<String> tags = host.getTags();
					if ((hostname != null && hostname.contains(filter))
							|| (ip != null && ip.contains(filter))) {
						continue;
					} else if (tags != null && tags.size() > 0) {
						boolean isMatched = false;
						for (String tag : tags) {
							if (tag != null && tag.contains(filter)) {
								isMatched = true;
								break;
							}
						}
						if (isMatched) {
							continue;
						}
					}
					it.remove();
				}
			}
			if (hosts.size() > 0) {
				detail.setTotal(hosts.size());
				int totalPage = (hosts.size() + pageSize - 1) / pageSize;
				if (current > totalPage) {
					current = totalPage;
				}
				detail.setCurrent(current);
				if (hosts.size() > pageSize) {
					int begin = (current - 1) * pageSize;
					int end = begin + pageSize;
					if (end >= hosts.size()) {
						end = hosts.size();
					}
					detail.setHosts(new ArrayList<AgentConfigDetailHost>(hosts.subList(begin, end)));
				}
			} else {
				detail.setTotal(0);
				detail.setCurrent(1);
			}
		}
		detail.setPageSize(pageSize);
	}

	private boolean checkPluginIsActive(String tenantId, String id, String pluginName, String source) {
		if (pluginName == null || pluginName.length() <= 0) {
			return false;
		}
		Map<String, Collection<String>> result = getAllPluginApp(tenantId, source, id);
		Collection<String> activeList = result.get(AGENT_PLUGIN_ACTIVE);
		for (String s : activeList) {
			if (pluginName.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private List<AgentConfigDetailHost> generateDetailHosts(List<YamlHost> yamlHosts) {
		List<AgentConfigDetailHost> hosts = new ArrayList<AgentConfigDetailHost>();
		if (yamlHosts == null || yamlHosts.size() <= 0) {
			return hosts;
		}
		for (YamlHost yamlHost : yamlHosts) {
			if (yamlHost != null) {
				AgentConfigDetailHost host = new AgentConfigDetailHost();
				host.setMethod(yamlHost.getCollect_method());
				host.setIp(yamlHost.getIp());
				host.setOs(yamlHost.getOs());
				host.setHostname(yamlHost.getHost());
				host.setTags(TagUtil.string2List(yamlHost.getTags()));
				host.setCheckStatus(HostCheckStatus.checkCode(yamlHost.getCheckStatus()).getName());
				host.setType(yamlHost.getType());
				host.setId(yamlHost.getId());
				hosts.add(host);
			}
		}
		return hosts;
	}

	@Override
	public boolean updateHostConfig(String tenantId, String id, String pluginName, boolean checkNow, String source,
									List<AgentConfigDetailHost> newHosts, List<AgentConfigDetailHost> removeHosts,
									List<AgentConfigDetailHost> updateHosts) {
		YamlFile yamlFile = LogicManager.getInstance().getYamlFileLogic()
				.getYamlFileByNameAndAgentId(tenantId, id, pluginName, source);
		if (yamlFile == null) {
			yamlFile = new YamlFile(id, pluginName, tenantId, source);
		}
		YamlBean yamlBean = YamlUtil.customYamlLoad(yamlFile.getContent());
		if (yamlBean == null) {
			LOG.warn("yaml load failed: {}", yamlFile.getContent());
			return false;
		}
		List<YamlHost> hosts = yamlBean.getHosts();
		if (newHosts != null && newHosts.size() > 0) {
			// add new hosts
			if (hosts == null || hosts.size() <= 0) {
				hosts = generateYamlHosts(newHosts, checkNow);
			} else {
				// 新增主机置顶
				List<YamlHost> newYamlHosts = generateYamlHosts(newHosts, checkNow);
				newYamlHosts.addAll(hosts);
				hosts = newYamlHosts;
			}
		}
		if (updateHosts != null && updateHosts.size() > 0) {
			// update host
			if (hosts != null && hosts.size() > 0) {
				for (YamlHost host : hosts) {
					String ip = host.getIp();
					for (AgentConfigDetailHost updateHost : updateHosts) {
						if (ip != null && ip.equals(updateHost.getIp())) {
							host.setOs(updateHost.getOs());
							host.setCollect_method(updateHost.getMethod());
							host.setHost(updateHost.getHostname());
							host.setTags(TagUtil.list2String(updateHost.getTags()));
							host.setType(updateHost.getType());
							if (StringUtils.isNotBlank(updateHost.getId())
									&& updateHost.getId().trim().length() == 32) {
								host.setId(updateHost.getId().trim());
							}
							if (checkNow) {
								host.setCheckStatus(HostCheckStatus.VERIFYING.getCode());
							} else {
								host.setCheckStatus(HostCheckStatus.UNCHECKED.getCode());
							}
							break;
						}
					}
				}
			}
		}
		if (removeHosts != null && removeHosts.size() > 0) {
			// delete hosts
			if (hosts != null && hosts.size() > 0) {
				Iterator<YamlHost> it = hosts.iterator();
				while (it.hasNext()) {
					YamlHost host = it.next();
					for (AgentConfigDetailHost detailHost : removeHosts) {
						String ip = detailHost.getIp();
						String method = detailHost.getMethod();
						if ((ip != null && ip.equals(host.getIp())
								&& (method != null && method.equals(host.getCollect_method())))) {
							it.remove();
							break;
						}
					}
				}
			}
		}
		// 新增时可能更改hosts的对象引用, 因此重新set
		yamlBean.setHosts(hosts);
		yamlFile.setContent(YamlUtil.customYamlDump(yamlBean));
		return upload(tenantId, Collections.singletonList(yamlFile));
	}

	private List<YamlHost> generateYamlHosts(List<AgentConfigDetailHost> hosts, boolean checkNow) {
		List<YamlHost> result = new ArrayList<YamlHost>();
		if (hosts != null && hosts.size() > 0) {
			for (AgentConfigDetailHost host : hosts) {
				YamlHost yamlHost = new YamlHost();
				yamlHost.setCollect_method(host.getMethod());
				yamlHost.setIp(host.getIp());
				yamlHost.setHost(host.getHostname());
				yamlHost.setOs(host.getOs());
				yamlHost.setTags(TagUtil.list2String(host.getTags()));
				if (StringUtils.isNotBlank(host.getId())
						&& host.getId().trim().length() == 32) {
					yamlHost.setId(host.getId().trim());
				}
				yamlHost.setId(host.getId());
				yamlHost.setType(host.getType());
				if (checkNow) {
					yamlHost.setCheckStatus(HostCheckStatus.VERIFYING.getCode());
				} else {
					yamlHost.setCheckStatus(HostCheckStatus.UNCHECKED.getCode());
				}
				result.add(yamlHost);
			}
		}
		return result;
	}

	@Override
	public boolean updateMethodConfig(String tenantId, String id, String pluginName, String source,
									  List<Map<String, Object>> newMethods, List<String> removeNameList,
									  Map<String, Object> updateMethod) {
		YamlFile yamlFile = LogicManager.getInstance().getYamlFileLogic()
				.getYamlFileByNameAndAgentId(tenantId, id, pluginName, source);
		if (yamlFile == null) {
			yamlFile = new YamlFile(id, pluginName, tenantId, source);
		}
		YamlBean yamlBean = YamlUtil.customYamlLoad(yamlFile.getContent());
		if (yamlBean == null) {
			return false;
		}
		List<Map<String, Object>> methods = yamlBean.getCollect_methods();
		if (newMethods != null && newMethods.size() > 0) {
			// add new method
			if (methods == null || methods.size() <= 0) {
				yamlBean.setCollect_methods(newMethods);
			} else {
				if (checkMethodsDupName(methods, newMethods)) {
					throw new IllegalArgumentException("same method name is not allow");
				}
				methods.addAll(newMethods);
			}
		}
		if (updateMethod != null && updateMethod.get("name") != null) {
			// update method
			String updateName = updateMethod.get("name").toString();
			List<Map<String, Object>> updateMethods = new ArrayList<Map<String, Object>>();
			if (methods != null && methods.size() > 0) {
				for (Map<String, Object> m : methods) {
					if (updateName.equals(m.get("name") == null ? null: m.get("name").toString())) {
						updateMethods.add(updateMethod);
						continue;
					}
					updateMethods.add(m);
				}
			}
			yamlBean.setCollect_methods(updateMethods);
		}
		if (removeNameList != null && removeNameList.size() > 0) {
			// delete method
			if (methods != null && methods.size() > 0) {
				Iterator<Map<String, Object>> it = methods.iterator();
				while (it.hasNext()) {
					Map<String, Object> method = it.next();
					for (String name : removeNameList) {
						String methodName = method.get("name") == null ? null : method.get("name").toString();
						if (name != null && name.equals(methodName)) {
							rmHostWithMethod(yamlBean.getHosts(), name);
							it.remove();
							break;
						}
					}
				}
			}
		}
		yamlFile.setContent(YamlUtil.customYamlDump(yamlBean));
		return upload(tenantId, Collections.singletonList(yamlFile));
	}

	/**
	 * 检查新增方法是否有命名重复
	 * @param methods
	 * @param newMethods
     */
	private boolean checkMethodsDupName(List<Map<String, Object>> methods, List<Map<String, Object>> newMethods) {
		// 调用前已经进行非空检查
		for (Map<String, Object> method : methods) {
			String name = method.get("name") == null ? null : method.get("name").toString();
			for (Map<String, Object> newMethod : newMethods) {
				String newMethodName = newMethod.get("name") == null ? null : newMethod.get("name").toString();
				if (newMethodName != null && newMethodName.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  移除配置方法前移除该配置方法下的主机
	 * @param hosts
	 * @param name
	 */
	private void rmHostWithMethod(List<YamlHost> hosts, String name) {
		if (hosts != null && hosts.size() > 0 && name != null) {
			Iterator<YamlHost> it = hosts.iterator();
			while (it.hasNext()) {
				YamlHost host = it.next();
				if (name.equals(host.getCollect_method())) {
					it.remove();
				}
			}
		}
	}

	public List<String> getDisabledYamlNames(String agentId) {
		return LogicManager.getInstance().getYamlFileLogic().getDisabledYamlNames(agentId);
	}

	public List<String> getEnabledYamlNames(String agentId) {
		return LogicManager.getInstance().getYamlFileLogic().getEnabledYamlNames(agentId);
	}

	@Override
	public boolean checkPluginConfig(String tenantId, String id, String pluginName, String source, List<String> ipList) {
		YamlFile yamlFile = LogicManager.getInstance().getYamlFileLogic()
				.getYamlFileByNameAndAgentId(tenantId, id, pluginName, source);
		if (yamlFile != null) {
			YamlBean yamlBean = YamlUtil.customYamlLoad(yamlFile.getContent());
			if (yamlBean == null) {
				LOG.warn("yaml load failed: {}", yamlFile.getContent());
				return false;
			}
			List<YamlHost> hosts = yamlBean.getHosts();
			if (hosts != null && hosts.size() > 0) {
				for (YamlHost host : hosts) {
					for (String ip : ipList) {
						if (ip != null && ip.equals(host.getIp())) {
							host.setCheckStatus(HostCheckStatus.VERIFYING.getCode());
						}
					}
				}
				String content = YamlUtil.customYamlDump(yamlBean);
				if (content != null) {
					yamlFile.setContent(content);
					return upload(tenantId, Collections.singletonList(yamlFile));
				}
			}
			return true;
		}
		throw new IllegalArgumentException("cannot find the yaml file");
	}
}
