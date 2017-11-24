package com.broada.carrier.monitor.server.impl.service;

import org.junit.Test;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestNodeServiceImpl {

	@Test
	public void test() {
		ServerNodeService service = TestRuntime.getServiceFactory().getNodeService();
		Page<MonitorNode> nodes = service.getNodesByGroupId(PageNo.ALL, "NetDev"); 			
		System.out.println(nodes.toString());
	}

}
