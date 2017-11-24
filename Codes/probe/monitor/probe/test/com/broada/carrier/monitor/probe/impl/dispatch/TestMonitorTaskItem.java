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

public class TestMonitorTaskItem {
	@Test
	public void test() throws ParseException {
		MonitorTask task = new MonitorTask();
		MonitorPolicy policy = new MonitorPolicy();
		MonitorRecord record = new MonitorRecord();
		
		policy.setInterval(600);
		policy.setErrorInterval(60);
		
		// 测试一般情况
		record.setTime(DateUtil.parse("2014-12-04 12:35:00"));
		assertNextRunTime(task, policy, record, "2014-12-04 12:45:00");			
		record.setTime(DateUtil.parse("2014-12-04 12:45:00"));		
		assertNextRunTime(task, policy, record, "2014-12-04 12:55:00");			
		
		// 测试监测失败的情况
		record.setState(MonitorState.FAILED);
		assertNextRunTime(task, policy, record, "2014-12-04 12:46:00");
		assertNextRunTime(task, policy, record, "2014-12-04 13:40:00", "2014-12-04 13:40:00");		
		
		// 测试有监测时间的情况
		policy.setWorkTimeRange("13:00:00~14:00:00");
		assertNextRunTime(task, policy, record, "2014-12-04 13:00:00");					
		record.setTime(DateUtil.parse("2014-12-04 12:59:34"));
		assertNextRunTime(task, policy, record, "2014-12-04 13:00:34");				
		
		// 测试追加周一与周三监测的情况		
		policy.setWorkWeekDays("13");
		assertNextRunTime(task, policy, record, "2014-12-08 13:00:00");					
		record.setTime(DateUtil.parse("2014-12-08 15:59:34"));
		assertNextRunTime(task, policy, record, "2014-12-10 13:00:00");
		
		// 测试追加停止监测时间段的情况
		policy.setStopTimeRanges(""
				+ "2014-12-01~2014-12-15\n"
				+ "2014-12-16 13:30:00~2014-12-16 13:50:00\n"
				+ "2014-12-17 13:30:00~2014-12-17 13:50:00\n");
		assertNextRunTime(task, policy, record, "2014-12-15 13:00:00");
		assertNextRunTime(task, policy, record, "2014-12-17 13:00:00", "2014-12-16 13:29:30");
		assertNextRunTime(task, policy, record, "2014-12-17 13:29:30", "2014-12-17 13:29:30");
		record.setTime(DateUtil.parse("2014-12-17 13:29:30"));
		assertNextRunTime(task, policy, record, "2014-12-17 13:50:00");
		
		// 测试一个监测任务一天有多个停止时间段的情况
		record.setTime(new Date(0));
		record.setState(MonitorState.SUCCESSED);
		policy = new MonitorPolicy();
		policy.setInterval(600);		
		policy.setStopTimeRanges(""
				+ "2014-12-01 08:00:00~2014-12-01 09:00:00\n"
				+ "2014-12-01 11:30:00~2014-12-01 12:45:00\n");		
		assertNextRunTime(task, policy, record, "2014-12-01 01:00:00", "2014-12-01 01:00:00");
		assertNextRunTime(task, policy, record, "2014-12-01 09:00:00", "2014-12-01 08:15:00");
		record.setTime(DateUtil.parse("2014-12-01 11:29:00"));
		assertNextRunTime(task, policy, record, "2014-12-01 12:45:00");
		
		// 测试超大的监测周期
		policy = new MonitorPolicy();
		policy.setInterval(3600 * 24 * 3);
		policy.setWorkTimeRange("08:00:00~10:00:00");
		policy.setWorkWeekDays("13");
		policy.setStopTimeRanges(""
				+ "2014-12-15 00:00:00~2014-12-16 23:00:00\n");				
		record.setTime(DateUtil.parse("2014-12-01 00:00:00"));
		assertNextRunTime(task, policy, record, "2014-12-08 08:00:00");
		record.setTime(DateUtil.parse("2014-12-08 08:00:00"));
		assertNextRunTime(task, policy, record, "2014-12-17 08:00:00");
		
		// 测试超超大的监测周期
		policy = new MonitorPolicy();
		policy.setInterval(3600 * 24 * 25);
		record.setTime(DateUtil.parse("2014-12-01 00:00:00"));
		assertNextRunTime(task, policy, record, "2014-12-26 00:00:00");
		record.setTime(DateUtil.parse("2014-12-08 08:00:00"));
		assertNextRunTime(task, policy, record, "2015-01-02 08:00:00");		
	}

	private void assertNextRunTime(MonitorTask task, MonitorPolicy policy, MonitorRecord record, String expect) throws ParseException {
		assertNextRunTime(task, policy, record, expect, "2000-01-01 00:00:00");
	}
	
	private void assertNextRunTime(MonitorTask task, MonitorPolicy policy, MonitorRecord record, String expect, String now) throws ParseException {
		MonitorTaskItem item = new MonitorTaskItem(task, policy, record, DateUtil.parse(now));
		assertEquals(DateUtil.parse(expect), new Date(item.getNextRunTime()));
	}
}
