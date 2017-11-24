package com.broada.carrier.monitor.common.swing.table;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;

public interface TableDataProvider<T> {
	Page<T> getData(PageNo pageNo, String key);
}
