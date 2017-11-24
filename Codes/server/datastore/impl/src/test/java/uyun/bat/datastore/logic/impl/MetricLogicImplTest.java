package uyun.bat.datastore.logic.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.PerfMetricBuilder;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.entity.QueryMetric.Order;
import uyun.bat.datastore.api.entity.TimeUnit;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.datastore.api.util.DateUtil;
import uyun.bat.datastore.logic.MetricLogic;

public class MetricLogicImplTest {
	private static MetricLogic metricLogic = (MetricLogic) Startup.getInstance().getBean("metricLogic");
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39c77";
	private static final String RESOURCE_ID = "12345678910111213141233171819203";
	private static final String RESOURCE_ID2 = "12345678910111213141516171819203";
	private static final String RESOURCE_ID3 = "12333333310111213141233171819203";

	@Test
	public void testInsertListOfPerfMetric() {
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		builder.addMetric("test.oracle.usage").addDataPoint(new DataPoint(DateUtil.getAnyDate(-1).getTime(), 45.88))
				.addTenantId(TENANT_ID).addResourceId(RESOURCE_ID).addTag("host", "myPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.redis.usage").addDataPoint(new DataPoint(DateUtil.getAnyDate(-1).getTime(), 25.88))
				.addTenantId(TENANT_ID).addResourceId(RESOURCE_ID).addTag("host", "myPC")
				.addTag("host", "cpu1");
		builder.addMetric("test.cpu.usage").addResourceId(RESOURCE_ID)
				.addDataPoint(new DataPoint(DateUtil.getAnyDate(-1).getTime(), 111.44))
				.addTenantId(TENANT_ID).addTag("host", "myPC").addTag("cpu", "cpu1");
		List<PerfMetric> list = builder.getMetrics();
		long count = metricLogic.insertPerf(list);
		Assert.assertEquals(count, list.size());
	}



	@Test
	public void testDelete() {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(24, uyun.bat.datastore.api.entity.TimeUnit.HOURS).addMetric("test.cpu.usage")
				.addTag("host", "myPC").addTenantId("tom");
		metricLogic.delete(builder);

	}

	@Test
	public void testQueryPerf() {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID).addTag("host", "myPC")
				.addAggregatorType(AggregatorType.max);
		metricLogic.queryPerf(builder);

	}

	@Test
	public void testQuerySeries() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addTag("host", "myPC");
		metric.addAggregatorType(AggregatorType.max);
		metric.setLimit(Integer.MAX_VALUE);
		metricLogic.querySeries(builder, 30, null);
	}

	@Test
	public void testTopN() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addGrouper("host");
		metric.addAggregatorType(AggregatorType.min);
		//设置top n的排序规则：升序、降序
		metric.setOrder(Order.DESCENDING);
		metric.setLimit(Integer.MAX_VALUE);
		metricLogic.queryTopN(builder, 10);
	}

	@Test
	public void testTags() {
		String metricName = "test.cpu.usage";
		metricLogic.getTags("tom", metricName);
	}

	@Test
	public void testGroupTagNames() {
		String metricName = "test.cpu.usage";
		metricLogic.getGroupTagName("tom", metricName);
	}

	@Test
	public void getMetricNamesByResId() {
		metricLogic.getResMetricNamesByResId(UUID.randomUUID().toString());
	}

	@Test
	public void getTagByTags() {
		metricLogic.getTagsByTag("tom", "test.cpu.usage", Arrays.asList(new Tag("host", "myPC")));
	}

	@Test
	public void queryPerfForMonitor() {

		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addGrouper("host");
		metric.addAggregatorType(AggregatorType.min);
		//设置top n的排序规则：升序、降序
		metric.setOrder(Order.DESCENDING);
		metricLogic.queryPerfForMonitor(builder);
	}

	@Test
	public void querySeriesGroupBy() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addTag("host", "myPC").addGrouper("host");
		metric.addAggregatorType(AggregatorType.max);
		metricLogic.querySeriesGroupBy(builder, 600);
	}

	@Test
	public void queryServiceApiMetricNames(){
		ResourceServiceQuery query=new ResourceServiceQuery();
		query.setHostname("hostname");
		query.setIpaddr("127.0.0.1");
		query.setTenantId(TENANT_ID);
		List<ServiceApiResMetrics> list=metricLogic.getMetricNames(query);
		System.out.println("list: "+list);
	}
}
