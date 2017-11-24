package com.broada.carrier.monitor.impl.stdsvc.dns;

import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DNS 监测参数类
 * <p>Title: </p>
 * <p>Description: DNS</p>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p>Company: Broada</p>
 * @author ruanjj
 * @version 1.0
 */

public class DNSParameter {
	private List<DNSMonitorCondition> conditions = new ArrayList<DNSMonitorCondition>();

	public DNSParameter() {
	}

	public DNSParameter(String json) {
		DNSParameter copy = SerializeUtil.decodeJson(json, DNSParameter.class);
		if (copy != null) 
			setConditions(copy.getConditions());
	}
	
	public void removeAllCondition() {
		conditions.clear();
	}
	
	public List<DNSMonitorCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<DNSMonitorCondition> conditions) {
		this.conditions = conditions;
	}
	
	public String encode() {
		return SerializeUtil.encodeJson(this);
	}	
	
	@JsonIgnore
	public void setCondition(DNSMonitorCondition monitorCondition) {
		getConditions().add(monitorCondition);
	}
}
