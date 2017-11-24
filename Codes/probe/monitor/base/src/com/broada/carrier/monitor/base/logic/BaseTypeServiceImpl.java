package com.broada.carrier.monitor.base.logic;

import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.BaseTypeService;
import com.broada.carrier.monitor.spi.MonitorPackage;
import com.broada.component.utils.error.ErrorUtil;

public class BaseTypeServiceImpl implements BaseTypeService {	
	private static final Logger logger = LoggerFactory.getLogger(BaseTypeServiceImpl.class);
	private Map<String, MonitorType> types = new ConcurrentHashMap<String, MonitorType>();
	private Map<String, MonitorItem> items = new ConcurrentHashMap<String, MonitorItem>();
	private Map<String, MonitorMethodType> methodTypes = new ConcurrentHashMap<String, MonitorMethodType>();

	public BaseTypeServiceImpl() {
		loadTypesFromPackage();
	}
	
	private void loadTypesFromPackage() {
		ServiceLoader<MonitorPackage> loader = ServiceLoader.load(MonitorPackage.class);
		try {
			for (MonitorPackage pack : loader) {
				if (pack.getTypes() != null) {
					for (MonitorType type : pack.getTypes()) {
						addType(type);
					}
				}
				
				if (pack.getItems() != null) {
					for (MonitorItem item : pack.getItems()) {
						addItem(item);
					}
				}
				
				if (pack.getMethodTypes() != null)
					for (MonitorMethodType type : pack.getMethodTypes()) {
						addMethodType(type);
				}
			}
		} catch (ServiceConfigurationError e) {
			ErrorUtil.warn(logger, "监测任务扩展包加载失败", e);
		}
	}

	private void addItem(MonitorItem item) {
		if (items.containsKey(item.getCode())) 
			logger.warn("监测指标类型重复，只保留第1个监测指标：{}", item);		
		items.put(item.getCode(), item);
	}

	private void addType(MonitorType type) {
		if (types.containsKey(type.getId())) 
			logger.warn("监测器类型重复，只保留第1个监测器类型：{}", type);		
		types.put(type.getId(), type);
	}
	
	private void addMethodType(MonitorMethodType type) {
		if (methodTypes.containsKey(type.getId())) 
			logger.warn("监测方法类型重复，只保留第1个监测方法类型：{}", type);		
		methodTypes.put(type.getId(), type);
	}

	@Override
	public MonitorType[] getTypes() {
		return types.values().toArray(new MonitorType[types.size()]);
	}

	@Override
	public MonitorType getType(String typeId) {
		return types.get(typeId);
	}

	@Override
	public MonitorItem getItem(String itemCode) {
		return items.get(itemCode);
	}

	@Override
	public MonitorMethodType getMethodType(String typeId) {
		return methodTypes.get(typeId);
	}

	@Override
	public MonitorItem[] getItems() {
		return items.values().toArray(new MonitorItem[items.size()]);
	}

}
