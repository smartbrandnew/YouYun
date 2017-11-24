package uyun.bat.web.impl.common.service;

import uyun.bat.agent.api.service.AgentService;
import uyun.bat.agent.api.service.YamlFileService;
import uyun.bat.common.proxy.ProxyFactory;
import uyun.bat.dashboard.api.service.DashboardService;
import uyun.bat.dashboard.api.service.DashwindowService;
import uyun.bat.dashboard.api.service.TenantResTemplateService;
import uyun.bat.datastore.api.overview.service.OverviewService;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.service.PacificResourceService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.service.StateMetricService;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.datastore.api.service.TagService;
import uyun.bat.event.api.service.EventService;
import uyun.bat.favourite.api.service.FavouriteService;
import uyun.bat.monitor.api.service.MonitorService;
import uyun.bat.report.api.service.ReportService;
import uyun.bird.tenant.api.ProductService;
import uyun.bird.tenant.api.TenantService;
import uyun.bird.tenant.api.UserService;

public abstract class ServiceManager {
	private static ServiceManager instance = new ServiceManager() {
	};

	public static ServiceManager getInstance() {
		return instance;
	}

	/**
	 * 集成租户
	 */
	private ProductService productService;
	private UserService userService;
	private TenantService tenantService;

	private DashboardService dashboardService;

	private DashwindowService dashwindowService;

	private FavouriteService favouriteService;

	private MonitorService monitorService;

	private MetricService metricService;

	private ResourceService resourceService;

	private PacificResourceService pacificResourceService;

	private EventService eventService;

	private MetricMetaDataService metricMetaDataService;
	
	private TagService tagService;
	
	private StateService stateService;

	private StateMetricService stateMetricService;
	
	private TenantResTemplateService tenantResTemplateService;
	
	private YamlFileService yamlFileService;

	private AgentService agentService;

	private OverviewService overviewService;

	private ReportService reportService;

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	public OverviewService getOverviewService() {
		return overviewService;
	}

	public void setOverviewService(OverviewService overviewService) {
		this.overviewService = overviewService;
	}

	public AgentService getAgentService() {
		return agentService;
	}

	public void setAgentService(AgentService agentService) {
		this.agentService = agentService;
	}

	public StateMetricService getStateMetricService() {
		return stateMetricService;
	}

	public void setStateMetricService(StateMetricService stateMetricService) {
		this.stateMetricService = stateMetricService;
	}

	public StateService getStateService() {
		return stateService;
	}

	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	public TagService getTagService() {
		return tagService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	public ProductService getProductService() {
		if (productService == null)
			productService = ProxyFactory.createProxy(ProductService.class);
		return productService;
	}

	public UserService getUserService() {
		if (userService == null)
			userService = ProxyFactory.createProxy(UserService.class);
		return userService;
	}

	public TenantService getTenantService() {
		if (tenantService == null)
			tenantService = ProxyFactory.createProxy(TenantService.class);
		return tenantService;
	}

	public DashboardService getDashboardService() {
		return dashboardService;
	}

	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	public DashwindowService getDashwindowService() {
		return dashwindowService;
	}

	public void setDashwindowService(DashwindowService dashwindowService) {
		this.dashwindowService = dashwindowService;
	}

	public FavouriteService getFavouriteService() {
		return favouriteService;
	}

	public void setFavouriteService(FavouriteService favouriteService) {
		this.favouriteService = favouriteService;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public MetricService getMetricService() {
		return metricService;
	}

	public void setMetricService(MetricService metricService) {
		this.metricService = metricService;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public PacificResourceService getPacificResourceService() {
		return pacificResourceService;
	}

	public void setPacificResourceService(PacificResourceService pacificResourceService) {
		this.pacificResourceService = pacificResourceService;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public MetricMetaDataService getMetricMetaDataService() {
		return metricMetaDataService;
	}

	public void setMetricMetaDataService(MetricMetaDataService metricMetaDataService) {
		this.metricMetaDataService = metricMetaDataService;
	}

	public YamlFileService getYamlFileService() {
		return yamlFileService;
	}

	public void setYamlFileService(YamlFileService yamlFileService) {
		this.yamlFileService = yamlFileService;
	}

	public TenantResTemplateService getTenantResTemplateService() {
		return tenantResTemplateService;
	}

	public void setTenantResTemplateService(TenantResTemplateService tenantResTemplateService) {
		this.tenantResTemplateService = tenantResTemplateService;
	}

}
