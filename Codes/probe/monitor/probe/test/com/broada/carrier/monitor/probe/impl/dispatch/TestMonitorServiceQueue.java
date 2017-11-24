package com.broada.carrier.monitor.probe.impl.dispatch;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.component.utils.text.DateUtil;

public class TestMonitorServiceQueue {

	@Test
	public void test() throws ParseException {
		MonitorTaskQueue queue = new MonitorTaskQueue();
		
		Date now = DateUtil.parse("2014-01-01 00:00:00");
		
		MonitorTask srv = new MonitorTask();
		srv.setId("1");
		MonitorPolicy policy = new MonitorPolicy();
		policy.setInterval(600);
		policy.setErrorInterval(60);
		MonitorRecord record = new MonitorRecord();
		record.setTime(DateUtil.parse("2014-01-01 00:00:00"));
		record.setState(MonitorState.SUCCESSED);
		queue.add(new MonitorTaskItem(srv, policy, record, now));
		
		srv = new MonitorTask();
		srv.setId("2");
		policy = new MonitorPolicy();
		policy.setInterval(600);
		policy.setErrorInterval(60);
		record = new MonitorRecord();
		record.setTime(DateUtil.parse("2014-01-01 00:00:00"));
		record.setState(MonitorState.FAILED);
		queue.add(new MonitorTaskItem(srv, policy, record, now));
		
		assertEquals(2, queue.get(0).getTask().getId());
		assertEquals(1, queue.get(1).getTask().getId());
	}

	@Test
	public void test1() throws ParseException {
		MonitorTaskQueue queue = new MonitorTaskQueue();
		
		Date now = DateUtil.parse("2014-01-01 00:00:00");
		
		MonitorTask srv = new MonitorTask();
		srv.setId("1");
		MonitorPolicy policy = new MonitorPolicy();
		policy.setInterval(600);
		policy.setErrorInterval(60);
		MonitorRecord record = new MonitorRecord();
		record.setTime(DateUtil.parse("2014-01-01 00:00:00"));
		record.setState(MonitorState.SUCCESSED);
		queue.add(new MonitorTaskItem(srv, policy, record, now));
		
		srv = new MonitorTask();
		srv.setId("2");
		policy = new MonitorPolicy();
		policy.setInterval(600);
		policy.setErrorInterval(60);
		policy.setWorkWeekDays("456");
		record = new MonitorRecord();
		record.setTime(DateUtil.parse("2014-01-01 00:00:00"));
		record.setState(MonitorState.FAILED);		
		queue.add(new MonitorTaskItem(srv, policy, record, now));		
		assertEquals(1, queue.get(0).getTask().getId());
		assertEquals(2, queue.get(1).getTask().getId());
	}
}
