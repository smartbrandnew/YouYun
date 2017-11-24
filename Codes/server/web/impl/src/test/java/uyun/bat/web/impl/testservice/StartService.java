package uyun.bat.web.impl.testservice;

import com.alibaba.dubbo.config.annotation.Service;

import uyun.bat.web.impl.Startup;
import uyun.bat.web.impl.common.service.ServiceManager;

public class StartService {
	static {
		Startup.getInstance().startup();
		ServiceManager.getInstance().setDashboardService(new DashbrodServiceTest());
		ServiceManager.getInstance().setFavouriteService(new FavouriteServiceTest());
		ServiceManager.getInstance().setDashwindowService(new DashwindowServiceTest());
		ServiceManager.getInstance().setEventService(new EventServiceTest());
		ServiceManager.getInstance().setMetricMetaDataService(new MetricMetaDataServiceTest());
		ServiceManager.getInstance().setMetricService(new MetricServiceTest());
		ServiceManager.getInstance().setMonitorService(new MonitorServiceTest());
		ServiceManager.getInstance().setResourceService(new ResourceServiceTest());
		ServiceManager.getInstance().setStateService(new StateServiceTest());
		ServiceManager.getInstance().setTagService(new TagServiceTest());
		ServiceManager.getInstance().setStateMetricService(new StateMetricSerrviceTest());
		ServiceManager.getInstance().setYamlFileService(new YamlFileServiceTest());
		ServiceManager.getInstance().setTenantResTemplateService(new TenantResTemplateServiceTest());
		ServiceManager.getInstance().setOverviewService(new OverviewServiceTest());
	}
}
