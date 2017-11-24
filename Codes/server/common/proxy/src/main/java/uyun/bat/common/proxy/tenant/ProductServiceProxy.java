package uyun.bat.common.proxy.tenant;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bird.tenant.api.ProductService;

public abstract class ProductServiceProxy {

	public static ProductService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ProductService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (ProductService) enhancer.create();
	}

}
