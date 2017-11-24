package uyun.bat.datastore.util;


import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;

public class HttpUtilTest {

	@Test
	public void testGetClient() {
		CloseableHttpClient client = HttpUtil.getClient();
		Assert.assertNotNull(client);
	}


	@Test
	public void testGet() {
		/*
		String ip = Config.getInstance().get("kairosdb.ipaddr", "localhost");
		int port = Config.getInstance().get("kairosdb.http.port", 8080);
		HttpUtil.get(String.format("http://%s:%d/api/v1/metricNames", ip, port));
		Assert.assertNotNull(true);
		*/
	}

}
