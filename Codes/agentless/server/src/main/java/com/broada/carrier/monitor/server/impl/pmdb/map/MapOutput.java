package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.List;

import com.broada.cmdb.api.data.AttributeValue;
import com.broada.cmdb.api.data.Relationship;
import com.broada.pmdb.api.data.PerfIndicatorGroup;
import com.broada.pmdb.api.data.StateIndicator;

public class MapOutput {
	/**
	 * 用于确定实例的key，请保证每个实例对应唯一key，且不能重复。（可以理解为monitor上的实例ID）
	 */
	private String localKey;
	private String remoteClassCode;
	private List<AttributeValue> attributes;
	private List<Relationship> relationships;
	private List<StateIndicator> states;
	private List<PerfIndicatorGroup> perfGroups;

	public String getRemoteClassCode() {
		return remoteClassCode;
	}

	public void setRemoteClassCode(String remoteClassCode) {
		this.remoteClassCode = remoteClassCode;
	}

	public String getLocalKey() {
		return localKey;
	}

	public void setLocalKey(String localKey) {
		this.localKey = localKey;
	}

	public List<AttributeValue> getAttributes() {
		if (attributes == null)
			attributes = new ArrayList<AttributeValue>(5);
		return attributes;
	}

	public List<Relationship> getRelationships() {
		if (relationships == null)
			relationships = new ArrayList<Relationship>(3);
		return relationships;
	}

	public List<StateIndicator> getStates() {
		if (states == null)
			states = new ArrayList<StateIndicator>(3);
		return states;
	}

	public List<PerfIndicatorGroup> getPerfGroups() {
		if (perfGroups == null)
			perfGroups = new ArrayList<PerfIndicatorGroup>(3);
		return perfGroups;
	}

	public boolean hasInstance() {
		return attributes != null && !attributes.isEmpty()
				|| relationships != null && !relationships.isEmpty();
	}

	public boolean hasStates() {
		return states != null && !states.isEmpty();
	}

	public boolean hasPerfs() {
		return perfGroups != null && !perfGroups.isEmpty();
	}

	public void setValue(MapInput input, String remoteCode, Object value) {
		MapItemRemote remote = new MapItemRemote(remoteCode);
		setValue(input, remote.getType(), remote.getCode(), value);
	}

	public void setValue(MapInput input, MapItemRemoteType type, String code, Object value) {
		if (value == null)
			return;

		switch (type) {
		case ATTRIBUTE:
			getAttributes().add(new AttributeValue(code, value));
			break;
		case RELATIONSHIP:
			getRelationships().add(new Relationship(code, null, value.toString()));
			break;
		case PERF_GROUP:
			processPerfGroup(input, code, value);
			break;
		case STATE:
			// TODO 2015-03-05 22:53:42 处理系统状态
			getStates().add(new StateIndicator(null, code, input.getResult().getTime(), value.toString(), null));
			break;
		default:
			throw new IllegalArgumentException(type.toString());
		}
	}

	private void processPerfGroup(MapInput input, String code, Object value) {
		int pos = code.indexOf('.');
		if (pos < 0)
			throw new IllegalArgumentException("性能指标配置错误，必须包含一个.号：" + code);
		if (!(value instanceof Number)) {
			try {
				value = Double.parseDouble(value.toString());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("性能指标数据类型必须是数值型，当前值：" + value);
			}
		}

		String groupCode = code.substring(0, pos);
		String perfCode = code.substring(pos + 1);

		PerfIndicatorGroup group = null;
		for (PerfIndicatorGroup temp : getPerfGroups()) {
			if (temp.getGroupCode().equalsIgnoreCase(groupCode)) {
				group = temp;
				break;
			}
		}

		if (group == null) {
			group = new PerfIndicatorGroup(null, groupCode, input.getResult().getTime());
			getPerfGroups().add(group);
		}

		group.add(perfCode, ((Number) value).doubleValue());
	}
}
