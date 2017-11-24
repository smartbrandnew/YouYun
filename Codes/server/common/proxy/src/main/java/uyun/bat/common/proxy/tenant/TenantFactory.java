package uyun.bat.common.proxy.tenant;

import org.springframework.context.ApplicationContext;

import uyun.bat.common.proxy.Factory;
import uyun.bat.common.proxy.ProxyFactory;
import uyun.bird.tenant.api.ProductService;
import uyun.bird.tenant.api.TenantService;
import uyun.bird.tenant.api.UserService;

/**
 * 租户相关的proxy工厂
 */
public class TenantFactory implements Factory {

	public void produce(ApplicationContext context, boolean isDeveloperMode) {
		ProductService productService = null;
		if (!isDeveloperMode)
			productService = context.getBean(ProductService.class);
		else
			productService = ProductServiceProxy.create();

		ProxyFactory.registerProxy(ProductService.class, productService);

		TenantService tenantService = null;

		if (!isDeveloperMode)
			tenantService = context.getBean(TenantService.class);
		else
			tenantService = TenantServiceProxy.create();

		ProxyFactory.registerProxy(TenantService.class, tenantService);

		UserService userService = null;

		if (!isDeveloperMode)
			userService = context.getBean(UserService.class);
		else
			userService = UserServiceProxy.create();

		ProxyFactory.registerProxy(UserService.class, userService);
	}

}
