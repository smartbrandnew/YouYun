package uyun.bat.monitor.impl.common;

import uyun.bat.common.proxy.ProxyFactory;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.event.api.service.EventService;
import uyun.bird.notify.api.NotifyService;
import uyun.bird.notify.api.imsg.IMessageService;
import uyun.bird.tenant.api.TenantService;
import uyun.bird.tenant.api.UserService;

public abstract class ServiceManager {

	private static ServiceManager instance = new ServiceManager() {
	};

	public static ServiceManager getInstance() {
		return instance;
	}

	private EventService eventService;

	private MetricService metricService;

	private ResourceService resourceService;

	private NotifyService notifyService;
	private IMessageService iMessageService;

	private UserService userService;

	private StateService stateService;

	private MetricMetaDataService metricMetaDataService;

	private TenantService tenantService;

	public StateService getStateService() {
		return stateService;
	}

	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	public UserService getUserService() {
		if (userService == null)
			userService = ProxyFactory.createProxy(UserService.class);
		return userService;
	}

	public NotifyService getNotifyService() {
		if (notifyService == null)
			notifyService = ProxyFactory.createProxy(NotifyService.class);
		return notifyService;
	}

	public IMessageService getiMessageService() {
		if (iMessageService == null)
			iMessageService = ProxyFactory.createProxy(IMessageService.class);
		return iMessageService;
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

	public TenantService getTenantService() {
		if (tenantService == null)
			tenantService = ProxyFactory.createProxy(TenantService.class);
		return tenantService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}
}
