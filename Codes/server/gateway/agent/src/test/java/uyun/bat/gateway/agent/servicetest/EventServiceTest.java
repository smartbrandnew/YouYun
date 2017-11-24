package uyun.bat.gateway.agent.servicetest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.PageEvent;
import uyun.bat.event.api.service.EventService;

public abstract class EventServiceTest {
	private static long count() {
		return 1L;
	}

	private static Event create(Event e) {
		return e;
	}
	
	private static PageEvent searchEvent() {

		PageEvent pageEvent = new PageEvent();
		List<Event> rows = new ArrayList<>();
		Event event = new Event();
		event.setTenantId("123");
		event.setId("12");
		event.setAgentId("12");
		event.setIdentity("identity");
		event.setMsgTitle("msgTitle");
		Date date = new Date();
		event.setOccurTime(date);
		event.setMsgContent("msgContent");
		event.setServerity((short) 10);
		event.setSourceType((short) 10);
		rows.add(event);
		pageEvent.setRows(rows);
		pageEvent.setTotal(10);
		return pageEvent;
	}

	public static EventService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(EventService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("create".equals(method.getName()) && args[0] instanceof Event) {
					return create((Event) args[0]);
				}
				if ("create".equals(method.getName())) {
					return count();
				}
				if ("searchEvent".equals(method.getName())) {
					return searchEvent();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (EventService) enhancer.create();
	}
}
