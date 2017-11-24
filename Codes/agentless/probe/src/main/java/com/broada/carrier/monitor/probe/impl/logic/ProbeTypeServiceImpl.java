package com.broada.carrier.monitor.probe.impl.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.broada.carrier.monitor.base.logic.BaseTypeServiceImpl;
import com.broada.carrier.monitor.probe.api.client.ProbeUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.BaseTypeService;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.component.utils.error.ErrorUtil;

public class ProbeTypeServiceImpl extends BaseTypeServiceImpl implements BaseTypeService {
	private Map<String, Monitor> monitors = new ConcurrentHashMap<String, Monitor>();

	public Monitor checkMonitor(String typeId) {
		Monitor monitor = monitors.get(typeId);
		if (monitor == null) {
			synchronized (ProbeTypeServiceImpl.class) {
				monitor = monitors.get(typeId);
				if (monitor == null) {
					MonitorType type = ProbeUtil.checkType(this, typeId);
					Class<?> monitorClass;
					try {
						monitorClass = Thread.currentThread().getContextClassLoader().loadClass(type.getMonitor());
						monitor = (Monitor) monitorClass.newInstance();
						monitors.put(typeId, monitor);
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("监测器类型无法实例化：" + type.getMonitor(), e);
					}
				}
			}
		}
		return monitor;
	}
}
