package com.broada.carrier.monitor.impl.virtual.fusionmanager.top;

import com.huawei.esdk.fusionmanager.local.ServiceFactory;
import com.huawei.esdk.fusionmanager.local.model.ClientProviderBean;
import com.huawei.esdk.fusionmanager.local.resources.common.CloudInfraBatchResource;
import com.huawei.esdk.fusionmanager.local.resources.common.VdcResource;
import com.huawei.esdk.fusionmanager.local.resources.net.VPCResource;
import com.huawei.esdk.fusionmanager.local.resources.system.TopnMonitorResource;
import com.huawei.esdk.fusionmanager.local.resources.vm.VmResource;

public class FMTopServiceManager {
	
	public static TopnMonitorResource getMonitorResource(ClientProviderBean bean) {
		TopnMonitorResource resource = ServiceFactory.getService(TopnMonitorResource.class, bean);
		return resource;
	}

	public static VdcResource getVdcResource(ClientProviderBean bean) {
		VdcResource resource = ServiceFactory.getService(VdcResource.class, bean);
		return resource;
	}

	public static CloudInfraBatchResource getCloudInfraBatchResource(ClientProviderBean bean) {
		CloudInfraBatchResource resource = ServiceFactory.getService(CloudInfraBatchResource.class, bean);
		return resource;
	}

	public static VPCResource getVpcResource(ClientProviderBean bean) {
		VPCResource resource = ServiceFactory.getService(VPCResource.class, bean);
		return resource;
	}
	
	public static VmResource getVmResource(ClientProviderBean bean){
		VmResource resource = ServiceFactory.getService(VmResource.class, bean);
		return resource;
	}
}
