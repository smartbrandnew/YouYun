package com.broada.carrier.monitor.server.impl.pmdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.entity.Query;
import com.broada.carrier.monitor.common.entity.QueryCondition;
import com.broada.carrier.monitor.common.entity.QueryOperator;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetAuditState;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetTypeBusiness;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorMethod;
import com.broada.carrier.monitor.server.impl.entity.ServerSideMonitorPolicy;
import com.broada.cmdb.api.data.AttributeValue;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.model.BusinessType;
import com.broada.cmdb.api.model.QueryRequest;
import com.broada.cmdb.api.model.Template;

public class PMDBConverter {
	/**
	 * 监测项IP地址属性编码
	 */
	public static final String ATTR_IP = "ipaddr";
	/**
	 * 监测项名称属性编码
	 */
	public static final String ATTR_NAME = "name";
	public static final String ATTR_DESCR = "descr";
	public static final String ATTR_CODE = "code";
	public static final String ATTR_EXTRA = "extra";
	public static final String NODE_RESOURCE_RT = "RunningOn";
	public static final String TARGET_METHOD_RT = "Use";

	public static MonitorTargetType toTargetType(Template template) {
		MonitorTargetTypeBusiness[] bussinesses = toBusiness(template.getBusinessTypes());
		
		return new MonitorTargetType(template.getCode(), template.getName(), template.getParentCode(),
				template.getSortIndex(), template.getPath(), template.getBigIcon(), template.getMediumIcon(), template.getSmallIcon(), 
				bussinesses);
	}

	private static MonitorTargetTypeBusiness[] toBusiness(BusinessType[] businessTypes) {
		if (businessTypes == null)
			return new MonitorTargetTypeBusiness[0];
		MonitorTargetTypeBusiness[] buesinesses = new MonitorTargetTypeBusiness[businessTypes.length];
		for (int i = 0; i < businessTypes.length; i++)
			buesinesses[i] = toBusiness(businessTypes[i]);
		return buesinesses;
	}

	private static MonitorTargetTypeBusiness toBusiness(BusinessType businessType) {
		switch (businessType) {
		case CMDB:
			return MonitorTargetTypeBusiness.CMDB;
		case ITAM:
			return MonitorTargetTypeBusiness.ITAM;
		case PMDB:
			return MonitorTargetTypeBusiness.PMDB;
		default:
			throw new IllegalArgumentException("未知的业务类型：" + businessType);
		}
	}

	public static Instance toInstance(MonitorNode node) {
		Instance inst = toInstance((MonitorTarget) node);
		inst.getValues().add(new AttributeValue(ATTR_IP, node.getIp()));
		return inst;
	}

	public static QueryRequest toQueryRequest(PageNo pageNo, Query query) {
		QueryRequest request = new QueryRequest(pageNo.getIndex(), pageNo.getSize());
		if (query != null) {
			for (QueryCondition cond : query.getConditions())
				request.addCondition(toQueryCondition(cond));
		}
		return request;
	}

	private static com.broada.cmdb.api.model.QueryCondition toQueryCondition(QueryCondition cond) {
		return new com.broada.cmdb.api.model.QueryCondition(cond.getField(), toQueryOperator(cond.getOperator()),
				cond.getValue());
	}

	private static String toQueryOperator(QueryOperator operator) {
		return operator.getSymbol();
	}

	public static MonitorNode[] toNodes(Instance[] insts, PMDBFacade pmdbFacade) {
		if (insts == null)
			return new MonitorNode[0];
		MonitorNode[] nodes = new MonitorNode[insts.length];
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = toNode(insts[i], pmdbFacade.checkInstanceDomainIdById(insts[i].getId()));
		return nodes;
	}

	public static MonitorNode toNode(Instance inst, String domainId) {		
		return new MonitorNode(inst.getId(), getAttrValue(inst, ATTR_NAME), inst.getTemplateCode(),
				toTargetAuditState(inst.getStatus()), 0, getAttrValue(inst, ATTR_IP), inst.getUpdateTime().getTime(), domainId);
	}

	private static String getAttrValue(Instance inst, String attr) {
		AttributeValue av = inst.getValues().get(attr);
		if (av == null || av.getValue() == null)
			return null;
		else
			return av.getValue().toString();
	}

	public static MonitorTargetAuditState toTargetAuditState(String status) {
		if (status != null && !status.equalsIgnoreCase("apply")) 
			return MonitorTargetAuditState.AUDITING;
		else
			return MonitorTargetAuditState.AUDITED;
	}

	public static boolean setNode(MonitorNode node, Instance inst, String domainId) {
		node.set(node.getId(), getAttrValue(inst, ATTR_NAME), inst.getTemplateCode(),
				toTargetAuditState(inst.getStatus()), node.getProbeId(), getAttrValue(inst, ATTR_IP), inst.getUpdateTime().getTime(), domainId);
		return true;
	}

	public static MonitorResource[] toResources(Instance[] insts, PMDBFacade pmdbFacade) {
		if (insts == null)
			return new MonitorResource[0];
		MonitorResource[] resources = new MonitorResource[insts.length];
		for (int i = 0; i < resources.length; i++)
			resources[i] = toResource(insts[i], pmdbFacade.checkInstanceDomainIdById(insts[i].getId()));
		return resources;
	}

	public static MonitorResource toResource(Instance inst, String domainId) {
		return new MonitorResource(inst.getId(), getAttrValue(inst, ATTR_NAME), inst.getTemplateCode(),
				toTargetAuditState(inst.getStatus()), null, inst.getUpdateTime().getTime(), domainId);
	}

	private static Instance toInstance(MonitorTarget target) {
		Instance inst = new Instance();
		inst.setId(target.getId());
		inst.setTemplateCode(target.getTypeId());
		inst.getValues().add(new AttributeValue(ATTR_NAME, target.getName()));
		return inst;
	}

	public static Instance toInstance(MonitorResource resource) {
		return toInstance((MonitorTarget) resource);
	}

	public static ServerSideMonitorPolicy toPolicy(Instance inst) {
		return new ServerSideMonitorPolicy(inst.getId(), getAttrValue(inst, ATTR_CODE),
				getAttrValue(inst, ATTR_NAME), 
				getAttrValue(inst, ServerSideMonitorPolicy.ATTR_INTERVAL, 600),
				getAttrValue(inst, ServerSideMonitorPolicy.ATTR_ERROR_INTERVAL, 600),
				getAttrValue(inst, ServerSideMonitorPolicy.ATTR_WORK_WEEK_DAYS),
				getAttrValue(inst, ServerSideMonitorPolicy.ATTR_WORK_TIME_RANGE),				
				getAttrValue(inst, ServerSideMonitorPolicy.ATTR_STOP_TIME_RANGES),
				getAttrValue(inst, ATTR_DESCR), 
				inst.getUpdateTime().getTime());
	}

	private static int getAttrValue(Instance inst, String attrCode, int defValue) {
		AttributeValue av = inst.getValues().get(attrCode);
		if (av == null || av.getValue() == null)
			return defValue;
		else if (av.getValue() instanceof Number)
			return ((Number)av.getValue()).intValue();
		else
			return Integer.parseInt(av.getValue().toString());			
	}

	public static Instance toInstance(ServerSideMonitorPolicy policy) {
		Instance inst = new Instance();
		inst.setId(policy.getId());
		inst.setTemplateCode(ServerSideMonitorPolicy.CLASS_CODE);
		inst.getValues().add(new AttributeValue(ATTR_CODE, policy.getCode()));
		inst.getValues().add(new AttributeValue(ATTR_NAME, policy.getName()));
		inst.getValues().add(new AttributeValue(ATTR_DESCR, policy.getDescr()));
		inst.getValues().add(new AttributeValue(ServerSideMonitorPolicy.ATTR_INTERVAL, policy.getInterval()));
		inst.getValues().add(new AttributeValue(ServerSideMonitorPolicy.ATTR_ERROR_INTERVAL, policy.getErrorInterval()));
		inst.getValues().add(new AttributeValue(ServerSideMonitorPolicy.ATTR_WORK_WEEK_DAYS, policy.getWorkWeekDays()));
		inst.getValues().add(new AttributeValue(ServerSideMonitorPolicy.ATTR_WORK_TIME_RANGE, policy.getWorkTimeRange()));
		inst.getValues().add(new AttributeValue(ServerSideMonitorPolicy.ATTR_STOP_TIME_RANGES, policy.getStopTimeRanges()));	
		return inst;
	}

	public static ServerSideMonitorMethod toMethod(Instance inst) {
		Map<String, Object> options = new HashMap<String, Object>();
		for (AttributeValue av : inst.getValues()) {
			if (isMethodAttribute(av.getCode())) 
				options.put(av.getCode(), av.getValue());
		}
		return new ServerSideMonitorMethod(inst.getId(), getAttrValue(inst, ATTR_CODE),
				getAttrValue(inst, ATTR_NAME), inst.getTemplateCode(), getAttrValue(inst, ATTR_DESCR), options, inst.getUpdateTime().getTime(), getAttrValue(inst, ATTR_EXTRA));
	}
	
	private static final String[] METHOD_FILTER_ATTRS = new String[] { ATTR_CODE, ATTR_NAME, ATTR_DESCR, "position", "ciStatus",
			"superintendent", "dept" };

	private static boolean isMethodAttribute(String attrCode) {
		for (String attr : METHOD_FILTER_ATTRS) {
			if (attrCode.equalsIgnoreCase(attr))
				return false;
		}
		return true;
	}

	public static Instance toInstance(ServerSideMonitorMethod method) {
		Instance inst = new Instance();
		inst.setId(method.getId());
		inst.setTemplateCode(method.getTypeId());
		inst.getValues().add(new AttributeValue(ATTR_CODE, method.getCode()));
		inst.getValues().add(new AttributeValue(ATTR_NAME, method.getName()));
		inst.getValues().add(new AttributeValue(ATTR_DESCR, method.getDescr()));
		inst.getValues().add(new AttributeValue(ATTR_EXTRA, method.getExtra()));
		for (Entry<String, Object> entry : method.getProperties().entrySet()) 
			inst.getValues().add(entry.getKey(), entry.getValue());
		return inst;
	}
}
