package com.broada.carrier.monitor.impl.db.oracle.tablespace;

import com.broada.carrier.monitor.impl.common.SpecificMultiInstanceConfiger;

public class OracleTableSpaceConfiger extends SpecificMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected String[] getItemCodes() {
		return new String[] {
				"ORACLE-TABLESPACE-1",
				"ORACLE-TABLESPACE-2",
				"ORACLE-TABLESPACE-11",
				"ORACLE-TABLESPACE-14",
				"ORACLE-TABLESPACE-10",
		};
	}
}
