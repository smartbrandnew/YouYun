package com.broada.carrier.monitor.server.impl.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;
import com.broada.carrier.monitor.server.impl.TestRuntime;

public class TestTaskService {

	@Test
	public void test() {
		ServerTaskService service = TestRuntime.getServiceFactory().getTaskService();
		Page<MonitorTask> page = service.getTasksByProbeId(PageNo.ALL, 0, false);
		assertEquals(0, page.getRows().length);
		MonitorTask[] tasks = service.getTasksByNodeId("");
		assertEquals(0, tasks.length);
		tasks = service.getTasksByResourceId("");
		assertEquals(0, tasks.length);
	}

}
