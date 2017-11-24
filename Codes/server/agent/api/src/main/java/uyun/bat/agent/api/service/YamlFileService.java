package uyun.bat.agent.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uyun.bat.agent.api.entity.AgentConfigDetail;
import uyun.bat.agent.api.entity.AgentConfigDetailHost;
import uyun.bat.agent.api.entity.YamlFile;

public interface YamlFileService {

	boolean upload(String tenantId, List<YamlFile> yamlFiles);

	String getYamlContent(String tenantId, String agentId, String fileName, String source);

	List<String> getAllYamlName(String tenantId, String source);

	/**
	 * 设置配置是否启用
	 * @param tenantId
	 * @param agentId
	 * @param fileName
	 * @param source
	 * @param enabled
	 * @return
	 */
	void updateEnabled(String tenantId, String agentId, String fileName, String source, boolean enabled);
	
	void deleteYaml(String tenantId, String agentId, String fileName, String source);

/************************* 新版 agent/agentless 配置升级(暂时只有 agentless 会升级) *******************************/

	/**
	 * 获取用户监控插件列表
	 * @param tenantId
	 * @param source 类型 agent/agentless
	 * @param id agent/agentless id
	 * @return
	 * {
	 *     "active" : [
	 *     		"apache",
	 *     		"activeMQ"
	 *     	],
	 *     	"inactive" : [
	 *     		"oracle"
	 *     	]
	 * }
	 */
	Map<String, Collection<String>> getAllPluginApp(String tenantId, String source, String id);

	/**
	 * 获取监控插件配置详情
	 * @param tenantId
	 * @param id agent/agentless id
	 * @param pluginName 监控插件名字
     * @param source 类型 agent/agentless
	 * @param current 页数 1,2,3..
	 * @param pageSize
	 * @param checkStatus 状态过滤 四个类型：UNCHECKED(0, "未验证"), CHECKED(1, "已验证"), VERIFYING(2, "验证中"), FAILED(-1, "验证失败")
	 * 					传英文字符串即可，多个则逗号分隔, 全部则全传
	 * @param filter
	 * @return
	 * {
	 *     "activeTime" : "2017-01-01 12:12:12",
	 *     "status" : "active",
	 *     "methodCoun" : 1,
	 *     "totalCount" : 100,
	 *     "current" : 1,
	 *     "pageSize" : 20,
	 *     "methods" : [
	 *     		{
	 *				"name": "test",
	 *				"type": "ProtocolApache",
	 *				"protocol": "http",
	 *				"port": 88
	 *			}
	 *     ]，
	 *     "host" : [
	 *     		{
	 *				"ip": "10.1.11.241",
	 *				"hostname": null,
	 * 				"checkStatus": "UNCHECKED",
	 *				"os": "windows",
	 *				"method": "test",
	 *				"tags": []
	 *     		}
	 *     ]
	 * }
     */
	AgentConfigDetail pluginConfigDetail(String tenantId, String id, String pluginName, String source, int current, int pageSize, String checkStatus, String filter);

	/**
	 * 监控配置更新主机配置(增删改)
	 * @param tenantId
	 * @param id
	 * @param pluginName
	 * @param checkNow 是否立即检查
	 * @param source
	 * @param newHosts 新增的主机
     * @return
	 * {
	 *
	 * }
     */
	boolean updateHostConfig(String tenantId, String id, String pluginName, boolean checkNow, String source,
							 List<AgentConfigDetailHost> newHosts, List<AgentConfigDetailHost> removeHosts, List<AgentConfigDetailHost> updateHosts);

	/**
	 * 监控配置更新监控方法配置(增删改)
	 * @param tenantId
	 * @param id
	 * @param pluginName
	 * @param source
	 * @param newMethods
	 * @param removeNameList
     * @return
     */
	boolean updateMethodConfig(String tenantId, String id, String pluginName, String source,
							   List<Map<String, Object>> newMethods, List<String> removeNameList, Map<String, Object> updateMethod);

	/**
	 * 根据查询agent已被禁用的app名称列表
	 * @param agentId
	 */
	List<String> getDisabledYamlNames(String agentId);

	/**
	 * 根据查询agent已被启用的app名称列表
	 * @param agentId
	 */
	List<String> getEnabledYamlNames(String agentId);

	/**
	 * 验证对应的主机配置
	 * 修改对应主机状态为验证中,
	 * 验证结果需等待 agent 端同步 yaml, 并上传验证成功与否
	 * @param tenantId
	 * @param id
	 * @param pluginName
	 * @param source
	 * @param ipList
	 * @return
	 */
	boolean checkPluginConfig(String tenantId, String id, String pluginName, String source, List<String> ipList);
}
