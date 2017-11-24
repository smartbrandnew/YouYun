package com.broada.carrier.monitor.server.impl.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeSync;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.service.ServerPolicyService;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorPolicy;
import com.broada.carrier.monitor.server.impl.event.PolicyChangedEvent;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.component.utils.error.ErrorUtil;

// TODO 2015-02-26 14:45:52 监听cmdb变更事件，并发出消息
public class ServerPolicyServiceImpl implements ServerPolicyService {	
	private static final Logger logger = LoggerFactory.getLogger(ServerPolicyServiceImpl.class);
	private Map<String, ServerSideMonitorPolicy> cache;
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private ServerProbeServiceImpl probeService;	
	
	public ServerPolicyServiceImpl() {
		EventBus.getDefault().registerObjectChangedListener(new PolicyEventListener());
	}
	
	private class PolicyEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (!(event instanceof PolicyChangedEvent))
				return;
			
			PolicyChangedEvent changeEvent = (PolicyChangedEvent) event;
			if (changeEvent.getType() == ObjectChangedType.UPDATED) {
				int[] probeIds = probeService.getProbeIdsByPolicyCode(changeEvent.getObject().getCode());
				for (int probeId : probeIds) {
					try {
						ProbeSync sync = probeService.checkProbeSyncByProbeId(probeId);
						sync.syncPolicy(changeEvent.getObject().getCode());
					} catch (Throwable e) {
						ErrorUtil.warn(logger, String.format("策略同步失败[probeId: %d policy: %s]", probeId, changeEvent.getObject()), e);
					}
				}				
			}			
		}		
	}
	
	private Map<String, ServerSideMonitorPolicy> getCache() {
		if (cache == null) {
			synchronized (this) {
				if (cache == null) {
					cache = new ConcurrentHashMap<String, ServerSideMonitorPolicy>();
					Instance[] insts = pmdbFacade.getInstanceByQuery(ServerSideMonitorPolicy.CLASS_CODE, new QueryRequest());
					if (insts != null) {
						for (Instance inst : insts) {
							try {
								ServerSideMonitorPolicy policy = PMDBConverter.toPolicy(inst);
								cache.put(policy.getCode(), policy);
							} catch (Throwable e) {
								ErrorUtil.warn(logger, "加载策略失败：" + inst, e);
							}
						}
					}
					
					MonitorPolicy policy = cache.get(MonitorPolicy.DEFAULT_POLICY_CODE);
					if (policy == null) {
						policy = new MonitorPolicy(MonitorPolicy.DEFAULT_POLICY_CODE, "默认策略", 10 * 60, 5 * 60);
						cache.put(MonitorPolicy.DEFAULT_POLICY_CODE, savePolicyNoCache(policy));
					}
				}
			}
		}
		return cache;
	}
	
	@Override
	public MonitorPolicy[] getPolicies() {
		return getCache().values().toArray(new MonitorPolicy[getCache().size()]);
	}
	
	private ServerSideMonitorPolicy savePolicyNoCache(MonitorPolicy policy) {
		policy.verify();
		
		ServerSideMonitorPolicy serverPolicy;
		if (policy instanceof ServerSideMonitorPolicy)
			serverPolicy = (ServerSideMonitorPolicy) policy;
		else {
			serverPolicy = new ServerSideMonitorPolicy(policy);
			ServerSideMonitorPolicy exists = getPolicy(policy.getCode());
			if (exists != null)
				serverPolicy.setId(exists.getId());
		}
		ObjectChangedType changeType = serverPolicy.getId() == null ? ObjectChangedType.CREATED : ObjectChangedType.UPDATED;
		Instance inst = PMDBConverter.toInstance(serverPolicy);	
		serverPolicy.setId(pmdbFacade.saveInstance(inst, null, null));
		serverPolicy.setModified(System.currentTimeMillis());		
		EventBus.getDefault().publishObjectChanged(new PolicyChangedEvent(changeType, null, serverPolicy));
		return serverPolicy;
	}

	@Override
	public void savePolicy(MonitorPolicy policy) {		
		getCache().put(policy.getCode(), savePolicyNoCache(policy));				
	}

	@Override
	public OperatorResult deletePolicy(String policyCode) {
		ServerSideMonitorPolicy policy = getPolicy(policyCode);
		if (policy == null)
			return OperatorResult.DELETED;
		
		if (policy.retDefault())
			throw new IllegalArgumentException("默认策略不允许删除");
		
		pmdbFacade.deleteInstanceById(policy.getId());
		Instance instance = pmdbFacade.getInstanceById(policy.getId());		
		EventBus.getDefault().publishObjectChanged(new PolicyChangedEvent(ObjectChangedType.DELETED, policy, null));
		if (instance == null) {
			getCache().remove(policy.getCode());
			return OperatorResult.DELETED;
		} else 
			return OperatorResult.MODIFIED;		
	}

	@Override
	public ServerSideMonitorPolicy getPolicy(String policyCode) {
		return getCache().get(policyCode);
	}

}
