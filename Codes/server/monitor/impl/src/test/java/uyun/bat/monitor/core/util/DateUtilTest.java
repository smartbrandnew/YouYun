package uyun.bat.monitor.core.util;


import java.util.Date;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void test() {
		
		Date date = new Date();
		String sDateTime = DateUtil.formatDateTime(date);
		String sSimpleTime = DateUtil.formatSimpleTime(date);
		System.out.println(sDateTime);
		System.out.println(sSimpleTime);
	}

}
