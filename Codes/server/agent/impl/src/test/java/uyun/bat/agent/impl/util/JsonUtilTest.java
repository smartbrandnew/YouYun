package uyun.bat.agent.impl.util;



import org.junit.Test;

public class JsonUtilTest {

	@Test
	public void testDecode() {
		try {
			JsonUtil.decode("", String.class);
		}catch (Exception e) {}
	}

	@Test
	public void testGetList() {try {
		JsonUtil.getList("[a,b]", String.class);
	}catch (Exception e) {}}

	@Test
	public void testEncode() {

		try {
			JsonUtil.encode("");
		}catch (Exception e) {}
	
	}

}
