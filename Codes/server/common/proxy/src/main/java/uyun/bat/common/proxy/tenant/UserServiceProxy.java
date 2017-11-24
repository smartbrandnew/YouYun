package uyun.bat.common.proxy.tenant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bird.tenant.api.UserService;
import uyun.bird.tenant.api.entity.Pagination;
import uyun.bird.tenant.api.entity.User;

public abstract class UserServiceProxy {

	private static User genenrateDefaultUser() {
		User u = new User();
		u.setTenantId(TenantConstants.TENANT_ID);
		u.setUserId(TenantConstants.USER_ID);
		u.setRealname(TenantConstants.USER_REAL_NAME);
		u.setEmail(TenantConstants.EMAIL);
		u.setMobile(TenantConstants.MOBILE);
		return u;
	}

	public static UserService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(UserService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("verify".equals(method.getName()) && TenantConstants.TOKEN.equals(args[0])) {
					Map<String, Object> data = new HashMap<String, Object>();
					// 临时塞些东西
					data.put("token", args[0]);
					data.put("userId", TenantConstants.USER_ID);
					data.put("tenantId", TenantConstants.TENANT_ID);
					return data;
				} else if ("listBy".equals(method.getName())) {
					List<User> users = new ArrayList<User>();
					users.add(genenrateDefaultUser());
					Pagination<User> pu = new Pagination<User>(0, users.size(), users.size(), users);
					return pu;
				} else if ("listByUserIds".equals(method.getName())) {
					List<User> users = new ArrayList<User>();
					users.add(genenrateDefaultUser());
					return users;
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (UserService) enhancer.create();
	}
}
