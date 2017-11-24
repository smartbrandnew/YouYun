package uyun.bat.datastore.mq.macro.metric;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.PerfMetricBuilder;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.mq.macro.MacroManager;

public class OpenAPIMetricMacroTest {
	static{
		Startup.getInstance().startup();
	}
	private static OpenAPIMetricMacro macro=  (OpenAPIMetricMacro) MacroManager.getInstance().getMetricMacro(ComplexMetricData.TYPE_OPENAPI);
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "12345678910111213141516171819202";
	private static final String RESOURCE_ID = "12345678910111213141516171819203";

//	@Test
	public void testGetCode() {
		macro.getCode();
	}

//	@Test
	public void testExec() {
		List<String> tags = new ArrayList<String>();
    tags.add("host:myPC");
    List<String> apps = new ArrayList<String>();
    apps.add("oracle");
    String id = UUID.randomUUID().toString();
		Resource resource = new Resource(id, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据......", UUID
				.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(), new Date(),
				"winiodws", tags, new ArrayList<String>(), new ArrayList<String>());

		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		builder.addMetric("test.oracle.usage").addDataPoint(new DataPoint(System.currentTimeMillis() - 1000, 35.88))
				.addTenantId(TENANT_ID).addResourceId(RESOURCE_ID).addTag("host", "myPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.redis.usage").addDataPoint(new DataPoint(System.currentTimeMillis() - 1000, 75.88))
				.addTenantId(TENANT_ID).addResourceId(RESOURCE_ID).addTag("host", "myPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.cpu.usage").addResourceId(RESOURCE_ID)
				.addDataPoint(new DataPoint(System.currentTimeMillis() - 1000, 100.88))
				.addTenantId(TENANT_ID).addTag("host", "myPC").addTag("cpu", "cpu1");
		List<PerfMetric> list = builder.getMetrics();
		ComplexMetricData data=new ComplexMetricData(resource, list, ComplexMetricData.TYPE_OPENAPI);
		macro.exec(data);
	}
}
