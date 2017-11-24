package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeSync;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorMethod;
import com.broada.carrier.monitor.server.impl.event.MethodChangedEvent;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.data.GlobalDataRegion;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.data.Relationship;
import com.broada.cmdb.api.model.QueryOperator;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.component.utils.error.ErrorUtil;

public class ServerMethodServiceImpl implements ServerMethodService {
	private static final Logger logger = LoggerFactory.getLogger(ServerMethodServiceImpl.class);
	private Map<String, ServerSideMonitorMethod> cache;
	private Map<String, ServerSideMonitorMethod> methodCache;
	private boolean needClean;
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private ServerProbeServiceImpl probeService;

	public ServerMethodServiceImpl() {
		EventBus.getDefault().registerObjectChangedListener(new MethodEventListener());
	}

	private class MethodEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (!(event instanceof MethodChangedEvent))
				return;

			int[] probeIds;
			MethodChangedEvent changedEvent = (MethodChangedEvent) event;
			switch (changedEvent.getType()) {
			case CREATED:
			case UPDATED:
				probeIds = probeService.getProbeIdsByMethodCode(changedEvent.getObject().getCode());
				for (int probeId : probeIds) {
					try {
						ProbeSync sync = probeService.checkProbeSyncByProbeId(probeId);
						sync.syncMethod(changedEvent.getObject().getCode());
					} catch (Throwable e) {
						ErrorUtil.warn(logger, String.format("方法同步失败[probeId: %d policy: %s]", probeId, changedEvent.getObject()),
								e);
					}
				}
				break;
			case DELETED:
				if (changedEvent.getObject() != null) {
					probeIds = probeService.getProbeIdsByMethodCode(changedEvent.getObject().getCode());
					for (int probeId : probeIds) {
						try {
							ProbeSync sync = probeService.checkProbeSyncByProbeId(probeId);
							sync.deleteMethod(changedEvent.getOldObject().getCode());
						} catch (Throwable e) {
							ErrorUtil.warn(logger,
									String.format("方法同步失败[probeId: %d policy: %s]", probeId, changedEvent.getObject()), e);
						}
					}
				}
			}

			needClean = true;
		}
	}

	private Map<String, ServerSideMonitorMethod> getCache() {
		if (cache == null || needClean) {
			synchronized (this) {
				if (cache == null || needClean) {
					cache = new ConcurrentHashMap<String, ServerSideMonitorMethod>();
					Instance[] insts = pmdbFacade.getInstanceByQuery(ServerSideMonitorMethod.CLASS_CODE, new QueryRequest());
					if (insts != null) {
						for (Instance inst : insts) {
							try {
								Instance fullInst = pmdbFacade.getInstanceById(inst.getId());
								if (fullInst == null) {
									logger.warn("监测方法不存在：" + inst);
									continue;
								}
								ServerSideMonitorMethod method = PMDBConverter.toMethod(fullInst);
								cache.put(method.getCode(), method);
							} catch (Throwable e) {
								ErrorUtil.warn(logger, "监测方法无法加载，将忽略：" + inst, e);
							}
						}
					}
					needClean = false;
				}
			}
		}
		return cache;
	}

	@Override
	public MonitorMethod[] getMethods() {
		return getCache().values().toArray(new MonitorMethod[getCache().size()]);
	}

	@Override
	public void saveMethod(MonitorMethod method) {
		saveMethodInner(method, false);
	}

	@Override
	public void createMethod(MonitorMethod method) {
		saveMethodInner(method, true);
	}

	private void saveMethodInner(MonitorMethod method, boolean create) {
		method.verify();
		ServerSideMonitorMethod serverMethod;
		if (method instanceof ServerSideMonitorMethod)
			serverMethod = (ServerSideMonitorMethod) method;
		else {
			serverMethod = new ServerSideMonitorMethod(method);
			ServerSideMonitorMethod exists = getMethod(method.getCode());
			if (exists != null) {
				if (create)
					throw new IllegalArgumentException(String.format("要保存的监测方法编码[%s]已经被另一个监测方法[类型：%s]占用", method.getCode(),
							method.getTypeId()));
				serverMethod.setId(exists.getId());
			}
		}
		ObjectChangedType changeType = serverMethod.getId() == null ? ObjectChangedType.CREATED : ObjectChangedType.UPDATED;
		Instance inst = PMDBConverter.toInstance(serverMethod);
		serverMethod.setId(pmdbFacade.saveInstance(inst, null, null));
		serverMethod.setModified(System.currentTimeMillis());
		getCache().put(method.getCode(), serverMethod);
		EventBus.getDefault().publishObjectChanged(new MethodChangedEvent(changeType, null, serverMethod));
	}

	@Override
	public OperatorResult deleteMethod(String methodCode) {
		ServerSideMonitorMethod method = getMethod(methodCode);
		if (method == null)
			return OperatorResult.DELETED;

		pmdbFacade.deleteInstanceById(method.getId());
		Instance instance = pmdbFacade.getInstanceById(method.getId());
		EventBus.getDefault().publishObjectChanged(new MethodChangedEvent(ObjectChangedType.DELETED, method, null));
		if (instance == null)
			return OperatorResult.DELETED;
		else
			return OperatorResult.MODIFIED;
	}

	@Override
	public ServerSideMonitorMethod getMethod(String methodCode) {
		return getCache().get(methodCode);
	}

	@Override
	public MonitorMethod[] getMethodsByTypeId(String typeId) {
		List<MonitorMethod> methods = new ArrayList<MonitorMethod>();
		for (MonitorMethod method : getCache().values()) {
			if (method.getTypeId().equalsIgnoreCase(typeId))
				methods.add(method);
		}
		return methods.toArray(new MonitorMethod[0]);
	}

	private Map<String, ServerSideMonitorMethod> getMethodMapByNodeIdAndType(String nodeId, String typeId) {

		if (methodCache == null || needClean) {
			synchronized (this) {
				if (methodCache == null || needClean) {
					methodCache = new ConcurrentHashMap<String, ServerSideMonitorMethod>();
					Instance[] insts = pmdbFacade.getInstanceByQuery(typeId, new QueryRequest());
					if (insts != null) {
						for (Instance inst : insts) {
							try {
								Instance fullInst = pmdbFacade.getInstanceById(inst.getId());
								if (fullInst == null) {
									logger.warn("监测方法不存在：" + inst);
									continue;
								}
								ServerSideMonitorMethod method = PMDBConverter.toMethod(fullInst);
								String protocolId = inst.getId();
								String relationshipCode = "Use";
								QueryRequest request = new QueryRequest();
								request.addCondition("dest", QueryOperator.EQUALS, protocolId);
								Relationship[] ships = pmdbFacade.getRelationshipsByQuery(GlobalDataRegion.INSTANCE, relationshipCode,
										request);
								for (Relationship ship : ships) {
									String srcId = ship.getSrc();
									Instance srcInst = pmdbFacade.getInstanceById(srcId);
									String nodeId1 = srcInst.getId();
									if (!nodeId.equals(nodeId1))
										continue;
									methodCache.put(method.getCode(), method);
								}
							} catch (Throwable e) {
								ErrorUtil.warn(logger, "监测方法无法加载，将忽略：" + inst, e);
							}
						}
					}
					needClean = false;
				}
			}
		}
		return methodCache;
	}

	public MonitorMethod[] getMethodsByNodeIdAndType(String nodeId, String typeId) {
		List<MonitorMethod> methods = new ArrayList<MonitorMethod>();
		for (MonitorMethod method : getMethodMapByNodeIdAndType(nodeId, typeId).values())
			methods.add(method);
		return methods.toArray(new MonitorMethod[0]);
	}
}
