package uyun.bat.gateway.agent.servicetest;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.service.MetricMetaDataService;

public class MetricMetaDataServiceTest {
	private static MetricMetaData queryByName(){
		MetricMetaData data =new MetricMetaData();
		data.setUnit("%");
		data.setName("system.cpu.idle");
		data.setcName("CPU可用率");
		return data;
	}

	public static MetricMetaDataService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(MetricMetaDataService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("queryByName".equals(method.getName())) {
					return queryByName();
				}else if("queryRangedMetaData".equals(method.getName())) {
					return new ArrayList<MetricMetaData>();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (MetricMetaDataService) enhancer.create();
	}
}
