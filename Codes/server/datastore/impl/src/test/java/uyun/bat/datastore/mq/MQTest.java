package uyun.bat.datastore.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.PerfMetricBuilder;
import uyun.bat.datastore.api.mq.MetricInfo;
import uyun.bat.datastore.mq.MQManager;

/**
 * MQ单元测试，由于基础环境还没有集成mq，先不进行单元测试
 */
public class MQTest {
	private static MQManager manager = Startup.getInstance().getBean(MQManager.class);

	@Test
	public void testMetricSaved() {
		MetricInfo metricInfo = new MetricInfo("uyun", "1");
		List<MetricInfo> list = new ArrayList<MetricInfo>();
		list.add(metricInfo);
		manager.getMetricMQService().metricSaved(list);
	}


	//测试使用mq发送性能指标数据
	@Test
	public void testMetricsSaved() {
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		builder.addMetric("test.cpu.usage").addDataPoint(new DataPoint(System.currentTimeMillis(), 35.88))
				.addTenantId(UUID.randomUUID().toString()).addResourceId(UUID.randomUUID().toString()).addTag("host", "myPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.cpu.usage").addDataPoint(new DataPoint(System.currentTimeMillis(), 75.88))
				.addTenantId(UUID.randomUUID().toString()).addResourceId(UUID.randomUUID().toString()).addTag("yyg", "yourPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.cpu.usage").addResourceId(UUID.randomUUID().toString())
				.addDataPoint(new DataPoint(System.currentTimeMillis(), 100.88))
				.addTenantId(UUID.randomUUID().toString()).addTag("uu", "hisPC").addTag("cpu", "cpu1");
		List<PerfMetric> list = builder.getMetrics();
		manager.getMetricMQService().metricSaved("bat.datastore.metric.buffer.queue", list);
	}
}
