package uyun.bat.gateway.agent.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void test() {
		StringUtils.isNotNull("");
		StringUtils.isNotNullAndBlank("");
		StringUtils.isNotNullAndTrimBlank("");
	}

}
