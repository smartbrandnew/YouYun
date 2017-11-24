package com.broada.carrier.monitor.server.impl.logic.trans;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;
import com.broada.carrier.monitor.server.impl.dao.ProbeDao;

public class ServerProbeServiceTrans implements ServerProbeService {
	@Autowired
	private ProbeDao dao;

	@Override
	public MonitorProbe[] getProbes() {
		return dao.getAll();
	}

	@Override
	public int saveProbe(MonitorProbe probe) {
		MonitorProbe exists = getProbeByCode(probe.getCode());
		if (exists == null) {
			exists = getProbeByHostPort(probe.getHost(), probe.getPort());
			if (exists == null)
				probe.setId(0);
			else
				throw new BaseException("PROBE_EXISTS", String.format("相同服务地址但编号不同探针[%s %s:%d]已存在，无法继续添加", exists.getCode(),
						exists.getHost(), exists.getPort()));
		} else
			probe.setId(exists.getId());
		return dao.save(probe);
	}

	@Override
	public void deleteProbe(int id) {
		dao.delete(id);
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
		return dao.get(id);
	}

	@Override
	public void syncProbe(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object executeMethod(int probeId, String className, String methodName, Object... params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void exitProbe(int probeId, String reason) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SyncStatus getProbeSyncStatus(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorProbeStatus getProbeStatus(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorProbeStatus[] getProbeStatuses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorProbeStatus testProbeStatus(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadFile(int probeId, String serverFilePath, String probeFilePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SystemInfo[] getProbeInfos(int id) {
		throw new UnsupportedOperationException();
	}
}
