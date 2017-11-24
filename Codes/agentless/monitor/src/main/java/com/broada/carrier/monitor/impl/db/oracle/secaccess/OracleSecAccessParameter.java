package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: OracleSecAccessParameter
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class OracleSecAccessParameter implements Serializable {
	private static final long serialVersionUID = 917772976304302598L;
	private List<OracleSecAccessMonitorCondition> conditions;

	public void addCondition(OracleSecAccessMonitorCondition cond) {		
		getConditions().add(cond);
	}

	public List<OracleSecAccessMonitorCondition> getConditions() {
		if (conditions == null)
			conditions = new ArrayList<OracleSecAccessMonitorCondition>();
		return conditions;
	}

	public void setConditions(List<OracleSecAccessMonitorCondition> conditions) {
		this.conditions = conditions;
	}

	public void removeAllCondition() {
		getConditions().clear();
	}
}
