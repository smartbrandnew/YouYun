package uyun.bat.datastore.api.util;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

public class UUIDUtilsTest {

	@Test
	public void testGenerateResId() {
		UUIDUtils.generateResId(UUID.randomUUID().toString(), "");
	}

}
