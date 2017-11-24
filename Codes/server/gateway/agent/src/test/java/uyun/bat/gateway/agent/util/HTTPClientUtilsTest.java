package uyun.bat.gateway.agent.util;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Test;

public class HTTPClientUtilsTest {

	//private static String URL = "https://cmdb.uyuntest.cn/openapi/v2/repo/cis";
	private static String URL = "https://www.baidu.com";
	private static String json = "{\"classCode\":\"Y-SERVER\",\"Y_ip\":\"unknown\",\"source\":{\"uid\":\"30b70f62ac8a182264b138ce565e82c2\",\"code\":\"monitor\",\"state\":\"未监控\"},\"Y_name\":\"VOOHMVDB-unknown\"}";
	@Test
	public void test1Post() throws Exception {
		HTTPClientUtils.post(URL,json);
	}
	
	@Test
	public void test2Post() throws Exception{
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		header.put("Cache-Control", "no-cache");
		header.put("MOBILE_DEVICE", "ANDROID_PHONE");
		HTTPClientUtils.post(URL, header, json);
	}

	@Test
	public void test3Post() throws ConnectTimeoutException, SocketTimeoutException, Exception{
		Map<String, String> header = new HashMap<String, String>();
		header.put("sysCode", "Monitor");
		header.put("apikey", "43e638f0f96643bc83c9620d12a07037");
		HTTPClientUtils.post(URL, header, json,null, "utf-8", 10000, 10000);
	}
	
	@Test
	public void testpostForm() throws ConnectTimeoutException, SocketTimeoutException, Exception{
		Map<String, String> header = new HashMap<String, String>();
		header.put("sysCode", "Monitor");
		header.put("apikey", "43e638f0f96643bc83c9620d12a07037");
		
		Map<String, String> params = new HashMap<String, String>();
		header.put("sysCode", "Monitor");
		header.put("apikey", "43e638f0f96643bc83c9620d12a07037");
		HTTPClientUtils.postForm(URL, params, header, 10000, 10000);
	}
	
	@Test
	public void testGet() throws Exception{
		HTTPClientUtils.get(URL, "utf-8",10000,10000);
	}
}
