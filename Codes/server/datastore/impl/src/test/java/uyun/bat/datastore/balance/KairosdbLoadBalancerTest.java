package uyun.bat.datastore.balance;

import org.junit.Test;

import uyun.bat.datastore.Startup;

public class KairosdbLoadBalancerTest {
	private static KairosdbLoadBalancer balancer = Startup.getInstance().getBean(KairosdbLoadBalancer.class);

	@Test
	public void testGetKairosdbClient() {
		balancer.getKairosdbClient();
	}

	@Test
	public void testGetTscachedClient() {
		balancer.getTscachedClient();
	}

	@Test
	public void testGetTelnetClient() {
		balancer.getTelnetClient();
	}

}
