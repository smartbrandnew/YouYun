package com.broada.carrier.monitor.impl.icmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.broada.carrier.monitor.impl.common.TestMonitorContext;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.common.net.icmp.IcmpPing;

public class TestICMPMonitor {

	@Test
	public void test() {
		MonitorNode node = new MonitorNode("127.0.0.1");
		MonitorResource resource = new MonitorResource();
		MonitorTask task = new MonitorTask();
		ICMPMonitor monitor = new ICMPMonitor();
		MonitorResult mr = monitor.monitor(new TestMonitorContext(node, resource, null, task, null));
		assertEquals(MonitorState.SUCCESSED, mr.getState());
		assertEquals(1, mr.getRows().size());
		assertNotNull(mr.getRows().get(0).getIndicator(ICMPMonitor.ITEMIDX_TTLAVG));
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("java.library.path", "bin");
		System.out.println(IcmpPing.ping("127.0.0.1", 50, 1, 10));
	}
}
