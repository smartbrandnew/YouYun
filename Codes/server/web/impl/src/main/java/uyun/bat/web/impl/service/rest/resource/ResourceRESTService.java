package uyun.bat.web.impl.service.rest.resource;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcException;
import uyun.bat.common.config.Config;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.entity.Request;
import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PageResourceGroup;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceGroup;
import uyun.bat.datastore.api.entity.ResourceMetrics;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceStatusCount;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.TimeUnit;
import uyun.bat.datastore.api.exception.Illegalargumentexception;
import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.util.CollatorComparator;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.MinePageEvent;
import uyun.bat.event.api.entity.PageResEvent;
import uyun.bat.event.api.entity.ResEvent;
import uyun.bat.web.api.agent.entity.TemplateParam;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.metric.entity.MetricMetaVO;
import uyun.bat.web.api.resource.entity.*;
import uyun.bat.web.api.resource.service.ResourceWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.common.util.EncryptUtil;
import uyun.bat.web.impl.common.util.IntegrationUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service(protocol = "rest")
@Path("v2/resources")
public class ResourceRESTService implements ResourceWebService {
	private static boolean isZH = Config.getInstance().isChinese();
	@GET
	@Path("tags")
	@Produces(MediaType.APPLICATION_JSON)
	public Tag getResourceTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		Tag tags = new Tag();
		tags.setTags(ServiceManager.getInstance().getResourceService().queryResTagNames(tenantId));
		return tags;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceVO getResourceByResId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("resourceId") String resourceId) {
		 
		Resource res = ServiceManager.getInstance().getResourceService().queryResById(resourceId, tenantId);
		List<Apps> appsList = new ArrayList<Apps>();
		for (String name : res.getApps()) {
			Apps app = new Apps();
			app.setName(name);
			appsList.add(app);
		}
		ResourceVO resource = new ResourceVO(res.getId(), null, null, null, res.getResourceTypeName(), appsList,
				res.getHostname(), res.getIpaddr(), res.getOnlineStatus().getId() == 0 ? true : false,
				res.getAlertStatus().getId(), res.getOs(), null, res.getUserTags());
		return resource;

	}

	@GET
	@Path("query")
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceList oldSearchResource(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("searchValue") String searchValue, @QueryParam("groupBy") String groupBy,
			@QueryParam("pageSize") int pageSize, @QueryParam("current") int current,
			@QueryParam("sortField") String sortField, @QueryParam("sortOrder") String sortOrder,
			@QueryParam("checkValue") String[] checkValue, @QueryParam("tagged") boolean tagged) {

		if (pageSize == 0)
			pageSize = 1;
		if (current == 0)
			current = 1;
		OnlineStatus onlineState = null;
		if (checkValue.length == 1)
			onlineState = OnlineStatus.checkByName(checkValue[0]);
		ResourceList resourceList = new ResourceList();
		List<ResourceVO> resources = new ArrayList<ResourceVO>();
		//如果进行批量打标签就直接返回空数组
		Map<String, double[][]> cpuMap = tagged ? new HashMap<String, double[][]>() : generateMetric("system.cpu.pct_usage", tenantId);
		Map<String, double[][]> memMap = tagged ? new HashMap<String, double[][]>() : generateMetric("system.mem.pct_usage", tenantId);
		Map<String, double[][]> loadMap = tagged ? new HashMap<String, double[][]>() : generateMetric("system.load.15", tenantId);
		// 有groupBy查询
		if (!StringUtils.isEmpty(groupBy)) {
			PageResourceGroup prg = ServiceManager.getInstance().getResourceService()
					.queryByFilterAndGroupByTag(tenantId, searchValue, groupBy, current, pageSize, onlineState);
			List<ResourceGroup> resourceGroup = prg.getResourceGroups();

			for (int i = 0; i < resourceGroup.size(); i++) {
				List<ResourceVO> rs = new ArrayList<ResourceVO>();
				ResourceVO resource = new ResourceVO();
				for (Resource r : resourceGroup.get(i).getResources()) {
					ResourceVO s = new ResourceVO();
					s.setAlertState(r.getAlertStatus().getId());
					s.setState(r.getOnlineStatus().getId() == 0 ? true : false);
					List<Apps> appsList = new ArrayList<Apps>();
					for (String name : r.getApps()) {
						Apps app = new Apps();
						app.setName(name);
						appsList.add(app);
					}

					s.setApps(appsList);
					s.setHostName(r.getHostname());
					s.setId(r.getId());
					s.setOs(r.getOs());
					double[][] cpu = cpuMap.get(r.getId());
					double[][] mem = memMap.get(r.getId());
					double[][] load = loadMap.get(r.getId());
					s.setResourceCpu(cpu == null ? new double[0][0] : cpu);
					s.setResourceRam(mem == null ? new double[0][0] : mem);
					s.setResourceLoad(load == null ? new double[0][0] : load);
					s.setResourceType(r.getResourceTypeName());
					s.setSysIp(r.getIpaddr());
					s.setUserTags(r.getUserTags());
					rs.add(s);
				}
				resource.setResourceType(resourceGroup.get(i).getName());
				resource.setChildren(rs);
				resource.setId(i + "");
				resources.add(resource);
			}
			resourceList.setLists(resources);
			resourceList.setCurrentPage(current);
			resourceList.setPageSize(pageSize);
			resourceList.setTotalCount(prg.getCount());

			resourceList.setOfflineCount(prg.getOfflineCount());
			resourceList.setOnlineCount(prg.getOnlineCount());

			resourceList.setDefaultExpandedRowKeys(new ArrayList<String>());
			List<String> defaultExpandedRowKeys = new ArrayList<String>();
			for (ResourceVO r : resources) {
				defaultExpandedRowKeys.add(r.getId());
			}
			resourceList.setDefaultExpandedRowKeys(defaultExpandedRowKeys);
			if(!isZH){
				for(ResourceVO r : resourceList.getLists()){
					if(r.getResourceType().equals("计算机设备"))
						r.setResourceType("Computer Equipment");
					else if(r.getResourceType().equals("网络设备"))
						r.setResourceType("Network Equipment");
					else if(r.getResourceType().equals("虚拟设备"))
						r.setResourceType("Virtual Equipment");
				}
			}
			return resourceList;
		}

		PageResource pr = new PageResource(0, new ArrayList<Resource>());
		// 有排序的查询
		if (!StringUtils.isEmpty(sortField)) {
			// 前后命名不一致进行转化
			sortOrder = "ascend".equals(sortOrder) ? "asc" : "desc";
			sortField = "resourceType".equals(sortField) ? "type"
					: ("state".equals(sortField) ? "online_status"
							: ("alertState".equals(sortField) ? "alert_status"
									: ("sysIp".equals(sortField) ? "ipaddr" : "hostname")));
			ResourceOrderBy orderBy = new ResourceOrderBy(ResourceOrderBy.Order.checkByName(sortOrder),
					ResourceOrderBy.SortBy.checkByName(sortField));
			pr = ServiceManager.getInstance().getResourceService().queryByKeyAndSortBy(tenantId, searchValue, orderBy,
					current, pageSize, onlineState);
		}
		// 只有searchValue的查询
		else if (!StringUtils.isEmpty(searchValue)) {
			pr = ServiceManager.getInstance().getResourceService().queryByKey(tenantId, searchValue, current, pageSize,
					onlineState);
		}
		// 无groupBy和searchValue的查询
		else {
			pr = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, current, pageSize,
					onlineState);
		}
		List<Resource> rs = pr.getResources();
		for (Resource r : rs) {
			ResourceVO s = new ResourceVO();
			s.setAlertState(r.getAlertStatus().getId());
			s.setState(r.getOnlineStatus().getId() == 0 ? true : false);
			List<Apps> appsList = new ArrayList<Apps>();
			for (String name : r.getApps()) {
				Apps app = new Apps();
				app.setName(name);
				appsList.add(app);
			}

			s.setApps(appsList);
			s.setHostName(r.getHostname());
			s.setId(r.getId());
			s.setOs(r.getOs());
			double[][] cpu = cpuMap.get(r.getId());
			double[][] mem = memMap.get(r.getId());
			double[][] load = loadMap.get(r.getId());
			s.setResourceCpu(cpu == null ? new double[0][0] : cpu);
			s.setResourceRam(mem == null ? new double[0][0] : mem);
			s.setResourceLoad(load == null ? new double[0][0] : load);
			s.setResourceType(r.getResourceTypeName());
			s.setSysIp(r.getIpaddr());
			s.setUserTags(r.getUserTags());
			resources.add(s);
		}
		resourceList.setLists(resources);
		resourceList.setCurrentPage(current);
		resourceList.setPageSize(pageSize);
		resourceList.setTotalCount(pr.getCount());

		resourceList.setOnlineCount(pr.getOnlineCount());
		resourceList.setOfflineCount(pr.getOfflineCount());

		resourceList.setDefaultExpandedRowKeys(new ArrayList<String>());
		if(!isZH){
			for(ResourceVO r : resourceList.getLists()){
				if(r.getResourceType().equals("计算机设备"))
					r.setResourceType("Computer Equipment");
				else if(r.getResourceType().equals("网络设备"))
					r.setResourceType("Network Equipment");
				else if(r.getResourceType().equals("虚拟设备"))
					r.setResourceType("Virtual Equipment");
			}
		}
		return resourceList;
	}

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceList1 searchResource(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
										@QueryParam("searchValue") String searchValue, @QueryParam("groupBy") String groupBy,
										@QueryParam("pageSize") int pageSize, @QueryParam("current") int current,
										@QueryParam("sortField") String sortField, @QueryParam("sortOrder") String sortOrder,
										@QueryParam("checkValue") String[] checkValue) {

		if (pageSize == 0)
			pageSize = 1;
		if (current == 0)
			current = 1;
		OnlineStatus onlineState = null;
		if (checkValue.length == 1)
			onlineState = OnlineStatus.checkByName(checkValue[0]);
		ResourceList1 resourceList = new ResourceList1();
		List<ResourceVO1> resources = new ArrayList<>();
		// 有groupBy查询
		if (!StringUtils.isEmpty(groupBy)) {
			PageResourceGroup prg = ServiceManager.getInstance().getResourceService()
					.queryByFilterAndGroupByTag(tenantId, searchValue, groupBy, current, pageSize, onlineState);
			List<ResourceGroup> resourceGroup = prg.getResourceGroups();

			for (int i = 0; i < resourceGroup.size(); i++) {
				List<ResourceVO1> rs = new ArrayList<>();
				ResourceVO1 resource = new ResourceVO1();
				for (Resource r : resourceGroup.get(i).getResources()) {
					ResourceVO1 s = new ResourceVO1();
					s.setAlertState(r.getAlertStatus().getId());
					s.setState(r.getOnlineStatus().getId() == 0 ? true : false);
					List<Apps> appsList = new ArrayList<Apps>();
					for (String name : r.getApps()) {
						Apps app = new Apps();
						app.setName(name);
						appsList.add(app);
					}

					s.setApps(appsList);
					s.setHostName(r.getHostname());
					s.setId(r.getId());
					s.setOs(r.getOs());
					s.setResourceType(r.getResourceTypeName());
					s.setSysIp(r.getIpaddr());
					s.setUserTags(r.getUserTags());
					rs.add(s);
				}
				resource.setResourceType(resourceGroup.get(i).getName());
				resource.setChildren(rs);
				resource.setId(i + "");
				resources.add(resource);
			}
			resourceList.setLists(resources);
			resourceList.setCurrentPage(current);
			resourceList.setPageSize(pageSize);
			resourceList.setTotalCount(prg.getCount());

			resourceList.setOfflineCount(prg.getOfflineCount());
			resourceList.setOnlineCount(prg.getOnlineCount());

			resourceList.setDefaultExpandedRowKeys(new ArrayList<String>());
			List<String> defaultExpandedRowKeys = new ArrayList<String>();
			for (ResourceVO1 r : resources) {
				defaultExpandedRowKeys.add(r.getId());
			}
			resourceList.setDefaultExpandedRowKeys(defaultExpandedRowKeys);
			if (!isZH) {
				for (ResourceVO1 r : resourceList.getLists()) {
					if (r.getResourceType().equals("计算机设备"))
						r.setResourceType("Computer Equipment");
					else if (r.getResourceType().equals("网络设备"))
						r.setResourceType("Network Equipment");
					else if (r.getResourceType().equals("虚拟设备"))
						r.setResourceType("Virtual Equipment");
				}
			}
			return resourceList;
		}

		PageResource pr = new PageResource(0, new ArrayList<Resource>());
		// 有排序的查询
		if (!StringUtils.isEmpty(sortField)) {
			// 前后命名不一致进行转化
			sortOrder = "ascend".equals(sortOrder) ? "asc" : "desc";
			sortField = "resourceType".equals(sortField) ? "type"
					: ("state".equals(sortField) ? "online_status"
					: ("alertState".equals(sortField) ? "alert_status"
					: ("sysIp".equals(sortField) ? "ipaddr" : "hostname")));
			ResourceOrderBy orderBy = new ResourceOrderBy(ResourceOrderBy.Order.checkByName(sortOrder),
					ResourceOrderBy.SortBy.checkByName(sortField));
			pr = ServiceManager.getInstance().getResourceService().queryByKeyAndSortBy(tenantId, searchValue, orderBy,
					current, pageSize, onlineState);
		}
		// 只有searchValue的查询
		else if (!StringUtils.isEmpty(searchValue)) {
			pr = ServiceManager.getInstance().getResourceService().queryByKey(tenantId, searchValue, current, pageSize,
					onlineState);
		}
		// 无groupBy和searchValue的查询
		else {
			pr = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, current, pageSize,
					onlineState);
		}
		List<Resource> rs = pr.getResources();
		for (Resource r : rs) {
			ResourceVO1 s = new ResourceVO1();
			s.setAlertState(r.getAlertStatus().getId());
			s.setState(r.getOnlineStatus().getId() == 0 ? true : false);
			List<Apps> appsList = new ArrayList<Apps>();
			for (String name : r.getApps()) {
				Apps app = new Apps();
				app.setName(name);
				appsList.add(app);
			}

			s.setApps(appsList);
			s.setHostName(r.getHostname());
			s.setId(r.getId());
			s.setOs(r.getOs());
			s.setResourceType(r.getResourceTypeName());
			s.setSysIp(r.getIpaddr());
			s.setUserTags(r.getUserTags());
			resources.add(s);
		}
		resourceList.setLists(resources);
		resourceList.setCurrentPage(current);
		resourceList.setPageSize(pageSize);
		resourceList.setTotalCount(pr.getCount());

		resourceList.setOnlineCount(pr.getOnlineCount());
		resourceList.setOfflineCount(pr.getOfflineCount());

		resourceList.setDefaultExpandedRowKeys(new ArrayList<String>());
		if (!isZH) {
			for (ResourceVO1 r : resourceList.getLists()) {
				if (r.getResourceType().equals("计算机设备"))
					r.setResourceType("Computer Equipment");
				else if (r.getResourceType().equals("网络设备"))
					r.setResourceType("Network Equipment");
				else if (r.getResourceType().equals("虚拟设备"))
					r.setResourceType("Virtual Equipment");
			}
		}
		return resourceList;
	}

	@GET
	@Path("list/metrics")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Map<String, double[][]>> getMetrics(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		//如果进行批量打标签就直接返回空数组
		Map<String, double[][]> cpuMap = generateMetric("system.cpu.pct_usage", tenantId);
		Map<String, double[][]> memMap = generateMetric("system.mem.pct_usage", tenantId);
		Map<String, double[][]> loadMap = generateMetric("system.load.15", tenantId);
		Map<String, Map<String, double[][]>> map = new HashMap<>();
		map.put("cpuMap", cpuMap);
		map.put("memMap", memMap);
		map.put("loadMap", loadMap);
		//s.setResourceCpu(cpu == null ? new double[0][0] : cpu);
		return map;
	}


	private Map<String, double[][]> generateMetric(String metricName, String tenantId) {
		Map<String, double[][]> map = new HashMap<String, double[][]>();
		QueryBuilder queryBuilder = new QueryBuilder();
		long currentTime = System.currentTimeMillis();
		// 最近30min
		queryBuilder.setStartAbsolute(currentTime - 1000 * 60 * 30);
		queryBuilder.setEndAbsolute(currentTime);

		QueryMetric metric = queryBuilder.addMetric(metricName).addTenantId(tenantId);
		metric.addGrouper("resourceId");
		// 取最后一个点
		metric.addAggregatorType(AggregatorType.checkByName("last"));

		// interval设置为225s使返回的点数在8个以内
		List<PerfMetric> perfMetricList = ServiceManager.getInstance().getMetricService().querySeries(queryBuilder,
				225);
		if (perfMetricList != null && perfMetricList.size() > 0) {
			for (PerfMetric p : perfMetricList) {
				// 按理说只能返回一组数据，多组数据是bug
				List<DataPoint> dataPoints = p.getDataPoints();
				if (dataPoints != null && dataPoints.size() > 0) {
					double[][] point = new double[dataPoints.size()][2];
					map.put(p.getResourceId(), point);
					for (int j = 0; j < dataPoints.size(); j++) {
						point[j][0] = dataPoints.get(j).getTimestamp();
						DecimalFormat dcmFmt = new DecimalFormat("0.0");
						String value = dcmFmt.format(Double.parseDouble(dataPoints.get(j).getValue().toString()));
						point[j][1] = Double.parseDouble(value);
					}
				}
			}
		}
		return map;
	}

	@GET
	@Path("query/service")
	@Produces(MediaType.APPLICATION_JSON)
	public MineDashboard getResourceById(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,@HeaderParam(TenantConstants.COOKIE_USERID) String userId,
			@QueryParam("resourceName") String resourceName, @QueryParam("appName") String appName,
			@QueryParam("resourceId") String resourceId, @QueryParam("limit") @DefaultValue("25") int limit) {
		 
		List<String> ms = ServiceManager.getInstance().getMetricService().getMetricNamesByResId(resourceId);
		Resource res = ServiceManager.getInstance().getResourceService().queryResById(resourceId, tenantId);
		String ipAddr = res.getIpaddr();
		List<String> metricList = new ArrayList<>();
		for (String m : ms) {
			if (m.indexOf(appName) == 0 && !appName.equals(""))
				metricList.add(m);
		}
		Collections.sort(metricList);
		if(appName.endsWith("system")){
			if(null!=res.getOs())
				appName = diffSystemApp(res.getOs());
			if(res.getDescribtion().startsWith("dd-agent:")==false){
				appName = appName+"-agentless";
			}
		}
		//agentless上报的system模板修改为零散数据
		
		MineDashboard mineDashboard = new MineDashboard();
		boolean isApplied=true;
		Dashboard dashboard;
		TenantResTemplate template = ServiceManager.getInstance().getTenantResTemplateService().getTemplate(appName,
				tenantId, resourceId);
		// 判断是否有模板，优先级为：自定义模板、全局模板、系统内置模板
		if (template != null) { // 自定义模板
			isApplied=false;
			dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId());
		} else {
			template = ServiceManager.getInstance().getTenantResTemplateService().getGlobalTemplate(appName, tenantId);
			if (template != null) { // 全局模板
				dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId());
			} else {
				// 内置模板
				dashboard = ServiceManager.getInstance().getDashboardService().getDashboardByTemplateName(appName);
				if (dashboard == null) { // 动态生成全局模板
						// 默认limit传100应该能显示全部数据
						dashboard = new Dashboard();
						dashboard.setCreateTime(new Date());
						dashboard.setDescr("");
						if(resourceId == null)
							throw new Illegalargumentexception("resourceId can not be null!");
						dashboard.setId(EncryptUtil.string2MD5("["+tenantId+"],["+appName+"]"));
						dashboard.setIsResource(true);
						dashboard.setModified(new Date());
						dashboard.setName(appName);
						dashboard.setTemplate(false);
						dashboard.setTenantId(tenantId);
						dashboard.setType("timeseries");
						dashboard.setUserId(userId);
						ServiceManager.getInstance().getDashboardService().createDashboard(dashboard);
						List<String> metrics = metricList.size() > limit ? metricList.subList(0, limit) : metricList;
						List<Dashwindow> dashwindows = new ArrayList<>();
						for (String name : metrics) {
							List<Request> rs = new ArrayList<>();
							Dashwindow d = new Dashwindow();
							MetricMetaData metricMetaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(name);
							d.setName((metricMetaData!=null&&metricMetaData.getcName()!=null)? metricMetaData.getcName():name);
							d.setId(UUIDTypeHandler.createUUID());
							d.setDashId(dashboard.getId());
							Request r = new Request();
							r.setAggregator("avg");
							r.setQ("avg:" + name +"{$scope}");
							r.setType("line");
							rs.add(r);
							d.setRequests(rs);
							d.setViz("timeseries");
							ServiceManager.getInstance().getDashwindowService().createDashwindow(d);
						}
						template = new TenantResTemplate();
						template.setAppName(appName);
						template.setDashId(dashboard.getId());
						template.setResourceId(null);
						template.setTenantId(tenantId);
						//创建默认的全局模板
						ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
						dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(dashboard.getId());
					}
				}
		}
		mineDashboard = generateMineDashboard(dashboard, ipAddr,resourceName,limit);
		mineDashboard.setApplied(isApplied);
		return mineDashboard;
	}



	private MineDashboard generateMineDashboard(Dashboard dashboard, String ipAddr,String resourceName,int limit) {
		MineDashboard mineDashboard = new MineDashboard();
		mineDashboard.setId(dashboard.getId());
		mineDashboard.setName(dashboard.getName());
		List<String> records = dashboard.getDashwindowIdList();
		if (records != null && records.size() > 0) {
			List<Dashwindow> dashwindows = ServiceManager.getInstance().getDashwindowService()
					.getDashwindowsByDashId(dashboard.getId());
			String scope = null;
			if (null != dashwindows && dashwindows.size() > 0) {
				if (ipAddr != null && ipAddr.length() > 0 && !"unknown".equalsIgnoreCase(ipAddr))
					scope = "ip:"+ipAddr;
				else
					scope = "host:" + resourceName;
				List<Dashwindow> sortList = new ArrayList<Dashwindow>();
				for (String dashwindowId : records) {
					for (Dashwindow temp : dashwindows) {
						if (temp.getId().endsWith(dashwindowId)) {
							List<Request> qs = temp.getRequests();
							if (qs != null && qs.size() > 0) {
								for (Request q : qs) {
									q.setQ(q.getQ().replaceAll("\\u0024scope", scope));
								}
							}
							sortList.add(temp);
							break;
						}
					}
				}
				// TODO: 2-20 0020   默认至多显示25条数据,后面要多展现或者分页再说
				mineDashboard.setDashwindows(sortList.size() > limit ? sortList.subList(0, limit) : sortList);
			}
		}
		return mineDashboard;
	}

	@GET
	@Path("circle")
	@Produces(MediaType.APPLICATION_JSON)
	public CircleList getCircleList(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("filterBy") String filterBy, @QueryParam("fillBy") String fillBy,
			@QueryParam("from") String from, @QueryParam("to") String to) {
		// 前端传过来的时间会有1473835902611.0327这样的格式，此处做下特殊处理
		DecimalFormat dcmFmt = new DecimalFormat("0");
		 
		CircleList circleList = new CircleList();
		Map<String, Circle> cs = new HashMap<>();
		List<Resource> rs = new ArrayList<>();
		QueryBuilder builder = QueryBuilder.getInstance();
		// 资源圈无过滤条件
		List<PerfMetric> metrics = null;
		long timestamp = System.currentTimeMillis();
		if (StringUtils.isEmpty(filterBy) && StringUtils.isEmpty(fillBy)) {
			rs = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
		} else {
			rs = ServiceManager.getInstance().getResourceService().queryByKey(tenantId, filterBy.replace(":其他",":"), 1, 5000, null)
					.getResources();
			long end = StringUtils.isEmpty(to)?timestamp:Long.parseLong(dcmFmt.format(Double.parseDouble(to)));
			long start = StringUtils.isEmpty(from)?timestamp - 30 * 60 * 1000:Long.parseLong(dcmFmt.format(Double.parseDouble(from)));
			builder.setStartAbsolute(start).setEndAbsolute(end).addMetric(fillBy).addAggregatorType(AggregatorType.last)
					.addTenantId(tenantId).addGrouper("resourceId");
			try {
				metrics = ServiceManager.getInstance().getMetricService().queryPerfForCircle(builder);
			} catch (NullPointerException e) {
				throw new NullPointerException("If the query fails, check the datastore process for error logging");
			} catch (RuntimeException e1){
				throw  new RuntimeException("Abnormal tscached started");
			}
		}
		for (Resource r : rs) {
			Circle c = new Circle();
			List<Apps> appsList = new ArrayList<Apps>();
			for (String name : r.getApps()) {
				Apps app = new Apps();
				app.setName(name);
				appsList.add(app);
			}
			c.setApps(appsList);
			c.setIp(r.getIpaddr());
			c.setHostName(r.getHostname());
			c.setId(r.getId());
			c.setSize(1);
			c.setState(null != r.getOnlineStatus() && r.getOnlineStatus().getId() == 0 ? true : false);
			if(!StringUtils.isEmpty(fillBy))
				for (PerfMetric metric : metrics) {
					if (r.getId().equals(metric.getResourceId())) {
						if (c.isState() == true && null != metric && !metric.getDataPoints().isEmpty()) {
							Double value = Double.parseDouble(
									metric.getDataPoints().get(metric.getDataPoints().size() - 1).getValue().toString());
							c.setIndication(value);
						}
					}
				}
			List<String> tagList = new ArrayList<String>();
			for (ResourceTag t : r.getTags()) {
				String tag = t.getKey() + ":" + t.getValue();
				tagList.add(tag);
			}
			c.setTags(tagList);
			cs.put(c.getId(), c);
		}
		Map<String, ResourceMonitorRecord> map = ServiceManager.getInstance().getOverviewService().queryResourceMonitorRecord(tenantId);
		if (null != map && map.size() > 0) {
			for (String key : map.keySet()) {
				int severity = map.get(key).isError() ? 3 : (map.get(key).isWarn() ? 2 : 0);
				if (null != cs.get(key))
					cs.get(key).setSeverity(severity);
			}
		}
		List<Circle> temp = new ArrayList<>();
		temp.addAll(cs.values());
		circleList.setHosts(temp);
		return circleList;
	}

	@GET
	@Path("hostIndiction")
	@Produces(MediaType.APPLICATION_JSON)
	public IndicationList getHostIndication(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("metricName") String metricName) {
		 

		IndicationList indicationList = new IndicationList();
		List<Indication> is = new ArrayList<Indication>();
		List<Resource> rs = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
		List<PerfMetric> metrics;
		QueryBuilder builder = QueryBuilder.getInstance();
		// 取30分钟以前单值
		builder.setStart(30, TimeUnit.MINUTES)
				.addMetric(StringUtils.isEmpty(metricName) ? "system.cpu.pct_usage" : metricName).addTenantId(tenantId)
				.addAggregatorType(AggregatorType.avg).addGrouper("resourceId");
		metrics = ServiceManager.getInstance().getMetricService().queryPerfForCircle(builder);

		for (Resource r : rs) {
			Indication i = new Indication();
			i.setId(r.getId());
			i.setState(r.getOnlineStatus().getId() == 0 ? true : false);
			for (PerfMetric metric : metrics) {
				if (r.getId().equals(metric.getResourceId())) {
					if (i.isState() == true && null != metric && !metric.getDataPoints().isEmpty()) {
						i.setIndication(Double.parseDouble(
								metric.getDataPoints().get(metric.getDataPoints().size() - 1).getValue().toString()));
					}
				}
			}
			is.add(i);
		}

		indicationList.setHosts(is);
		MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricName);
		if (metaData != null) {
			indicationList.setMin(metaData.getValueMin());
			indicationList.setMax(metaData.getValueMax());
			indicationList.setUnit(metaData.getUnit());
		}
		return indicationList;
	}

	@GET
	@Path("alertState")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AlertState> getHostAlertState(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {

		Map<String, AlertState> list = new HashMap<>();
		List<Resource> rs = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
		for (Resource res : rs) {
			AlertState alertState = new AlertState(res.getId(), res.getOnlineStatus().getId() == 0 ? true : false);
			list.put(res.getId(), alertState);
		}
		Map<String, ResourceMonitorRecord> map = ServiceManager.getInstance().getOverviewService().queryResourceMonitorRecord(tenantId);
		if (null != map && map.size() > 0) {
			for (String key : map.keySet()) {
				int severity = map.get(key).isError() ? 3 : (map.get(key).isWarn() ? 2 :(map.get(key).isInfo() ? 1 : 0));
				if (null != list.get(key))
					list.get(key).setSeverity(severity);
			}
		}
		List<AlertState> temp = new ArrayList<>();
		temp.addAll(list.values());
		return temp;
	}

	@GET
	@Path("isExist")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isResourceExist(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		int count = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, 1, 10, null).getResources()
				.size();
		if (count > 0)
			return true;
		return false;
	}


	private static final String en_to_ch = "{\"vendor_id\": \"厂商\",\"model_name\": \"型号\",\"cpu_cores\": \"物理核数\",\"cpu_logical_processors\": \"逻辑核数\",\"mhz\": \"主频(mhz)\",\"cache_size\": \"缓存大小\",\"total\": \"物理内存\",\"swap_total\": \"交换分区\",\"ipaddress\": \"IP\",\"ipaddressv6\": \"IPV6\",\"macaddress\": \"MAC\",\"os\": \"操作系统\",\"hostname\": \"主机名称\",\"kernel_name\": \"内核\",\"kernel_release\": \"内核版本\",\"kernel_version\": \"构建版本\",\"machine\": \"硬件平台\",\"machine_type\":\"设备类型\"}";
	private static final Map<String, String> enToChMap = (Map<String, String>) JSONUtils.parse(en_to_ch);

	private List<String> changeUserAndAgentlessTags(String tempTag) {
		List<String> tags = new ArrayList<>();
		if (!StringUtils.isEmpty(tempTag)) {
			String[] uTags = tempTag.split(";");
			for (String t : uTags) {
				if (!"".equals(t))
					tags.add(t);
			}
		}
		return tags;
	}

	@GET
	@Path("detail")
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceDetailVO getResourceDetailById(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("resourceId") String resourceId) {
		 
		Resource res = ServiceManager.getInstance().getResourceService().queryResById(resourceId, tenantId);
		if (res == null)
			return new ResourceDetailVO();
		List<String> tags = new ArrayList<>();
		// 网络设备的摘要
		String devSuma = null;
		if (res.getTags() != null && res.getTags().size() > 0)
			for (ResourceTag tag : res.getTags()) {
				String t = tag.getKey() + ":" + tag.getValue();
				if (tag.getKey().equals("producer"))
					devSuma = tag.getValue();
				tags.add(t);
			}
		boolean onlineState = res.getOnlineStatus().getName().equals("在线") ? true : false;
		ResourceDetail resDetail = ServiceManager.getInstance().getResourceService().queryByResId(resourceId);
		// 增加用户自定义tag
		List<String> userTags = res.getUserTags();
		//用户自定义tag可能有其他产品的非k-v形式
		for(String t:userTags){
			tags.remove(t.contains(":") ? t : t + ":");
		}
		List<String> agentlessTags = res.getAgentlessTags();
		tags.removeAll(agentlessTags);
		if (resDetail == null) {
			Device d = new Device(onlineState, tags);
			d.setUserTags(userTags);
			d.setAgentlessTags(agentlessTags);
			return new ResourceDetailVO(d);
		}
		List<ResourceInfo> resInfoList = new ArrayList<>();
		if (res.getResourceTypeName().equals("网络设备")) {
			Device d = new Device(devSuma, onlineState, tags, resDetail.getAgentDesc());
			d.setUserTags(userTags);
			d.setAgentlessTags(agentlessTags);
			ResourceInfo r = new ResourceInfo("设备描述", resDetail.getDetail());
			resInfoList.add(r);
			return new ResourceDetailVO(d, resInfoList);
		}
		if (resDetail.getDetail().length() == 0) {
			Device d = new Device("", onlineState, tags, resDetail.getAgentDesc());
			d.setUserTags(userTags);
			d.setAgentlessTags(agentlessTags);
			return new ResourceDetailVO(d);
		}
		Map<String, Object> map = (Map<String, Object>) JSONUtils.parse(resDetail.getDetail());
		// 对标签进行排序
		List<String> listName = sort(map.keySet());
		// 显示摘要格式如：Linux, 4 CPU, 4GB 内存, 256GB 磁盘, 10.1.1.1
		StringBuilder sbBig = new StringBuilder();
		for (String key : listName) {
			Map<String, String> map1 = new HashMap<String, String>();
			if (!key.equals("filesystem")) {
				map1 = (Map<String, String>) map.get(key);
				ResourceInfoVO infoVo = myIterator(map1, key);
				if (infoVo.getSuma().getType() != null)
					sbBig.append(infoVo.getSuma().getType() + ", ");
				if (infoVo.getSuma().getOs() != null)
					sbBig.append(infoVo.getSuma().getOs() + ", ");
				if (infoVo.getSuma().getCores() != null)
					sbBig.append(infoVo.getSuma().getCores() + " CPU, ");
				if (infoVo.getSuma().getTotal() != null)
					sbBig.append(infoVo.getSuma().getTotal() + (isZH ? " 内存, " : " Memory, "));
				resInfoList.add(new ResourceInfo(infoVo.getName(), infoVo.getAttr()));
			} else {
				// 将磁盘按顺序排序 ;
				List<Disk> list = sortDisk((List<Object>) map.get(key));
				StringBuilder sb1 = new StringBuilder();
				double d = 0d;
				DecimalFormat dcmFmt = new DecimalFormat("0.0");
				for (Disk disk : list) {
					String value = dcmFmt
							.format(Double.parseDouble(disk.getKbSize().equals("Unknown") ? "0" : disk.getKbSize())
									/ (1024 * 1024));
					sb1.append(disk.getName() + " mounted on " + disk.getMountedOn() + " " + value + "GB");
					sb1.append(",");
					d += Double.parseDouble(value);
				}
				String str = sb1.deleteCharAt(sb1.length() - 1).toString();
				sbBig.append(dcmFmt.format(d) + "GB" + (isZH ? " 磁盘, " : " Disk, "));
				ResourceInfo rInfo = new ResourceInfo();
				rInfo.setName(isZH ? "文件系统" : "FileSystem");
				rInfo.setAttr(str);
				resInfoList.add(rInfo);
			}

		}
		if (res.getIpaddr() != null && !res.getIpaddr().equals("unknown"))
			sbBig.append(res.getIpaddr() + ", ");
		String desc = sbBig.deleteCharAt(sbBig.length() - 2).toString();
		Device dev = new Device(desc, onlineState, tags, resDetail.getAgentDesc(), userTags, agentlessTags);
		ResourceDetailVO detailVO = new ResourceDetailVO();
		detailVO.setDev(dev);
		detailVO.setInfo(resInfoList);
		return detailVO;
	}

	private static final String[] TOPIC = new String[] { "platform", "cpu", "memory", "filesystem", "network" };

	private List<String> sort(Set<String> set) {
		// 对字段进行按操作系统，cpu,内存，文件系统，网络排序
		List<String> list = new ArrayList<>();
		for (String temp : TOPIC) {
			if (set.contains(temp))
				list.add(temp);
		}
		return list;
	}

	private static class Disk implements Comparable<Disk> {
		private String kbSize;
		private String mountedOn;
		private String name;

		public String getMountedOn() {
			return mountedOn;
		}

		public String getKbSize() {
			return kbSize;
		}

		public String getName() {
			return name;
		}

		public Disk(String kbSize, String mountedOn, String name) {
			super();
			this.kbSize = kbSize;
			this.mountedOn = mountedOn;
			this.name = name;
		}

		private static Disk generate(Map<String, String> map) {
			Disk disk = new Disk(map.get("kb_size"), map.get("mounted_on"), map.get("name"));
			return disk;
		}

		@Override
		public int compareTo(Disk d) {
			return this.mountedOn.compareToIgnoreCase(d.getMountedOn());
		}
	}

	private List<Disk> sortDisk(List<Object> l) {
		List<Disk> list = new ArrayList<>();
		for (Object o : l) {
			if (!(o instanceof Map))
				continue;
			Disk d = Disk.generate((Map<String, String>) o);
			list.add(d);
		}
		Collections.sort(list);
		return list;
	}

	private ResourceInfoVO myIterator(Map<String, String> map, String key) {
		ResourceInfoVO resInfo = new ResourceInfoVO(org.apache.commons.lang.StringUtils.capitalize(key));
		if (key.equals("cpu")) {
			resInfo.setName("CPU");
		}
		if (isZH) {
			String name = key.equals("platform") ? "操作系统"
					: (key.equals("cpu") ? "处理器" : (key.equals("memory") ? "内存" : "网络"));
			resInfo.setName(name);
		}
		Summary suma = new Summary();
		DecimalFormat dcmFmt = new DecimalFormat("0.0");

		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
		if (map.get("total") != null) {
			// 内存linux为KB window为B
			String mem = map.get("total").indexOf("kB") != -1
					? dcmFmt.format(Double.parseDouble(map.get("total").substring(0, map.get("total").length() - 2))
							/ (1024 * 1024))
					: dcmFmt.format(Double.parseDouble(map.get("total")) / (1024 * 1024 * 1024));
			suma.setTotal(mem + "GB");
		}
		suma.setCores(map.get("cpu_cores"));
		suma.setOs(map.get("os"));
		if (null != map.get("machine_type"))
			suma.setType(isZH ? map.get("machine_type").replace("VM", "虚拟机").replace("PM", "物理机") : map.get("machine_type"));
		resInfo.setSuma(suma);
		StringBuilder sb = new StringBuilder();
		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			if (!key.equals("filesystem") && StringUtils.isEmpty(enToChMap.get(entry.getKey())))
				continue;
			// i18n
			sb.append(isZH ? enToChMap.get(entry.getKey()) : entry.getKey());
			sb.append("：");
			if (!key.equals("memory"))
				sb.append(isZH ? entry.getValue().replace("VM", "虚拟机").replace("PM", "物理机") : entry.getValue());
			else {
				// 内存linux为KB window为B
				sb.append(entry.getValue().contains("kB")
						? dcmFmt.format(Double.parseDouble(entry.getValue().substring(0, entry.getValue().length() - 2))
								/ (1024 * 1024)) + "GB"
						: dcmFmt.format(Double.parseDouble(entry.getValue()) / (1024 * 1024 * 1024)) + "GB");
			}
			sb.append(",");
		}
		String attr = sb.deleteCharAt(sb.length() - 1).toString();
		resInfo.setAttr(attr);
		return resInfo;
	}

	@GET
	@Path("tags/query")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getResourceTagList(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("filter") String filter) {
		 
		List<String> temp = ServiceManager.getInstance().getResourceService().getResTagsByTag(tenantId, filter);
		List<String> list = new ArrayList<>();
		for (String str : temp) {
			if (!str.equals("*****"))
				list.add(str);
		}
		return list;
	}

	@GET
	@Path("tags/node")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, List<String>> getResourceTagNode(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		CollatorComparator comparator = new CollatorComparator();
		Map<String, List<String>> map = new TreeMap<>(comparator);
		List<String> temp = ServiceManager.getInstance().getResourceService().getResTagsByTag(tenantId, "");
		Collections.sort(temp);
		for (String str : temp) {
			if (!str.equals("*****")) {
				String key = str.split(":")[0];
				String value = str.split(":").length > 1 ? str.split(":")[1] : "";
				List<String> list = map.get(key);
				if (null == list) {
					list = new ArrayList<>();
					map.put(key, list);
				}
				list.add(value);
			}
		}
		return map;
	}

	@GET
	@Path("query/count")
	@Produces(MediaType.APPLICATION_JSON)
	public StatusCount getResCountByOnlineStatus(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		List<ResourceStatusCount> list = ServiceManager.getInstance().getResourceService().getResStatusCount(tenantId);
		StatusCount counts = new StatusCount(list);
		return counts;
	}

	@GET
	@Path("host/tags")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> queryAllResTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		List<String> tags = ServiceManager.getInstance().getResourceService().queryAllResTags(tenantId);
		return tags;
	}

	@GET
	@Path("events")
	@Produces(MediaType.APPLICATION_JSON)
	public EventList getEventByResId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("resourceId") String resourceId, @QueryParam("current") int current,
			@QueryParam("pageSize") int pageSize) {
		 
		long currentTime = System.currentTimeMillis();
		Date endTime = new Date(currentTime);
		Date beginTime = new Date(currentTime - 24 * 60 * 60 * 1000);
		// 获取一天的关联事件
		MinePageEvent pageEvents = ServiceManager.getInstance().getEventService().searchEvent(tenantId, current,
				pageSize, resourceId, beginTime, endTime);
		EventList list = new EventList();
		List<EventVO> events = new ArrayList<EventVO>();
		if (null != pageEvents && null != pageEvents.getEvents() && pageEvents.getEvents().size() > 0) {
			for (Event temp : pageEvents.getEvents()) {
				EventVO e = new EventVO(temp.getMsgContent(), temp.getOccurTime(), temp.getServerity());
				events.add(e);
			}
			list.setTotal(pageEvents.getCount());
		}
		list.setLists(events);
		list.setPageSize(pageSize);
		list.setCurrent(current);
		return list;
	}

	@GET
	@Path("monitors")
	@Produces(MediaType.APPLICATION_JSON)
	public PageResMonitorInfo getMonitorDetailByResId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
												@QueryParam("resourceId") String resourceId, @QueryParam("current") @DefaultValue("1") int current,
												@QueryParam("pageSize") @DefaultValue("20") int pageSize) {
		 
		PageResEvent pageResEvent = ServiceManager.getInstance().getEventService().getAlertResEvents(tenantId, resourceId, current, pageSize);
		if (null == pageResEvent)
			return null;
		List<ResEvent> events = pageResEvent.getLists();
		PageResMonitorInfo pageResMonitorInfo = new PageResMonitorInfo();
		List<ResMonitorInfo> list = new ArrayList<>();
		Map<String, ResourceMonitorRecord> map = ServiceManager.getInstance().getOverviewService().queryResourceMonitorRecord(tenantId);

		if (null != events && events.size() > 0)
			for (ResEvent e : events) {
				//如果异常事件在记录里获取不到或者为正常则跳过
				if (null != map && map.size() > 0 && (null == map.get(resourceId) || map.get(resourceId).isOk()))
					continue;
				//监测器告警条件
				String condition = e.getIdentity().split("\\],\\[")[1].replace("\"", "");
				boolean isMetricMonitor = condition.contains("avg:") || condition.contains("max:") || condition.contains("min:") || condition.contains("sum:");
				//不是指标监测器integration为空
				String integration = isMetricMonitor ? condition.substring(condition.indexOf(":") + 1, condition.indexOf(".")) : "";
				//特殊处理一些与integration字段不符合的指标
				integration = IntegrationUtil.inMap.get(integration) != null ? IntegrationUtil.inMap.get(integration) : integration;
				ResMonitorInfo resMonitorInfo = new ResMonitorInfo(integration, condition, e.getServerity(), e.getMsgContent(),e.getOccurTime(),e.getMonitorId());
				list.add(resMonitorInfo);
			}
		pageResMonitorInfo.setLists(list);
		pageResMonitorInfo.setCurrentPage(pageResEvent.getCurrentPage());
		pageResMonitorInfo.setPageSize(pageResEvent.getPageSize());
		pageResMonitorInfo.setTotal(pageResEvent.getTotal());
		return pageResMonitorInfo;
	}

	@POST
	@Path("userTags")
	@Produces(MediaType.APPLICATION_JSON)
	public UserTag updateUserTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, UserTag tags) {
		if (null != tags && null != tags.getUserTags() && tags.getUserTags().length() > 0)
			TagUtil.checkUserTag(TagUtil.string2List(tags.getUserTags()));
		Resource res = ServiceManager.getInstance().getResourceService().queryResById(tags.getResourceId(), tenantId);
		boolean isUpdated = false;
		if (null != res) {
			List<String> list = new ArrayList<>();
			//Store的userTag
			try {
				Map<String, List<String>> map = ServiceManager.getInstance().getPacificResourceService().queryStoreTags(tenantId, tags.getResourceId());
				if (null != map && map.size() > 0) {
					list = map.get("userTags");
					//移除Monitor旧userTag
					if (null != list && list.size() > 0) {
						TagUtil.checkUserTag(list);
						list.removeAll(res.getUserTags());
					}
				}
			} catch (RpcException e) {
				//啥也不做
			} finally {
				//Monitor新userTag
				Collections.addAll(list, tags.getUserTags().split(";"));
				res.setUserTags(TagUtil.rmDuplicateTag(list));
				isUpdated = ServiceManager.getInstance().getResourceService().saveResourceSync(res);
				try {
					if (null != list && list.size() > 0)
						ServiceManager.getInstance().getPacificResourceService().setTags(tenantId, res.getId(), list);
				} catch (RpcException e1) {
					//啥也不做
				}
			}
		}
		if (!isUpdated)
			throw new IllegalArgumentException("update userTag failed");
		return tags;
	}

	@POST
	@Path("batchSetUserTags")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean batchSetUserTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, BatchSetUserTag tags) {
		for (String id : tags.getResId()) {
			updateUserTags(tenantId, new UserTag(id, tags.getTags()));
		}
		return true;
	}

	@POST
	@Path("batchAddUserTags")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean batchAddUserTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, List<BatchAddUserTag> userTags) {
		for (BatchAddUserTag ut : userTags) {
			List<String> temp = new ArrayList<>();
			temp.addAll(ut.getUserTags());
			Collections.addAll(temp, ut.getAddTags().split(";"));
			temp = TagUtil.rmDuplicateTag(temp);
			String tags = TagUtil.list2String(temp);
			updateUserTags(tenantId, new UserTag(ut.getResId(), tags));
		}
		return true;
	}

	@POST
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteByResId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("id") String id) {
		ServiceManager.getInstance().getResourceService().delete(tenantId, id);
	}

	@POST
	@Path("globalapply")
	@Produces(MediaType.APPLICATION_JSON)
	public Dashboard globalApply(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,TenantResTemplate template) {
		// 查询设备是linux还是window展现不同的system模板
		if(template.getAppName().endsWith("system")){
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(template.getResourceId(), template.getTenantId());
			if(null!=res.getOs())
				template.setAppName(diffSystemApp(res.getOs()));
		}
		//获取全局模板,防止重复全局引用
		TenantResTemplate globalTemp = ServiceManager.getInstance().getTenantResTemplateService()
				.getGlobalTemplate(template.getAppName(), template.getTenantId());
		if(globalTemp!=null&&globalTemp.getDashboardId().equals(template.getDashboardId())){ //如果已经有当前的全局模板，结束
			return null;
		}
		//获取自定义模板
		TenantResTemplate temp = ServiceManager.getInstance().getTenantResTemplateService()
				.getTemplate(template.getAppName(), template.getTenantId(), template.getResourceId());
		if(temp==null){	//没有自定义模板，不能推广
			return null;
		}else{//为自定义模板
			if(globalTemp!=null)//原来有全局模板，则需要将之前的全局模板删除，更新现在的模板为全局模板
				ServiceManager.getInstance().getTenantResTemplateService().delete(globalTemp);
			template.setResourceId(null);
			ServiceManager.getInstance().getTenantResTemplateService().update(template);
			return ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId());
		}
	}

	@POST
	@Path("template/update")
	@Produces(MediaType.APPLICATION_JSON)
	public Dashboard updateTemplate(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,TemplateParam tempParam) {
		TenantResTemplate template = tempParam.getTemplate();
		Dashwindow dashwindow = tempParam.getDashwindow();
		if(template.getAppName().endsWith("system")){		// 查询设备是linux还是window展现不同的system模板
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(template.getResourceId(), template.getTenantId());
			if(null!=res.getOs())
				template.setAppName(diffSystemApp(res.getOs()));
		}
		for(Request q :dashwindow.getRequests()){ //处理前端的标签，替换为$scope
			String[] qs=q.getQ().split("\\{|\\}");
			for(String s : qs){
				if(s.indexOf("host")==0||s.indexOf("ip")==0){
					q.setQ(q.getQ().replace(s, "$scope"));
				}
			}
		}
		int flag = isTemplate(template.getAppName(), template.getTenantId(),template.getResourceId());
		if (flag == 1) { //自定义模板
			ServiceManager.getInstance().getDashwindowService().updateDashwindow(dashwindow);
			return null;
		} else if(flag == 2||flag==3){ //全局模板，内置模板
			Dashboard dashboard = generateNewDashboard(template.getDashboardId(),template.getTenantId(),userId);
			Map<String,String> relation = buildRelation(ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId()), dashboard);
			for(String s : ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId()).getDashwindowIdList()){
				if(dashwindow.getId().equals(s)){
					dashwindow.setId(relation.get(s));
				}
			}
			ServiceManager.getInstance().getDashwindowService().updateDashwindow(dashwindow);
			template.setDashId(dashboard.getId());
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return dashboard;
		}else{//不确定会不会调用到，没有模板的情况
			ServiceManager.getInstance().getDashwindowService().updateDashwindow(dashwindow);
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return null;
		}
	}

	@POST
	@Path("template/create")
	@Produces(MediaType.APPLICATION_JSON)
	public Dashboard createTemplate(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,TemplateParam tempParam) {
		TenantResTemplate template = tempParam.getTemplate();
		Dashwindow dashwindow = tempParam.getDashwindow();
		if(template.getAppName().endsWith("system")){
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(template.getResourceId(), template.getTenantId());
			if(null!=res.getOs())
				template.setAppName(diffSystemApp(res.getOs()));
		}
		
		int flag = isTemplate(template.getAppName(), template.getTenantId(), template.getResourceId());
		if (flag == 1) { //自定义模板
			dashwindow.setId(UUIDTypeHandler.createUUID());
			for(Request q :dashwindow.getRequests()){
				String[] qs=q.getQ().split("\\{|\\}");
				for(String s : qs){
					if(s.indexOf("host")==0||s.indexOf("ip")==0){
						q.setQ(q.getQ().replace(s, "$scope"));
					}
				}
			}
			ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
			return ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId());
		} else if(flag==2||flag == 3){
			Dashboard dashboard = generateNewDashboard(template.getDashboardId(),template.getTenantId(),userId);
			dashwindow.setId(UUIDTypeHandler.createUUID());
			dashwindow.setDashId(dashboard.getId());
			template.setDashId(dashboard.getId());
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			for(Request q :dashwindow.getRequests()){
				String[] qs=q.getQ().split("\\{|\\}");
				for(String s : qs){
					if(s.indexOf("host")==0||s.indexOf("ip")==0){
						q.setQ(q.getQ().replace(s, "$scope"));
					}
				}
			}
			ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
			return ServiceManager.getInstance().getDashboardService().getDashboardById(dashboard.getId());
		}else{//没有模板
			dashwindow.setId(UUIDTypeHandler.createUUID());
			for(Request q :dashwindow.getRequests()){
				String[] qs=q.getQ().split("\\{|\\}");
				for(String s : qs){
					if(s.indexOf("host")==0||s.indexOf("ip")==0){
						q.setQ(q.getQ().replace(s, "$scope"));
					}
				}
			}
			ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId());
		}
	}

	@POST
	@Path("template/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Dashboard deleteDashwindowTemplate(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,TemplateParam tempParam) {
		TenantResTemplate template = tempParam.getTemplate();
		Dashwindow dashwindow = tempParam.getDashwindow();
		if(template.getAppName().endsWith("system")){
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(template.getResourceId(), template.getTenantId());
			if(null!=res.getOs())
				template.setAppName(diffSystemApp(res.getOs()));
		}
		
		int flag = isTemplate(template.getAppName(), template.getTenantId(), template.getResourceId());
		if (flag == 1) { //
			ServiceManager.getInstance().getDashwindowService().deleteDashwindow(dashwindow);
			return null;
		} else if(flag == 2||flag == 3){
			Dashboard dashboard = generateNewDashboard(template.getDashboardId(),template.getTenantId(),userId);
			Map<String,String> relation = buildRelation(ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId()), dashboard);
			for(String s : ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId()).getDashwindowIdList()){
				if(dashwindow.getId().equals(s)){
					dashwindow.setId(relation.get(s));
				}
			}
			template.setDashId(dashboard.getId());
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			ServiceManager.getInstance().getDashwindowService().deleteDashwindow(dashwindow);
			dashboard.getDashwindowIdList().remove(dashwindow.getId());
			return dashboard;
		}else {
			ServiceManager.getInstance().getDashwindowService().deleteDashwindow(dashwindow);
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return null;
		}
	}

	@POST
	@Path("template/record")
	@Produces(MediaType.APPLICATION_JSON)
	public Dashboard sortTemplate(@HeaderParam(TenantConstants.COOKIE_USERID) String userId,TemplateParam tempParam) {
		TenantResTemplate template = tempParam.getTemplate();
		Dashboard dashboard = tempParam.getDashboard();
		dashboard.setTenantId(template.getTenantId());
		if(template.getAppName().endsWith("system")){
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(template.getResourceId(), template.getTenantId());
			if(null!=res.getOs())
				template.setAppName(diffSystemApp(res.getOs()));
		}
		
		int flag = isTemplate(template.getAppName(), template.getTenantId(),template.getResourceId());
		if (flag == 1) { //
			return ServiceManager.getInstance().getDashwindowService().sortDashwindows(dashboard);
		} else if(flag==2||flag == 3){
			Dashboard newDashboard = new Dashboard();
			List<String> temp = new ArrayList<String>();
			newDashboard = generateNewDashboard(dashboard.getId(),template.getTenantId(),userId);
			Map<String,String> relation = buildRelation(ServiceManager.getInstance().getDashboardService().getDashboardById(template.getDashboardId()), newDashboard);
			for(String s : dashboard.getDashwindowIdList()){
				temp.add(relation.get(s));
			}
			template.setDashId(newDashboard.getId());
			newDashboard.setDashwindowIdList(temp.size() > 25 ? temp.subList(0, 25) : temp);
			newDashboard = ServiceManager.getInstance().getDashwindowService().sortDashwindows(newDashboard);
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return newDashboard;
		}else{
			dashboard = ServiceManager.getInstance().getDashwindowService().sortDashwindows(dashboard);
			ServiceManager.getInstance().getTenantResTemplateService().createTemplate(template);
			return dashboard;
		}
	}

	private Dashboard generateNewDashboard(String dashId,String tenantId,String userId) {
		Dashboard dashboard = ServiceManager.getInstance().getDashboardService().getDashboardById(dashId);
		// 创建新的id
		String newDashId = UUIDTypeHandler.createUUID();
		List<Dashwindow> dashtemp =ServiceManager.getInstance().getDashwindowService()
				.getDashwindowsByDashId(dashboard.getId());
		List<String> dashwindowIdList = dashboard.getDashwindowIdList();
		if(null!=dashwindowIdList)
			dashwindowIdList=dashwindowIdList.size()>25?dashwindowIdList.subList(0, 25):dashwindowIdList;
		dashboard.setId(newDashId);
		dashboard.setDashwindowIdList(null);
		dashboard.setTenantId(tenantId);
		dashboard.setUserId(userId);
		dashboard.setTemplate(false);
		dashboard.setIsResource(true);
		dashboard.setModified(new Date());
		dashboard = ServiceManager.getInstance().getDashboardService().createDashboard(dashboard);
		List<String> s = new ArrayList<String>();
		if(null!=dashwindowIdList)
		for(String dwid : dashwindowIdList){
			for (Dashwindow dashwindow : dashtemp) {
				if(dashwindow.getId().equals(dwid)){
					String newDashwindowId = UUIDTypeHandler.createUUID();
					dashwindow.setId(newDashwindowId);
					dashwindow.setDashId(dashboard.getId());
					ServiceManager.getInstance().getDashwindowService().createDashwindow(dashwindow);
					s.add(newDashwindowId);
				}
			}
		}
		dashboard.setDashwindowIdList(s);
		return dashboard;
	}

	public int isTemplate(String appName, String tenantId, String resourceId) {
		if (resourceId != null && ServiceManager.getInstance().getTenantResTemplateService().getTemplate(appName,
				tenantId, resourceId) != null) {
			return 1; // 返回1：自定义模板
		} 
		if(ServiceManager.getInstance().getTenantResTemplateService().getGlobalTemplate(appName, tenantId)!=null){
			return 2; // 返回2：全局模板
		}
		if(ServiceManager.getInstance().getDashboardService().getDashboardByTemplateName(appName)!=null){
			return 3; //返回3：内置模板
		}
		return 0; // 返回0:错误
	}

	public Map<String ,String > buildRelation(Dashboard dashboard , Dashboard newDashboard){
		 Iterator oit = dashboard.getDashwindowIdList().iterator();
		 Iterator nit = newDashboard.getDashwindowIdList().iterator();
		 Map<String,String> map = new HashMap<String, String>();
		 while(nit.hasNext()){
			 map.put((String)oit.next(),(String)nit.next());
		 }
		return map;
	}

	@POST
	@Path("tags/metrics")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> queryResTagsByMetrics(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
													 List<String> metrics) {
		List<Map<String, String>> results =
				ServiceManager.getInstance().getResourceService().queryResTagsByMetrics(tenantId, metrics);
		Set<String> resIds = new HashSet<String>();
		Set<String> resTags = new LinkedHashSet<String>();
		if (results != null && results.size() > 0) {
			for (Map<String, String> result : results) {
				String resTag = result.get("res_tag");
				if (resTag.contains(",")) {
					resTag = resTag.split(",")[0];
				}
				if (resTags.contains(resTag)) {
					continue;
				}
				resTags.add(resTag);
			}
			for (Map<String, String> result : results) {
				String resId = result.get("res_id");
				if (resIds.contains(resId)) {
					continue;
				}
				resIds.add(resId);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("resTags", resTags);
		map.put("resIds", resIds);
		return map;
	}

	@POST
	@Path("query/customize")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public ResourceMetricsList queryResAndMetricCustomize(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
														  Map<String, Object> params) {
		List<String> resTags = (ArrayList<String>) params.get("resTags");
		List<String> metricsArr = (ArrayList<String>) params.get("metrics");
		String sortField = params.get("sortField") == null ? null : params.get("sortField").toString();
		String sortOrder = params.get("sortOrder") == null ? null : params.get("sortOrder").toString();
		int pageSize = (int) params.get("pageSize");
		int current = (int) params.get("current");
		Long start = (Long) params.get("start");
		Long end = (Long) params.get("end");
		//type 取得数据类型daily昨日, weekly上周, monthly上月
		String type = String.valueOf(params.get("type"));
		List<Resource> resources = ServiceManager.getInstance().getResourceService()
				.queryTenantResByTags(tenantId, resTags, sortField, sortOrder);
		if (resources.isEmpty()) {
			return new ResourceMetricsList(0, pageSize, current);
		}
		List<ResourceMetrics> results = ServiceManager.getInstance().getMetricService()
				.queryPerfForEachResource(resources, metricsArr, tenantId, sortField, sortOrder, type, start, end);
		return resourceMetricsByPage(pageSize, current, results);
	}


	/**
	 * 手动分页
	 */
	private ResourceMetricsList resourceMetricsByPage(int pageSize, int current, List<ResourceMetrics> resourceMetrics) {
		ResourceMetricsList result = new ResourceMetricsList();
		result.setPageSize(pageSize);
		if (pageSize <= 0 || current <= 0 ||
				resourceMetrics == null || resourceMetrics.size() == 0) {
			result.setCurrentPage(1);
			result.setTotalCount(0);
			return result;
		}
		result.setTotalCount(resourceMetrics.size());
		if (resourceMetrics.size() > pageSize) {
			int totalPage = (resourceMetrics.size()  +  pageSize  - 1) / pageSize;
			if (current > totalPage) {
				current = totalPage;
			}
			result.setCurrentPage(current);
			int begin = (current - 1) * pageSize;
			int end = begin + pageSize;
			if (end >= resourceMetrics.size()) {
				end = resourceMetrics.size();
			}
			result.setLists(resourceMetrics.subList(begin, end));
		} else {
			result.setLists(resourceMetrics);
		}
		return result;
	}


	/**
	 * 临时提供front api
	 * monitor resourceId ==> store unitId
	 * 转换成统一资源库资源id
	 * @param tenantId
	 * @param resourceId
	 * @return
	 * {
	 *     "id" : "xxxx"
	 * }
	 */
	@GET
	@Path("query/store")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> queryResourceStoreId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
													@QueryParam("resourceId") String resourceId) {
		 
		if (!uyun.bat.datastore.api.util.StringUtils.isNotNullAndBlank(resourceId))
			throw new Illegalargumentexception("resourceId can not be empty！");
		String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
		if (!resourceId.matches(regex))
			throw new Illegalargumentexception("resourceId is incurrect！");
		String unitId =
				ServiceManager.getInstance().getResourceService().resIdTransform(tenantId, resourceId);
		Map<String, Object> result = new HashMap<>();
		result.put("id", unitId);
		return result;
	}

	@GET
	@Path("metricNames")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MetricMetaVO> getMetricNames(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, @QueryParam("resourceId") String resourceId) {
		 

		List<MetricMetaVO> list = new ArrayList<>();
		Map<String, List<String>> maps = new HashMap<>();
		List<String> mames = ServiceManager.getInstance().getMetricService().getMetricNamesByResId(resourceId);
		List<MetricMetaData> metricMetaDatas = ServiceManager.getInstance().getMetricMetaDataService()
				.queryAll(tenantId);
		boolean flag = false;
		List<String> temp = new ArrayList<>();
		for (String n : mames) {
			String inName = IntegrationUtil.changeMap.get(n) != null ? IntegrationUtil.changeMap.get(n)
					: (n.indexOf(".") == -1 ? n : n.substring(0, n.indexOf("."))).replace("wsnd", "websphere")
					.replace("coss", "业务").replace("business", "业务");
			List<String> ps = maps.get(inName);
			if (ps == null) {
				ps = new ArrayList<String>();
				maps.put(inName, ps);
			}
			flag = false;
			for (MetricMetaData m : metricMetaDatas)
				if (n.equals(m.getName())) {
					flag = true;
					ps.add(n + ":" + m.getcName());
				}
			if (!flag)
				ps.add(n + ":" + "");
		}
		for (String key : maps.keySet()) {
			if (IntegrationUtil.inMap.get(key) != null) {
				MetricMetaVO vo = new MetricMetaVO();
				vo.setMetaDatas(maps.get(key));
				vo.setIntegration(IntegrationUtil.inMap.get(key));
				list.add(vo);
			} else {
				temp.addAll(maps.get(key));
			}

		}
		MetricMetaVO vo = new MetricMetaVO();
		vo.setMetaDatas(temp);
		vo.setIntegration("other");
		list.add(vo);
		Collections.sort(list);
		return list;
	}

	@GET
	@Path("query/ipAll")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> queryStoreIPListByTenantId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		List<Resource> resources =
				ServiceManager.getInstance().getPacificResourceService().queryAllRes(tenantId, true);
		return resources.stream()
				.map(Resource::getIpaddr)
				.filter(s -> s != null && s.trim().length() > 0)
				.sorted()
				.collect(Collectors.toList());
	}

	@POST
	@Path("query/byIp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<SimpleResInfo> queryResourceByIpAddrs(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
													  List<String> ipaddrs) {
		List<SimpleResInfo> resources = new ArrayList<SimpleResInfo>();
		List<Resource> resourceList = ServiceManager.getInstance()
				.getPacificResourceService().queryResByIpList(tenantId, ipaddrs);
		for (Resource r : resourceList) {
			if (r != null) {
				resources.add(new SimpleResInfo(r.getIpaddr(), r.getHostname(), r.getOs(),
						r.getResTagsAll(), r.getType().getCode()));
			}
		}
		return resources;
	}

	/**
	 * 根据os区分操作系统模板
	 * 
	 * @param os
	 * @return appName
	 */
	private String diffSystemApp(String os) {
		switch (os) {
			case "linux":return "system-linux";
			case "netdev":return "system-netdev";
			
			case "unix":return "system-unix";			//不确定要不要保留
			
			/*case "aix":return "system-aix";
			case "freebsd":return "system-freebsd";
			case "hpux":return "system-hpux";
			case "irix":return "system-irix";
			case "scounix":return "system-scounix";
			case "solaris":return "system-solaris";
			case "tru64":return "system-tru64";*/
			default:return "system";
			}
	}
}
