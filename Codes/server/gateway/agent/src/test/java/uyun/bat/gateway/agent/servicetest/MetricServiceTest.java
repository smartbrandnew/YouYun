package uyun.bat.gateway.agent.servicetest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.service.MetricService;

public class MetricServiceTest {
	private static List<PerfMetric> querySeries() {
		List<PerfMetric> list = new ArrayList<>();
		PerfMetric perfMetric = new PerfMetric();
		perfMetric.setName("system.cpu.idle");
		List<DataPoint> points = new ArrayList<>();
		DataPoint p =new DataPoint(System.currentTimeMillis(),1);
		points.add(p);
		perfMetric.setDataPoints(points);
		list.add(perfMetric);
		return list;
	}

	private static List<Tag> getTagsByTag() {
		List<Tag> tags = new ArrayList<>();
		Tag tag = new Tag("groupBy");
		tag.setKey("key");
		tag.setValue("value");
		tags.add(tag);
		return tags;
	}
	public static MetricService create() {

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(MetricService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("querySeries".equals(method.getName())) {
					return querySeries();
				}
				if ("getTagsByTag".equals(method.getName())) {
					return getTagsByTag();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (MetricService) enhancer.create();
	}
}
