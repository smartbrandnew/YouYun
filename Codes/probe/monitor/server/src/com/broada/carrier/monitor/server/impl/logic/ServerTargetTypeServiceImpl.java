package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.client.EventListener;
import com.broada.cmdb.api.event.Event;
import com.broada.cmdb.api.event.TemplateChangedEvent;
import com.broada.cmdb.api.model.Attribute;
import com.broada.cmdb.api.model.Template;

public class ServerTargetTypeServiceImpl implements ServerTargetTypeService {
	private static final Logger logger = LoggerFactory.getLogger(ServerTargetTypeServiceImpl.class);
	private Map<String, MonitorTargetType[]> targetTypesByParentId;
	private Map<String, MonitorTargetType> targetTypeById;
	private Listener pmdbListener;
	@Autowired
	private PMDBFacade pmdbFacade;

	private void initListener() {
		if (pmdbListener == null) {
			pmdbListener = new Listener();
			pmdbFacade.addCmdbListener(TemplateChangedEvent.class, pmdbListener);
		}
	}

	private Map<String, MonitorTargetType> getTargetTypeById() {
		if (targetTypeById == null) {
			synchronized (this) {
				if (targetTypeById == null) {
					targetTypeById = new ConcurrentHashMap<String, MonitorTargetType>();
					initListener();
				}
			}
		}
		return targetTypeById;
	}

	private Map<String, MonitorTargetType[]> getTargetTypesByParentId() {
		if (targetTypesByParentId == null) {
			synchronized (this) {
				if (targetTypesByParentId == null) {
					targetTypesByParentId = new ConcurrentHashMap<String, MonitorTargetType[]>();

					loadTargetTypes(MonitorTargetType.ROOT_NODE_ID, true);
					loadTargetTypes("Object", false);

					initListener();
				}
			}
		}
		return targetTypesByParentId;
	}

	private boolean loadTargetTypes(String parentId, boolean needIpAttr) {
		MonitorTargetType[] existsChilds = targetTypesByParentId.get(parentId);
		if (existsChilds != null)
			return existsChilds.length > 0;

		ArrayList<MonitorTargetType> result = new ArrayList<MonitorTargetType>();
		Template[] classes = pmdbFacade.getClassesByParentId(parentId);
		if (classes == null)
			return false;

		for (Template cls : classes) {
			if (cls.isHidden())
				continue;

			if (needIpAttr) {
				boolean containsIp = false;
				Attribute[] attrs = pmdbFacade.getAttributesByTemplateId(cls.getId());
				for (Attribute attr : attrs) {
					if (attr.getCode().equalsIgnoreCase(PMDBConverter.ATTR_IP)) {//基础设备下的模型需要ipaddr属性才能同步到client
						containsIp = true;
						break;
					}
				}

				if (!containsIp)
					continue;
			}

			MonitorTargetType type = PMDBConverter.toTargetType(cls);
			boolean hasChild = loadTargetTypes(type.getId(), false);
			if (type.isPMDB() || hasChild)
				result.add(type);
		}

		if (result.isEmpty())
			return false;

		targetTypesByParentId.put(parentId, result.toArray(new MonitorTargetType[result.size()]));
		return true;
	}

	private class Listener implements EventListener {
		@Override
		public void handle(Event event) {
			logger.debug("配置项类型变更，将清理缓存");
			synchronized (ServerTargetTypeServiceImpl.this) {
				targetTypesByParentId = null;
				targetTypeById = null;
			}
		}
	}

	@Override
	public MonitorTargetType[] getTargetTypesByNode() {
		return getTargetTypesByParentId(MonitorTargetType.ROOT_NODE_ID);
	}

	@Override
	public MonitorTargetType[] getTargetTypesByResource() {
		return getTargetTypesByParentId(MonitorTargetType.ROOT_RESOURCE_ID);
	}

	@Override
	public MonitorTargetType[] getTargetTypesByParentId(String parentId) {
		MonitorTargetType[] result = getTargetTypesByParentId().get(parentId);
		if (result == null)
			result = new MonitorTargetType[0];
		return result;
	}

	@Override
	public MonitorTargetType getTargetType(String typeId) {
		MonitorTargetType targetType = getTargetTypeById().get(typeId);
		if (targetType == null) {
			for (Entry<String, MonitorTargetType[]> entry : getTargetTypesByParentId().entrySet()) {
				for (MonitorTargetType type : entry.getValue()) {
					if (type.getId().equalsIgnoreCase(typeId)) {
						targetType = type;
						break;
					}
				}
				if (targetType != null) {
					getTargetTypeById().put(typeId, targetType);
					break;
				}
			}
		}
		if (targetType != null && !(targetType.isNode() || targetType.isResource()))
			return null;
		return targetType;
	}
}
