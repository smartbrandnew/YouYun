package uyun.bat.dashboard.impl.util;


import org.junit.Test;

public class JsonUtilTest {

	@Test
	public void testDecode() {
		try {
			JsonUtil.decode(null,String.class);
		}catch (Exception e) {}
	}

	@Test
	public void testGetList() {try {
		JsonUtil.getList("[a,b]", String.class);
	}catch (Exception e) {}}
	
	@Test
	public void test1GetList() {try {
		JsonUtil.getList(null, String.class);
	}catch (Exception e) {}}

	@Test
	public void testEncode() {
		try {
			JsonUtil.encode("");
		}catch (Exception e) {}
	}
	
	@Test
	public void test1Encode() {
		try {
			JsonUtil.encode(null);
		}catch (Exception e) {}
	}
}
