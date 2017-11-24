package com.broada.carrier.monitor.server.api.client.restful;

import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.entity.ExecuteMethodRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.UploadFileRequest;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;

public class ServerProbeClient extends BaseServiceClient implements ServerProbeService {
	public ServerProbeClient(String baseServiceUrl) {		
		super(baseServiceUrl, "/api/v1/monitor/probes");
	}

	@Override
	public MonitorProbe[] getProbes() {
		return client.get(MonitorProbe[].class);
	}

	@Override
	public int saveProbe(MonitorProbe probe) {
		return client.post(Integer.class, probe);
	}

	@Override
	public void deleteProbe(int id) {
		client.post(id + "/delete", null, id);
	}

	@Override
	public MonitorProbe getProbeByCode(String code) {
		return convert(client.get(MonitorProbe[].class, "code", code));
	}

	private MonitorProbe convert(MonitorProbe[] probes) {
		return probes.length > 0 ? probes[0] : null;
	}

	@Override
	public MonitorProbe getProbeByHostPort(String host, int port) {
		return convert(client.get(MonitorProbe[].class, "host", host, "port", port));
	}

	@Override
	public MonitorProbe getProbe(int id) {
		return client.get(Integer.toString(id), MonitorProbe.class);
	}

	@Override
	public void syncProbe(int id) {
		client.post(id + "/sync", null, id);
	}

	@Override
	public Object executeMethod(int probeId, String className, String methodName, Object... params) {
		String result = client.post(probeId + "/executeMethod", String.class, new ExecuteMethodRequest(className, methodName, params));
		return Base64Util.decodeObject(result);
	}

	@Override
	public void exitProbe(int probeId, String reason) {
		client.post(probeId + "/exit", null, reason);
	}

	@Override
	public SyncStatus getProbeSyncStatus(int probeId) {
		return client.get(probeId + "/sync", SyncStatus.class);
	}

	@Override
	public MonitorProbeStatus getProbeStatus(int probeId) {
		return client.get(probeId + "/status", MonitorProbeStatus.class);
	}

	@Override
	public MonitorProbeStatus[] getProbeStatuses() {
		return client.get("0/status", MonitorProbeStatus[].class);
	}

	@Override
	public MonitorProbeStatus testProbeStatus(int probeId) {
		return client.post(probeId + "/status/test", MonitorProbeStatus.class);
	}

	@Override
	public void uploadFile(int probeId, String serverFilePath, String probeFilePath) {
		client.post(probeId + "/uploadFile", null, new UploadFileRequest(serverFilePath, probeFilePath));
	}

	@Override
	public SystemInfo[] getProbeInfos(int probeId) {
		return client.get(probeId + "/infos", SystemInfo[].class);
	}
}
