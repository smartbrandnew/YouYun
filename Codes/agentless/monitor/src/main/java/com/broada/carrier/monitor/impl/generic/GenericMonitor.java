package com.broada.carrier.monitor.impl.generic;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.method.script.ScriptExecuteSide;
import com.broada.carrier.monitor.method.script.ScriptMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.numen.agent.script.entity.DataColumn;
import com.broada.numen.agent.script.entity.DataRow;
import com.broada.numen.agent.script.entity.Result;

public class GenericMonitor extends BaseMonitor {
	public static final String ITEM_CLASS = "class";
	public static final String ITEM_RS_PREFIX = "rs.";
	public static final String ITEM_ATTR_PREFIX = "attr.";
	public static final String ITEM_PERF_PREFIX = "perf.";
	public static final String ITEM_STATE_PREFIX = "state.";

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return (MonitorResult) collect(context.getNode(),
				context.getMethod(), new MonitorTask(), context.getTask()
				.getParameterObject(ExtParameter.class));
	}

	@SuppressWarnings("unchecked")
	private Serializable collect(MonitorNode node, MonitorMethod method,
			MonitorTask task, ExtParameter extPara) {
		Result agentResult = null;
		try {
			ScriptMethod sm = new ScriptMethod(method);
			boolean isServerSide = sm.getExecuteSide() == ScriptExecuteSide.PROBE;
			agentResult = GenericExecutor.executeScript(node, task,
					sm.getAgentPort(), isServerSide, extPara, method);
		} catch (Throwable e) {
			if (e.getMessage() != null
					&& e.getMessage().contains("ConnectException"))
				throw new CollectException(ErrorUtil.createMessage("无法连接远程代理",
						e), e);
			throw new CollectException(ErrorUtil.createMessage("获取数据错误", e), e);
		}

		if (agentResult == null)
			throw new CollectException("执行脚本后没有采集到任何数据。");

		MonitorResult mr = new MonitorResult();
		Set<DataColumn> cols = agentResult.getCols();
		for (DataColumn col : cols) {
			mr.addItem(createItem(col));
		}

		Set<DataRow> rows = agentResult.getRows();
		for (DataRow row : rows) {
			MonitorResultRow mrr = new MonitorResultRow(row.getKey());
			for (Object entry : row.getProperties().entrySet()) {
				Entry<String, Object> kv = (Entry<String, Object>) entry;
				Object value = kv.getValue();
				if (value != null) {
					mrr.setIndicator(kv.getKey(), kv.getValue());
				}
			}
			mrr.addTag("belong:" + row.getKey());
			mr.addRow(mrr);
		}

		GenericCollectResult cr = new GenericCollectResult();
		cr.setExecuteText(agentResult.getExecuteText());
		mr.setExtraObject(cr);

		return mr;
	}

	@SuppressWarnings("unused")
	private MonitorItem find(MonitorResult mr, String key, Object value) {
		if (mr.getItems() != null) {
			for (MonitorItem item : mr.getItems()) {
				if (item.getCode().equalsIgnoreCase(key))
					return item;
			}
		}

		MonitorItemType type;
		if (value instanceof Number)
			type = MonitorItemType.NUMBER;
		else
			type = MonitorItemType.TEXT;

		MonitorItem item = new MonitorItem(null,key, null, null, null, type);
		mr.addItem(item);
		return item;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return (MonitorResult) collect(context.getNode(),
				context.getMethod(), new MonitorTask(),
				context.getParameterObject(ExtParameter.class));
	}

	private MonitorItem createItem(DataColumn dataColumn) {
		MonitorItemType type;
		switch (dataColumn.getType()) {
		case DataColumn.TYPE_NUMBER:
			type = MonitorItemType.NUMBER;
			break;
		case DataColumn.TYPE_TEXT:
			type = MonitorItemType.TEXT;
			break;
		default:
			throw new IllegalArgumentException("未知的数据类型：" + dataColumn);
		}
		return new MonitorItem(null,dataColumn.getCode(), dataColumn.getName(),
				dataColumn.getUnit(), null, type);
	}

	public static boolean isHideItem(String code) {
		return code.equalsIgnoreCase(ITEM_CLASS)
		|| code.startsWith(ITEM_RS_PREFIX);
	}
}
