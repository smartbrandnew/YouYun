package uyun.bat.monitor.core.util;

import org.junit.Test;

public class EncryptUtilTest {

	private byte []src = {1,2,3};
	private String inStr = "123test";
	private String testString;
	@Test
	public void testHash() {
		
		Long longHash = EncryptUtil.hash(src);
		System.out.println(longHash);
		
	}
	
	@Test
	public void testString2MD5(){
		testString = EncryptUtil.string2MD5(inStr);
		System.out.println(testString);
	}
	
	@Test
	public void testConvertMD5(){
		testString = EncryptUtil.convertMD5(inStr);
		System.out.println(testString);
	}

}
