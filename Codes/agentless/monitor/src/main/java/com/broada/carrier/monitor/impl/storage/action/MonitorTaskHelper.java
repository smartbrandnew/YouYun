package com.broada.carrier.monitor.impl.storage.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.cid.action.api.entity.ActionRequest;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.smis.SmisProtocol;
import com.broada.cid.action.protocol.impl.snmp.SnmpProtocol;

import edu.emory.mathcs.backport.java.util.Collections;

public class MonitorTaskHelper {
	private static final Logger logger = LoggerFactory.getLogger(MonitorTaskHelper.class);
	
	/**
	 * 根据收集环境获得对应的协议
	 * @param context
	 * @return
	 */
	public static Protocol getProtocol(CollectContext context) {
		MonitorMethod method = context.getMethod();
		DefaultDynamicObject properties = method.getProperties();
		Protocol protocol = null;
		try {
			String protocolType = context.getMethod().getTypeId();
			logger.debug("存储监测协议类型：" + protocolType + ", " + properties.toString());
			if (CLIMonitorMethodOption.TYPE_ID.equals(protocolType)) {
				protocol = new CcliProtocol(new Protocol("ccli", properties));
			} else if (SmisMethod.TYPE_ID.equals(protocolType)) {
				protocol = new SmisProtocol(new Protocol("smis", properties));
				
			} else if (SnmpMethod.TYPE_ID.equals(protocolType)) {
				protocol = new SnmpProtocol(new Protocol("snmp", properties));
			}
			protocol.setField("ip", context.getNode().getIp());
		} catch (Exception e) {
			throw new RuntimeException("获取监测器协议出错", e);
		}
		return protocol;
	}
	
	
	/**
	 * 组装指定节点的待执行脚本请求。
	 * 
	 * @param node 待发现节点
	 * @param actionMetas 发现脚本
	 * @param protocol 相应的发现参数
	 * @return 发现脚本请求
	 */
	public static List<ActionRequest> assembleActionRequests(MonitorNode node,	List<MonitorActionMetadata> actionMetas, 
			Protocol protocol, Map<String, Object> requestParams) {
		Collections.sort(actionMetas, new MonitorActionMetadata.PriorityComparator());
		List<ActionRequest> requestList = new ArrayList<ActionRequest>();
		for (MonitorActionMetadata actionMeta : actionMetas) {
			String actionCode = actionMeta.getCode();
			String[] protocolCodes = actionMeta.getProtocols();
			if (protocolCodes == null || protocolCodes.length == 0) {
				logger.warn("发现脚本[ {} ]未定义所需要的发现协议，放弃。", actionCode);
				continue;
			}
			
			for (String protocolCode : protocolCodes) {
				if (!protocolCode.equals(protocol.getCode())) {
					logger.warn("脚本没有匹配到的监测协议{}， 放弃。", protocol.getCode());
					continue;
				}
				ActionRequest request = createActionRequest(actionCode, protocol, requestParams);
				requestList.add(request);
			}
		}
		return requestList;
	}

	public static Map<String, Object> createRequestParam(CollectContext context) {
		String typeId = context.getTypeId();
		MonitorMethod method = context.getMethod();
		MonitorNode node = context.getNode(); 
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("method", method);
		params.put("node", node); 
		params.put("typeId", typeId);
		return params;
	}
	
	public static ActionRequest createActionRequest(String action, Protocol protocol, Map<String, Object> requestParams) {
		int timeout = ActionRequest.EXECUTE_TIMEOUT_MS;//config.getProps().get("cid.action.timeout", ActionRequest.EXECUTE_TIMEOUT_MS);
		return new ActionRequest(action, timeout, new Protocol[] {protocol}, requestParams);
	}
}
