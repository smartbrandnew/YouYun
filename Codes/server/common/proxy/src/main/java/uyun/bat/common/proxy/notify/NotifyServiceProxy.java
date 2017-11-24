package uyun.bat.common.proxy.notify;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bird.notify.api.NotifyService;

public abstract class NotifyServiceProxy {

	public static NotifyService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(NotifyService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (NotifyService) enhancer.create();
	}

}
