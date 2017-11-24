package com.broada.carrier.monitor.probe.impl.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.probe.impl.TestRuntime;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class TestNodeService {

	@Test
	public void test() {
		ProbeNodeService service = TestRuntime.getServiceFactory().getNodeService();
		
		MonitorNode[] nodes = service.getNodes();
		assertNotNull(nodes);
		
		MonitorNode node = new MonitorNode("192.168.0.1");		
		node.setId("1");
		node.setTypeId("Switch");
		service.saveNode(node);
		
		nodes = service.getNodes();
		MonitorNode node2 = getNode(nodes, node.getId());		
		assertEquals(node, node2);
		
		service.deleteNode(node.getId());
		nodes = service.getNodes();
		node2 = getNode(nodes, node.getId());		
		assertNull(node2);
	}

	private MonitorNode getNode(MonitorNode[] nodes, String code) {
		for (MonitorNode node : nodes) 
			if (node.getId().equals(code))
				return node;
		return null;
	}

}
