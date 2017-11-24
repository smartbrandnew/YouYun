package uyun.bat.datastore.service.impl;

import org.junit.Assert;
import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.entity.QueryMetric.Order;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MetricServiceImplTest {
	private static MetricService service = (MetricService) Startup.getInstance().getBean("metricService");
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "12345678910111213141516171819202";

	@Test
	public void testDelete() {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(24, uyun.bat.datastore.api.entity.TimeUnit.HOURS).addMetric("test.cpu.usage")
				.addTag("host", "myPC").addTenantId(TENANT_ID);
		service.delete(builder);

	}

	@Test
	public void testQueryPerf() {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTag("host", "myPC").addTenantId(TENANT_ID)
				.addAggregatorType(AggregatorType.max);
		service.queryPerf(builder);

	}

	@Test
	public void testQuerySeries() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addTag("host", "myPC");
		metric.addAggregatorType(AggregatorType.max);
		metric.setOrder(Order.DESCENDING);
		metric.setLimit(Integer.MAX_VALUE);
		service.querySeries(builder, 30);
	}

	@Test
	public void testTopN() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addGrouper("host");
		metric.addAggregatorType(AggregatorType.max);
		metric.setOrder(Order.DESCENDING);
		metric.setLimit(Integer.MAX_VALUE);
		service.queryTopN(builder, 10);
	}

	@Test
	public void testTags() {
		String metricName = "test.cpu.usage";
		service.getTags("tom", metricName);
	}

	@Test
	public void testGroupTagNames() {
		String metricName = "test.cpu.usage";
		service.getGroupTagName("tom", metricName);
	}

	@Test
	public void testCleanTrashData() {
		Map<String, String> tags = new HashMap<String, String>();
		tags.put("host", "zhaoyn,broada");
		service.deleteTrashData("test.cpu.point", "tom", tags);
	}

	@Test
	public void getMetricNamesByResId() {
		service.getMetricNamesByResId(UUID.randomUUID().toString());
	}

	@Test
	public void testQueryLastPerf() {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.addMetric("test.cpu.usage").addTenantId(TENANT_ID).addTag("host", "myPC");
		service.queryLastPerf(builder);
	}

	@Test
	public void queryMetricNamesByTenantId() {
		service.getMetricNamesByTenantId(TENANT_ID);
	}

	@Test
	public void queryPerfForMonitor() {

		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addGrouper("host");
		metric.addAggregatorType(AggregatorType.min);
		//设置top n的排序规则：升序、降序
		metric.setOrder(Order.DESCENDING);
		service.queryPerfForMonitor(builder);

	}

	@Test
	public void querySeriesGroupBy() {
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.setStart(24, TimeUnit.HOURS).addMetric("test.cpu.usage").addTenantId(TENANT_ID)
				.addTag("host", "myPC").addGrouper("host");
		metric.addAggregatorType(AggregatorType.max);
		service.querySeriesGroupBy(builder, 600);
	}
	
	@Test
	public void queryServiceApiMetricNames(){
		ResourceServiceQuery query=new ResourceServiceQuery();
		query.setHostname("hostname");
		query.setIpaddr("127.0.0.1");
		query.setTenantId(TENANT_ID);
		List<ServiceApiResMetrics> list=service.getMetricNames(query);
		System.out.println("list: "+list);
	}
	
	public static void main(String[] args) {
		System.out.println("tenantId length: "+
	TENANT_ID.length());
	}

}
