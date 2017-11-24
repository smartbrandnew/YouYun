package uyun.bat.datastore.api.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testIsNotNullAndTrimBlank() {
		StringUtils.isNotNullAndTrimBlank("");
	}

	@Test
	public void testIsNotNullAndBlank() {
		StringUtils.isNotNullAndBlank("");
	}

	@Test
	public void testIsBlank() {
		StringUtils.isBlank("");
	}

	@Test
	public void testIsNotBlank() {
		StringUtils.isNotBlank("");
	}

	@Test
	public void testIsNotNull() {
		StringUtils.isNotNull("");
	}

}
