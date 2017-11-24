package com.broada.carrier.monitor.server.impl.logic;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.remoteio.api.RemoteFile;
import com.broada.carrier.monitor.common.remoteio.api.RemoteIOClient;
import com.broada.carrier.monitor.common.util.WorkPathUtil;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.client.ProbeSync;
import com.broada.carrier.monitor.probe.api.client.restful.RestfulProbeServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.ProbeChangedEvent;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;
import com.broada.carrier.monitor.server.api.service.ServerPolicyService;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;
import com.broada.carrier.monitor.server.impl.dao.NodeDao;
import com.broada.carrier.monitor.server.impl.logic.probe.ProbeRunInfoCollector;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

public class ServerProbeServiceImpl implements ServerProbeService {
	private static final Logger logger = LoggerFactory.getLogger(ServerProbeServiceImpl.class);
	private MonitorProbe[] cache;
	private ServerProbeService target;
	@Autowired
	private ServerNodeService nodeService;
	@Autowired
	private ServerMethodService methodService;
	@Autowired
	private ServerPolicyService policyService;
	@Autowired
	private ServerResourceService resourceService;
	@Autowired
	private ServerTaskService taskService;
	@Autowired
	private ServerServiceFactory serverFactory;
	@Autowired
	private NodeDao dao;

	public ServerProbeServiceImpl(ServerProbeService target) {
		this.target = target;
	}

	@Override
	public MonitorProbe[] getProbes() {
		if (cache == null) {
			synchronized (this) {
				if (cache == null) {
					cache = target.getProbes();
				}
			}
		}
		return cache;
	}

	@Override
	public int saveProbe(MonitorProbe probe) {
		clean();
		MonitorProbe exists = null;
		if (probe.getId() > 0)
			exists = getProbe(probe.getId());
		int probeId = target.saveProbe(probe);
		if (exists == null)
			EventBus.getDefault().publishObjectChanged(new ProbeChangedEvent(ObjectChangedType.CREATED, null, probe));
		else
			EventBus.getDefault().publishObjectChanged(new ProbeChangedEvent(ObjectChangedType.UPDATED, exists, probe));
		return probeId;
	}

	@Override
	public void deleteProbe(int id) {
		MonitorProbe probe = getProbe(id);
		if (probe == null)
			return;
		
		Page<MonitorNode> page = nodeService.getNodesByProbeId(PageNo.ONE, id, false);
		if (!page.isEmpty()) 
			throw new IllegalArgumentException("此探针还部署有监测节点，无法删除");
				
		clean();
		target.deleteProbe(id);
		EventBus.getDefault().publishObjectChanged(new ProbeChangedEvent(ObjectChangedType.DELETED, probe, null));
	}

	private void clean() {
		cache = null;
	}

	@Override
	public MonitorProbe getProbeByCode(String code) {
		for (MonitorProbe probe : getProbes()) {
			if (probe.getCode().equalsIgnoreCase(code))
				return probe;
		}
		return null;
	}

	@Override
	public MonitorProbe getProbeByHostPort(String host, int port) {
		for (MonitorProbe probe : getProbes()) {
			if (probe.getHost().equals(host) && probe.getPort() == port)
				return probe;
		}
		return null;
	}

	@Override
	public MonitorProbe getProbe(int id) {
		for (MonitorProbe probe : getProbes())
			if (probe.getId() == id)
				return probe;
		return null;
	}

	public ProbeSync checkProbeSyncByTask(MonitorTask task) {
		return checkProbeSyncByNodeId(task.getNodeId());
	}

	public ProbeSync checkProbeSyncByNodeId(String nodeId) {
		MonitorProbe probe = ServerUtil.checkProbe(this, nodeService, nodeId);
		return new ProbeSync(probe, new RestfulProbeServiceFactory(probe.getHost(), probe.getPort()), serverFactory);
	}

	public ProbeSync checkProbeSyncByProbeId(int probeId) {
		MonitorProbe probe = ServerUtil.checkProbe(this, probeId);
		return new ProbeSync(probe, new RestfulProbeServiceFactory(probe.getHost(), probe.getPort()), serverFactory);
	}

	public ProbeServiceFactory checkProbeFactoryByTask(MonitorTask task) {
		return checkProbeFactoryByNodeId(task.getNodeId());
	}

	public ProbeServiceFactory checkProbeFactoryByTaskId(String taskId) {
		MonitorTask task = ServerUtil.checkTask(taskService, taskId);
		return checkProbeFactoryByTask(task);
	}

	public ProbeServiceFactory checkProbeFactoryByNodeId(String nodeId) {
		MonitorProbe probe = ServerUtil.checkProbe(this, nodeService, nodeId);
		return new RestfulProbeServiceFactory(probe.getHost(), probe.getPort());
	}

	private Map<Integer, ProbeSync> syncs = new ConcurrentHashMap<Integer, ProbeSync>();

	@Override
	public void syncProbe(int probeId) {
		MonitorProbe probe = ServerUtil.checkProbe(this, probeId);
		ProbeSync sync = syncs.get(probeId);
		if (sync != null)
			return;

		sync = new ProbeSync(probe, checkProbeFactory(probeId), serverFactory);
		syncs.put(probeId, sync);
		ThreadUtil.createThread(new ProbeSyncThread(sync)).start();
	}

	private class ProbeSyncThread implements Runnable {
		private ProbeSync sync;

		public ProbeSyncThread(ProbeSync sync) {
			this.sync = sync;
		}

		@Override
		public void run() {
			try {
				sync.syncAll(true);
			} finally {
				syncs.remove(sync.getProbe().getId());
			}
		}

	}

	public ProbeServiceFactory checkProbeFactory(int id) {
		MonitorProbe probe = ServerUtil.checkProbe(this, id);
		return new RestfulProbeServiceFactory(probe.getHost(), probe.getPort());
	}

	public ProbeServiceFactory getProbeFactoryByTask(MonitorTask task) {
		return getProbeManagerByNodeId(task.getNodeId());
	}

	private ProbeServiceFactory getProbeManagerByNodeId(String nodeId) {
		MonitorNode node = nodeService.getNode(nodeId);
		if (node == null || node.getProbeId() == null)
			return null;
		
		MonitorProbe probe = getProbe(node.getProbeId());
		if (probe == null)
			return null;
		
		return new RestfulProbeServiceFactory(probe.getHost(), probe.getPort());
	}

	public int[] getProbeIdsByPolicyCode(String policyCode) {
		return dao.getProbeIdsByPolicyCode(policyCode);
	}

	public int[] getProbeIdsByMethodCode(String methodCode) {
		return dao.getProbeIdsByMethodCode(methodCode);
	}

	@Override
	public Object executeMethod(int probeId, String className, String methodName, Object... params) {
		ProbeServiceFactory probeFactory = checkProbeFactory(probeId);
		return probeFactory.getSystemService().executeMethod(className, methodName, params);
	}

	@Override
	public void exitProbe(int probeId, String reason) {
		ProbeServiceFactory probeFactory = checkProbeFactory(probeId);
		probeFactory.getSystemService().exit(reason);
	}

	@Override
	public SyncStatus getProbeSyncStatus(int id) {
		ProbeSync sync = syncs.get(id);
		if (sync == null)
			return new SyncStatus(100, "探针同步完成");
		else
			return new SyncStatus(sync.getLastProgress(), sync.getLastMessage());
	}

	private Map<Integer, MonitorProbeStatus> probeStates = new ConcurrentHashMap<Integer, MonitorProbeStatus>();

	@Override
	public MonitorProbeStatus getProbeStatus(int id) {
		MonitorProbeStatus status = probeStates.get(id);
		if (status == null) {
			status = new MonitorProbeStatus(id);
			probeStates.put(id, status);
		}
		return status;
	}

	@Override
	public MonitorProbeStatus[] getProbeStatuses() {
		MonitorProbe[] probes = getProbes();
		MonitorProbeStatus[] result = new MonitorProbeStatus[probes.length];
		for (int i = 0; i < result.length; i++)
			result[i] = getProbeStatus(probes[i].getId());
		return result;
	}

	public void saveProbeState(MonitorProbeStatus state) {
		probeStates.put(state.getProbeId(), state);
	}

	@Override
	public MonitorProbeStatus testProbeStatus(int id) {
		MonitorProbe probe = ServerUtil.checkProbe(this, id);
		return ProbeRunInfoCollector.getDefault().checkStatus(probe);
	}

	@Override
	public void uploadFile(int probeId, String serverFilePath, String probeFilePath) {
		File file = WorkPathUtil.getFile(serverFilePath);
		if (!file.exists())
			throw new IllegalArgumentException("文件不存在：" + serverFilePath);

		ProbeServiceFactory probeFactory = checkProbeFactory(probeId);

		RemoteIOClient io = new RemoteIOClient(probeFactory.getFileService());

		long size = file.length();
		long lastModified = file.lastModified();
		RemoteFile rf = io.get(probeFilePath);
		if (rf != null) {
			if (rf.equals(size, lastModified)) {
				if (logger.isDebugEnabled())
					logger.debug(String.format("文件相同，取消同步[probe: %d sf: %s pf: %s]", probeId, serverFilePath, probeFilePath));
			}
		}

		try {
			io.save(probeFilePath, file);
		} catch (IOException e) {
			String msg = String.format("文件同步失败[probe: %d sf: %s pf: %s]", probeId, serverFilePath, probeFilePath);
			ErrorUtil.warn(logger, msg, e);
			throw ErrorUtil.createRuntimeException(msg, e);
		}
		if (io.setLastModified(serverFilePath, lastModified)) {
			if (logger.isDebugEnabled())
				logger.debug(String.format("文件同步完成[probe: %d sf: %s pf: %s]", probeId, serverFilePath, probeFilePath));
		} else
			logger.warn(String.format("文件同步完成但时间修改失败[probe: %d sf: %s pf: %s]", probeId, serverFilePath, probeFilePath));		
	}

	@Override
	public SystemInfo[] getProbeInfos(int id) {
		ProbeServiceFactory probeFactory = checkProbeFactory(id);
		return probeFactory.getSystemService().getInfos();
	}
}
