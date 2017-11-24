package com.broada.carrier.monitor.common.swing.table;

public interface BeanTableRowFilter<T> {
	boolean match(T row);
}
