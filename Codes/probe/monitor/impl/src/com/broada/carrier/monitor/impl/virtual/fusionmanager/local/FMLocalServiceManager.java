package com.broada.carrier.monitor.impl.virtual.fusionmanager.local;

import com.huawei.esdk.fm.local.local.ServiceFactory;
import com.huawei.esdk.fm.local.local.model.ClientProviderBean;
import com.huawei.esdk.fm.local.local.resources.compute.ClusterResource;
import com.huawei.esdk.fm.local.local.resources.system.LocalMonitorResource;
import com.huawei.esdk.fm.local.local.resources.vm.VmResource;

public class FMLocalServiceManager {
	public static LocalMonitorResource getMonitorResource(ClientProviderBean bean) {
		LocalMonitorResource resource = ServiceFactory.getService(LocalMonitorResource.class, bean);
		return resource;
	}

	public static ClusterResource getClusterResource(ClientProviderBean bean) {
		ClusterResource resource = ServiceFactory.getService(ClusterResource.class, bean);
		return resource;
	}

	public static VmResource getVmResource(ClientProviderBean bean) {
		VmResource resource = ServiceFactory.getService(VmResource.class, bean);
		return resource;
	}
}
