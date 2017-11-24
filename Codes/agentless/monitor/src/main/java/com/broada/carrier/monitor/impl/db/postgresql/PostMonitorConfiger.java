package com.broada.carrier.monitor.impl.db.postgresql;




import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;

public class PostMonitorConfiger extends MultiInstanceConfiger{
	/**
	 *PostgreSQLsession的多实例config配置 
	 */
	private static final long serialVersionUID = 1L;
	private static final BaseTableColumn COLUMN_NAME = new BaseTableColumn("name", "会话ID", 100);		

	@Override
	protected BaseTableColumn createColumnForName() {		
		return PostMonitorConfiger.COLUMN_NAME;
	}

	

}
