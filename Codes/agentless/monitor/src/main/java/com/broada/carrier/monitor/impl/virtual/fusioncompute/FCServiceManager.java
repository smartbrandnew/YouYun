package com.broada.carrier.monitor.impl.virtual.fusioncompute;

import com.huawei.esdk.fusioncompute.local.ServiceFactory;
import com.huawei.esdk.fusioncompute.local.model.ClientProviderBean;
import com.huawei.esdk.fusioncompute.local.resources.cluster.ClusterResource;
import com.huawei.esdk.fusioncompute.local.resources.common.AuthenticateResource;
import com.huawei.esdk.fusioncompute.local.resources.common.MonitorResource;
import com.huawei.esdk.fusioncompute.local.resources.host.HostResource;
import com.huawei.esdk.fusioncompute.local.resources.net.DVSwitchResource;
import com.huawei.esdk.fusioncompute.local.resources.net.PortGroupResource;
import com.huawei.esdk.fusioncompute.local.resources.site.SiteResource;
import com.huawei.esdk.fusioncompute.local.resources.storage.DataStorageResource;
import com.huawei.esdk.fusioncompute.local.resources.vm.VmResource;

public class FCServiceManager {

	public static DVSwitchResource getdVSwitchResource(ClientProviderBean bean) {
		DVSwitchResource dVSwitchResource = ServiceFactory.getService(DVSwitchResource.class, bean);
		return dVSwitchResource;
	}

	// 获取鉴权资源类对象
	public static AuthenticateResource getUserService(ClientProviderBean bean) {
		AuthenticateResource authenticateResource = ServiceFactory.getService(AuthenticateResource.class, bean);
		return authenticateResource;
	}

	// 获取主机资源类对象
	public static HostResource getHostResource(ClientProviderBean bean) {
		HostResource hostResource = ServiceFactory.getService(HostResource.class, bean);
		return hostResource;
	}

	// 获取虚拟分布式交换机资源类对象
	public static DVSwitchResource getDVSwitchResource(ClientProviderBean bean) {
		DVSwitchResource dVSwitchResource = ServiceFactory.getService(DVSwitchResource.class, bean);
		return dVSwitchResource;
	}

	// 获取站点资源类对象
	public static SiteResource getSiteResource(ClientProviderBean bean) {
		SiteResource siteResource = ServiceFactory.getService(SiteResource.class, bean);
		return siteResource;
	}

	// 获取集群资源类对象
	public static ClusterResource getClusterResource(ClientProviderBean bean) {
		ClusterResource clusterResource = ServiceFactory.getService(ClusterResource.class, bean);
		return clusterResource;
	}

	// 获取端口组资源类对象
	public static PortGroupResource getPortGroupResource(ClientProviderBean bean) {
		PortGroupResource portGroupResource = ServiceFactory.getService(PortGroupResource.class, bean);
		return portGroupResource;
	}

	// 获取数据存储资源类对象
	public static DataStorageResource getDataStorageResource(ClientProviderBean bean) {
		DataStorageResource dataStorageResource = ServiceFactory.getService(DataStorageResource.class, bean);
		return dataStorageResource;
	}

	// 获取虚拟机资源类对象
	public static VmResource getVmResource(ClientProviderBean bean) {
		VmResource vmResource = ServiceFactory.getService(VmResource.class, bean);
		return vmResource;
	}

	// 获取监视接口
	public static MonitorResource getMonitorResource(ClientProviderBean bean) {
		MonitorResource monitorResource = ServiceFactory.getService(MonitorResource.class, bean);
		return monitorResource;
	}
}
