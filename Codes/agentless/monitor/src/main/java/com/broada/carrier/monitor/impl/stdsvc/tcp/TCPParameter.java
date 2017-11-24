package com.broada.carrier.monitor.impl.stdsvc.tcp;

import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.common.util.SerializeUtil;

/**
 * TCP 监测参数实体类
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author Maico Pang
 * @version 1.0
 */

public class TCPParameter {
	private List<TCPMonitorCondition> conditions = new ArrayList<TCPMonitorCondition>();

	public TCPParameter() {
	}

	public TCPParameter(String json) {
		TCPParameter copy = SerializeUtil.decodeJson(json, TCPParameter.class);
		if (copy != null) {

			setConditions(copy.getConditions());
		}
	}

	public List<TCPMonitorCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<TCPMonitorCondition> conditions) {
		this.conditions = conditions;
	}

	public void removeAllCondition() {
		conditions.clear();
	}

	public void setCondition(TCPMonitorCondition monitorCondition) {
		conditions.add(monitorCondition);
	}

	public String encode() {
		return SerializeUtil.encodeJson(this);
	}

}
