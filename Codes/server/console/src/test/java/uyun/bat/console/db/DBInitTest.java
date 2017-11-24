package uyun.bat.console.db;

import org.junit.Test;
import uyun.bat.console.env.Startup;

public class DBInitTest {
	@Test
	public void testInit() {
		Startup.getInstance().startup();
	}
}
