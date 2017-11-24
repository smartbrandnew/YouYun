package uyun.bat.event.impl.common;

import uyun.bat.common.proxy.ProxyFactory;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.monitor.api.service.MonitorService;
import uyun.bird.tenant.api.ProductService;
import uyun.bird.tenant.api.TenantService;

public abstract class ServiceManager {

	private static ServiceManager instance = new ServiceManager() {
	};

	public static ServiceManager getInstance() {
		return instance;
	}

	private ResourceService resourceService;
	private MonitorService monitorService;
	private MetricMetaDataService metricMetaDataService;
	private TenantService tenantService;
	private ProductService productService;

	public TenantService getTenantService() {
		if (tenantService == null)
			tenantService = ProxyFactory.createProxy(TenantService.class);
		return tenantService;
	}


	public ProductService getProductService() {
		if (productService == null)
			productService = ProxyFactory.createProxy(ProductService.class);
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public MetricMetaDataService getMetricMetaDataService() {
		return metricMetaDataService;
	}

	public void setMetricMetaDataService(MetricMetaDataService metricMetaDataService) {
		this.metricMetaDataService = metricMetaDataService;
	}
}
