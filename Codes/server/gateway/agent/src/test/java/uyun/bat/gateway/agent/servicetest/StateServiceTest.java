package uyun.bat.gateway.agent.servicetest;

import java.lang.reflect.Method;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.service.StateService;

public class StateServiceTest {
	private static Checkpoint[] getCheckpoints(){
		Checkpoint[] checkpoints = new Checkpoint[1];
		Checkpoint c = new Checkpoint();
		c.setValue("123");
		String[] tags = { "location:a", "level:high" };
		c.setTags(tags);
		checkpoints[0] = c;
		return checkpoints;
	}
	public static StateService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(StateService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if("getCheckpoints".equals(method.getName())){
					return getCheckpoints();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (StateService) enhancer.create();
	}

}
