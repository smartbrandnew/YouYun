package com.broada.carrier.monitor.impl.mw.websphere.pmi;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.websphere.conf.WebSphereGroupFacade;
import com.broada.carrier.monitor.impl.mw.websphere.entity.WASMonitorResult;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.WebSphere;
import com.broada.carrier.monitor.method.websphere.WASMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

/**
 * @author lixy Sep 17, 2008 3:53:27 PM
 */
public class WASPmiMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(WASPmiMonitor.class);
	private static String DEBUG_VERSION = null;
	private static Map<String, String> taskVersions = new ConcurrentHashMap<String, String>();

	static {
		DEBUG_VERSION = System.getProperty("monitor.websphere.debug.version");
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorTempData temp = context.getTempData();
		if (temp == null)
			temp = new MonitorTempData();
		MonitorResult mr = collect(new CollectContext(context), temp);
		temp.setTime(new Date());
		context.setTempData(temp);
		return mr;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect(context, null);
	}

	private MonitorResult collect(CollectContext context, MonitorTempData lastData) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		String host = context.getNode().getIp();
		WASMonitorMethodOption option = new WASMonitorMethodOption(context.getMethod());
		String port = new Integer(option.getPort()).toString();
		String username = option.getUsername();
		String password = option.getPassword();
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("port", port);
		params.put("username", username);
		params.put("password", password);
		params.put("connector_type", option.getConnectorType());
		params.put("connector_port", option.getConnectorPort() + "");
		params.put("connector_host", option.getConnectorHost());
		params.put("useSSL", String.valueOf(option.isChkSSL()));
		params.put("server_cer", option.getServerCerPath());
		params.put("server_cer_file", option.getServerCerPath());
		params.put("client_key", option.getClientKeyPath());
		params.put("client_key_pwd", option.getClientKeyPwd());

		String version = DEBUG_VERSION;
		if (version == null) {
			String key = context.getNode().getId() + "-" + context.getMethod().getCode();
			version = taskVersions.get(key);
			if (version == null) {
				try {
					version = WebSphereGroupFacade.getWASVersion(host, port, username, password, params);
					taskVersions.put(key, version);
				} catch (Exception e) {
					logger.error("获取WebSphere版本号失败", e);
					result.setResultDesc("获取WebSphere版本号失败.");
					result.setState(MonitorConstant.MONITORSTATE_FAILING);
					return result;
				}
			}
		}

		String typeId = context.getTypeId();

		WebSphere webSphere;
		try {
			webSphere = WebSphereGroupFacade.getWebSphereByVersion(version);
		} catch (Exception e) {
			logger.error("无法获取当前版本的XML配置.", e);
			result.setResultDesc(e.getMessage());
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		if(webSphere.getType(typeId)==null){
		logger.debug("该版本websphere不支持该监测项:{}的监控",typeId);
		result.setResultDesc("该版本websphere不支持该监测项的监控");
		result.setState(MonitorConstant.MONITORSTATE_FAILING);
		return result;
		}

		// 监测结果信息和告警的当前值信息
		StringBuffer msgSB = new StringBuffer();
		MonitorState state = MonitorConstant.MONITORSTATE_NICER;

		try {
			long respTime = System.currentTimeMillis();
			WebSphereGroupFacade.link(WebSphereGroupFacade.getDefaultLinkUrl(), params);
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 0;
			}
			result.setResponseTime(respTime);
		} catch (Exception e) {
			logger.error("无法连接到目标服务器或连接超时.", e);
			result.setResultDesc("无法连接到目标服务器或连接超时.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}

		try {
			Map<String, WASMonitorResult> perfResults = WebSphereGroupFacade.getAllWASMonitorResults(version, typeId,
					host, port, null, null, username, password, params);

			if (perfResults == null || perfResults.isEmpty()) {
				logger.error("获取Websphere性能数据失败.");
				result.setResultDesc("获取Websphere性能数据失败.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}

			// j2c需要进行特殊处理
			WebSphereGroupFacade.doJ2CCalculate(typeId, lastData, perfResults);

			for (Entry<String, WASMonitorResult> entry : perfResults.entrySet()) {
				WASMonitorResult mr = entry.getValue();
				MonitorResultRow row = new MonitorResultRow(entry.getKey(), mr.getInstName());

				List<String> perfIndexList = mr.toPerfIndexList();// [WASWEBAPP-4,
																	// WASWEBAPP-6,
																	// WASWEBAPP-2,
																	// WASWEBAPP-5,
																	// WASWEBAPP-1,
																	// WASWEBAPP-3]
				for (int j = 0; j < perfIndexList.size(); j++) {// 6
					double pValue = mr.getPerfValue(perfIndexList.get(j));
					row.setIndicator(perfIndexList.get(j), pValue);
				}
				result.addRow(row);
			}
		} catch (JDOMException ex1) {
			logger.error("获取Websphere性能数据出错.", ex1);
			msgSB.append("获取Websphere性能数据出错.");
			state = MonitorConstant.MONITORSTATE_FAILING;
		} catch (IOException ex) {
			logger.error("无法连接到Websphere服务器或连接超时.", ex);
			msgSB.append("无法连接到Websphere服务器或连接超时.");
			state = MonitorConstant.MONITORSTATE_FAILING;
		} catch (Exception ee) {
			logger.error("获取Websphere性能数据出错.", ee);
			msgSB.append("获取Websphere性能数据出错.");
			state = MonitorConstant.MONITORSTATE_FAILING;
		}

		result.setResultDesc(msgSB.toString());
		result.setState(state);
		return result;
	}
	
	/**
	 * 判断记录中是否有 指标
	 * @param row
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean existInst(MonitorResultRow row){
		boolean flag = false;
		for(String key:row.keySet())
			if(key.contains("inst-"))
				flag = true;
		return flag;
	}
}
