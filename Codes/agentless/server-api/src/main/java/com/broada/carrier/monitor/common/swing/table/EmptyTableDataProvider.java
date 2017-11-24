package com.broada.carrier.monitor.common.swing.table;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;

public class EmptyTableDataProvider implements TableDataProvider<Object> {
	@Override
	public Page<Object> getData(PageNo pageNo, String key) {
		return new Page<Object>();
	}
}
