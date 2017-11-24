package com.broada.carrier.monitor.server.impl.pmdb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.cmdb.api.client.ResourceDomainClient;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.carrier.monitor.server.impl.event.MethodChangedEvent;
import com.broada.carrier.monitor.server.impl.logic.EventBus;
import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.cmdb.api.client.CMDBEventClient;
import com.broada.cmdb.api.client.CMDBModelClient;
import com.broada.cmdb.api.client.EventClient;
import com.broada.cmdb.api.client.EventListener;
import com.broada.cmdb.api.data.GlobalDataRegion;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.data.InstanceSource;
import com.broada.cmdb.api.data.Relationship;
import com.broada.cmdb.api.event.Event;
import com.broada.cmdb.api.event.InstanceChangedEvent;
import com.broada.cmdb.api.event.RealInstanceChangedEvent;
import com.broada.cmdb.api.model.Attribute;
import com.broada.cmdb.api.model.QueryOperator;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.cmdb.api.model.RelationshipMode;
import com.broada.cmdb.api.model.Template;
import com.broada.cmdb.api.service.CMDBDataService;
import com.broada.cmdb.api.service.CMDBModelService;
import com.broada.cmdb.api.service.CMDBServiceFactory;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.pmdb.api.data.PerfIndicatorGroup;
import com.broada.pmdb.api.data.StateIndicator;
import com.broada.pmdb.api.model.PerfIndicatorType;
import com.broada.pmdb.api.model.StateIndicatorType;
import com.broada.pmdb.api.service.PMDBDataService;
import com.broada.pmdb.api.service.PMDBModelService;
import com.broada.pmdb.api.service.PMDBServiceFactory;

/**
 * PMDB接口封装，避免后续PMDB接口改进对monitor模块的影响
 * @author Jiangjw
 */
public class PMDBFacade implements PMDBClient {
	private static final Logger logger = LoggerFactory.getLogger(PMDBFacade.class);
	private static final InstanceSource source = new InstanceSource("numen", null);
	private CMDBServiceFactory cmdbServiceFactory;
	private CMDBModelClient cmdbModelService;
	private CMDBDataService cmdbDataService;
	private CMDBEventClient cmdbEventClient;
	private PMDBServiceFactory pmdbServiceFactory;
	private PMDBModelService pmdbModelService;
	private PMDBDataService pmdbDataService;
	@Autowired
	private Config config;
	@Autowired
	private ServerServiceFactory serverFactory;
	@Autowired
	private LocalRemoteMapper localRemoteMapper;
	private PMDBClient client;
	private Set<String> methodTypes;

	private PMDBClient getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					client = new RestfulPMDBClient(Config.getDefault().getPMDBApiUrl(), serverFactory.getTargetTypeService(),
							serverFactory.getNodeService(), serverFactory.getResourceService());
				}
			}
		}
		return client;
	}

	private CMDBServiceFactory getCmdbServiceFactory() {
		if (cmdbServiceFactory == null) {
			synchronized (this) {
				if (cmdbServiceFactory == null) {
					cmdbServiceFactory = new CMDBServiceFactory(config.getPMDBIp(),
							config.getPMDBPort());
				}
			}
		}
		return cmdbServiceFactory;
	}

	private PMDBServiceFactory getPmdbServiceFactory() {
		if (pmdbServiceFactory == null) {
			synchronized (this) {
				if (pmdbServiceFactory == null) {
					pmdbServiceFactory = new PMDBServiceFactory(config.getPMDBIp(),
							config.getPMDBPort());
				}
			}
		}
		return pmdbServiceFactory;
	}

	private CMDBDataService getCmdbDataService() {
		if (cmdbDataService == null) {
			synchronized (this) {
				if (cmdbDataService == null) {
					cmdbDataService = getCmdbServiceFactory().getDataService();
					try {
						cmdbDataService.isServiceAvaliable();
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("无法连接PMDB服务", e);
					}
				}
			}
		}
		return cmdbDataService;
	}

	private CMDBModelService getCmdbModelService() {
		if (cmdbModelService == null) {
			synchronized (this) {
				if (cmdbModelService == null) {
					cmdbModelService = new CMDBModelClient(getCmdbServiceFactory().getModelService());
					try {
						cmdbModelService.getTemplateByRoot();
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("无法连接PMDB服务", e);
					}
				}
			}
		}
		return cmdbModelService;
	}

	private EventClient getCmdbEventClient() {
		if (cmdbEventClient == null) {
			synchronized (this) {
				if (cmdbEventClient == null) {
					cmdbEventClient = new CMDBEventClient("carrier.monitor.pmdbFacade", config.getPMDBEventIp(),
							config.getPMDBEventPort());
					cmdbEventClient.startup();
				}
			}
		}
		return cmdbEventClient;
	}

	/**
	 * 添加指定事件的监听器
	 * @param eventClass
	 * @param listener
	 */
	public void addCmdbListener(Class<?> eventClass, EventListener listener) {
		getCmdbEventClient().addListener(eventClass, listener);
	}

	/**
	 * 获取指定编码的配置项，如果不存在则弹出异常
	 * @param code
	 * @return
	 */
	public Template checkClassByCode(String code) {
		Template template = getCmdbModelService().getTemplateByCode(code);
		if (template == null)
			throw new IllegalArgumentException("未知的配置项类型：" + code);
		return template;
	}

	/**
	 * 获取指定配置项类型下的一级子配置项类型
	 * @param id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Template[] getClassesByParentId(String id) {
		Template template = checkClassByCode(id);
		return getCmdbModelService().getTemplateByParentId(template.getId());
	}

	/**
	 * 获取指定配置项类型的所有属性
	 * @param id
	 * @return
	 */
	public Attribute[] getAttributesByTemplateId(String id) {
		return getCmdbModelService().getAttributeByTemplateId(id);
	}

	public Attribute getAttribute(String templateCode, String attrCode) {
		return getCmdbModelService().getAttributeByCode(templateCode, attrCode);
	}

	public Attribute[] getAttributesByTemplateCode(String code) {
		Template template = getCmdbModelService().getTemplateByCode(code);
		if (template == null)
			return null;
		return getCmdbModelService().getAttributeByTemplateId(template.getId());
	}

	/**
	 * 删除指定的一个监测项
	 * @param nodeId
	 */
	public void deleteInstanceById(String id) {
		getCmdbDataService().deleteInstance(new String[] { id });
	}

	public void removeInstanceBySrcId(String srcId) {
		getCmdbDataService().removeInstanceSource(new InstanceSource(source.getSrcSys(), srcId));
	}

	/**
	 * 查询一个指定的监测项
	 * @param id
	 * @return
	 */
	public Instance getInstanceById(String id) {
		return getCmdbDataService().getInstanceById(GlobalDataRegion.INSTANCE, id);
	}
	
	/**
	 * 按id批量查询监测项
	 * @param id list
	 * @return
	 */
	public List<Instance> getInstanceByIds(List<String> idList) {
		return getCmdbDataService().getInstanceByIds(GlobalDataRegion.INSTANCE, idList);
	}

	/**
	 * 保存一个监测项
	 * @param instance
	 * @param relationships
	 * @return
	 */
	public String saveInstance(Instance instance, Relationship[] relationships, String domainId) {
		try {
			InstanceSource source = new InstanceSource(PMDBFacade.source.getSrcSys(), null);
			source.setDomainId(domainId);
			return getCmdbDataService().saveInstance(source, instance, relationships);
		} catch (RuntimeException e) {
			throw processSaveError(e);
		}
	}

	private RuntimeException processSaveError(RuntimeException e) {
		if (e.getMessage().contains("id: null instId: null]下唯一定位改配置项"))
			throw new BaseException("此配置项类型为虚拟基础类型，未配置调和关键字，不允许建立实例", e);
		else
			throw e;
	}

	public String saveInstance(String srcId, Instance instance, Relationship[] relationships, String domainId) {
		try {
			InstanceSource source = new InstanceSource(PMDBFacade.source.getSrcSys(), srcId);
			source.setDomainId(domainId);
			return getCmdbDataService().saveInstance(source, instance, relationships);
		} catch (RuntimeException e) {
			throw processSaveError(e);
		}
	}

	/**
	 * 按条件查询监测项
	 * @param templateCode
	 * @param request
	 * @return
	 */
	public Instance[] getInstanceByQuery(String templateCode, QueryRequest request) {
		return getCmdbDataService().getInstanceByQuery(GlobalDataRegion.INSTANCE, templateCode, request);
	}
	
	/**
	 * 按条件从指定的relationship中查找src
	 * @param relationship
	 * @param request
	 * @return
	 */
	public Relationship[] getRelationshipByQuery(String relationship, QueryRequest request) {
		Relationship[] rss = getCmdbDataService().getRelationshipByQuery(GlobalDataRegion.INSTANCE, relationship, request);
		if(rss == null)
			return new Relationship[0];
		return rss;
	}

	public String getNodeIdByResourceId(String resourceId) {
		QueryRequest request = new QueryRequest(0, 1);
		request.addCondition("src", QueryOperator.EQUALS, resourceId);
		Relationship[] rss = getCmdbDataService().getRelationshipByQuery(GlobalDataRegion.INSTANCE,
				PMDBConverter.NODE_RESOURCE_RT, request);
		if (rss != null && rss.length > 0)
			return rss[0].getDest();
		return null;
	}

	public String[] getResourceIdsByNodeId(String nodeId) {
		QueryRequest request = new QueryRequest(0, Integer.MAX_VALUE);
		request.addCondition("dest", QueryOperator.EQUALS, nodeId);
		Relationship[] rss = getCmdbDataService().getRelationshipByQuery(GlobalDataRegion.INSTANCE,
				PMDBConverter.NODE_RESOURCE_RT, request);
		String[] ids = new String[rss == null ? 0 : rss.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = rss[i].getSrc();
		return ids;
	}

	public Template getClassById(String typeId) {
		return getCmdbModelService().getTemplateByCode(typeId);
	}

	public PMDBDataService getPmdbDataService() {
		if (pmdbDataService == null) {
			synchronized (this) {
				if (pmdbDataService == null)
					pmdbDataService = getPmdbServiceFactory().getDataService();
			}
		}
		return pmdbDataService;
	}

	public PMDBModelService getPmdbModelService() {
		if (pmdbModelService == null) {
			synchronized (this) {
				if (pmdbModelService == null)
					pmdbModelService = getPmdbServiceFactory().getModelService();
			}
		}
		return pmdbModelService;
	}

	public void saveState(StateIndicator state) {
		getPmdbDataService().saveStateIndicator(source, state);
	}

	public void savePerfGroup(PerfIndicatorGroup group) {
		getPmdbDataService().savePerfIndicatorGroup(source, group);
	}

	@Override
	public MonitorTargetGroup[] getGroups(String userId, String domainId, String parentId) {
		return getClient().getGroups(userId, domainId, parentId);
	}

	public boolean isResourceRunningOnNode(String resourceClassCode, String nodeClassCode) {
		RelationshipMode query = new RelationshipMode();
		query.setSrcTemplateCode(resourceClassCode);
		query.setDestTemplateCode(nodeClassCode);
		query.setRelationshipTypeCode(PMDBConverter.NODE_RESOURCE_RT);
		RelationshipMode[] rms = getCmdbModelService().getRelationshipModes(query);
		return rms != null && rms.length > 0;
	}

	@Override
	public MonitorTarget[] getGroupInstances(PageNo pageNo, String groupId, String ipKey) {
		return getClient().getGroupInstances(pageNo, groupId, ipKey);
	}

	public void startup() {
		CmdbEventListener listener = new CmdbEventListener();
		addCmdbListener(InstanceChangedEvent.class, listener);
		addCmdbListener(RealInstanceChangedEvent.class, listener);
	}

	private class CmdbEventListener implements com.broada.cmdb.api.client.EventListener {
		@Override
		public void handle(Event arg) {
			logger.debug("配置项变更事件：{}", arg);
			com.broada.cmdb.api.event.ObjectChangedType changedType;
			String instanceId;
			String templateCode;
			if (arg instanceof RealInstanceChangedEvent) {
				RealInstanceChangedEvent event = (RealInstanceChangedEvent) arg;
				changedType = event.getChangedType();
				instanceId = event.getInstanceId();
				templateCode = event.getTemplateCode();
			} else {
				InstanceChangedEvent event = (InstanceChangedEvent) arg;
				changedType = event.getChangedType();
				instanceId = event.getInstanceId();
				templateCode = event.getTemplateCode();
			}
			MonitorTargetType targetType = serverFactory.getTargetTypeService().getTargetType(templateCode);
			if (targetType == null) {
				if (processMethodEvent(templateCode, instanceId, changedType))
					return;
				logger.debug("配置项变更事件：{}，非监测项", arg);
				return;
			}

			MonitorTarget target = null;
			switch (changedType) {
			case CREATED:
			case UPDATED:
				Instance inst = getInstanceById(instanceId);
				if (inst == null)
					return;
				if (targetType.isNode())
					target = PMDBConverter.toNode(inst, checkInstanceDomainIdById(inst.getId()));
				else
					target = PMDBConverter.toResource(inst, checkInstanceDomainIdById(inst.getId()));
				break;
			case DELETED:
				if (targetType.isNode())
					target = new MonitorNode();
				else
					target = new MonitorResource();
				target.setId(instanceId);
				// 当配置项被删除，本地的资源映射也建议删除
				localRemoteMapper.deleteKeysByRemoteKey(instanceId);
				break;
			default:
				return;
			}

			if (targetType.isNode()) {
				MonitorNode node = (MonitorNode) target;
				switch (changedType) {
				case CREATED:
					EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(ObjectChangedType.CREATED, null, node));
					break;
				case UPDATED:
					EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(ObjectChangedType.UPDATED, null, node));
					break;
				case DELETED:
					if (arg instanceof InstanceChangedEvent)
						EventBus.getDefault().publishObjectChanged(new NodeChangedEvent(ObjectChangedType.DELETED, node, null));
					break;
				default:
					return;
				}
			} else {
				MonitorResource resource = (MonitorResource) target;
				switch (changedType) {
				case CREATED:
					EventBus.getDefault().publishObjectChanged(
							new ResourceChangedEvent(ObjectChangedType.CREATED, null, resource));
					break;
				case UPDATED:
					EventBus.getDefault().publishObjectChanged(
							new ResourceChangedEvent(ObjectChangedType.UPDATED, null, resource));
					break;
				case DELETED:
					if (arg instanceof InstanceChangedEvent)
						EventBus.getDefault().publishObjectChanged(
								new ResourceChangedEvent(ObjectChangedType.DELETED, resource, null));
					break;
				default:
					return;
				}
			}
		}

		private boolean processMethodEvent(String templateCode, String instanceId,
				com.broada.cmdb.api.event.ObjectChangedType changedType) {
			if (!isMethodType(templateCode))
				return false;
			EventBus.getDefault().publishObjectChanged(new MethodChangedEvent(ObjectChangedType.DELETED, null, null));
			return true;
		}
	}

	public String[] getMethodIdsByTargetUsed(String targetId) {
		QueryRequest request = new QueryRequest(0, 1);
		request.addCondition("src", QueryOperator.EQUALS, targetId);
		request.setPageSize(Integer.MAX_VALUE);
		Relationship[] rss = getCmdbDataService().getRelationshipByQuery(GlobalDataRegion.INSTANCE,
				PMDBConverter.TARGET_METHOD_RT, request);
		if (rss == null || rss.length == 0)
			return new String[0];

		Set<String> methodIds = new HashSet<String>();
		for (Relationship rs : rss)
			methodIds.add(rs.getDest());
		return methodIds.toArray(new String[methodIds.size()]);
	}

	public boolean isMethodType(String templateCode) {
		return getMethodTypes().contains(templateCode);
	}

	private Set<String> getMethodTypes() {
		if (methodTypes == null) {
			synchronized (this) {
				methodTypes = new HashSet<String>();
				Template[] templs = getCmdbModelService().getTemplatesByParentId("Protocol", true);
				for (Template temp : templs) {
					methodTypes.add(temp.getCode());
				}
			}
		}
		return methodTypes;
	}

	public void saveRelationship(String src, String type, String dest) {
		Relationship rs = new Relationship(type, src, dest);
		getCmdbDataService().saveRelationship(source, rs);
	}

	public void deleteRelationship(String src, String type, String dest) {
		QueryRequest request = new QueryRequest(0, 1);
		request.addCondition("src", QueryOperator.EQUALS, src);
		request.addCondition("dest", QueryOperator.EQUALS, dest);
		Relationship[] rss = getCmdbDataService().getRelationshipByQuery(GlobalDataRegion.INSTANCE, type, request);
		if (rss == null || rss.length == 0)
			return;

		for (Relationship rs : rss)
			getCmdbDataService().deleteInstance(new String[] { rs.getId() });
	}

	public String checkInstanceDomainIdById(String resourceId) {
		String[] ids = ResourceDomainClient.getDefault().getDomainIds(resourceId, true);
		if (ids.length > 0)
			return ids[0];
		else
			return "rootDomain";
	}

	public PerfIndicatorType getPerfIndicatorByCode(String indicatorCode) {
		return getPmdbModelService().getPerfIndicatorTypeByCode(indicatorCode);
	}

	public StateIndicatorType getStateIndicatorByCode(String indicatorCode) {
		return getPmdbModelService().getStateIndicatorTypeByCode(indicatorCode);
	}

	public Relationship[] getRelationshipsByQuery(GlobalDataRegion dataRegion, String relationshipCode,
			QueryRequest request) {
		return getCmdbDataService().getRelationshipByQuery(dataRegion, relationshipCode, request);
	}
}
