package uyun.bat.web.api.resource.service;

import java.util.List;
import java.util.Map;

import uyun.bat.agent.api.entity.AgentConfigDetailHost;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.web.api.agent.entity.TemplateParam;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.metric.entity.MetricMetaVO;
import uyun.bat.web.api.resource.entity.*;

public interface ResourceWebService {

	/**
	 * 获取主机和网络设备标签
	 * 
	 * @param tenantId
	 * @return
	 */
	Tag getResourceTags(String tenantId);

	/**
	 * 通过查询和分组条件获取资源列表
	 * 
	 * @param tenantId
	 * @param searchValue
	 * @param groupBy
	 * @param pageSize
	 * @param current
	 * @param sortField
	 * @param sortOrder
	 * @param tagged
	 * @return
	 */
	ResourceList oldSearchResource(String tenantId, String searchValue, String groupBy, int pageSize, int current,
			String sortField, String sortOrder, String[] checkValue, boolean tagged);

	/**
	 * 新接口
     */
	ResourceList1 searchResource(String tenantId, String searchValue, String groupBy, int pageSize, int current,
								   String sortField, String sortOrder, String[] checkValue);

	Map<String, Map<String, double[][]>> getMetrics(String tenantId);
	/**
	 * 获取对应资源的服务详情
	 * 
	 * @param resourceName
	 * @param appName
	 * @param resourceId
	 * @return
	 */
	MineDashboard getResourceById(String tenantId,String userId,String resourceName, String appName, String resourceId, int limit);

	/**
	 * 通过过滤条件获取hostMap列表
	 * 
	 * @param tenantId
	 * @param filterBy
	 * @param fillBy
	 * @param from
	 * @param to
	 * @return
	 */
	CircleList getCircleList(String tenantId, String filterBy, String fillBy, String from, String to);

	/**
	 * 通过指标名获取host的指标值
	 * 
	 * @param tenantId
	 * @param q
	 * @return
	 */
	IndicationList getHostIndication(String tenantId, String q);

	/**
	 * 判断用户是否有资源
	 * 
	 * @param tenantId
	 * @return
	 */
	boolean isResourceExist(String tenantId);

	/**
	 * 通过资源Id获取详情
	 * 
	 * @param tenantId
	 * @param resourceId
	 * @return
	 */
	ResourceDetailVO getResourceDetailById(String tenantId, String resourceId);

	List<String> getResourceTagList(String tenantId, String filter);

	/**
	 * 根据资源在线离线总数
	 * 
	 * @param tenantId
	 * @return
	 */
	StatusCount getResCountByOnlineStatus(String tenantId);

	/**
	 * 根据租户和资源id获取单个资源详情
	 * 
	 * @param tenantId
	 * @param id
	 * @return
	 */
	ResourceVO getResourceByResId(String tenantId, String id);

	/**
	 * 获取租户的所有tag包括host标签
	 * 
	 * @param tenantId
	 * @return
	 */
	List<String> queryAllResTags(String tenantId);

	/**
	 * 通过主机Id查询最近一天关联的事件列表
	 *
	 * @param tenantId
	 * @param resourceId
	 * @param current
	 * @param pageSize
	 * @return
	 */
	EventList getEventByResId(String tenantId, String resourceId, int current, int pageSize);

	/**
	 * 通过主机Id查询监测器告警条件和信息
	 *
	 * @param tenantId
	 * @param resourceId
	 * @param current
	 * @param pageSize
	 * @return
	 */
	PageResMonitorInfo getMonitorDetailByResId(String tenantId, String resourceId, int current, int pageSize);
	/**
	 * 用户自定义标签更新
	 *
	 * @param tags
	 * @return
	 */
	UserTag updateUserTags(String tenantId, UserTag tags);

	/**
	 * 用户批量设置标签 (覆盖)
	 *
	 * @param tags
	 * @return
	 */
	boolean batchSetUserTags(String tenantId, BatchSetUserTag tags);

	/**
	 * 用户批量设置标签 (增加)
	 *
	 * @param userTags
	 * @return
	 */
	boolean batchAddUserTags(String tenantId, List<BatchAddUserTag> userTags);

	/**
	 * 通过id删除资源
	 *
	 * @param tenantId
	 * @param id
	 * @return
	 */
	void deleteByResId(String tenantId, String id);
	
	/**
	 * 全局推广
	 * 
	 * @param template
	 */
	public Dashboard globalApply(String userId,TenantResTemplate template);

	/**
	 * 用户对模板进行更新操作
	 * 
	 * @param template
	 * @param dashwindow
	 * @return
	 */
	public Dashboard updateTemplate(String userId,TemplateParam tempParam);

	/**
	 * 用户对模板新增一个仪表
	 * 
	 * @param template
	 * @param dashwindow
	 * @return
	 */
	public Dashboard createTemplate(String userId,TemplateParam tempParam);
	
	/**
	 * 用户删除模板的一个仪表
	 * 
	 * @param template
	 * @param dashwindow
	 */
	public Dashboard deleteDashwindowTemplate(String userId,TemplateParam tempParam) ;

	/**
	 * 用户对模板内某一仪表进行排序操作
	 * 
	 * @param template
	 * @param dashboard
	 * @return
	 */
	public Dashboard sortTemplate(String userId,TemplateParam tempParam);

	Map<String, Object> queryResTagsByMetrics(String tenantId, List<String> metrics);

	/**
	 * 自定义资源指标组合查询
	 * @param params
	 * {
	 *     "resIds":["","",..],
	 *     "metricNames":["","",..]
	 * }
	 * @return
	 *
     */
	ResourceMetricsList queryResAndMetricCustomize(String tenantId, Map<String, Object> params);

	List<AlertState> getHostAlertState(String tenantId);

	/**
	 * 临时提供front api
	 * monitor resourceId ==> store unitId
	 * 转换成统一资源库资源id
	 * @param tenantId
	 * @param resource
     * @return
     */
	Map<String, Object> queryResourceStoreId(String tenantId, String resource);

	/**
	 * 获取资源的指标列表
	 * @param tenantId
	 * @param resourceId
     * @return
     */
	List<MetricMetaVO> getMetricNames(String tenantId, String resourceId);

	/**
	 * 获取资源的tag树
	 * @param tenantId
	 * @return
	 */
	Map<String, List<String>> getResourceTagNode(String tenantId);

	List<String> queryStoreIPListByTenantId(String tenantId);

	List<SimpleResInfo> queryResourceByIpAddrs(String tenantId, List<String> ipaddrs);

}
