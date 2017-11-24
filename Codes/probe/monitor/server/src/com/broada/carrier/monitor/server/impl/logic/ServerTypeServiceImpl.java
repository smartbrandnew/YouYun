package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.base.logic.BaseTypeServiceImpl;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.api.service.ServerTypeService;

public class ServerTypeServiceImpl extends BaseTypeServiceImpl implements ServerTypeService {
	private static final Logger logger = LoggerFactory.getLogger(ServerTypeServiceImpl.class);
	private Map<String, MonitorType[]> typesByTargetTypeId = new ConcurrentHashMap<String, MonitorType[]>();
	@Autowired
	private ServerTargetTypeService targetTypeService;
	
	@Override
	public MonitorType[] getTypesByTargetTypeId(String targetTypeId) {
		MonitorType[] result = typesByTargetTypeId.get(targetTypeId);
		if (result == null) {
			result = createTypesByTargetTypeId(targetTypeId);
			typesByTargetTypeId.put(targetTypeId, result);
		}
		return result;
	}

	private MonitorType[] createTypesByTargetTypeId(String targetTypeId) {
		MonitorTargetType getTargetType = ServerUtil.checkTargetType(targetTypeService, targetTypeId);
		
		List<MonitorType> result = new ArrayList<MonitorType>();
		for (MonitorType type : getTypes()) {
			for (String id : type.getTargetTypeIds()) {								
				if (id.equalsIgnoreCase(targetTypeId)) {
					result.add(type);
					break;
				}
				
				MonitorTargetType targetType = targetTypeService.getTargetType(id);
				if (targetType == null) {
					logger.warn("目标类型不存在：{}", id);
					continue;
				}
				if (targetType.isSubType(getTargetType)) {
					result.add(type);
					break;
				}
			}
		}
		
		Collections.sort(result);
		
		return result.toArray(new MonitorType[result.size()]);
	}
}
