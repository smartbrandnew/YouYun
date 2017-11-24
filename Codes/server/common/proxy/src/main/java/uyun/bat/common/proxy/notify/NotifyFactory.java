package uyun.bat.common.proxy.notify;

import org.springframework.context.ApplicationContext;

import uyun.bat.common.proxy.Factory;
import uyun.bat.common.proxy.ProxyFactory;
import uyun.bird.notify.api.NotifyService;
import uyun.bird.notify.api.imsg.IMessageService;

/**
 * 租户相关的proxy工厂
 */
public class NotifyFactory implements Factory {

	public void produce(ApplicationContext context, boolean isDeveloperMode) {
		NotifyService notifyService = null;
		if (!isDeveloperMode)
			notifyService = context.getBean(NotifyService.class);
		else
			notifyService = NotifyServiceProxy.create();
		ProxyFactory.registerProxy(NotifyService.class, notifyService);

		IMessageService messageService = null;
		if (!isDeveloperMode)
			messageService = context.getBean(IMessageService.class);
		else
			messageService = IMessageServiceProxy.create();
		ProxyFactory.registerProxy(IMessageService.class, messageService);
	}

}
