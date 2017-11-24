package uyun.bat.common.proxy.tenant;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bird.tenant.api.TenantService;
import uyun.bird.tenant.api.entity.ApiKey;
import uyun.bird.tenant.api.entity.Tenant;

public abstract class TenantServiceProxy {

	private static Tenant generateDefaultTenant() {
		Tenant t = new Tenant(TenantConstants.TENANT_ID, TenantConstants.USER_REAL_NAME);
		t.setApiKeys(new ArrayList<ApiKey>());
		ApiKey key = new ApiKey(t.getTenantId());
		key.setKey(TenantConstants.API_KEY);
		t.getApiKeys().add(key);
		return t;
	}

	public static TenantService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(TenantService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("view".equals(method.getName()) && TenantConstants.TENANT_ID.equals(args[0])) {
					return generateDefaultTenant();
				} else if ("selectByApikey".equals(method.getName()) && TenantConstants.API_KEY.equals(args[0])) {
					return generateDefaultTenant();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (TenantService) enhancer.create();
	}

}
