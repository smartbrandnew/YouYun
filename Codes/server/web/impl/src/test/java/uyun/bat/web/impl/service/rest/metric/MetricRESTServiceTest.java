package uyun.bat.web.impl.service.rest.metric;

import org.junit.Test;
import uyun.bat.web.api.metric.entity.MetricTrashCleanQuery;
import uyun.bat.web.api.metric.request.BatchRequestParams;
import uyun.bat.web.api.metric.request.SingleValueRequestParams;
import uyun.bat.web.impl.testservice.StartService;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MetricRESTServiceTest extends StartService{
	MetricRESTService metricREST = new MetricRESTService();
	private static final String USER_ID="94baaadca64344d2a748dff88fe7159e";
	private static final String T_ID="94baaadca64344d2a748dff88fe7159e";
	

//构建报错临时注释
//	@Test
	public void testGetSeries() {
		//TODO
		BatchRequestParams requestParam=new BatchRequestParams();
		String[] q= {"avg:bat.env.activemq.alive{ip:10.1.51.235;10.1.53.100;host:10.1.53.100}"};
		requestParam.setQ(q);
		long from=System.currentTimeMillis();
		requestParam.setFrom(from);
		long to=System.currentTimeMillis()+11;
		requestParam.setTo(to);
		int interval=1;
		requestParam.setInterval(interval);
		String[] types={"cloumn"};
		requestParam.setTypes(types);
		assertTrue(metricREST.getSeries(T_ID, requestParam)!=null);
	}

	@Test
	public void testGetTop() {
		String tenantId = T_ID;
		BatchRequestParams requestParam = new BatchRequestParams();
		//String[] q= {"top(avg:mysql.performance.com_insert_select{*} by{host},10,'avg','desc')"};
		String[] q= {"top(avg:system.cpu.idle{ip:10.1.53.98} by{host} exclude{},10,avg,desc)"};
		requestParam.setQ(q);
		long from=System.currentTimeMillis();
		requestParam.setFrom(from);
		long to=System.currentTimeMillis()+11;
		requestParam.setTo(to);
		int interval=1;
		requestParam.setInterval(interval);
		String[] types={"cloumn"};
		requestParam.setTypes(types);
		// TODO
		assertTrue(metricREST.getTop(tenantId, requestParam)!=null);
	}

	@Test
	public void testGetValue() {
		SingleValueRequestParams singleValueRequestParams=new SingleValueRequestParams();
		String aggregator="";
		singleValueRequestParams.setAggregator(aggregator);
		long from=System.currentTimeMillis();
		singleValueRequestParams.setFrom(from);
		long to=System.currentTimeMillis()+11;
		singleValueRequestParams.setTo(to);
		int interval=1;
		singleValueRequestParams.setInterval(interval);
		String q="avg:mysql.performance.com_insert_select{*}";
		singleValueRequestParams.setQ(q);
		assertTrue(metricREST.getValue(T_ID, singleValueRequestParams)!=null);
	}

	@Test
	public void testGetMetricNames() {
		// TODO
		//assertTrue(metricREST.getMetricNames(T_ID, null, 10).get(0).equals("getMetricNamesByKey Successful!"));
	}

	@Test
	public void testGetTagsByMetricName() {
		String metricName = "mysql.innodb.buffer_pool_total";
		String[] q={"host:JJTom"};
		assertTrue(metricREST.getTagsByMetricName(T_ID, metricName, q).get(0).equals("94baaadca64344d2a748dff88fe7159e:mysql.innodb.buffer_pool_total"));
	}

	@Test
	public void testDeleteTrashData() {
		MetricTrashCleanQuery query=new MetricTrashCleanQuery();
		String metricName = "mysql.innodb.buffer_pool_total";
		query.setMetricName(metricName);
		Map<String, String> tags = null;
		query.setTags(tags);
		query.setTenantId(T_ID);
		assertTrue(metricREST.deleteTrashData(query));
	}

	@Test
	public void testGetMetricMetaData() {
		String metricName="system.load.5";
		assertTrue(metricREST.getMetricMetaData(metricName)!=null);
	}

	@Test
	public void testIsMetricExist() {
		// TODO
		assertTrue(metricREST.isMetricExist(T_ID));
	}

	@Test
	public void testGetMetricMetaDataByKey() {
		String key="system.load.5";
		// TODO
		assertTrue(metricREST.getMetricMetaDataByKey(key).get(0).getName().equals(key));
	}

}
