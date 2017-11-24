package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceMetrics;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;

public class MetricServiceTest implements MetricService {

	@Override
	public boolean delete(QueryBuilder queryBuilder) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<PerfMetric> queryTopN(QueryBuilder builder, int n) {
		// TODO Auto-generated method stub
		List<PerfMetric> list=new ArrayList<PerfMetric>();
		PerfMetric p=new PerfMetric();
		p.setName("123");
		List<DataPoint> dataPoints=new ArrayList<DataPoint>();
		long timestamp=1;
		Object value=new Integer(1);
		DataPoint e=new DataPoint(timestamp, value);
		dataPoints.add(e);
		p.setDataPoints(dataPoints);
		Map<String, List<String>> tags;
		//p.setTags(tags);
		return list;
	}

	@Override
	public PerfMetric queryPerf(QueryBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tag> getTags(String tenantId, String metricName) {
		// TODO Auto-generated method stub
		List<Tag> list = new ArrayList<Tag>();
		Tag e=new Tag(tenantId,metricName);
		list.add(e);
		return list;
	}

	@Override
	public Set<String> getGroupTagName(String tenantId, String metricName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<String> getMetricNamesByResId(String resourceId) {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();
		list.add("app");
		list.add("test");
		if (list.size() < 1) {
			System.out.println("error");
		}
		return list;
	}

	@Override
	public PerfMetric queryLastPerf(QueryBuilder queryBuilder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags) {
		// TODO Auto-generated method stub
		List<Tag> list = new ArrayList<Tag>();
		Tag e=new Tag(tenantId,metricName);
		list.add(e);
		return list;
	}

	@Override
	public List<String> getMetricNamesByTenantId(String tenantId) {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();
		String a="123";
		list.add(a);
		String b="345";
		list.add(b);
		return list;
	}

	@Override
	public List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval) {
		// TODO Auto-generated method stub
		List<PerfMetric> list = new ArrayList<PerfMetric>();
		PerfMetric pf = new PerfMetric();
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		String data = "123";
		DataPoint datap = new DataPoint(0, data);
		dataPoints.add(datap);
		pf.setDataPoints(dataPoints);
		pf.addResourceId(data);
		list.add(pf);
		return list;
	}

	@Override
	public List<PerfMetric> queryPerfForMonitor(QueryBuilder queryBuilder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PerfMetric> queryPerfForCircle(QueryBuilder queryBuilder) {
		List<PerfMetric> perfMetrics = new ArrayList<>();
		PerfMetric perfMetric = new PerfMetric();
		perfMetric.setName("name");
		DataPoint dataPoint = new DataPoint(10, "123");
		List<DataPoint>dataPoints = new ArrayList<>();
		dataPoints.add(dataPoint);
		dataPoint.setValue("123");
		perfMetric.setDataPoints(dataPoints);
		perfMetric.addResourceId("123");
		perfMetrics.add(perfMetric);
		return perfMetrics;
	}

	@Override
	public Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PerfMetric queryCurrentPerfMetric(QueryBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResourceMetrics> queryPerfForEachResource(List<Resource> resources, List<String> metricsArr, String tenantId, String sortField, String sortOrder, String type, Long start, Long end) {
		return null;
	}
}
