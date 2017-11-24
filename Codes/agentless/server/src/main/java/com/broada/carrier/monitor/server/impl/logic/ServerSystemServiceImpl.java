package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteLookupFailureException;

import com.broada.acm.IllegalArgumentException;
import com.broada.acm.authentication.entity.Authentication;
import com.broada.acm.authentication.entity.impl.DefaultAuthentication;
import com.broada.acm.authentication.service.AuthenticationService;
import com.broada.acm.authorization.entity.FunctionPermission;
import com.broada.acm.authorization.service.AuthorizationService;
import com.broada.acm.domain.entity.Domain;
import com.broada.acm.session.SessionContext;
import com.broada.carrier.monitor.base.logic.BaseSystemServiceImpl;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.carrier.monitor.server.impl.entity.LicenseInfo;
import com.broada.cmdb.api.util.RMIUtil;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.mq.client.ServiceInvoker;
import com.broada.spr.common.ServiceException;

public class ServerSystemServiceImpl extends BaseSystemServiceImpl implements ServerSystemService {
	@Autowired
	private ServerTaskServiceEx taskService;
	private AuthenticationService authenticationService;
	private AuthorizationService authorizationService;
	private static FunctionPermission fp = new FunctionPermission("itsm_monitor_mgr");
	private static LicenseInfo licenseInfo;

	@Override
	public String login(String username, String password) {
		if (!getLicenseInfo().isMonitorBase())
			throw new IllegalArgumentException("无法登录，产品缺少“监测基础”许可授权");
		// TODO 如何获取远程IP地址
		Authentication auth = new DefaultAuthentication(username, password, "192.168.14.10");
		try {
			SessionContext context = getAuthenticationService().login(auth);
			Domain domain = context.getAuthentication().getDomains()[0];
			if (!getAuthorizationService().hasPermission(domain.getId(), fp, context.getSessionId())) {
				getAuthenticationService().logout(context.getSessionId());
				throw new IllegalArgumentException("用户没有监控配置权限");
			}
			SessionManager.addSession(context);
			return context.getSessionId().toString();
		} catch (RemoteLookupFailureException e) {			
			throw new ServiceException("无法连接系统用户验证服务", e);
		}
	}
	
	public AuthorizationService getAuthorizationService() {
		if (authorizationService == null) {
			authorizationService = (AuthorizationService) RMIUtil.getRemoteService("localhost", 9103,
					"authorizationService", AuthorizationService.class);
		}
		return authorizationService;
	}
	
	public AuthenticationService getAuthenticationService() {
		if (authenticationService == null) {
			authenticationService = (AuthenticationService) RMIUtil.getRemoteService("localhost", 9103,
					"authenticationService", AuthenticationService.class);
		}
		return authenticationService;
	}

	@Override
	public void logout(String token) {
		SessionManager.removeSession(token);
		getAuthenticationService().logout(token);
	}

	@Override
	public SystemInfo[] getInfos() {
		SessionManager.checkSessionUserId();
		
		List<SystemInfo> infos = new ArrayList<SystemInfo>();

		infos.add(new SystemInfo("stopedTasksCount", "未监测任务总数", taskService.getTasksCountByState(MonitorState.UNMONITOR)));
		infos.add(new SystemInfo("successedTasksCount", "正常监测任务总数", taskService
				.getTasksCountByState(MonitorState.SUCCESSED)));
		infos.add(new SystemInfo("failedTasksCount", "异常监测任务总数", taskService.getTasksCountByState(MonitorState.FAILED)));
		infos.add(new SystemInfo("processedTasksCount", "处理总数", taskService.getTasksCountByProcessed()));
		infos.add(new SystemInfo("processedTasksSpeed30m", "处理性能（30分钟内）", taskService.getTasksSpeedByProcessed()));

		return infos.toArray(new SystemInfo[0]);
	}
	
	@Override
	public String getProperty(String code) {
		return Config.getDefault().getProps().get(code);
	}
	
	public static LicenseInfo getLicenseInfo() {
		if (licenseInfo == null) {
			synchronized (ServerSystemServiceImpl.class) {
				if (licenseInfo == null) {
					boolean monitorBase;
					int monitorPCServer;
					int monitorMiniServer;
					int monitorAppPlatform;
					int monitorStorageDev;
					
					ServiceInvoker invoker = null;
					try {
						invoker = new ServiceInvoker(EventBus.getDefault().getGlobalConnection(), "cm");
						monitorBase = (Boolean) invoker.invoke("Monitor.Base");
						invoker.close();
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("LIC许可信息获取失败", e);
					}
					
					try {
						invoker = new ServiceInvoker(EventBus.getDefault().getGlobalConnection(), "cn");
						monitorPCServer = (Integer) invoker.invoke("Monitor.PCServer");
						monitorMiniServer = (Integer) invoker.invoke("Monitor.MiniServer");
						monitorAppPlatform = (Integer) invoker.invoke("Monitor.AppPlatform");
						monitorStorageDev = (Integer) invoker.invoke("Monitor.StorageDev");
						invoker.close();
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("LIC许可信息获取失败", e);
					}
					
					licenseInfo = new LicenseInfo(monitorBase, monitorPCServer, monitorMiniServer, monitorAppPlatform, monitorStorageDev);
				}
			}
		}
		return licenseInfo;
	}

	@Override
	public int getLicenseUsedQuota(String moduleId) {
		if (moduleId.equalsIgnoreCase("Monitor.PCServer"))
			return taskService.getLicenseUsedQuotaPCServer();
		else if (moduleId.equalsIgnoreCase("Monitor.MiniServer"))
			return taskService.getLicenseUsedQuotaMiniServer();
		else if (moduleId.equalsIgnoreCase("Monitor.AppPlatform"))
			return taskService.getLicenseUsedQuotaAppPlatform();
		else if (moduleId.equalsIgnoreCase("Monitor.StorageDev"))			
			return taskService.getLicenseUsedQuotaStorageDev();
		else
			throw new IllegalArgumentException("未知的moduleId：" + moduleId);
			
	}
}
