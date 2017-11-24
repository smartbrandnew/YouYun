package com.broada.carrier.monitor.common.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class TestTextUtil {

	@Test
	public void test() {
		String[] lines = TextUtil.splitLines(null);
		assertEquals(0, lines.length);
	
		lines = TextUtil.splitLines("abc\ndef");
		assertEquals(2, lines.length);
		assertEquals("abc", lines[0]);
		assertEquals("def", lines[1]);
		
		lines = TextUtil.splitLines("abc\r\ndef");
		assertEquals(2, lines.length);
		assertEquals("abc", lines[0]);
		assertEquals("def", lines[1]);
	}

	@Test
	public void testSplit() throws UnsupportedEncodingException {
		for (int i = 0; i < 10000; i++) {
		assertArrayEquals(new String[] { "abcdefg" }, TextUtil.split("abcdefg", 10));		
		assertArrayEquals(new String[] { "中文abcd" }, TextUtil.split("中文abcd", 8));		
		assertArrayEquals(new String[] { "一二(1/4)", "三四(2/4)", "五abc(3/4)", "六(4/4)" }, TextUtil.split("一二三四五abc六", 10));
		assertArrayEquals(new String[] { "rem 短信网关告警通知批处理\nrem 使用方法：\nrem 1. 首先修改本批处理(1/6)", 
				"，选择合适用户的通知方式\nrem    通过rem注释snmptrap或sendmail，可(2/6)",
				"确定禁用哪种通知方式。\nrem    同时修改通知方式中相应的参数。\nrem (3/6)", 
				"2. 网关服务出现故障后，会自动调用此批处理完成通知\nrem 3. 手工执行(4/6)", 
				"测试方法\nrem    alert.bat 时间 内容\nrem    使用举例：alert.bat \"2(5/6)", 
				"014-07-29 09:10:01\" \"网关服务进程sms-gateway存在故障重启\"(6/6)" }, 
				TextUtil.split("rem 短信网关告警通知批处理\n"
				+ "rem 使用方法：\n"
				+ "rem 1. 首先修改本批处理，选择合适用户的通知方式\n"
				+ "rem    通过rem注释snmptrap或sendmail，可确定禁用哪种通知方式。\n"
				+ "rem    同时修改通知方式中相应的参数。\n"
				+ "rem 2. 网关服务出现故障后，会自动调用此批处理完成通知\n"
				+ "rem 3. 手工执行测试方法\n"
				+ "rem    alert.bat 时间 内容\n"
				+ "rem    使用举例：alert.bat \"2014-07-29 09:10:01\" \"网关服务进程sms-gateway存在故障重启\"", 70));
		try {
			TextUtil.split("一二三四五六", 6);
			fail("当maxBytes <= 分页信息本身的长度时应当进行告警，这种情况下无法进行分页");
		} catch (IllegalArgumentException e) {
		}
		}
	}

	@Test
	public void testTruncate() {
		for (int i = 0; i < 10000; i++) {
		assertEquals("abcdefg", TextUtil.truncate("abcdefg", 10));
		assertEquals("中文测试", TextUtil.truncate("中文测试", 8));
		assertEquals("一(省5字)", TextUtil.truncate("一二三四五六", 10));
		assertEquals("rem 短信网关告警通知批处理\nrem 使用方法：\nrem 1. 首先修改本批(省222字)", TextUtil.truncate("rem 短信网关告警通知批处理\n"
				+ "rem 使用方法：\n"
				+ "rem 1. 首先修改本批处理，选择合适用户的通知方式\n"
				+ "rem    通过rem注释snmptrap或sendmail，可确定禁用哪种通知方式。\n"
				+ "rem    同时修改通知方式中相应的参数。\n"
				+ "rem 2. 网关服务出现故障后，会自动调用此批处理完成通知\n"
				+ "rem 3. 手工执行测试方法\n"
				+ "rem    alert.bat 时间 内容\n"
				+ "rem    使用举例：alert.bat \"2014-07-29 09:10:01\" \"网关服务进程sms-gateway存在故障重启\"", 70));
		try {
			TextUtil.truncate("一二三四五六", 5);
			fail("当maxBytes <= 截断信息本身的长度时应当进行告警，这种情况下无法进行截断");
		} catch (IllegalArgumentException e) {
		}
		}
	}
}
