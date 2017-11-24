package com.broada.carrier.monitor.server.api.entity;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.component.utils.text.DateUtil;

public class TestMonitorPolicy {

	@Test
	public void test() throws ParseException {
		MonitorPolicy policy = new MonitorPolicy();
		TimeRange timeRange = policy.retWorkTimeRange();
		assertEquals("00:00:00", DateUtil.format(new Date(timeRange.getStart()), MonitorPolicy.WORK_TIME_FORMAT));
		assertEquals("23:59:59", DateUtil.format(new Date(timeRange.getEnd()), MonitorPolicy.WORK_TIME_FORMAT));
		
		policy.putWorkTimeRange(new TimeRange(DateUtil.parse("00:00:00", MonitorPolicy.WORK_TIME_FORMAT), DateUtil.parse("23:59:59", MonitorPolicy.WORK_TIME_FORMAT)));
		assertEquals(timeRange, policy.retWorkTimeRange());
	}

}
