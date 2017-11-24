package com.broada.carrier.monitor.method.cli;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.broada.component.utils.text.DateUtil;

public class TestCLIDateFormat {

	@Test
	public void test() {
		Date date = CLIDateFormat.format("2011/12/13_星期二_下午_1:51:26");
		assertEquals("2011-12-13 13:51:26", DateUtil.format(date, DateUtil.PATTERN_YYYYMMDD_HHMMSS));
	}

	@Test
	public void test1() {
		Calendar c = Calendar.getInstance();
		assertEquals("2007-11-07 11:12:52", DateUtil.format(CLIDateFormat.format("2007-11-7_11:12:52")));
		assertEquals("2006-12-28 17:01:00", DateUtil.format(CLIDateFormat.format("Dec 28 17:01 2006")));
		assertEquals("2006-12-28 17:01:00", DateUtil.format(CLIDateFormat.format("28 Dec 17:01 2006")));	
		assertEquals("2013-01-22 19:08:11", DateUtil.format(CLIDateFormat.format("2013/1/22_星期二_19:08:11")));
		assertEquals(c.get(Calendar.YEAR) + "-04-26 21:01:00", DateUtil.format(CLIDateFormat.format("Apr 26 21:01")));
		assertEquals(c.get(Calendar.YEAR) + "-05-20 22:04:00", DateUtil.format(CLIDateFormat.format("5 20 22:04")));
		assertEquals(DateUtil.format(new Date()).substring(0, 11) + "21:01:00", DateUtil.format(CLIDateFormat.format("21:01")));				
	}

}
