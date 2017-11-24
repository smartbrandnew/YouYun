package uyun.bat.datastore.authorization;

import static org.junit.Assert.*;

import org.junit.Test;

import uyun.bat.datastore.Startup;

public class ResourceCleanTaskTest {
	private static ResourceCleanTask task=Startup.getInstance().getBean(ResourceCleanTask.class);
	@Test
	public void test() {
	}

}
