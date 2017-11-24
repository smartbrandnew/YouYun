package com.broada.carrier.monitor.server.api.service;

import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;

/**
 * 探针管理服务
 * @author Jiangjw
 */
public interface ServerProbeService {
	/**
	 * 获取所有探针
	 * @return
	 */
	MonitorProbe[] getProbes();
	
	/**
	 * 获取指定编码的探针
	 * @param code
	 * @return
	 */
	MonitorProbe getProbeByCode(String code);
	
	/**
	 * 获取指定服务地址的探针
	 * @param host
	 * @param port
	 * @return
	 */
	MonitorProbe getProbeByHostPort(String host, int port);
	
	/**
	 * 注册一个探针，遵循以下约束
	 * 1. 检查是否存在同编码的probe，如果已存在，则覆盖此probe，表示探针修改了ip地址
	 * 2. 检查是否存在同服务地址的probe，如果已存在，则弹出异常，表示此地址已被其它探针使用，不允许继续使用
	 * 3. 新增probe
	 * @param probe
	 * @return
	 */
	int saveProbe(MonitorProbe probe);
	
	/**
	 * 删除一个探针
	 * @param id
	 * @return
	 */
	void deleteProbe(int id);

	MonitorProbe getProbe(int id);	
	
	void syncProbe(int id);
	
	SyncStatus getProbeSyncStatus(int id);
	
	Object executeMethod(int probeId, String className, String methodName, Object... params);
	
	void exitProbe(int probeId, String reason);
	
	MonitorProbeStatus getProbeStatus(int id);
	
	MonitorProbeStatus[] getProbeStatuses();
	
	MonitorProbeStatus testProbeStatus(int id);

	void uploadFile(int probeId, String serverFilePath, String probeFilePath);

	SystemInfo[] getProbeInfos(int id);
}
