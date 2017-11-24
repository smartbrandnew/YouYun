package uyun.bat.agent.impl.autosync.common;




import org.junit.Assert;
import org.junit.Test;

import uyun.bat.agent.impl.autosync.common.Md5Util;

public class Md5UtilTest {
	@Test
	public void testDigestFile() {
		
	}

	@Test
	public void testDigestYamlFile() {
		
	}

	@Test
	public void testDigestString() {
		String json="测试md5上传/下载";
		String sign1=Md5Util.digest(json);
		String sign2=Md5Util.digest(json);
		Assert.assertEquals(sign1, sign2);
	}

}
